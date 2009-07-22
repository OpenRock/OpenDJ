package org.opends.ldap.impl;



import static org.opends.server.protocols.ldap.LDAPConstants.*;
import static org.opends.server.util.ServerConstants.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLEngine;
import javax.security.sasl.SaslException;

import org.opends.ldap.Connection;
import org.opends.ldap.DecodeException;
import org.opends.ldap.ResponseHandler;
import org.opends.ldap.ResultCode;
import org.opends.ldap.SearchResponseHandler;
import org.opends.ldap.requests.AbandonRequest;
import org.opends.ldap.requests.AddRequest;
import org.opends.ldap.requests.BindRequest;
import org.opends.ldap.requests.CompareRequest;
import org.opends.ldap.requests.DeleteRequest;
import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.requests.ModifyDNRequest;
import org.opends.ldap.requests.ModifyRequest;
import org.opends.ldap.requests.SearchRequest;
import org.opends.ldap.requests.SimpleBindRequest;
import org.opends.ldap.requests.UnbindRequest;
import org.opends.ldap.responses.BindResult;
import org.opends.ldap.responses.BindResultFuture;
import org.opends.ldap.responses.CompareResult;
import org.opends.ldap.responses.CompareResultFuture;
import org.opends.ldap.responses.ExtendedResultFuture;
import org.opends.ldap.responses.GenericExtendedResult;
import org.opends.ldap.responses.GenericIntermediateResponse;
import org.opends.ldap.responses.Result;
import org.opends.ldap.responses.ResultFuture;
import org.opends.ldap.responses.SearchResult;
import org.opends.ldap.responses.SearchResultEntry;
import org.opends.ldap.responses.SearchResultFuture;
import org.opends.ldap.responses.SearchResultReference;
import org.opends.ldap.sasl.AbstractSASLBindRequest;
import org.opends.ldap.sasl.SASLBindRequest;

import com.sun.grizzly.filterchain.Filter;
import com.sun.grizzly.filterchain.FilterChain;
import com.sun.grizzly.filterchain.StreamTransformerFilter;
import com.sun.grizzly.ssl.SSLFilter;
import com.sun.grizzly.ssl.SSLStreamReader;
import com.sun.grizzly.ssl.SSLStreamWriter;
import com.sun.grizzly.streams.StreamWriter;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 27, 2009 Time:
 * 9:48:51 AM To change this template use File | Settings | File
 * Templates.
 */
