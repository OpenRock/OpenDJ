package org.opends.client.protocol.ldap;

import org.opends.common.protocols.ldap.LDAPEncoder;
import org.opends.common.protocols.ldap.AbstractLDAPMessageHandler;
import org.opends.common.protocols.asn1.ASN1StreamWriter;
import org.opends.common.protocols.sasl.SASLFilter;
import org.opends.common.api.request.*;
import org.opends.common.api.response.*;
import org.opends.common.api.extended.*;
import org.opends.common.api.ResultCode;
import org.opends.common.api.DecodeException;
import static org.opends.server.util.ServerConstants.OID_START_TLS_REQUEST;
import static org.opends.server.protocols.ldap.LDAPConstants.OID_NOTICE_OF_DISCONNECTION;
import org.opends.messages.Message;
import org.opends.client.api.ResponseHandler;
import org.opends.client.api.SearchResponseHandler;
import org.opends.client.api.ExtendedResponseHandler;
import org.opends.client.api.request.AbstractSASLBindRequest;
import org.opends.client.spi.Connection;
import org.opends.client.spi.futures.*;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.net.InetSocketAddress;

import com.sun.grizzly.ssl.*;
import com.sun.grizzly.streams.StreamWriter;
import com.sun.grizzly.filterchain.FilterChain;
import com.sun.grizzly.filterchain.Filter;
import com.sun.grizzly.filterchain.StreamTransformerFilter;

import javax.net.ssl.SSLEngine;
import javax.security.sasl.SaslException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 27, 2009 Time: 9:48:51
 * AM To change this template use File | Settings | File Templates.
 */
