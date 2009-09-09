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



import static org.opends.server.protocols.ldap.LDAPConstants.OID_NOTICE_OF_DISCONNECTION;
import static org.opends.server.util.ServerConstants.OID_START_TLS_REQUEST;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLEngine;
import javax.security.sasl.SaslException;

import org.opends.sdk.AbandonRequest;
import org.opends.sdk.AddRequest;
import org.opends.sdk.AttributeSequence;
import org.opends.sdk.BindRequest;
import org.opends.sdk.BindResult;
import org.opends.sdk.BindResultFuture;
import org.opends.sdk.CompareRequest;
import org.opends.sdk.CompareResult;
import org.opends.sdk.CompareResultFuture;
import org.opends.sdk.Connection;
import org.opends.sdk.ConnectionEventListener;
import org.opends.sdk.DecodeException;
import org.opends.sdk.DeleteRequest;
import org.opends.sdk.ErrorResultException;
import org.opends.sdk.ExtendedRequest;
import org.opends.sdk.ExtendedResultFuture;
import org.opends.sdk.GenericExtendedResult;
import org.opends.sdk.GenericIntermediateResponse;
import org.opends.sdk.ModifyDNRequest;
import org.opends.sdk.ModifyRequest;
import org.opends.sdk.Requests;
import org.opends.sdk.Responses;
import org.opends.sdk.Result;
import org.opends.sdk.ResultCode;
import org.opends.sdk.ResultFuture;
import org.opends.sdk.ResultHandler;
import org.opends.sdk.SearchRequest;
import org.opends.sdk.SearchResult;
import org.opends.sdk.SearchResultEntry;
import org.opends.sdk.SearchResultFuture;
import org.opends.sdk.SearchResultHandler;
import org.opends.sdk.SearchResultReference;
import org.opends.sdk.SearchScope;
import org.opends.sdk.SimpleBindRequest;
import org.opends.sdk.UnbindRequest;
import org.opends.sdk.sasl.AbstractSASLBindRequest;
import org.opends.sdk.sasl.SASLBindRequest;
import org.opends.sdk.util.Validator;
import org.opends.server.types.ByteString;

import com.sun.grizzly.filterchain.Filter;
import com.sun.grizzly.filterchain.FilterChain;
import com.sun.grizzly.filterchain.StreamTransformerFilter;
import com.sun.grizzly.ssl.SSLFilter;
import com.sun.grizzly.ssl.SSLStreamReader;
import com.sun.grizzly.ssl.SSLStreamWriter;
import com.sun.grizzly.streams.StreamWriter;



/**
 * LDAP connection implementation.
 * <p>
 * TODO: handle illegal state exceptions.
 */
