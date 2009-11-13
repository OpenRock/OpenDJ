/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2009 Sun Microsystems, Inc.
 */

package org.opends.sdk.ldap;



import static org.opends.sdk.ldap.LDAPConstants.OID_NOTICE_OF_DISCONNECTION;

import java.io.IOException;
import java.io.EOFException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.security.sasl.SaslException;

import org.opends.sdk.AttributeSequence;
import org.opends.sdk.Connection;
import org.opends.sdk.ConnectionEventListener;
import org.opends.sdk.ConnectionFuture;
import org.opends.sdk.ConnectionResultHandler;
import org.opends.sdk.DecodeException;
import org.opends.sdk.ErrorResultException;
import org.opends.sdk.InitializationException;
import org.opends.sdk.ResultCode;
import org.opends.sdk.SearchScope;
import org.opends.sdk.controls.Control;
import org.opends.sdk.controls.ControlDecoder;
import org.opends.sdk.extensions.StartTLSRequest;
import org.opends.sdk.requests.AbandonRequest;
import org.opends.sdk.requests.AddRequest;
import org.opends.sdk.requests.BindRequest;
import org.opends.sdk.requests.CompareRequest;
import org.opends.sdk.requests.DeleteRequest;
import org.opends.sdk.requests.ExtendedRequest;
import org.opends.sdk.requests.ModifyDNRequest;
import org.opends.sdk.requests.ModifyRequest;
import org.opends.sdk.requests.Requests;
import org.opends.sdk.requests.SearchRequest;
import org.opends.sdk.requests.SimpleBindRequest;
import org.opends.sdk.requests.UnbindRequest;
import org.opends.sdk.responses.BindResult;
import org.opends.sdk.responses.BindResultFuture;
import org.opends.sdk.responses.CompareResult;
import org.opends.sdk.responses.CompareResultFuture;
import org.opends.sdk.responses.ExtendedResultFuture;
import org.opends.sdk.responses.GenericExtendedResult;
import org.opends.sdk.responses.GenericIntermediateResponse;
import org.opends.sdk.responses.Responses;
import org.opends.sdk.responses.Result;
import org.opends.sdk.responses.ResultFuture;
import org.opends.sdk.responses.ResultHandler;
import org.opends.sdk.responses.SearchResult;
import org.opends.sdk.responses.SearchResultEntry;
import org.opends.sdk.responses.SearchResultFuture;
import org.opends.sdk.responses.SearchResultHandler;
import org.opends.sdk.responses.SearchResultReference;
import org.opends.sdk.sasl.SASLBindRequest;
import org.opends.sdk.sasl.SASLContext;
import org.opends.sdk.util.Validator;
import org.opends.sdk.util.ByteString;

import com.sun.grizzly.filterchain.Filter;
import com.sun.grizzly.filterchain.FilterChain;
import com.sun.grizzly.filterchain.StreamTransformerFilter;
import com.sun.grizzly.ssl.*;
import com.sun.grizzly.streams.StreamWriter;



/**
 * LDAP connection implementation.
 * <p>
 * TODO: handle illegal state exceptions.
 */
public class LDAPConnection implements Connection
{
  private class FirstEntrySearchResultHandler implements SearchResultHandler
  {
    private SearchResultEntry result;
    public void handleEntry(SearchResultEntry entry) {
      if(result == null)
        result = entry;
    }
    public void handleReference(SearchResultReference reference) {
    }

    public void handleResult(SearchResult result) {
    }

    public void handleError(ErrorResultException error) {
    }
  }