public class LDAPConnection extends AbstractLDAPMessageHandler
    implements Connection
{

  private final com.sun.grizzly.Connection connection;
  private Result connectionClosedResult;
  private final LDAPConnectionFactory connFactory;

  private FilterChain customFilterChain;
  private final AtomicInteger nextMsgID = new AtomicInteger(1);
  private volatile int pendingBindOrStartTLS = -1;

  private final ConcurrentHashMap<Integer, AbstractResultFutureImpl> pendingRequests =
      new ConcurrentHashMap<Integer, AbstractResultFutureImpl>();
  private final InetSocketAddress serverAddress;
  private StreamWriter streamWriter;
  private final Object writeLock = new Object();



  LDAPConnection(com.sun.grizzly.Connection connection,
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
    AbstractResultFutureImpl pendingRequest =
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
          if (connectionClosedResult != null)
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
            close(errorResult);
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
  public ResultFuture add(AddRequest request)
  {
    return add(request, null);
  }



  /**
   * {@inheritDoc}
   */
  public ResultFuture add(AddRequest request,
      ResponseHandler<Result> handler)
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
        if (connectionClosedResult != null)
        {
          future.handleErrorResult(connectionClosedResult);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          future.handleResult(new Result(ResultCode.OPERATIONS_ERROR,
              "", "Bind or Start TLS operation in progress"));
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
          close(errorResult);
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
  public BindResultFuture bind(BindRequest request)
  {
    return bind(request, null);
  }



  /**
   * {@inheritDoc}
   */
  public BindResultFuture bind(BindRequest request,
      ResponseHandler<BindResult> handler)
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
        if (connectionClosedResult != null)
        {
          future.handleErrorResult(connectionClosedResult);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          future.handleResult(new BindResult(
              ResultCode.OPERATIONS_ERROR, "",
              "Bind or Start TLS operation in progress"));
          return future;
        }
        if (!pendingRequests.isEmpty())
        {
          future.handleResult(new BindResult(
              ResultCode.OPERATIONS_ERROR, "",
              "There are other operations pending on this connection"));
          return future;
        }

        pendingRequests.put(messageID, future);
        pendingBindOrStartTLS = messageID;

        if (request instanceof AbstractSASLBindRequest)
        {
          try
          {
            ((AbstractSASLBindRequest) request)
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
    // FIXME: I18N need to internationalize this message.
    close(new Result(ResultCode.CLIENT_SIDE_USER_CANCELLED,
        "Connection closed by client", (Throwable) null));
  }



  /**
   * {@inheritDoc}
   */
  public CompareResultFuture compare(CompareRequest request)
  {
    return compare(request, null);
  }



  /**
   * {@inheritDoc}
   */
  public CompareResultFuture compare(CompareRequest request,
      ResponseHandler<CompareResult> handler)
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
        if (connectionClosedResult != null)
        {
          future.handleErrorResult(connectionClosedResult);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          future.handleResult(new CompareResult(
              ResultCode.OPERATIONS_ERROR, "",
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
          close(errorResult);
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
  public ResultFuture delete(DeleteRequest request)
  {
    return delete(request, null);
  }



  /**
   * {@inheritDoc}
   */
  public ResultFuture delete(DeleteRequest request,
      ResponseHandler<Result> handler)
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
        if (connectionClosedResult != null)
        {
          future.handleErrorResult(connectionClosedResult);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          future.handleResult(new Result(ResultCode.OPERATIONS_ERROR,
              "", "Bind or Start TLS operation in progress"));
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
          close(errorResult);
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
      ExtendedRequest<?, R> request)
  {
    return extendedRequest(request, null);
  }



  /**
   * {@inheritDoc}
   */
  public <R extends Result> ExtendedResultFuture<R> extendedRequest(
      ExtendedRequest<?, R> request, ResponseHandler<R> handler)
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
        if (connectionClosedResult != null)
        {
          future.handleErrorResult(connectionClosedResult);
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
          close(errorResult);
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
    AbstractResultFutureImpl pendingRequest =
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
    AbstractResultFutureImpl pendingRequest =
        pendingRequests.remove(messageID);
    if (pendingRequest != null)
    {
      if (pendingRequest instanceof BindResultFutureImpl)
      {
        BindResultFutureImpl future =
            ((BindResultFutureImpl) pendingRequest);
        BindRequest request = future.getRequest();

        if (request instanceof AbstractSASLBindRequest)
        {
          AbstractSASLBindRequest saslBind =
              (AbstractSASLBindRequest) request;

          try
          {
            saslBind.evaluateCredentials(result.getServerSASLCreds());
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
    AbstractResultFutureImpl pendingRequest =
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
    AbstractResultFutureImpl pendingRequest =
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
    close(errorResult);
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public void handleExtendedResult(int messageID,
      GenericExtendedResult result)
  {
    if (messageID == -1)
    {
      if ((result.getResponseName() != null)
          && result.getResponseName().equals(
              OID_NOTICE_OF_DISCONNECTION))
      {

        Result errorResult =
            new Result(result.getResultCode(), result
                .getDiagnosticMessage(), (Throwable) null);
        close(errorResult);
        return;
      }
    }

    AbstractResultFutureImpl pendingRequest =
        pendingRequests.remove(messageID);

    if (pendingRequest instanceof ExtendedResultFutureImpl)
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
            new Result(ResultCode.CLIENT_SIDE_DECODING_ERROR, de
                .getLocalizedMessage(), de);
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
    AbstractResultFutureImpl pendingRequest =
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
    AbstractResultFutureImpl pendingRequest =
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
    AbstractResultFutureImpl pendingRequest =
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
    AbstractResultFutureImpl pendingRequest =
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
    AbstractResultFutureImpl pendingRequest =
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
    AbstractResultFutureImpl pendingRequest =
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



  public boolean isTLSEnabled()
  {
    FilterChain currentFilterChain =
        (FilterChain) connection.getProcessor();
    return currentFilterChain.get(2) instanceof SSLFilter;
  }



  /**
   * {@inheritDoc}
   */
  public ResultFuture modify(ModifyRequest request)
  {
    return modify(request, null);
  }



  /**
   * {@inheritDoc}
   */
  public ResultFuture modify(ModifyRequest request,
      ResponseHandler<Result> handler)
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
        if (connectionClosedResult != null)
        {
          future.handleErrorResult(connectionClosedResult);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          future.handleResult(new Result(ResultCode.OPERATIONS_ERROR,
              "", "Bind or Start TLS operation in progress"));
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
          close(errorResult);
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
  public ResultFuture modifyDN(ModifyDNRequest request)
  {
    return modifyDN(request, null);
  }



  /**
   * {@inheritDoc}
   */
  public ResultFuture modifyDN(ModifyDNRequest request,
      ResponseHandler<Result> handler)
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
        if (connectionClosedResult != null)
        {
          future.handleErrorResult(connectionClosedResult);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          future.handleResult(new Result(ResultCode.OPERATIONS_ERROR,
              "", "Bind or Start TLS operation in progress"));
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
          close(errorResult);
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
  public SearchResultFuture search(SearchRequest request)
  {
    return search(request, null);
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultFuture search(SearchRequest request,
      SearchResponseHandler handler)
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
        if (connectionClosedResult != null)
        {
          future.handleErrorResult(connectionClosedResult);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          future.handleResult(new SearchResult(
              ResultCode.OPERATIONS_ERROR, "",
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
          close(errorResult);
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
          new Result(ResultCode.CLIENT_SIDE_LOCAL_ERROR,
              "An error occurred during SASL authentication", e);
    }
    catch (IOException e)
    {
      // FIXME: I18N need to have a better error message.
      // FIXME: what sort of IOExceptions can be thrown?
      // FIXME: Is this the best result code?
      errorResult =
          new Result(ResultCode.CLIENT_SIDE_LOCAL_ERROR,
              "An error occurred whilst attempting to send a request",
              e);
    }
    catch (Throwable e)
    {
      // FIXME: I18N need to have a better error message.
      // FIXME: Is this the best result code?
      errorResult =
          new Result(ResultCode.CLIENT_SIDE_LOCAL_ERROR,
              "An unknown error occurred", e);
    }

    return errorResult;
  }



  private void close(Result reason)
  {
    synchronized (writeLock)
    {
      if (connectionClosedResult != null)
      {
        return;
      }

      // First abort all outstanding requests.
      for (AbstractResultFutureImpl future : pendingRequests.values())
      {
        if (pendingBindOrStartTLS <= 0)
        {
          ASN1StreamWriter asn1Writer =
              connFactory.getASN1Writer(streamWriter);
          int messageID = nextMsgID.getAndIncrement();
          AbandonRequest abandon =
              new AbandonRequest(future.getMessageID());
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
        unbindRequest();
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

      // Mark the connection as closed.
      connectionClosedResult = reason;
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
          close(errorResult);
          return;
        }
      }
      pendingBindOrStartTLS = -1;
    }

    future.handleResult(decodedResponse);
  }



  private void handleIncorrectResponse(
      AbstractResultFutureImpl pendingRequest)
  {
    // FIXME: I18N need to have a better error message.
    Result errorResult =
        new Result(ResultCode.CLIENT_SIDE_DECODING_ERROR,
            "LDAP response message did not match request",
            (Throwable) null);

    pendingRequest.handleErrorResult(errorResult);
    close(errorResult);
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
          else if (bindRequest instanceof SASLBindRequest)
          {
            LDAPEncoder.encodeBindRequest(asn1Writer, messageID, 3,
                (SASLBindRequest) bindRequest);
          }
          else
          {
            pendingRequests.remove(messageID);
            future.handleResult(new BindResult(
                ResultCode.PROTOCOL_ERROR, "",
                "Auth type not supported"));
            return;
          }
          asn1Writer.flush();
        }
        catch (IOException e)
        {
          pendingRequests.remove(messageID);

          Result errorResult = adaptException(e);
          close(errorResult);
          future.handleErrorResult(errorResult);
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }
  }



  private void unbindRequest() throws IOException
  {
    if (connectionClosedResult != null)
    {
      // Connection already closed. No point in sending unbind.
      return;
    }

    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);
    UnbindRequest abandonRequest = new UnbindRequest();

    try
    {
      synchronized (writeLock)
      {
        LDAPEncoder.encodeUnbindRequest(asn1Writer, nextMsgID
            .getAndIncrement(), abandonRequest);
        asn1Writer.flush();
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }
  }
}
