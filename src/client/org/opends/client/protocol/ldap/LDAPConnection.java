package org.opends.client.protocol.ldap;

import org.opends.common.protocols.ldap.LDAPEncoder;
import org.opends.common.protocols.ldap.AbstractLDAPMessageHandler;
import org.opends.common.protocols.asn1.ASN1StreamWriter;
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

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.grizzly.Connection;
import com.sun.grizzly.ssl.*;
import com.sun.grizzly.streams.StreamWriter;
import com.sun.grizzly.filterchain.FilterChain;
import com.sun.grizzly.filterchain.Filter;
import com.sun.grizzly.filterchain.StreamTransformerFilter;

import javax.net.ssl.SSLEngine;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 27, 2009 Time: 9:48:51
 * AM To change this template use File | Settings | File Templates.
 */
public class LDAPConnection extends AbstractLDAPMessageHandler 
    implements RawConnection
{
  private Connection connection;
  private FilterChain customFilterChain;
  private LDAPConnectionFactory connFactory;
  private StreamWriter streamWriter;

  private final Object writeLock;
  private ConcurrentHashMap<Integer, ResultResponseFuture> pendingRequests;
  private AtomicInteger nextMsgID;
  private InvalidConnectionException closedException;

  LDAPConnection(Connection connection, LDAPConnectionFactory connFactory)
      throws IOException
  {
    this.connection = connection;
    this.connFactory = connFactory;
    this.writeLock = new Object();
    pendingRequests = new ConcurrentHashMap<Integer, ResultResponseFuture>();
    nextMsgID = new AtomicInteger(1);

    streamWriter = getFilterChainStreamWriter();

    if(isTLSEnabled())
    {
      performSSLHandshake();
    }
  }

  private void performSSLHandshake() throws IOException
  {
    // We have a TLS layer already installed so handshake
    SSLStreamReader reader =
        new SSLStreamReader(connection.getStreamReader());
    SSLStreamWriter writer =
        new SSLStreamWriter(connection.getStreamWriter());
    Future<SSLEngine> future =
        connFactory.getSSLHandshaker().handshake(
            reader, writer, connFactory.getSSLEngineConfigurator());
    try
    {
      future.get();
    }
    catch(InterruptedException ie)
    {
      throw new IOException("Interrupted!");
    }
    catch(ExecutionException ee)
    {
      if(ee.getCause() instanceof IOException)
      {
        throw (IOException)ee.getCause();
      }

      throw new IOException("Got an error:" + ee.getCause());
    }
    catch(CancellationException ce)
    {
      throw new IOException("Cancelled!");
    }
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

  public ResponseFuture<AddResponse> addRequest(AddRequest addRequest,
                                                   ResponseHandler<AddResponse> responseHandler)
      throws InvalidConnectionException
  {
    int messageID = nextMsgID.getAndIncrement();
    ResultResponseFuture<AddResponse> future =
        new ResultResponseFuture<AddResponse>(messageID, addRequest,
                                                 responseHandler, this);
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        checkClosed();
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, addRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
        {
          pendingRequests.remove(messageID);
          future.failure(e);
          try
          {
            close(e);
          }
          catch(IOException ioe) {}
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }

  public ResponseFuture<BindResponse> bindRequest(
      SimpleBindRequest bindRequest,
      ResponseHandler<BindResponse> responseHandler)
      throws InvalidConnectionException
  {
    int messageID = nextMsgID.getAndIncrement();
    ResultResponseFuture<BindResponse> future =
        new ResultResponseFuture<BindResponse>(messageID, bindRequest,
                                                  responseHandler, this);
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        checkClosed();
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, 3, bindRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
        {
          pendingRequests.remove(messageID);
          future.failure(e);
          try
          {
            close(e);
          }
          catch(IOException ioe) {}
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }

  public ResponseFuture<BindResponse> bindRequest(
      SASLBindRequest bindRequest,
      ResponseHandler<BindResponse> responseHandler)
      throws InvalidConnectionException
  {
    int messageID = nextMsgID.getAndIncrement();
    ResultResponseFuture<BindResponse> future =
        new ResultResponseFuture<BindResponse>(messageID, bindRequest,
                                                  responseHandler, this);
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        checkClosed();
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, 3, bindRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
        {
          pendingRequests.remove(messageID);
          future.failure(e);
          try
          {
            close(e);
          }
          catch(IOException ioe) {}
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }

  public ResponseFuture<CompareResponse> compareRequest(
      CompareRequest compareRequest,
      ResponseHandler<CompareResponse> responseHandler)
      throws InvalidConnectionException
  {
    int messageID = nextMsgID.getAndIncrement();
    ResultResponseFuture<CompareResponse> future =
        new ResultResponseFuture<CompareResponse>(messageID, compareRequest,
                                                     responseHandler, this);
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        checkClosed();
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, compareRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
        {
          pendingRequests.remove(messageID);
          future.failure(e);
          try
          {
            close(e);
          }
          catch(IOException ioe) {}
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }

  public ResponseFuture<DeleteResponse> deleteRequest(
      DeleteRequest deleteRequest,
      ResponseHandler<DeleteResponse> responseHandler)
      throws InvalidConnectionException
  {
    int messageID = nextMsgID.getAndIncrement();
    ResultResponseFuture<DeleteResponse> future =
        new ResultResponseFuture<DeleteResponse>(messageID, deleteRequest,
                                                    responseHandler, this);
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        checkClosed();
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, deleteRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
        {
          pendingRequests.remove(messageID);
          future.failure(e);
          try
          {
            close(e);
          }
          catch(IOException ioe) {}
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }

  public ResponseFuture<ModifyDNResponse> modifyDNRequest(
      ModifyDNRequest modifyDNRequest,
      ResponseHandler<ModifyDNResponse> responseHandler)
      throws InvalidConnectionException
  {
    int messageID = nextMsgID.getAndIncrement();
    ResultResponseFuture<ModifyDNResponse> future =
        new ResultResponseFuture<ModifyDNResponse>(messageID,
                                                      modifyDNRequest,
                                                      responseHandler, this);
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        checkClosed();
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, modifyDNRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
        {
          pendingRequests.remove(messageID);
          future.failure(e);
          try
          {
            close(e);
          }
          catch(IOException ioe) {}
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }

  public ResponseFuture<ModifyResponse> modifyRequest(
      ModifyRequest modifyRequest,
      ResponseHandler<ModifyResponse> responseHandler)
      throws InvalidConnectionException
  {
    int messageID = nextMsgID.getAndIncrement();
    ResultResponseFuture<ModifyResponse> future =
        new ResultResponseFuture<ModifyResponse>(messageID, modifyRequest,
                                                    responseHandler, this);
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        checkClosed();
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, modifyRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
        {
          pendingRequests.remove(messageID);
          future.failure(e);
          try
          {
            close(e);
          }
          catch(IOException ioe) {}
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
      SearchRequest searchRequest, SearchResponseHandler responseHandler)
      throws InvalidConnectionException
  {
    int messageID = nextMsgID.getAndIncrement();
    SearchResponseFuture future =
        new SearchResponseFuture(messageID, searchRequest,
                                 responseHandler, this);
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        checkClosed();
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, searchRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
        {
          pendingRequests.remove(messageID);
          future.failure(e);
          try
          {
            close(e);
          }
          catch(IOException ioe) {}
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }

  public <T extends ExtendedOperation>
  ExtendedResponseFuture<T> extendedRequest(
      ExtendedRequest<T> extendedRequest,
      ExtendedResponseHandler<T> responseHandler)
      throws InvalidConnectionException
  {
    int messageID = nextMsgID.getAndIncrement();
    ExtendedResponseFuture<T> future =
        new ExtendedResponseFuture<T>(
            messageID, extendedRequest, responseHandler, this);
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      if(extendedRequest.getRequestName().equals(OID_START_TLS_REQUEST))
      {
        // Also make sure TLS is not already installed
        // Also need to check to make sure there are no pending requests.
        // Someone need to hold the write lock so other requests don't go through...
      }
      synchronized(writeLock)
      {
        checkClosed();
        pendingRequests.put(messageID, future);
        try
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, extendedRequest);
          asn1Writer.flush();
        }
        catch(IOException e)
        {
          pendingRequests.remove(messageID);
          future.failure(e);
          try
          {
            close(e);
          }
          catch(IOException ioe) {}
        }
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }

  void abandonRequest(int abandonID) throws IOException
  {
    if(pendingRequests.remove(abandonID) != null)
    {
      int messageID = nextMsgID.getAndIncrement();
      ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);
      AbandonRequest abandonRequest = new AbandonRequest(abandonID);

      try
      {
        synchronized(writeLock)
        {
          LDAPEncoder.encodeRequest(asn1Writer, messageID, abandonRequest);
          asn1Writer.flush();
        }
      }
      finally
      {
        connFactory.releaseASN1Writer(asn1Writer);
      }
    }
  }

  void unbindRequest() throws IOException
  {
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

  public void close() throws IOException
  {
    close(null);
  }

  private void close(Throwable cause) throws IOException
  {
    synchronized(writeLock)
    {
      if(closedException != null)
      {
        return;
      }

      InvalidConnectionException exception = new InvalidConnectionException(
          Message.raw("Connection closed"), cause);
      closedException = exception;
      failAllPendingRequests(exception);

      try
      {
        unbindRequest();
      }
      catch(Exception e)
      {
        // Underlying channel prob blown up. Just ignore.
      }

      streamWriter.close();
      connection.close();
      pendingRequests.clear();
    }
  }

  private void failAllPendingRequests(Throwable cause) throws IOException
  {
    for(ResultResponseFuture future : pendingRequests.values())
    {
      future.failure(cause);
      try
      {
        abandonRequest(future.getMessageID());
      }
      catch(Exception e)
      {
        // Underlying channel prob blown up. Just ignore.
      }
    }
  }

  public LDAPConnectionFactory getConnFactory()
  {
    return connFactory;
  }

  public void handleResponse(int messageID, AddResponse addResponse)
  {
    ResultResponseFuture<AddResponse> pendingRequest =
        pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest.getOrginalRequest() instanceof AddRequest)
      {
        pendingRequest.setResult(addResponse);
      }
      else
      {
        handleIncorrectResponse(messageID);
        // TODO: Should we close the connection?
      }
    }
  }

  public void handleResponse(int messageID, BindResponse bindResponse)
  {
    ResultResponseFuture<BindResponse> pendingRequest =
        pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest.getOrginalRequest() instanceof BindRequest)
      {
        pendingRequest.setResult(bindResponse);
      }
      else
      {
        handleIncorrectResponse(messageID);
        // TODO: Should we close the connection?
      }
    }
  }

  public void handleResponse(int messageID, CompareResponse compareResponse)
  {
    ResultResponseFuture<CompareResponse> pendingRequest =
        pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest.getOrginalRequest() instanceof CompareRequest)
      {
        pendingRequest.setResult(compareResponse);
      }
      else
      {
        handleIncorrectResponse(messageID);
        // TODO: Should we close the connection?
      }
    }
  }

  public void handleResponse(int messageID, DeleteResponse deleteResponse)
  {
    ResultResponseFuture<DeleteResponse> pendingRequest =
        pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest.getOrginalRequest() instanceof DeleteRequest)
      {
        pendingRequest.setResult(deleteResponse);
      }
      else
      {
        handleIncorrectResponse(messageID);
        // TODO: Should we close the connection?
      }
    }
  }

  public void handleResponse(int messageID, GenericExtendedResponse extendedResponse)
  {
    if(messageID == -1)
    {
      if(extendedResponse.getResponseName() != null &&
          extendedResponse.getResponseName().equals(
              OID_NOTICE_OF_DISCONNECTION))
      {
        try
        {
          close(new InvalidConnectionException(Message.raw("Connection closed by server")));
          return;
        }
        catch(IOException ioe) {}
      }
    }

    ResultResponseFuture<ExtendedResponse> pendingRequest =
        pendingRequests.remove(messageID);

    if(pendingRequest instanceof ExtendedResponseFuture)
      {
        ExtendedRequest extendedRequest = (
            ExtendedRequest)pendingRequest.getOrginalRequest();

        try
        {
          ExtendedResponse decodedResponse =
              extendedRequest.getExtendedOperation().decodeResponse(
                  extendedResponse.getResultCode(),
                  extendedResponse.getMatchedDN(),
                  extendedResponse.getDiagnosticMessage(),
                  extendedResponse.getResponseName(),
                  extendedResponse.getResponseValue());

          if(decodedResponse instanceof
              StartTLSExtendedOperation.Response &&
              extendedResponse.getResultCode() == ResultCode.SUCCESS)
          {
            assert(!isTLSEnabled());
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

              // Get the updated streamwriter
              // TODO: We should make sure nothing else is occuring while
              // this is going on
              streamWriter = getFilterChainStreamWriter();
            }
            catch(IOException ioe)
            {
              // Remove the SSLFilter we just tried to add.
              customFilterChain.remove(1);
              //TODO: We should disconnect.
              pendingRequest.failure(ioe);
              return;
            }
          }
          pendingRequest.setResult(decodedResponse);
        }
        catch(DecodeException de)
        {
          pendingRequest.failure(de);
        }
      }
    else
    {
      handleIncorrectResponse(messageID);
        // TODO: Should we close the connection?
      }
  }

  public void handleResponse(int messageID, ModifyDNResponse modifyDNResponse)
  {
    ResultResponseFuture<ModifyDNResponse> pendingRequest =
        pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest.getOrginalRequest() instanceof ModifyDNRequest)
      {
        pendingRequest.setResult(modifyDNResponse);
      }
      else
      {
        handleIncorrectResponse(messageID);
        // TODO: Should we close the connection?
      }
    }
  }

  public void handleResponse(int messageID, ModifyResponse modifyResponse)
  {
    ResultResponseFuture<ModifyResponse> pendingRequest =
        pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest.getOrginalRequest() instanceof ModifyRequest)
      {
        pendingRequest.setResult(modifyResponse);
      }
      else
      {
        handleIncorrectResponse(messageID);
        // TODO: Should we close the connection?
      }
    }
  }

  public void handleResponse(int messageID, SearchResultEntry searchResultEntry)
  {
    ResultResponseFuture pendingRequest =
        pendingRequests.get(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest instanceof SearchResponseFuture)
      {
        ((SearchResponseFuture)pendingRequest).setResult(searchResultEntry);
      }
      else
      {
        handleIncorrectResponse(messageID);
        // TODO: Should we close the connection?
      }
    }
  }

  public void handleResponse(int messageID, SearchResultReference searchResultReference)
  {
    ResultResponseFuture pendingRequest =
        pendingRequests.get(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest instanceof SearchResponseFuture)
      {
        ((SearchResponseFuture)pendingRequest).setResult(searchResultReference);
      }
      else
      {
        handleIncorrectResponse(messageID);
        // TODO: Should we close the connection?
      }
    }
  }

  public void handleResponse(int messageID, SearchResultDone searchResultDone)
  {
    ResultResponseFuture pendingRequest =
        pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest instanceof SearchResponseFuture)
      {
        ((SearchResponseFuture)pendingRequest).setResult(searchResultDone);
      }
      else
      {
        handleIncorrectResponse(messageID);
        // TODO: Should we close the connection?
      }
    }
  }

  public void handleResponse(int messageID,
                             GenericIntermediateResponse intermediateResponse)
  {
    ResultResponseFuture pendingRequest =
        pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      if(pendingRequest instanceof ExtendedResponseFuture)
      {
        ExtendedRequest extendedRequest =
            (ExtendedRequest)pendingRequest.getOrginalRequest();

        try
        {
          IntermediateResponse decodedResponse =
              extendedRequest.getExtendedOperation().decodeIntermediateResponse(
                  intermediateResponse.getResponseName(),
                  intermediateResponse.getResponseValue());
          ((ExtendedResponseFuture)pendingRequest).setResult(decodedResponse);
        }
        catch(DecodeException de)
        {
          pendingRequest.failure(de);
        }
      }
      else
      {
        handleIncorrectResponse(messageID);
        // TODO: Should we close the connection?
      }
    }
  }

  public void handleException(Throwable throwable)
  {
    try
    {
      close(throwable);
    }
    catch(IOException ioe){}
  }

  private void handleIncorrectResponse(int messageID)
  {
    ResultResponseFuture pendingRequest = pendingRequests.remove(messageID);
    if(pendingRequest != null)
    {
      pendingRequest.failure(new IOException("Incorrect response!"));
      // TODO: Should we close the connection?
    }
  }

  private void checkClosed() throws InvalidConnectionException
  {
    if(closedException != null)
    {
      throw closedException;
    }
  }
}