  private final class LDAPMessageHandlerImpl extends
      AbstractLDAPMessageHandler
  {

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleAddResult(int messageID, Result result)
    {
      AbstractResultFutureImpl<?> pendingRequest =
          pendingRequests.remove(messageID);
      if (pendingRequest != null)
      {
        if (pendingRequest instanceof ResultFutureImpl)
        {
          ResultFutureImpl future = (ResultFutureImpl) pendingRequest;
          if (future.getRequest() instanceof AddRequest)
          {
            future.handleResult(result);
            return;
          }
        }
        handleIncorrectResponse(pendingRequest);
      }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void handleBindResult(int messageID, BindResult result)
    {
      AbstractResultFutureImpl<?> pendingRequest =
          pendingRequests.remove(messageID);
      if (pendingRequest != null)
      {
        if (pendingRequest instanceof BindResultFutureImpl)
        {
          BindResultFutureImpl future =
              ((BindResultFutureImpl) pendingRequest);
          BindRequest request = future.getRequest();

          if (request instanceof SASLBindRequest)
          {
            SASLBindRequest saslBind = (SASLBindRequest) request;
            SASLContext saslContext = future.getSASLContext();

            if((result.getResultCode() == ResultCode.SUCCESS ||
                result.getResultCode() == ResultCode.SASL_BIND_IN_PROGRESS) &&
                    !saslContext.isComplete())
            {
              try
              {
                saslContext.evaluateCredentials(result
                    .getServerSASLCredentials());
              }
              catch (SaslException se)
              {
                pendingBindOrStartTLS = -1;

                Result errorResult = adaptException(se);
                future.handleErrorResult(errorResult);
                return;
              }
            }

            if (result.getResultCode() == ResultCode.SASL_BIND_IN_PROGRESS)
            {
              // The server is expecting a multi stage bind response.
              messageID = nextMsgID.getAndIncrement();
              ASN1StreamWriter asn1Writer =
                  connFactory.getASN1Writer(streamWriter);

              try
              {
                synchronized (writeLock)
                {
                  pendingRequests.put(messageID, future);
                  try
                  {
                    LDAPEncoder.encodeBindRequest(asn1Writer, messageID, 3,
                        saslBind, saslContext.getSASLCredentials());
                    asn1Writer.flush();
                  }
                  catch (IOException e)
                  {
                    pendingRequests.remove(messageID);

                    Result errorResult = adaptException(e);
                    connectionErrorOccurred(errorResult);
                    future.handleErrorResult(errorResult);
                  }
                }
              }
              finally
              {
                connFactory.releaseASN1Writer(asn1Writer);
              }
              return;
            }

            if ((result.getResultCode() == ResultCode.SUCCESS)
                && saslContext.isSecure())
            {
              // The connection needs to be secured by the SASL
              // mechanism.
              installFilter(SASLFilter.getInstance(saslContext, connection));
            }
          }
          pendingBindOrStartTLS = -1;
          future.handleResult(result);          
        }
        else
        {
          handleIncorrectResponse(pendingRequest);
        }
      }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCompareResult(int messageID, CompareResult result)
    {
      AbstractResultFutureImpl<?> pendingRequest =
          pendingRequests.remove(messageID);
      if (pendingRequest != null)
      {
        if (pendingRequest instanceof CompareResultFutureImpl)
        {
          CompareResultFutureImpl future =
              (CompareResultFutureImpl) pendingRequest;
          future.handleResult(result);
        }
        else
        {
          handleIncorrectResponse(pendingRequest);
        }
      }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void handleDeleteResult(int messageID, Result result)
    {
      AbstractResultFutureImpl<?> pendingRequest =
          pendingRequests.remove(messageID);
      if (pendingRequest != null)
      {
        if (pendingRequest instanceof ResultFutureImpl)
        {
          ResultFutureImpl future = (ResultFutureImpl) pendingRequest;
          if (future.getRequest() instanceof DeleteRequest)
          {
            future.handleResult(result);
            return;
          }
        }
        handleIncorrectResponse(pendingRequest);
      }
    }



    /**
     * {@inheritDoc}
     */
    public void handleException(Throwable throwable)
    {
      Result errorResult = adaptException(throwable);
      connectionErrorOccurred(errorResult);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void handleExtendedResult(int messageID,
        GenericExtendedResult result)
    {
      if (messageID == 0)
      {
        if ((result.getResponseName() != null)
            && result.getResponseName().equals(
                OID_NOTICE_OF_DISCONNECTION))
        {

          Result errorResult =
              Responses.newResult(result.getResultCode())
                  .setDiagnosticMessage(result.getDiagnosticMessage());
          close(null, true, errorResult);
          return;
        }
        else
        {
          // Unsolicited notification received.
          synchronized (writeLock)
          {
            if (isClosed)
            {
              // Don't notify after connection is closed.
              return;
            }

            for (ConnectionEventListener listener : listeners)
            {
              listener.connectionReceivedUnsolicitedNotification(
                  LDAPConnection.this, result);
            }
          }
        }
      }

      AbstractResultFutureImpl<?> pendingRequest =
          pendingRequests.remove(messageID);

      if (pendingRequest instanceof ExtendedResultFutureImpl<?>)
      {
        ExtendedResultFutureImpl<?> extendedFuture =
            ((ExtendedResultFutureImpl<?>) pendingRequest);
        try
        {
          handleExtendedResult0(extendedFuture, result);
        }
        catch (DecodeException de)
        {
          // FIXME: should the connection be closed as well?
          Result errorResult =
              Responses
                  .newResult(ResultCode.CLIENT_SIDE_DECODING_ERROR)
                  .setDiagnosticMessage(de.getLocalizedMessage())
                  .setCause(de);
          extendedFuture.handleErrorResult(errorResult);
        }
      }
      else
      {
        handleIncorrectResponse(pendingRequest);
      }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void handleIntermediateResponse(int messageID,
        GenericIntermediateResponse response)
    {
      AbstractResultFutureImpl<?> pendingRequest =
          pendingRequests.remove(messageID);
      if (pendingRequest != null)
      {
        handleIncorrectResponse(pendingRequest);

        // FIXME: intermediate responses can occur for all operations.

        // if (pendingRequest instanceof ExtendedResultFutureImpl)
        // {
        // ExtendedResultFutureImpl extendedFuture =
        // ((ExtendedResultFutureImpl) pendingRequest);
        // ExtendedRequest request = extendedFuture.getRequest();
        //
        // try
        // {
        // IntermediateResponse decodedResponse =
        // request.getExtendedOperation()
        // .decodeIntermediateResponse(
        // response.getResponseName(),
        // response.getResponseValue());
        // extendedFuture.handleIntermediateResponse(decodedResponse);
        // }
        // catch (DecodeException de)
        // {
        // pendingRequest.failure(de);
        // }
        // }
        // else
        // {
        // handleIncorrectResponse(pendingRequest);
        // }
      }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void handleModifyDNResult(int messageID, Result result)
    {
      AbstractResultFutureImpl<?> pendingRequest =
          pendingRequests.remove(messageID);
      if (pendingRequest != null)
      {
        if (pendingRequest instanceof ResultFutureImpl)
        {
          ResultFutureImpl future = (ResultFutureImpl) pendingRequest;
          if (future.getRequest() instanceof ModifyDNRequest)
          {
            future.handleResult(result);
            return;
          }
        }
        handleIncorrectResponse(pendingRequest);
      }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void handleModifyResult(int messageID, Result result)
    {
      AbstractResultFutureImpl<?> pendingRequest =
          pendingRequests.remove(messageID);
      if (pendingRequest != null)
      {
        if (pendingRequest instanceof ResultFutureImpl)
        {
          ResultFutureImpl future = (ResultFutureImpl) pendingRequest;
          if (future.getRequest() instanceof ModifyRequest)
          {
            future.handleResult(result);
            return;
          }
        }
        handleIncorrectResponse(pendingRequest);
      }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void handleSearchResult(int messageID, SearchResult result)
    {
      AbstractResultFutureImpl<?> pendingRequest =
          pendingRequests.remove(messageID);
      if (pendingRequest != null)
      {
        if (pendingRequest instanceof SearchResultFutureImpl)
        {
          ((SearchResultFutureImpl) pendingRequest)
              .handleResult(result);
        }
        else
        {
          handleIncorrectResponse(pendingRequest);
        }
      }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void handleSearchResultEntry(int messageID,
        SearchResultEntry entry)
    {
      AbstractResultFutureImpl<?> pendingRequest =
          pendingRequests.get(messageID);
      if (pendingRequest != null)
      {
        if (pendingRequest instanceof SearchResultFutureImpl)
        {
          ((SearchResultFutureImpl) pendingRequest)
              .handleSearchResultEntry(entry);
        }
        else
        {
          handleIncorrectResponse(pendingRequest);
        }
      }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void handleSearchResultReference(int messageID,
        SearchResultReference reference)
    {
      AbstractResultFutureImpl<?> pendingRequest =
          pendingRequests.get(messageID);
      if (pendingRequest != null)
      {
        if (pendingRequest instanceof SearchResultFutureImpl)
        {
          ((SearchResultFutureImpl) pendingRequest)
              .handleSearchResultReference(reference);
        }
        else
        {
          handleIncorrectResponse(pendingRequest);
        }
      }
    }

    @Override
    public Control decodeResponseControl(int messageID, String oid,
                                         boolean isCritical, ByteString value)
        throws DecodeException
    {
      ControlDecoder decoder = connFactory.getControlDecoder(oid);
      if(decoder != null)
      {
        return decoder.decode(isCritical, value);
      }
      return super.decodeResponseControl(messageID, oid, isCritical, value);
    }
  }



  /**
   * Connects to the Directory Server at the provided host and port
   * address using the provided connection options.
   *
   * @param host
   *          The host name.
   * @param port
   *          The port number.
   * @param handler
   *          A completion handler which can be used to asynchronously
   *          process the connection when it is successfully connects,
   *          may be {@code null}.
   * @return A future representing the connection.
   * @throws InitializationException
   *           If a problem occurred while configuring the connection
   *           parameters using the provided options.
   * @throws NullPointerException
   *           If {@code host} was {@code null}.
   */
  public static ConnectionFuture connect(String host, int port,
      ConnectionResultHandler handler)
      throws InitializationException, NullPointerException
  {
    return new LDAPConnectionFactory(host, port)
        .connect(handler);
  }

  private final com.sun.grizzly.Connection<?> connection;
  private Result connectionInvalidReason;

  private final LDAPConnectionFactory connFactory;
  private FilterChain customFilterChain;
  private final LDAPMessageHandler handler =
      new LDAPMessageHandlerImpl();

  private boolean isClosed = false;
  private final List<ConnectionEventListener> listeners =
      new LinkedList<ConnectionEventListener>();
  private final AtomicInteger nextMsgID = new AtomicInteger(1);
  private volatile int pendingBindOrStartTLS = -1;
  private final ConcurrentHashMap<Integer,
      AbstractResultFutureImpl<?>> pendingRequests =
      new ConcurrentHashMap<Integer, AbstractResultFutureImpl<?>>();

  private final InetSocketAddress serverAddress;

  private StreamWriter streamWriter;

  private final Object writeLock = new Object();



  /**
   * Creates a new LDAP connection.
   *
   * @param connection
   *          The Grizzly connection.
   * @param serverAddress
   *          The address of the server.
   * @param connFactory
   *          The associated connection factory.
   */
  LDAPConnection(com.sun.grizzly.Connection<?> connection,
      InetSocketAddress serverAddress, LDAPConnectionFactory connFactory)
  {
    this.connection = connection;
    this.serverAddress = serverAddress;
    this.connFactory = connFactory;
    this.streamWriter = getFilterChainStreamWriter();
  }



  /**
   * {@inheritDoc}
   */
  public void abandon(AbandonRequest request)
  {
    AbstractResultFutureImpl<?> pendingRequest =
        pendingRequests.remove(request.getMessageID());
    if (pendingRequest != null)
    {
      pendingRequest.cancel(false);
      int messageID = nextMsgID.getAndIncrement();
      ASN1StreamWriter asn1Writer =
          connFactory.getASN1Writer(streamWriter);

      try
      {
        synchronized (writeLock)
        {
          if (connectionInvalidReason != null)
          {
            return;
          }
          if (pendingBindOrStartTLS > 0)
          {
            // This is not allowed. We will just ignore this
            // abandon request.
          }
          try
          {
            LDAPEncoder.encodeAbandonRequest(asn1Writer, messageID,
                request);
            asn1Writer.flush();
          }
          catch (IOException e)
          {
            Result errorResult = adaptException(e);
            connectionErrorOccurred(errorResult);
          }
        }
      }
      finally
      {
        connFactory.releaseASN1Writer(asn1Writer);
      }
    }
  }



  /**
   * {@inheritDoc}
   */
  public void abandon(int messageID) throws IllegalStateException
  {
    abandon(Requests.newAbandonRequest(messageID));
  }



  /**
   * {@inheritDoc}
   */
  public ResultFuture add(AddRequest request,
      ResultHandler<Result> handler)
  {
    int messageID = nextMsgID.getAndIncrement();
    ResultFutureImpl future =
        new ResultFutureImpl(messageID, request, handler, this,
            connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized (writeLock)
      {
        if (connectionInvalidReason != null)
        {
          future.handleErrorResult(connectionInvalidReason);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          future.handleResult(Responses.newResult(
              ResultCode.OPERATIONS_ERROR).setDiagnosticMessage(
              "Bind or Start TLS operation in progress"));
          return future;
        }
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeAddRequest(asn1Writer, messageID, request);
          asn1Writer.flush();
        }
        catch (IOException e)
        {
          pendingRequests.remove(messageID);

          Result errorResult = adaptException(e);
          connectionErrorOccurred(errorResult);
          future.handleErrorResult(errorResult);
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }



  /**
   * {@inheritDoc}
   */
  public ResultFuture add(AttributeSequence entry)
      throws IllegalStateException, NullPointerException
  {
    return add(Requests.asAddRequest(entry), null);
  }



  /**
   * {@inheritDoc}
   */
  public ResultFuture add(String dn, String... ldifAttributes)
      throws IllegalArgumentException, IllegalStateException,
      NullPointerException
  {
    return add(Requests.newAddRequest(dn, ldifAttributes), null);
  }



  /**
   * {@inheritDoc}
   */
  public void addConnectionEventListener(
      ConnectionEventListener listener) throws IllegalStateException,
      NullPointerException
  {
    Validator.ensureNotNull(listener);

    synchronized (writeLock)
    {
      if (isClosed)
      {
        throw new IllegalStateException();
      }

      listeners.add(listener);
    }
  }



  /**
   * {@inheritDoc}
   */
  public BindResultFuture bind(BindRequest request,
      ResultHandler<BindResult> handler)
  {
    int messageID = nextMsgID.getAndIncrement();
    BindResultFutureImpl future =
        new BindResultFutureImpl(messageID, request, handler, this,
            connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized (writeLock)
      {
        if (connectionInvalidReason != null)
        {
          future.handleErrorResult(connectionInvalidReason);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          future.handleResult(Responses.newBindResult(
              ResultCode.OPERATIONS_ERROR).setDiagnosticMessage(
              "Bind or Start TLS operation in progress"));
          return future;
        }
        if (!pendingRequests.isEmpty())
        {
          future.handleResult(Responses.newBindResult(
              ResultCode.OPERATIONS_ERROR).setDiagnosticMessage(
              "There are other operations pending on this connection"));
          return future;
        }

        pendingRequests.put(messageID, future);
        pendingBindOrStartTLS = messageID;

        try
        {
          if (request instanceof SASLBindRequest)
          {
            try
            {
              SASLBindRequest saslBind = (SASLBindRequest)request;
              SASLContext saslContext = saslBind.getClientContext(
                      serverAddress.getHostName());
              future.setSASLContext(saslContext);
              LDAPEncoder.encodeBindRequest(asn1Writer, messageID, 3,
                  saslBind, saslContext.getSASLCredentials());
            }
            catch (SaslException e)
            {
              Result errorResult = adaptException(e);
              future.handleErrorResult(errorResult);
              return future;
            }
          }
          else if(request instanceof SimpleBindRequest)
          {
            LDAPEncoder.encodeBindRequest(asn1Writer, messageID, 3,
                                          (SimpleBindRequest) request);
          }
          else
          {
            pendingRequests.remove(messageID);
            future.handleResult(Responses.newBindResult(
                ResultCode.PROTOCOL_ERROR).setDiagnosticMessage(
                "Auth type not supported"));
          }
          asn1Writer.flush();
        }
        catch (IOException e)
        {
          pendingRequests.remove(messageID);

          Result errorResult = adaptException(e);
          connectionErrorOccurred(errorResult);
          future.handleErrorResult(errorResult);
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }



  /**
   * {@inheritDoc}
   */
  public BindResultFuture bind(String name, String password)
      throws IllegalStateException, NullPointerException
  {
    return bind(Requests.newSimpleBindRequest(name, password), null);
  }



  /**
   * {@inheritDoc}
   */
  public void close()
  {
    close(Requests.newUnbindRequest());
  }



  /**
   * {@inheritDoc}
   */
  public void close(UnbindRequest request) throws NullPointerException
  {
    // FIXME: I18N need to internationalize this message.
    Validator.ensureNotNull(request);

    close(request, false, Responses.newResult(
        ResultCode.CLIENT_SIDE_USER_CANCELLED).setDiagnosticMessage(
        "Connection closed by client"));
  }



  /**
   * {@inheritDoc}
   */
  public CompareResultFuture compare(CompareRequest request,
      ResultHandler<CompareResult> handler)
  {
    int messageID = nextMsgID.getAndIncrement();
    CompareResultFutureImpl future =
        new CompareResultFutureImpl(messageID, request, handler, this,
            connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized (writeLock)
      {
        if (connectionInvalidReason != null)
        {
          future.handleErrorResult(connectionInvalidReason);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          future.handleResult(Responses.newCompareResult(
              ResultCode.OPERATIONS_ERROR).setDiagnosticMessage(
              "Bind or Start TLS operation in progress"));
          return future;
        }
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeCompareRequest(asn1Writer, messageID,
              request);
          asn1Writer.flush();
        }
        catch (IOException e)
        {
          pendingRequests.remove(messageID);

          Result errorResult = adaptException(e);
          connectionErrorOccurred(errorResult);
          future.handleErrorResult(errorResult);
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }



  /**
   * {@inheritDoc}
   */
  public CompareResultFuture compare(String dn,
      String attributeDescription, String assertionValue)
      throws IllegalStateException, NullPointerException
  {
    return compare(Requests.newCompareRequest(dn, attributeDescription,
        assertionValue), null);
  }



  /**
   * {@inheritDoc}
   */
  public ResultFuture delete(DeleteRequest request,
      ResultHandler<Result> handler)
  {
    int messageID = nextMsgID.getAndIncrement();
    ResultFutureImpl future =
        new ResultFutureImpl(messageID, request, handler, this,
            connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized (writeLock)
      {
        if (connectionInvalidReason != null)
        {
          future.handleErrorResult(connectionInvalidReason);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          future.handleResult(Responses.newResult(
              ResultCode.OPERATIONS_ERROR).setDiagnosticMessage(
              "Bind or Start TLS operation in progress"));
          return future;
        }
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeDeleteRequest(asn1Writer, messageID,
              request);
          asn1Writer.flush();
        }
        catch (IOException e)
        {
          pendingRequests.remove(messageID);

          Result errorResult = adaptException(e);
          connectionErrorOccurred(errorResult);
          future.handleErrorResult(errorResult);
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }



  /**
   * {@inheritDoc}
   */
  public ResultFuture delete(String dn) throws IllegalStateException,
      NullPointerException
  {
    return delete(Requests.newDeleteRequest(dn), null);
  }



  /**
   * {@inheritDoc}
   */
  public <R extends Result> ExtendedResultFuture<R> extendedRequest(
      ExtendedRequest<R> request, ResultHandler<R> handler)
  {
    int messageID = nextMsgID.getAndIncrement();
    ExtendedResultFutureImpl<R> future =
        new ExtendedResultFutureImpl<R>(messageID, request, handler,
            this, connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized (writeLock)
      {
        if (connectionInvalidReason != null)
        {
          future.handleErrorResult(connectionInvalidReason);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          future.handleResult(request.getExtendedOperation()
              .decodeResponse(ResultCode.OPERATIONS_ERROR, "",
                  "Bind or Start TLS operation in progress"));
          return future;
        }
        if (request.getRequestName().equals(
            StartTLSRequest.OID_START_TLS_REQUEST))
        {
          if (!pendingRequests.isEmpty())
          {
            future.handleResult(request.getExtendedOperation()
                .decodeResponse(ResultCode.OPERATIONS_ERROR, "",
                    "There are pending operations on this connection"));
            return future;
          }
          if (isTLSEnabled())
          {
            future.handleResult(request.getExtendedOperation()
                .decodeResponse(ResultCode.OPERATIONS_ERROR, "",
                    "This connection is already TLS enabled"));
          }
          pendingBindOrStartTLS = messageID;
        }
        pendingRequests.put(messageID, future);

        try
        {
          LDAPEncoder.encodeExtendedRequest(asn1Writer, messageID,
              request);
          asn1Writer.flush();
        }
        catch (IOException e)
        {
          pendingRequests.remove(messageID);

          Result errorResult = adaptException(e);
          connectionErrorOccurred(errorResult);
          future.handleErrorResult(errorResult);
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }



  /**
   * {@inheritDoc}
   */
  public ExtendedResultFuture<GenericExtendedResult> extendedRequest(
      String requestName, ByteString requestValue)
      throws IllegalStateException, NullPointerException
  {
    return extendedRequest(Requests.newGenericExtendedRequest(
        requestName, requestValue), null);
  }



  /**
   * {@inheritDoc}
   */
  public ResultFuture modify(ModifyRequest request,
      ResultHandler<Result> handler)
  {
    int messageID = nextMsgID.getAndIncrement();
    ResultFutureImpl future =
        new ResultFutureImpl(messageID, request, handler, this,
            connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized (writeLock)
      {
        if (connectionInvalidReason != null)
        {
          future.handleErrorResult(connectionInvalidReason);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          future.handleResult(Responses.newResult(
              ResultCode.OPERATIONS_ERROR).setDiagnosticMessage(
              "Bind or Start TLS operation in progress"));
          return future;
        }
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeModifyRequest(asn1Writer, messageID,
              request);
          asn1Writer.flush();
        }
        catch (IOException e)
        {
          pendingRequests.remove(messageID);

          Result errorResult = adaptException(e);
          connectionErrorOccurred(errorResult);
          future.handleErrorResult(errorResult);
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }



  /**
   * {@inheritDoc}
   */
  public ResultFuture modify(String dn, String... ldifChanges)
      throws IllegalArgumentException, IllegalStateException,
      NullPointerException
  {
    return modify(Requests.newModifyRequest(dn, ldifChanges), null);
  }



  /**
   * {@inheritDoc}
   */
  public ResultFuture modifyDN(ModifyDNRequest request,
      ResultHandler<Result> handler)
  {
    int messageID = nextMsgID.getAndIncrement();
    ResultFutureImpl future =
        new ResultFutureImpl(messageID, request, handler, this,
            connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized (writeLock)
      {
        if (connectionInvalidReason != null)
        {
          future.handleErrorResult(connectionInvalidReason);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          future.handleResult(Responses.newResult(
              ResultCode.OPERATIONS_ERROR).setDiagnosticMessage(
              "Bind or Start TLS operation in progress"));
          return future;
        }
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeModifyDNRequest(asn1Writer, messageID,
              request);
          asn1Writer.flush();
        }
        catch (IOException e)
        {
          pendingRequests.remove(messageID);

          Result errorResult = adaptException(e);
          connectionErrorOccurred(errorResult);
          future.handleErrorResult(errorResult);
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }



  /**
   * {@inheritDoc}
   */
  public ResultFuture modifyDN(String dn, String newRDN)
      throws IllegalStateException, NullPointerException
  {
    return modifyDN(Requests.newModifyDNRequest(dn, newRDN), null);
  }



  /**
   * {@inheritDoc}
   */
  public void removeConnectionEventListener(
      ConnectionEventListener listener) throws NullPointerException
  {
    Validator.ensureNotNull(listener);

    synchronized (writeLock)
    {
      listeners.remove(listener);
    }
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultFuture search(SearchRequest request,
      SearchResultHandler handler)
  {
    int messageID = nextMsgID.getAndIncrement();
    SearchResultFutureImpl future =
        new SearchResultFutureImpl(messageID, handler, this,
            connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized (writeLock)
      {
        if (connectionInvalidReason != null)
        {
          future.handleErrorResult(connectionInvalidReason);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          future.handleResult(Responses.newSearchResult(
              ResultCode.OPERATIONS_ERROR).setDiagnosticMessage(
              "Bind or Start TLS operation in progress"));
          return future;
        }
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeSearchRequest(asn1Writer, messageID,
              request);
          asn1Writer.flush();
        }
        catch (IOException e)
        {
          pendingRequests.remove(messageID);

          Result errorResult = adaptException(e);
          connectionErrorOccurred(errorResult);
          future.handleErrorResult(errorResult);
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultFuture search(String baseDN, SearchScope scope,
      String filter, String... attributes)
      throws IllegalArgumentException, IllegalStateException,
      NullPointerException
  {
    return search(Requests.newSearchRequest(baseDN, scope, filter,
        attributes), null);
  }

  public SearchResultEntry get(String dn, String... attributes)
      throws IllegalArgumentException, IllegalStateException,
      NullPointerException, ErrorResultException, InterruptedException
  {
    FirstEntrySearchResultHandler handler = new FirstEntrySearchResultHandler();
    search(Requests.newSearchRequest(dn, SearchScope.BASE_OBJECT,
        org.opends.sdk.Filter.getObjectClassPresentFilter(),
        attributes), handler).get();
    // TODO: Race condition: result could be null since the handler is executed
    // by another thread.
    Thread.sleep(50);
    return handler.result;
  }

  /**
   * Returns the LDAP message handler associated with this connection.
   *
   * @return The LDAP message handler associated with this connection.
   */
  LDAPMessageHandler getLDAPMessageHandler()
  {
    return handler;
  }



  /**
   * Indicates whether or not TLS is enabled on this connection.
   *
   * @return {@code true} if TLS is enabled on this connection,
   *         otherwise {@code false}.
   */
  boolean isTLSEnabled()
  {
    FilterChain currentFilterChain =
        (FilterChain) connection.getProcessor();
    return currentFilterChain.get(2) instanceof SSLFilter;
  }



  private Result adaptException(Throwable t)
  {
    if (t instanceof ExecutionException)
    {
      ExecutionException e = (ExecutionException) t;
      t = e.getCause();
    }

    Result errorResult;

    try
    {
      throw t;
    }
    catch (SaslException e)
    {
      // FIXME: I18N need to have a better error message.
      // FIXME: Is this the best result code?
      errorResult =
          Responses.newResult(ResultCode.CLIENT_SIDE_LOCAL_ERROR)
              .setDiagnosticMessage(
                  "An error occurred during SASL authentication")
              .setCause(e);
    }
    catch (EOFException e)
    {
      // FIXME: I18N need to have a better error message.
      // FIXME: what sort of IOExceptions can be thrown?
      // FIXME: Is this the best result code?
      errorResult =
          Responses
              .newResult(ResultCode.CLIENT_SIDE_SERVER_DOWN)
              .setDiagnosticMessage(
                  "Connection unexpectedly terminated by server")
              .setCause(e);
    }
    catch (IOException e)
    {
      // FIXME: I18N need to have a better error message.
      // FIXME: what sort of IOExceptions can be thrown?
      // FIXME: Is this the best result code?
      errorResult =
          Responses
              .newResult(ResultCode.CLIENT_SIDE_LOCAL_ERROR)
              .setDiagnosticMessage(
                  "An error occurred whilst attempting to send a request: " +
                      e.toString())
              .setCause(e);
    }
    catch (Throwable e)
    {
      // FIXME: I18N need to have a better error message.
      // FIXME: Is this the best result code?
      errorResult =
          Responses.newResult(ResultCode.CLIENT_SIDE_LOCAL_ERROR)
              .setDiagnosticMessage("An unknown error occurred: "
                  + e.toString())
              .setCause(e);
    }

    return errorResult;
  }



  private void close(UnbindRequest unbindRequest,
      boolean isDisconnectNotification, Result reason)
  {
    synchronized (writeLock)
    {
      boolean notifyClose = false;
      boolean notifyErrorOccurred = false;

      if (isClosed)
      {
        // Already closed.
        return;
      }

      if (unbindRequest != null)
      {
        // User closed.
        isClosed = true;
        notifyClose = true;
      }
      else
      {
        notifyErrorOccurred = true;
      }

      if (connectionInvalidReason != null)
      {
        // Already invalid.
        if (notifyClose)
        {
          // TODO: uncomment if close notification is required.
          // for (ConnectionEventListener listener : listeners)
          // {
          // listener.connectionClosed(this);
          // }
        }
        return;
      }

      // First abort all outstanding requests.
      for (AbstractResultFutureImpl<?> future : pendingRequests
          .values())
      {
        if (pendingBindOrStartTLS <= 0)
        {
          ASN1StreamWriter asn1Writer =
              connFactory.getASN1Writer(streamWriter);
          int messageID = nextMsgID.getAndIncrement();
          AbandonRequest abandon =
              Requests.newAbandonRequest(future.getMessageID());
          try
          {
            LDAPEncoder.encodeAbandonRequest(asn1Writer, messageID,
                abandon);
            asn1Writer.flush();
          }
          catch (IOException e)
          {
            // Underlying channel probably blown up. Just ignore.
          }
          finally
          {
            connFactory.releaseASN1Writer(asn1Writer);
          }
        }

        future.handleErrorResult(reason);
      }
      pendingRequests.clear();

      // Now try cleanly closing the connection if possible.
      try
      {
        ASN1StreamWriter asn1Writer =
            connFactory.getASN1Writer(streamWriter);
        if (unbindRequest == null)
        {
          unbindRequest = Requests.newUnbindRequest();
        }

        try
        {
          LDAPEncoder.encodeUnbindRequest(asn1Writer, nextMsgID
              .getAndIncrement(), unbindRequest);
          asn1Writer.flush();
        }
        finally
        {
          connFactory.releaseASN1Writer(asn1Writer);
        }
      }
      catch (IOException e)
      {
        // Underlying channel prob blown up. Just ignore.
      }

      try
      {
        streamWriter.close();
      }
      catch (IOException e)
      {
        // Ignore.
      }

      try
      {
        connection.close();
      }
      catch (IOException e)
      {
        // Ignore.
      }

      // Mark the connection as invalid.
      connectionInvalidReason = reason;

      // Notify listeners.
      if (notifyClose)
      {
        // TODO: uncomment if close notification is required.
        // for (ConnectionEventListener listener : listeners)
        // {
        // listener.connectionClosed(this);
        // }
      }

      if (notifyErrorOccurred)
      {
        for (ConnectionEventListener listener : listeners)
        {
          listener.connectionErrorOccurred(this, false,
              ErrorResultException.wrap(reason));
        }
      }
    }
  }



  private void connectionErrorOccurred(Result reason)
  {
    close(null, false, reason);
  }



  // TODO uncomment if we decide these methods are useful.
  // /**
  // * {@inheritDoc}
  // */
  // public boolean isClosed()
  // {
  // synchronized (writeLock)
  // {
  // return isClosed;
  // }
  // }
  //
  //
  //
  // /**
  // * {@inheritDoc}
  // */
  // public boolean isValid() throws InterruptedException
  // {
  // synchronized (writeLock)
  // {
  // return connectionInvalidReason == null;
  // }
  // }
  //
  //
  //
  // /**
  // * {@inheritDoc}
  // */
  // public boolean isValid(long timeout, TimeUnit unit)
  // throws InterruptedException, TimeoutException
  // {
  // // FIXME: no support for timeout.
  // return isValid();
  // }

  private StreamWriter getFilterChainStreamWriter()
  {
    StreamWriter writer = connection.getStreamWriter();
    FilterChain currentFilterChain =
        (FilterChain) connection.getProcessor();
    for (Filter filter : currentFilterChain)
    {
      if (filter instanceof StreamTransformerFilter)
      {
        writer =
            ((StreamTransformerFilter) filter).getStreamWriter(writer);
      }
    }

    return writer;
  }



  // Needed in order to expose type information.
  private <R extends Result> void handleExtendedResult0(
      ExtendedResultFutureImpl<R> future, GenericExtendedResult result)
      throws DecodeException
  {
    R decodedResponse =
        future.decodeResponse(result.getResultCode(), result
            .getMatchedDN(), result.getDiagnosticMessage(), result
            .getResponseName(), result.getResponseValue());

    if (future.getRequest() instanceof StartTLSRequest)
    {
      if (result.getResultCode() == ResultCode.SUCCESS)
      {
        StartTLSRequest request = (StartTLSRequest) future.getRequest();
        try
        {
          startTLS(request.getSSLContext());
        }
        catch (ErrorResultException e)
        {
          future.handleErrorResult(e.getResult());  
          return;
        }
      }
      pendingBindOrStartTLS = -1;
    }

    future.handleResult(decodedResponse);
  }



  private void handleIncorrectResponse(
      AbstractResultFutureImpl<?> pendingRequest)
  {
    // FIXME: I18N need to have a better error message.
    Result errorResult =
        Responses.newResult(ResultCode.CLIENT_SIDE_DECODING_ERROR)
            .setDiagnosticMessage(
                "LDAP response message did not match request");

    pendingRequest.handleErrorResult(errorResult);
    connectionErrorOccurred(errorResult);
  }

  private void startTLS(SSLContext sslContext)
      throws ErrorResultException
  {
    SSLHandshaker sslHandshaker = connFactory.getSslHandshaker();
    SSLFilter sslFilter;
    SSLEngineConfigurator sslEngineConfigurator;
    if(sslContext == connFactory.getSSLContext())
    {
      // Use factory SSL objects since it is the same SSLContext
      sslFilter = connFactory.getSSlFilter();
      sslEngineConfigurator = connFactory.getSSlEngineConfigurator();
    }
    else
    {
      sslEngineConfigurator =
          new SSLEngineConfigurator(sslContext, true, false, false);
      sslFilter = new SSLFilter(sslEngineConfigurator, sslHandshaker);
    }
    installFilter(sslFilter);

    performSSLHandshake(sslHandshaker, sslEngineConfigurator);
  }

  void performSSLHandshake(SSLHandshaker sslHandshaker,
                     SSLEngineConfigurator sslEngineConfigurator)
      throws ErrorResultException
  {
    SSLStreamReader reader =
        new SSLStreamReader(connection.getStreamReader());
    SSLStreamWriter writer =
        new SSLStreamWriter(connection.getStreamWriter());

    try {
      sslHandshaker.handshake(reader, writer, sslEngineConfigurator).get();
    } catch (Exception e) {
      Result result = adaptException(e);
      connectionErrorOccurred(result);
      throw ErrorResultException.wrap(result);
    }
  }

  synchronized void installFilter(Filter filter)
  {
    if (customFilterChain == null)
    {
      customFilterChain =
          connFactory.getDefaultFilterChainFactory().create();
      connection.setProcessor(customFilterChain);
    }

    // Install the SSLFilter in the custom filter chain
    Filter oldFilter = customFilterChain.remove(customFilterChain.size() - 1);
    customFilterChain.add(filter);
    customFilterChain.add(oldFilter);

    // Update stream writer
    streamWriter = getFilterChainStreamWriter();
  }
}
