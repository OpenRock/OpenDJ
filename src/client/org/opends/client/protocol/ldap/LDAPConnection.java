package org.opends.client.protocol.ldap;

import org.opends.common.protocols.ldap.LDAPMessageHandler;
import org.opends.common.protocols.ldap.LDAPEncoder;
import org.opends.common.protocols.ldap.asn1.ASN1StreamWriter;
import org.opends.common.api.raw.request.*;
import org.opends.common.api.raw.response.*;
import org.opends.server.protocols.asn1.ASN1Exception;
import org.opends.messages.Message;
import org.opends.client.api.ResponseHandler;
import org.opends.client.api.SearchResponseHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.grizzly.Connection;
import com.sun.grizzly.streams.StreamWriter;
import com.sun.grizzly.filterchain.FilterChain;

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
  {
    this.connection = connection;
    this.connFactory = connFactory;
    this.streamWriter = connection.getStreamWriter(); //TODO: hack for now
    this.writeLock = new Object();
    this.msgHandler = new Handler();
    pendingRequests = new ConcurrentHashMap<Integer, ResponseFuture>();
    nextMsgID = new AtomicInteger(1);
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

    public void handleRequest(int messageID, int version, RawSimpleBindRequest bindRequest)
    {
      handleIncorrectResponse(messageID);
    }

    public void handleRequest(int messageID, int version, RawSASLBindRequest bindRequest)
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
      //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleResponse(int messageID, RawExtendedResponse extendedResponse)
    {
      //To change body of implemented methods use File | Settings | File Templates.
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