public class LDAPConnection extends AbstractLDAPMessageHandler 
    implements Connection
{
  private com.sun.grizzly.Connection connection;
  private InetSocketAddress serverAddress;

  private FilterChain customFilterChain;
  private LDAPConnectionFactory connFactory;
  private StreamWriter streamWriter;

  private final Object writeLock;
  private ConcurrentHashMap<Integer, AbstractResponseFuture> pendingRequests;
  private AtomicInteger nextMsgID;
  private ClosedConnectionException closedException;
  private volatile int pendingBindOrStartTLS;

  LDAPConnection(com.sun.grizzly.Connection connection,
                 InetSocketAddress serverAddress,
                 LDAPConnectionFactory connFactory)
      throws IOException
  {
    this.connection = connection;
    this.serverAddress = serverAddress;
    this.connFactory = connFactory;
    this.writeLock = new Object();
    this.pendingRequests =
        new ConcurrentHashMap<Integer, AbstractResponseFuture>();
    this.nextMsgID = new AtomicInteger(1);
    this.pendingBindOrStartTLS = -1;

    streamWriter = getFilterChainStreamWriter();

    if(isTLSEnabled())
    {
      try
      {
        performSSLHandshake();
      }
      catch(Exception e)
      {
        if(e instanceof ExecutionException)
        {
          throw new IOException("SSL Handshake failed:",
              e.getCause());
        }

        throw new IOException("SSL Handshake failed:", e);
      }
    }
  }

  private void performSSLHandshake()
      throws InterruptedException, ExecutionException, IOException
  {
    // We have a TLS layer already installed so handshake
    SSLStreamReader reader =
        new SSLStreamReader(connection.getStreamReader());
    SSLStreamWriter writer =
        new SSLStreamWriter(connection.getStreamWriter());
    Future<SSLEngine> future =
        connFactory.getSSLHandshaker().handshake(
            reader, writer, connFactory.getSSLEngineConfigurator());

    future.get();
  }

  public boolean isTLSEnabled()
  {
    FilterChain currentFilterChain = (FilterChain)connection.getProcessor();
    return currentFilterChain.get(2) instanceof SSLFilter;
  }

  private StreamWriter getFilterChainStreamWriter()
  {
    StreamWriter writer = connection.getStreamWriter();
    FilterChain currentFilterChain = (FilterChain)connection.getProcessor();
    for(Filter filter : currentFilterChain)
    {
      if(filter instanceof StreamTransformerFilter)
      {
        writer = ((StreamTransformerFilter)filter).getStreamWriter(writer);
      }
    }

    return writer;
  }

  public AddResponseFuture addRequest(AddRequest addRequest,
                                      ResponseHandler<AddResponse> responseHandler)
  {
    int messageID = nextMsgID.getAndIncrement();
    DefaultAddResponseFuture future =
        new DefaultAddResponseFuture(messageID, addRequest,
            responseHandler, this, connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        if(closedException != null)
        {
          future.failure(closedException);
          return future;
        }
        if(pendingBindOrStartTLS > 0)
        {
          future.setResult(
              new AddResponse(ResultCode.OPERATIONS_ERROR, "",
                  "Bind or Start TLS operation in progress"));
          return future;
        }
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, addRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
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

  public BindResponseFuture bindRequest(BindRequest bindRequest,
      ResponseHandler<BindResponse> responseHandler)
  {
    int messageID = nextMsgID.getAndIncrement();
    DefaultBindResponseFuture future =
        new DefaultBindResponseFuture(messageID, bindRequest,
            responseHandler, this, connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
       if(closedException != null)
        {
          future.failure(closedException);
          return future;
        }
        if(pendingBindOrStartTLS > 0)
        {
          future.setResult(
              new BindResponse(ResultCode.OPERATIONS_ERROR, "",
                  "Bind or Start TLS operation in progress"));
          return future;
        }
        if(!pendingRequests.isEmpty())
        {
          future.setResult(
              new BindResponse(ResultCode.OPERATIONS_ERROR, "",
            "There are other operations pending on this connection"));
          return future;
        }

        pendingRequests.put(messageID, future);
        pendingBindOrStartTLS = messageID;

        if(bindRequest instanceof AbstractSASLBindRequest)
        {
          try
          {
          ((AbstractSASLBindRequest)bindRequest).initialize(
              serverAddress.getHostName());
          }
          catch(SaslException e)
          {
            future.failure(e);
            return future;
          }
        }

        sendBind(future, bindRequest);
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }

  private void sendBind(DefaultBindResponseFuture future,
                            BindRequest bindRequest)
  {
    int messageID = nextMsgID.getAndIncrement();
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        pendingRequests.put(messageID, future);
        try
        {
          if(bindRequest instanceof SimpleBindRequest)
          {
            LDAPEncoder.encodeRequest(asn1Writer, messageID, 3,
                (SimpleBindRequest)bindRequest);
          }
          else if(bindRequest instanceof SASLBindRequest)
          {
            LDAPEncoder.encodeRequest(asn1Writer, messageID, 3,
                (SASLBindRequest)bindRequest);
          }
          else
          {
            pendingRequests.remove(messageID);
            future.setResult(
                new BindResponse(ResultCode.PROTOCOL_ERROR, "",
                    "Auth type not supported"));
            return;
          }
          asn1Writer.flush();
        }
        catch(IOException e)
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

  public CompareResponseFuture compareRequest(
      CompareRequest compareRequest,
      ResponseHandler<CompareResponse> responseHandler)
  {
    int messageID = nextMsgID.getAndIncrement();
    DefaultCompareResponseFuture future =
        new DefaultCompareResponseFuture(messageID, compareRequest,
            responseHandler, this, connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        if(closedException != null)
        {
          future.failure(closedException);
          return future;
        }
        if(pendingBindOrStartTLS > 0)
        {
          future.setResult(
              new CompareResponse(ResultCode.OPERATIONS_ERROR, "",
                  "Bind or Start TLS operation in progress"));
          return future;
        }
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, compareRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
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

  public DeleteResponseFuture deleteRequest(
      DeleteRequest deleteRequest,
      ResponseHandler<DeleteResponse> responseHandler)
  {
    int messageID = nextMsgID.getAndIncrement();
    DefaultDeleteResponseFuture future =
        new DefaultDeleteResponseFuture(messageID, deleteRequest,
            responseHandler, this, connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        if(closedException != null)
        {
          future.failure(closedException);
          return future;
        }
        if(pendingBindOrStartTLS > 0)
        {
          future.setResult(
              new DeleteResponse(ResultCode.OPERATIONS_ERROR, "",
                  "Bind or Start TLS operation in progress"));
          return future;
        }
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, deleteRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
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

  public ModifyDNResponseFuture modifyDNRequest(
      ModifyDNRequest modifyDNRequest,
      ResponseHandler<ModifyDNResponse> responseHandler)
  {
    int messageID = nextMsgID.getAndIncrement();
    DefaultModifyDNResponseFuture future =
        new DefaultModifyDNResponseFuture(messageID, modifyDNRequest,
            responseHandler, this, connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        if(closedException != null)
        {
          future.failure(closedException);
          return future;
        }
        if(pendingBindOrStartTLS > 0)
        {
          future.setResult(
              new ModifyDNResponse(ResultCode.OPERATIONS_ERROR, "",
                  "Bind or Start TLS operation in progress"));
          return future;
        }
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, modifyDNRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
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

  public ModifyResponseFuture modifyRequest(
      ModifyRequest modifyRequest,
      ResponseHandler<ModifyResponse> responseHandler)
  {
    int messageID = nextMsgID.getAndIncrement();
    DefaultModifyResponseFuture future =
        new DefaultModifyResponseFuture(messageID, modifyRequest,
            responseHandler, this, connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        if(closedException != null)
        {
          future.failure(closedException);
          return future;
        }
        if(pendingBindOrStartTLS > 0)
        {
          future.setResult(
              new ModifyResponse(ResultCode.OPERATIONS_ERROR, "",
                  "Bind or Start TLS operation in progress"));
          return future;
        }
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, modifyRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
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

  public SearchResponseFuture searchRequest(
      SearchRequest searchRequest,
      SearchResponseHandler responseHandler)
  {
    int messageID = nextMsgID.getAndIncrement();
    DefaultSearchResponseFuture future =
        new DefaultSearchResponseFuture(messageID, searchRequest,
            responseHandler, this, connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        if(closedException != null)
        {
          future.failure(closedException);
          return future;
        }
        if(pendingBindOrStartTLS > 0)
        {
          future.setResult(
              new SearchResultDone(ResultCode.OPERATIONS_ERROR, "",
                  "Bind or Start TLS operation in progress"));
          return future;
        }
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, searchRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
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

  public ExtendedResponseFuture extendedRequest(
      ExtendedRequest extendedRequest,
      ExtendedResponseHandler responseHandler)
  {
    int messageID = nextMsgID.getAndIncrement();
    DefaultExtendedResponseFuture future =
        new DefaultExtendedResponseFuture(messageID, extendedRequest,
            responseHandler, this, connFactory.getHandlerInvokers());
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        if(closedException != null)
        {
          future.failure(closedException);
          return future;
        }
        if(pendingBindOrStartTLS > 0)
        {
          try
          {
          future.setResult(
              extendedRequest.getExtendedOperation().decodeResponse(
                  ResultCode.OPERATIONS_ERROR, "",
                  "Bind or Start TLS operation in progress",
                  null, null));
          }
          catch(DecodeException de)
          {
            future.setResult(new GenericExtendedResponse(
                ResultCode.OPERATIONS_ERROR, "",
                "Bind or Start TLS operation in progress"));
          }
          return future;
        }
        if(extendedRequest.getRequestName().equals(OID_START_TLS_REQUEST))
        {
          if(!pendingRequests.isEmpty())
          {
            try
            {
              future.setResult(
                  extendedRequest.getExtendedOperation().decodeResponse(
                      ResultCode.OPERATIONS_ERROR, "",
                      "There are pending operations on this connection",
                      null, null));
            }
            catch(DecodeException de)
            {
              future.setResult(new GenericExtendedResponse(
                  ResultCode.OPERATIONS_ERROR, "",
                  "There are pending operations on this connection"));
            }
            return future;
          }
          if(isTLSEnabled())
          {
            try
            {
              future.setResult(
                  extendedRequest.getExtendedOperation().decodeResponse(
                      ResultCode.OPERATIONS_ERROR, "",
                      "This connection is already TLS enabled",
                      null, null));
            }
            catch(DecodeException de)
            {
              future.setResult(new GenericExtendedResponse(
                  ResultCode.OPERATIONS_ERROR, "",
                  "This connection is already TLS enabled"));
            }
          }
          pendingBindOrStartTLS = messageID;
        }
        pendingRequests.put(messageID, future);

        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, extendedRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
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

  public void abandonRequest(AbandonRequest abandonRequest)
  {
    AbstractResponseFuture pendingRequest =
        pendingRequests.remove(abandonRequest.getMessageID());
    if(pendingRequest != null)
    {
      pendingRequest.cancel(false);
      int messageID = nextMsgID.getAndIncrement();
      ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

      try
      {
        synchronized(writeLock)
        {
          if(closedException != null)
          {
            return;
          }
          if(pendingBindOrStartTLS > 0)
          {
            // This is not allowed. We will just ignore this
            // abandon request.
          }
          try
          {
            LDAPEncoder.encodeRequest(asn1Writer, messageID,
                abandonRequest);
            asn1Writer.flush();
          }
          catch(IOException e)
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

  private void unbindRequest() throws IOException
  {
    if(closedException != null)
    {
      // Connection already closed. No point in sending unbind.
      return;
    }

    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);
    UnbindRequest abandonRequest = new UnbindRequest();

    try
    {
      synchronized(writeLock)
      {
        LDAPEncoder.encodeRequest(asn1Writer, nextMsgID.getAndIncrement(),
                                  abandonRequest);
        asn1Writer.flush();
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }
  }

  public void close()
  {
    close(null);
  }

  private void close(Throwable cause)
  {
    synchronized(writeLock)
    {
      if(closedException != null)
      {
        return;
      }

      try
      {
        unbindRequest();

        closedException = new ClosedConnectionException(
            Message.raw("Connection closed"), cause);
        failAllPendingRequests(closedException);

        streamWriter.close();
        connection.close();
        pendingRequests.clear();
      }
      catch(Exception e)
      {
        // Underlying channel prob blown up. Just ignore.
      }
    }
  }

  private void failAllPendingRequests(Throwable cause)
  {
    for(AbstractResponseFuture future : pendingRequests.values())
    {
      future.failure(cause);
      try
      {
        abandonRequest(new AbandonRequest(future.getMessageID()));
      }
      catch(Exception e)
      {
        // Underlying channel prob blown up. Just ignore.
      }
    }
  }

  public void handleResponse(int messageID, AddResponse addResponse)
  {
    AbstractResponseFuture pendingRequest =
        pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest instanceof DefaultAddResponseFuture)
      {
        ((DefaultAddResponseFuture)pendingRequest).setResult(
            addResponse);
      }
      else
      {
        handleIncorrectResponse(pendingRequest);
      }
    }
  }

  public void handleResponse(int messageID, BindResponse bindResponse)
  {
    AbstractResponseFuture pendingRequest =
        pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest instanceof DefaultBindResponseFuture)
      {
        DefaultBindResponseFuture bindFuture =
            ((DefaultBindResponseFuture)pendingRequest);
        BindRequest request = bindFuture.getRequest();

        if(request instanceof AbstractSASLBindRequest)
        {
          AbstractSASLBindRequest saslBind =
              (AbstractSASLBindRequest)request;

          try
          {
            saslBind.evaluateCredentials(
                bindResponse.getServerSASLCreds());
          }
          catch(SaslException se)
          {
            pendingBindOrStartTLS = -1;
            pendingRequest.failure(se);
            return;
          }

          if(bindResponse.getResultCode() ==
              ResultCode.SASL_BIND_IN_PROGRESS)
          {
            // The server is expecting a multi stage bind response.
            sendBind(bindFuture, saslBind);
            return;
          }

          if(bindResponse.getResultCode() == ResultCode.SUCCESS &&
              saslBind.isSecure())
          {
            // The connection needs to be secured by the SASL mechanism.
            if(customFilterChain == null)
            {
              customFilterChain =
                  connFactory.getDefaultFilterChainFactory().create();
              connection.setProcessor(customFilterChain);
            }

            // Install the SSLFilter in the custom filter chain
            Filter oldFilter = customFilterChain.remove(2);
            customFilterChain.add(SASLFilter.getInstance(saslBind,
                connection));
            if(!(oldFilter instanceof SSLFilter))
            {
              customFilterChain.add(oldFilter);
            }

            streamWriter = getFilterChainStreamWriter();
          }
        }

        bindFuture.setResult(bindResponse);
        pendingBindOrStartTLS = -1;
      }
      else
      {
        handleIncorrectResponse(pendingRequest);
      }
    }
  }

  public void handleResponse(int messageID, CompareResponse compareResponse)
  {
    AbstractResponseFuture pendingRequest =
        pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest instanceof DefaultCompareResponseFuture)
      {
        ((DefaultCompareResponseFuture)pendingRequest).setResult(
            compareResponse);
      }
      else
      {
        handleIncorrectResponse(pendingRequest);
      }
    }
  }

  public void handleResponse(int messageID, DeleteResponse deleteResponse)
  {
    AbstractResponseFuture pendingRequest =
        pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest instanceof DefaultDeleteResponseFuture)
      {
        ((DefaultDeleteResponseFuture)pendingRequest).setResult(
            deleteResponse);
      }
      else
      {
        handleIncorrectResponse(pendingRequest);
      }
    }
  }

  public void handleResponse(int messageID,
                             GenericExtendedResponse extendedResponse)
  {
    if(messageID == -1)
    {
      if(extendedResponse.getResponseName() != null &&
          extendedResponse.getResponseName().equals(
              OID_NOTICE_OF_DISCONNECTION))
      {
          close(new ClosedConnectionException(Message.raw("Connection closed by server")));
          return;
      }
    }

    AbstractResponseFuture pendingRequest =
        pendingRequests.remove(messageID);

    if(pendingRequest instanceof DefaultExtendedResponseFuture)
    {
      DefaultExtendedResponseFuture extendedFuture =
          ((DefaultExtendedResponseFuture)pendingRequest);
      ExtendedRequest request = extendedFuture.getRequest();

      try
      {
        ExtendedResponse decodedResponse =
            request.getExtendedOperation().decodeResponse(
                extendedResponse.getResultCode(),
                extendedResponse.getMatchedDN(),
                extendedResponse.getDiagnosticMessage(),
                extendedResponse.getResponseName(),
                extendedResponse.getResponseValue());

        if(decodedResponse instanceof
            StartTLSExtendedOperation.Response)
        {
          if(extendedResponse.getResultCode() == ResultCode.SUCCESS)
          {
            if(customFilterChain == null)
            {
              customFilterChain =
                  connFactory.getDefaultFilterChainFactory().create();
              connection.setProcessor(customFilterChain);
            }

            // Install the SSLFilter in the custom filter chain
            Filter oldFilter = customFilterChain.remove(2);
            customFilterChain.add(connFactory.getSSLFilter());
            if(!(oldFilter instanceof SSLFilter))
            {
              customFilterChain.add(oldFilter);
            }

            try
            {
              performSSLHandshake();
              streamWriter = getFilterChainStreamWriter();
            }
            catch(Exception ioe)
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
        extendedFuture.setResult(decodedResponse);
      }
      catch(DecodeException de)
      {
        pendingRequest.failure(de);
      }
    }
    else
    {
      handleIncorrectResponse(pendingRequest);
    }
  }

  public void handleResponse(int messageID, ModifyDNResponse modifyDNResponse)
  {
    AbstractResponseFuture pendingRequest =
        pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest instanceof DefaultModifyDNResponseFuture)
      {
        ((DefaultModifyDNResponseFuture)pendingRequest).setResult(
            modifyDNResponse);
      }
      else
      {
        handleIncorrectResponse(pendingRequest);
      }
    }
  }

  public void handleResponse(int messageID, ModifyResponse modifyResponse)
  {
    AbstractResponseFuture pendingRequest =
        pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest instanceof DefaultModifyResponseFuture)
      {
        ((DefaultModifyResponseFuture)pendingRequest).setResult(
            modifyResponse);
      }
      else
      {
        handleIncorrectResponse(pendingRequest);
      }
    }
  }

  public void handleResponse(int messageID, SearchResultEntry searchResultEntry)
  {
    AbstractResponseFuture pendingRequest =
        pendingRequests.get(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest instanceof DefaultSearchResponseFuture)
      {
        ((DefaultSearchResponseFuture)pendingRequest).setResult(
            searchResultEntry);
      }
      else
      {
        handleIncorrectResponse(pendingRequest);
      }
    }
  }

  public void handleResponse(int messageID, SearchResultReference searchResultReference)
  {
    AbstractResponseFuture pendingRequest =
        pendingRequests.get(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest instanceof DefaultSearchResponseFuture)
      {
        ((DefaultSearchResponseFuture)pendingRequest).setResult(
            searchResultReference);
      }
      else
      {
        handleIncorrectResponse(pendingRequest);
      }
    }
  }

  public void handleResponse(int messageID, SearchResultDone searchResultDone)
  {
    AbstractResponseFuture pendingRequest =
        pendingRequests.get(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest instanceof DefaultSearchResponseFuture)
      {
        ((DefaultSearchResponseFuture)pendingRequest).setResult(
            searchResultDone);
      }
      else
      {
        handleIncorrectResponse(pendingRequest);
      }
    }
  }

  public void handleResponse(int messageID,
                             GenericIntermediateResponse intermediateResponse)
  {
    AbstractResponseFuture pendingRequest =
        pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest instanceof DefaultExtendedResponseFuture)
      {
      DefaultExtendedResponseFuture extendedFuture =
          ((DefaultExtendedResponseFuture)pendingRequest);
      ExtendedRequest request = extendedFuture.getRequest();

        try
        {
          IntermediateResponse decodedResponse =
              request.getExtendedOperation().decodeIntermediateResponse(
                  intermediateResponse.getResponseName(),
                  intermediateResponse.getResponseValue());
          extendedFuture.setResult(decodedResponse);
        }
        catch(DecodeException de)
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

  public void handleException(Throwable throwable)
  {
      close(throwable);
  }

  private void handleIncorrectResponse(
      AbstractResponseFuture pendingRequest)
  {
    IOException ioe = new IOException("Incorrect response!");
      pendingRequest.failure(ioe);
      close(ioe);
  }
}
