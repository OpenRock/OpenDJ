package org.opends.ldap.impl;



import org.opends.ldap.requests.AbandonRequest;
import org.opends.ldap.requests.AddRequest;
import org.opends.ldap.requests.CompareRequest;
import org.opends.ldap.requests.DeleteRequest;
import org.opends.ldap.requests.GenericBindRequest;
import org.opends.ldap.requests.GenericExtendedRequest;
import org.opends.ldap.requests.ModifyDNRequest;
import org.opends.ldap.requests.ModifyRequest;
import org.opends.ldap.requests.SearchRequest;
import org.opends.ldap.requests.SimpleBindRequest;
import org.opends.ldap.requests.UnbindRequest;
import org.opends.ldap.responses.AddResult;
import org.opends.ldap.responses.BindResult;
import org.opends.ldap.responses.CompareResult;
import org.opends.ldap.responses.DeleteResult;
import org.opends.ldap.responses.GenericExtendedResult;
import org.opends.ldap.responses.GenericIntermediateResponse;
import org.opends.ldap.responses.ModifyDNResult;
import org.opends.ldap.responses.ModifyResult;
import org.opends.ldap.responses.SearchResult;
import org.opends.ldap.responses.SearchResultEntry;
import org.opends.ldap.responses.SearchResultReference;
import org.opends.ldap.sasl.SASLBindRequest;
import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 16, 2009 Time:
 * 11:57:26 AM To change this template use File | Settings | File
 * Templates.
 */
public abstract class AbstractLDAPMessageHandler implements
    LDAPMessageHandler
{
  public void handleMessage(int messageID, byte messageTag,
      ByteString messageBytes) throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, messageTag,
        messageBytes);
  }



  public void handleRequest(int messageID, AbandonRequest abandonRequest)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, abandonRequest);
  }



  public void handleRequest(int messageID, AddRequest addRequest)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, addRequest);
  }



  public void handleRequest(int messageID, CompareRequest compareRequest)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, compareRequest);
  }



  public void handleRequest(int messageID, DeleteRequest deleteRequest)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, deleteRequest);
  }



  public void handleRequest(int messageID,
      GenericExtendedRequest extendedRequest)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, extendedRequest);
  }



  public void handleRequest(int messageID, int version,
      GenericBindRequest bindRequest) throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, bindRequest);
  }



  public void handleRequest(int messageID, int version,
      SASLBindRequest bindRequest) throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, bindRequest);
  }



  public void handleRequest(int messageID, int version,
      SimpleBindRequest bindRequest) throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, bindRequest);
  }



  public void handleRequest(int messageID,
      ModifyDNRequest modifyDNRequest)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, modifyDNRequest);
  }



  public void handleRequest(int messageID, ModifyRequest modifyRequest)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, modifyRequest);
  }



  public void handleRequest(int messageID, SearchRequest searchRequest)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, searchRequest);
  }



  public void handleRequest(int messageID, UnbindRequest unbindRequest)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, unbindRequest);
  }



  public void handleResponse(int messageID, AddResult addResponse)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, addResponse);
  }



  public void handleResponse(int messageID, BindResult bindResponse)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, bindResponse);
  }



  public void handleResponse(int messageID,
      CompareResult compareResponse) throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, compareResponse);
  }



  public void handleResponse(int messageID, DeleteResult deleteResponse)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, deleteResponse);
  }



  public void handleResponse(int messageID,
      GenericExtendedResult extendedResponse)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, extendedResponse);
  }



  public void handleResponse(int messageID,
      GenericIntermediateResponse intermediateResponse)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID,
        intermediateResponse);
  }



  public void handleResponse(int messageID,
      ModifyDNResult modifyDNResponse)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, modifyDNResponse);
  }



  public void handleResponse(int messageID, ModifyResult modifyResponse)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, modifyResponse);
  }



  public void handleResponse(int messageID,
      SearchResult searchResultDone) throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, searchResultDone);
  }



  public void handleResponse(int messageID,
      SearchResultEntry searchResultEntry)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, searchResultEntry);
  }



  public void handleResponse(int messageID,
      SearchResultReference searchResultReference)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID,
        searchResultReference);
  }
}
