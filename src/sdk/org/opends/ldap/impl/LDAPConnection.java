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

import org.opends.ldap.ClosedConnectionException;
import org.opends.ldap.Connection;
import org.opends.ldap.DecodeException;
import org.opends.ldap.ExtendedResponseHandler;
import org.opends.ldap.ResponseHandler;
import org.opends.ldap.ResultCode;
import org.opends.ldap.SearchResponseHandler;
import org.opends.ldap.extensions.StartTLSExtendedOperation;
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
import org.opends.ldap.responses.ExtendedResult;
import org.opends.ldap.responses.ExtendedResultFuture;
import org.opends.ldap.responses.GenericExtendedResult;
import org.opends.ldap.responses.GenericIntermediateResponse;
import org.opends.ldap.responses.IntermediateResponse;
import org.opends.ldap.responses.Result;
import org.opends.ldap.responses.ResultFuture;
import org.opends.ldap.responses.SearchResult;
import org.opends.ldap.responses.SearchResultEntry;
import org.opends.ldap.responses.SearchResultFuture;
import org.opends.ldap.responses.SearchResultReference;
import org.opends.ldap.sasl.AbstractSASLBindRequest;
import org.opends.ldap.sasl.SASLBindRequest;
import org.opends.messages.Message;

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
  private ClosedConnectionException closedException;
  private final com.sun.grizzly.Connection connection;

  private final LDAPConnectionFactory connFactory;
  private FilterChain customFilterChain;
  private final AtomicInteger nextMsgID;
  private volatile int pendingBindOrStartTLS;
  private final ConcurrentHashMap<Integer, ResultFutureImpl> pendingRequests;

  private final InetSocketAddress serverAddress;

  private StreamWriter streamWriter;

  private final Object writeLock;



  LDAPConnection(com.sun.grizzly.Connection connection,
      InetSocketAddress serverAddress, LDAPConnectionFactory connFactory)
      throws IOException
  {
    this.connection = connection;
    this.serverAddress = serverAddress;
    this.connFactory = connFactory;
    this.writeLock = new Object();
    this.pendingRequests =
        new ConcurrentHashMap<Integer, ResultFutureImpl>();
    this.nextMsgID = new AtomicInteger(1);
    this.pendingBindOrStartTLS = -1;

    streamWriter = getFilterChainStreamWriter();

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
    ResultFutureImpl pendingRequest =
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
          if (closedException != null)
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
            close(e);
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
    ResultFutureImpl<AddRequest, Result> future =
        new ResultFutureImpl<AddRequest, Result>(messageID, request,
            handler, this, connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized (writeLock)
      {
        if (closedException != null)
        {
          future.failure(closedException);
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
          close(e);
          future.failure(e);
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
        if (closedException != null)
        {
          future.failure(closedException);
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
            future.failure(e);
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
    close(null);
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
        if (closedException != null)
        {
          future.failure(closedException);
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
          close(e);
          future.failure(e);
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
    ResultFutureImpl<DeleteRequest, Result> future =
        new ResultFutureImpl<DeleteRequest, Result>(messageID, request,
            handler, this, connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized (writeLock)
      {
        if (closedException != null)
        {
          future.failure(closedException);
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
          close(e);
          future.failure(e);
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
  public ExtendedResultFuture extendedRequest(ExtendedRequest request)
  {
    return extendedRequest(request, null);
  }



  /**
   * {@inheritDoc}
   */
  public ExtendedResultFuture extendedRequest(ExtendedRequest request,
      ExtendedResponseHandler handler)
  {
    int messageID = nextMsgID.getAndIncrement();
    ExtendedResultFutureImpl future =
        new ExtendedResultFutureImpl(messageID, request, handler, this,
            connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized (writeLock)
      {
        if (closedException != null)
        {
          future.failure(closedException);
          return future;
        }
        if (pendingBindOrStartTLS > 0)
        {
          try
          {
            future.handleResult(request.getExtendedOperation()
                .decodeResponse(ResultCode.OPERATIONS_ERROR, "",
                    "Bind or Start TLS operation in progress", null,
                    null));
          }
          catch (DecodeException de)
          {
            future.handleResult(new GenericExtendedResult(
                ResultCode.OPERATIONS_ERROR, "",
                "Bind or Start TLS operation in progress"));
          }
          return future;
        }
        if (request.getRequestName().equals(OID_START_TLS_REQUEST))
        {
          if (!pendingRequests.isEmpty())
          {
            try
            {
              future
                  .handleResult(request
                      .getExtendedOperation()
                      .decodeResponse(
                          ResultCode.OPERATIONS_ERROR,
                          "",
                          "There are pending operations on this connection",
                          null, null));
            }
            catch (DecodeException de)
            {
              future.handleResult(new GenericExtendedResult(
                  ResultCode.OPERATIONS_ERROR, "",
                  "There are pending operations on this connection"));
            }
            return future;
          }
          if (isTLSEnabled())
          {
            try
            {
              future.handleResult(request.getExtendedOperation()
                  .decodeResponse(ResultCode.OPERATIONS_ERROR, "",
                      "This connection is already TLS enabled", null,
                      null));
            }
            catch (DecodeException de)
            {
              future.handleResult(new GenericExtendedResult(
                  ResultCode.OPERATIONS_ERROR, "",
                  "This connection is already TLS enabled"));
            }
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
          close(e);
          future.failure(e);
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
  public void handleException(Throwable throwable)
  {
    close(throwable);
  }



  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public void handleAddResult(int messageID, Result result)
  {
    ResultFutureImpl pendingRequest = pendingRequests.remove(messageID);
    if (pendingRequest != null)
    {
      if (pendingRequest.getRequest() instanceof AddRequest)
      {
        pendingRequest.handleResult(result);
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
  public void handleBindResult(int messageID, BindResult result)
  {
    ResultFutureImpl pendingRequest = pendingRequests.remove(messageID);
    if (pendingRequest != null)
    {
      if (pendingRequest instanceof BindResultFutureImpl)
      {
        BindResultFutureImpl bindFuture =
            ((BindResultFutureImpl) pendingRequest);
        BindRequest request = bindFuture.getRequest();

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
            pendingRequest.failure(se);
            return;
          }

          if (result.getResultCode() == ResultCode.SASL_BIND_IN_PROGRESS)
          {
            // The server is expecting a multi stage bind response.
            sendBind(bindFuture, saslBind);
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

        bindFuture.handleResult(result);
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
    ResultFutureImpl pendingRequest = pendingRequests.remove(messageID);
    if (pendingRequest != null)
    {
      if (pendingRequest instanceof CompareResultFutureImpl)
      {
        ((CompareResultFutureImpl) pendingRequest).handleResult(result);
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
  @SuppressWarnings("unchecked")
  public void handleDeleteResult(int messageID, Result result)
  {
    ResultFutureImpl pendingRequest = pendingRequests.remove(messageID);
    if (pendingRequest != null)
    {
      if (pendingRequest.getRequest() instanceof DeleteRequest)
      {
        pendingRequest.handleResult(result);
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
  public void handleExtendedResult(int messageID,
      GenericExtendedResult result)
  {
    if (messageID == -1)
    {
      if ((result.getResponseName() != null)
          && result.getResponseName().equals(
              OID_NOTICE_OF_DISCONNECTION))
      {
        close(new ClosedConnectionException(Message
            .raw("Connection closed by server")));
        return;
      }
    }

    ResultFutureImpl pendingRequest = pendingRequests.remove(messageID);

    if (pendingRequest instanceof ExtendedResultFutureImpl)
    {
      ExtendedResultFutureImpl extendedFuture =
          ((ExtendedResultFutureImpl) pendingRequest);
      ExtendedRequest request = extendedFuture.getRequest();

      try
      {
        ExtendedResult decodedResponse =
            request.getExtendedOperation().decodeResponse(
                result.getResultCode(), result.getMatchedDN(),
                result.getDiagnosticMessage(),
                result.getResponseName(), result.getResponseValue());

        if (decodedResponse instanceof StartTLSExtendedOperation.Response)
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
              pendingRequest.failure(ioe);
              close(ioe);
              return;
            }
          }
          pendingBindOrStartTLS = -1;
        }
        extendedFuture.handleResult(decodedResponse);
      }
      catch (DecodeException de)
      {
        pendingRequest.failure(de);
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
    ResultFutureImpl pendingRequest = pendingRequests.remove(messageID);
    if (pendingRequest != null)
    {
      if (pendingRequest instanceof ExtendedResultFutureImpl)
      {
        ExtendedResultFutureImpl extendedFuture =
            ((ExtendedResultFutureImpl) pendingRequest);
        ExtendedRequest request = extendedFuture.getRequest();

        try
        {
          IntermediateResponse decodedResponse =
              request.getExtendedOperation()
                  .decodeIntermediateResponse(
                      response.getResponseName(),
                      response.getResponseValue());
          extendedFuture.handleIntermediateResponse(decodedResponse);
        }
        catch (DecodeException de)
        {
          pendingRequest.failure(de);
        }
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
  @SuppressWarnings("unchecked")
  public void handleModifyDNResult(int messageID, Result result)
  {
    ResultFutureImpl pendingRequest = pendingRequests.remove(messageID);
    if (pendingRequest != null)
    {
      if (pendingRequest.getRequest() instanceof ModifyDNRequest)
      {
        pendingRequest.handleResult(result);
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
  @SuppressWarnings("unchecked")
  public void handleModifyResult(int messageID, Result result)
  {
    ResultFutureImpl pendingRequest = pendingRequests.remove(messageID);
    if (pendingRequest != null)
    {
      if (pendingRequest.getRequest() instanceof ModifyRequest)
      {
        pendingRequest.handleResult(result);
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
  public void handleSearchResult(int messageID, SearchResult result)
  {
    ResultFutureImpl pendingRequest = pendingRequests.get(messageID);
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
    ResultFutureImpl pendingRequest = pendingRequests.get(messageID);
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
    ResultFutureImpl pendingRequest = pendingRequests.get(messageID);
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
    ResultFutureImpl<ModifyRequest, Result> future =
        new ResultFutureImpl<ModifyRequest, Result>(messageID, request,
            handler, this, connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized (writeLock)
      {
        if (closedException != null)
        {
          future.failure(closedException);
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
          close(e);
          future.failure(e);
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
    ResultFutureImpl<ModifyDNRequest, Result> future =
        new ResultFutureImpl<ModifyDNRequest, Result>(messageID,
            request, handler, this, connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized (writeLock)
      {
        if (closedException != null)
        {
          future.failure(closedException);
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
          close(e);
          future.failure(e);
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
        new SearchResultFutureImpl(messageID, request, handler, this,
            connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer =
        connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized (writeLock)
      {
        if (closedException != null)
        {
          future.failure(closedException);
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
          close(e);
          future.failure(e);
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }



  private void close(Throwable cause)
  {
    synchronized (writeLock)
    {
      if (closedException != null)
      {
        return;
      }

      try
      {
        unbindRequest();

        closedException =
            new ClosedConnectionException(Message
                .raw("Connection closed"), cause);
        failAllPendingRequests(closedException);

        streamWriter.close();
        connection.close();
        pendingRequests.clear();
      }
      catch (Exception e)
      {
        // Underlying channel prob blown up. Just ignore.
      }
    }
  }



  private void failAllPendingRequests(Throwable cause)
  {
    for (ResultFutureImpl future : pendingRequests.values())
    {
      future.failure(cause);
      try
      {
        abandon(new AbandonRequest(future.getMessageID()));
      }
      catch (Exception e)
      {
        // Underlying channel prob blown up. Just ignore.
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



  private void handleIncorrectResponse(ResultFutureImpl pendingRequest)
  {
    IOException ioe = new IOException("Incorrect response!");
    pendingRequest.failure(ioe);
    close(ioe);
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
          close(e);
          future.failure(e);
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
    if (closedException != null)
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