public class LDAPConnection extends AbstractLDAPMessageHandler
    implements Connection
{

  private final com.sun.grizzly.Connection<?> connection;
  private Result connectionInvalidReason;
  private final LDAPConnectionFactory connFactory;

  private FilterChain customFilterChain;
  private final AtomicInteger nextMsgID = new AtomicInteger(1);
  private volatile int pendingBindOrStartTLS = -1;

  private final ConcurrentHashMap<Integer, AbstractResultFutureImpl<?>> pendingRequests =
      new ConcurrentHashMap<Integer, AbstractResultFutureImpl<?>>();
  private final InetSocketAddress serverAddress;
  private StreamWriter streamWriter;
  private final Object writeLock = new Object();
  private boolean isClosed = false;

  private final List<ConnectionEventListener> listeners =
      new LinkedList<ConnectionEventListener>();



  /**
   * Creates a new LDAP connection.
   *
   * @param connection
   *          The Grizzly connection.
   * @param serverAddress
   *          The address of the server.
   * @param connFactory
   *          The associated connection factory.
   * @throws IOException
   *           If an error occurred while connecting.
   */
  LDAPConnection(com.sun.grizzly.Connection<?> connection,
      InetSocketAddress serverAddress, LDAPConnectionFactory connFactory)
      throws IOException
  {
    this.connection = connection;
    this.serverAddress = serverAddress;
    this.connFactory = connFactory;
    this.streamWriter = getFilterChainStreamWriter();

    if (isTLSEnabled())
    {
      try
      {
        performSSLHandshake();
      }
      catch (Exception e)
      {
        if (e instanceof ExecutionException)
        {
          throw new IOException("SSL Handshake failed:", e.getCause());
        }

        throw new IOException("SSL Handshake failed:", e);
      }
    }
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

        if (request instanceof AbstractSASLBindRequest<?>)
        {
          try
          {
            ((AbstractSASLBindRequest<?>) request)
                .initialize(serverAddress.getHostName());
          }
          catch (SaslException e)
          {
            Result errorResult = adaptException(e);
            future.handleErrorResult(errorResult);
            return future;
          }
        }

        sendBind(future, request);
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
  public void close()
  {
    close(Requests.newUnbindRequest());
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
        if (request.getRequestName().equals(OID_START_TLS_REQUEST))
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

        // FIXME: should not reference AbstractSASLBindRequest.
        if (request instanceof AbstractSASLBindRequest<?>)
        {
          AbstractSASLBindRequest<?> saslBind =
              (AbstractSASLBindRequest<?>) request;

          try
          {
            saslBind.evaluateCredentials(result
                .getServerSASLCredentials());
          }
          catch (SaslException se)
          {
            pendingBindOrStartTLS = -1;

            Result errorResult = adaptException(se);
            future.handleErrorResult(errorResult);
            return;
          }

          if (result.getResultCode() == ResultCode.SASL_BIND_IN_PROGRESS)
          {
            // The server is expecting a multi stage bind response.
            sendBind(future, saslBind);
            return;
          }

          if ((result.getResultCode() == ResultCode.SUCCESS)
              && saslBind.isSecure())
          {
            // The connection needs to be secured by the SASL mechanism.
            if (customFilterChain == null)
            {
              customFilterChain =
                  connFactory.getDefaultFilterChainFactory().create();
              connection.setProcessor(customFilterChain);
            }

            // Install the SSLFilter in the custom filter chain
            Filter oldFilter = customFilterChain.remove(2);
            customFilterChain.add(SASLFilter.getInstance(saslBind,
                connection));
            if (!(oldFilter instanceof SSLFilter))
            {
              customFilterChain.add(oldFilter);
            }

            streamWriter = getFilterChainStreamWriter();
          }
        }

        future.handleResult(result);
        pendingBindOrStartTLS = -1;
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
            listener.connectionReceivedUnsolicitedNotification(this,
                result);
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
            Responses.newResult(ResultCode.CLIENT_SIDE_DECODING_ERROR)
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
        pendingRequests.get(messageID);
    if (pendingRequest != null)
    {
      if (pendingRequest instanceof SearchResultFutureImpl)
      {
        ((SearchResultFutureImpl) pendingRequest).handleResult(result);
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



  /**
   * Indicates whether or not TLS is enabled on this connection.
   *
   * @return {@code true} if TLS is enabled on this connection,
   *         otherwise {@code false}.
   */
  public boolean isTLSEnabled()
  {
    FilterChain currentFilterChain =
        (FilterChain) connection.getProcessor();
    return currentFilterChain.get(2) instanceof SSLFilter;
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
    catch (IOException e)
    {
      // FIXME: I18N need to have a better error message.
      // FIXME: what sort of IOExceptions can be thrown?
      // FIXME: Is this the best result code?
      errorResult =
          Responses
              .newResult(ResultCode.CLIENT_SIDE_LOCAL_ERROR)
              .setDiagnosticMessage(
                  "An error occurred whilst attempting to send a request")
              .setCause(e);
    }
    catch (Throwable e)
    {
      // FIXME: I18N need to have a better error message.
      // FIXME: Is this the best result code?
      errorResult =
          Responses.newResult(ResultCode.CLIENT_SIDE_LOCAL_ERROR)
              .setDiagnosticMessage("An unknown error occurred")
              .setCause(e);
    }

    return errorResult;
  }



  private void connectionErrorOccurred(Result reason)
  {
    close(null, false, reason);
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
      for (AbstractResultFutureImpl<?> future : pendingRequests.values())
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

    if (OID_START_TLS_REQUEST.equals(result.getResponseName()))
    {
      if (result.getResultCode() == ResultCode.SUCCESS)
      {
        if (customFilterChain == null)
        {
          customFilterChain =
              connFactory.getDefaultFilterChainFactory().create();
          connection.setProcessor(customFilterChain);
        }

        // Install the SSLFilter in the custom filter chain
        Filter oldFilter = customFilterChain.remove(2);
        customFilterChain.add(connFactory.getSSLFilter());
        if (!(oldFilter instanceof SSLFilter))
        {
          customFilterChain.add(oldFilter);
        }

        try
        {
          performSSLHandshake();
          streamWriter = getFilterChainStreamWriter();
        }
        catch (Exception ioe)
        {
          // Remove the SSLFilter we just tried to add.
          customFilterChain.remove(1);

          Result errorResult = adaptException(ioe);
          future.handleErrorResult(errorResult);
          connectionErrorOccurred(errorResult);
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



  private void performSSLHandshake() throws InterruptedException,
      ExecutionException, IOException
  {
    // We have a TLS layer already installed so handshake
    SSLStreamReader reader =
        new SSLStreamReader(connection.getStreamReader());
    SSLStreamWriter writer =
        new SSLStreamWriter(connection.getStreamWriter());
    Future<SSLEngine> future =
        connFactory.getSSLHandshaker().handshake(reader, writer,
            connFactory.getSSLEngineConfigurator());

    future.get();
  }



  private void sendBind(BindResultFutureImpl future,
      BindRequest bindRequest)
  {
    int messageID = nextMsgID.getAndIncrement();
    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized (writeLock)
      {
        pendingRequests.put(messageID, future);
        try
        {
          if (bindRequest instanceof SimpleBindRequest)
          {
            LDAPEncoder.encodeBindRequest(asn1Writer, messageID, 3,
                (SimpleBindRequest) bindRequest);
          }
          else if (bindRequest instanceof SASLBindRequest<?>)
          {
            LDAPEncoder.encodeBindRequest(asn1Writer, messageID, 3,
                (SASLBindRequest<?>) bindRequest);
          }
          else
          {
            pendingRequests.remove(messageID);
            future.handleResult(Responses.newBindResult(
                ResultCode.PROTOCOL_ERROR).setDiagnosticMessage(
                "Auth type not supported"));
            return;
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
  public ResultFuture add(String dn, String... ldifAttributes)
      throws IllegalArgumentException, IllegalStateException,
      NullPointerException
  {
    return add(Requests.newAddRequest(dn, ldifAttributes), null);
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
  public BindResultFuture bind(String name, String password)
      throws IllegalStateException, NullPointerException
  {
    return bind(Requests.newSimpleBindRequest(name, password), null);
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
  public ResultFuture delete(String dn) throws IllegalStateException,
      NullPointerException
  {
    return delete(Requests.newDeleteRequest(dn), null);
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
  public ResultFuture modifyDN(String dn, String newRDN)
      throws IllegalStateException, NullPointerException
  {
    return modifyDN(Requests.newModifyDNRequest(dn, newRDN), null);
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
  public void removeConnectionEventListener(
      ConnectionEventListener listener) throws NullPointerException
  {
    Validator.ensureNotNull(listener);

    synchronized (writeLock)
    {
      listeners.remove(listener);
    }
  }
}
