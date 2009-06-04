package org.opends.client.protocol.ldap;

import org.opends.common.protocols.ldap.LDAPMessageHandler;
import org.opends.common.protocols.ldap.LDAPEncoder;
import org.opends.common.protocols.ldap.asn1.ASN1StreamWriter;
import org.opends.common.api.raw.request.*;
import org.opends.common.api.raw.response.*;
import org.opends.server.protocols.asn1.ASN1Exception;
import static org.opends.server.util.ServerConstants.OID_START_TLS_REQUEST;
import org.opends.server.types.ResultCode;
import org.opends.messages.Message;
import org.opends.client.api.ResponseHandler;
import org.opends.client.api.SearchResponseHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;

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
public class LDAPConnection
{
  private Connection connection;
  private FilterChain customFilterChain;
  private LDAPConnectionFactory connFactory;
  private StreamWriter streamWriter;

  private final Object writeLock;
  private final LDAPMessageHandler msgHandler;
  private ConcurrentHashMap<Integer, ResponseFuture> pendingRequests;
  private AtomicInteger nextMsgID;

  LDAPConnection(Connection connection, LDAPConnectionFactory connFactory)
      throws IOException
  {
    this.connection = connection;
    this.connFactory = connFactory;
    this.writeLock = new Object();
    this.msgHandler = new Handler();
    pendingRequests = new ConcurrentHashMap<Integer, ResponseFuture>();
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
    return currentFilterChain.get(1) instanceof SSLFilter;
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

  public ResponseFuture<RawAddResponse> addRequest(RawAddRequest addRequest,
                                ResponseHandler<RawAddResponse> responseHandler)
      throws IOException
  {
    int messageID = nextMsgID.getAndIncrement();
    ResultResponseFuture<RawAddResponse> future =
        new ResultResponseFuture<RawAddResponse>(messageID, addRequest,
                                           responseHandler, this);
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        pendingRequests.put(messageID, future);
        LDAPEncoder.encodeRequest(asn1Writer, messageID, addRequest);
        asn1Writer.flush();
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }

  public ResponseFuture<RawBindResponse> bindRequest(
      RawSimpleBindRequest bindRequest,
      ResponseHandler<RawBindResponse> responseHandler)
      throws IOException
  {
    int messageID = nextMsgID.getAndIncrement();
    ResultResponseFuture<RawBindResponse> future =
        new ResultResponseFuture<RawBindResponse>(messageID, bindRequest,
                                            responseHandler, this);
    ASN1StreamWriter asn1Writer = connFactory.getASN1Writer(streamWriter);

    try
    {
      synchronized(writeLock)
      {
        pendingRequests.put(messageID, future);
        LDAPEncoder.encodeRequest(asn1Writer, messageID, 3, bindRequest);
        asn1Writer.flush();
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }

  public ResponseFuture<RawSearchResultDone> searchRequest(
      RawSearchRequest searchRequest, SearchResponseHandler responseHandler)
      throws IOException
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
        pendingRequests.put(messageID, future);
        LDAPEncoder.encodeRequest(asn1Writer, messageID, searchRequest);
        asn1Writer.flush();
      }
    }
    finally
    {
      connFactory.releaseASN1Writer(asn1Writer);
    }

    return future;
  }

  public ResponseFuture<RawExtendedResponse> extendedRequest(
      RawExtendedRequest extendedRequest,
      ResponseHandler<RawExtendedResponse> responseHandler) throws IOException
  {
    int messageID = nextMsgID.getAndIncrement();
    ResultResponseFuture<RawExtendedResponse> future =
        new ResultResponseFuture<RawExtendedResponse>(
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
        pendingRequests.put(messageID, future);
        LDAPEncoder.encodeRequest(asn1Writer, messageID, extendedRequest);
        asn1Writer.flush();
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
      RawAbandonRequest abandonRequest = new RawAbandonRequest(abandonID);

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
    RawUnbindRequest abandonRequest = new RawUnbindRequest();

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

  public void close(Message reason) throws IOException
  {
    synchronized(writeLock)
    {
      for(ResponseFuture future : pendingRequests.values())
      {
        future.failure(new IOException(reason.toString()));
        try
        {
          abandonRequest(future.getMessageID());
        }
        catch(Exception e)
        {
          // Underlying channel prob blown up. Just ignore.
        }
      }

      try
      {
        unbindRequest();
      }
      catch(Exception e)
      {
        // Underlying channel prob blown up. Just ignore.   
      }
      pendingRequests.clear();
      streamWriter.close();
      connection.close();
    }
  }

  LDAPMessageHandler getLDAPMessageHandler()
  {
    return msgHandler;
  }

  public LDAPConnectionFactory getConnFactory()
  {
    return connFactory;
  }

  private class Handler implements LDAPMessageHandler
  {
    public void handleRequest(int messageID, RawAbandonRequest abandonRequest)
    {
      handleIncorrectResponse(messageID);
    }

    public void handleRequest(int messageID, RawAddRequest addRequest)
    {
      handleIncorrectResponse(messageID);
    }

    public void handleRequest(int messageID, int version,
                              RawSimpleBindRequest bindRequest)
    {
      handleIncorrectResponse(messageID);
    }

    public void handleRequest(int messageID, int version,
                              RawSASLBindRequest bindRequest)
    {
      handleIncorrectResponse(messageID);
    }

    public void handleResponse(int messageID, RawAddResponse addResponse)
    {
      ResponseFuture<RawAddResponse> pendingRequest =
          pendingRequests.remove(messageID);
      if(pendingRequest != null)
      {
        if(pendingRequest.getOrginalRequest() instanceof RawAddRequest)
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

    public void handleResponse(int messageID, RawBindResponse bindResponse)
    {
      ResponseFuture<RawBindResponse> pendingRequest =
          pendingRequests.remove(messageID);
      if(pendingRequest != null)
      {
        if(pendingRequest.getOrginalRequest() instanceof RawBindRequest)
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

    public void handleRequest(int messageID, RawCompareRequest compareRequest)
    {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleResponse(int messageID, RawCompareResponse compareResponse)
    {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleRequest(int messageID, RawDeleteRequest deleteRequest)
    {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleResponse(int messageID, RawDeleteResponse deleteResponse)
    {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleRequest(int messageID, RawExtendedRequest extendedRequest)
    {

    }

    public void handleResponse(int messageID, RawExtendedResponse extendedResponse)
    {
      ResponseFuture<RawExtendedResponse> pendingRequest =
          pendingRequests.remove(messageID);
      if(pendingRequest != null)
      {
        if(pendingRequest.getOrginalRequest() instanceof RawExtendedRequest)
        {
          if(extendedResponse.getResultCode() == ResultCode.SUCCESS &&
             extendedResponse.getResponseName().equals(OID_START_TLS_REQUEST))
          {
            assert(!isTLSEnabled());
            if(customFilterChain == null)
            {
              customFilterChain =
                  connFactory.getDefaultFilterChainFactory().create();
              connection.setProcessor(customFilterChain);
            }

            // Install the SSLFilter in the custom filter chain
            Filter oldFilter = customFilterChain.remove(1);
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

          pendingRequest.setResult(extendedResponse);
        }
        else
        {
          handleIncorrectResponse(messageID);
          // TODO: Should we close the connection?
        }
      }
    }

    public void handleRequest(int messageID, RawModifyDNRequest modifyDNRequest)
    {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleResponse(int messageID, RawModifyDNResponse modifyDNResponse)
    {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleRequest(int messageID, RawModifyRequest modifyRequest)
    {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleResponse(int messageID, RawModifyResponse modifyResponse)
    {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleRequest(int messageID, RawSearchRequest searchRequest)
    {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleResponse(int messageID, RawSearchResultEntry searchResultEntry)
    {
      ResponseFuture pendingRequest =
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

    public void handleResponse(int messageID, RawSearchResultReference searchResultReference)
    {
      ResponseFuture pendingRequest =
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

    public void handleResponse(int messageID, RawSearchResultDone searchResultDone)
    {
      ResponseFuture pendingRequest =
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

    public void handleRequest(int messageID, RawUnbindRequest unbindRequest)
    {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleResponse(int messageID, RawIntermediateResponse intermediateResponse)
    {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleException(IOException ioException)
    {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleException(ASN1Exception asn1Exception)
    {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    private void handleIncorrectResponse(int messageID)
    {
      ResponseFuture pendingRequest = pendingRequests.remove(messageID);
      if(pendingRequest != null)
      {
        pendingRequest.failure(new IOException("Incorrect response!"));
        // TODO: Should we close the connection?
      }
    }
  }
}
