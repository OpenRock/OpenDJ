package org.opends.ldap.impl;



import org.opends.ldap.GenericExtendedRequest;
import org.opends.ldap.GenericExtendedResponse;
import org.opends.ldap.GenericIntermediateResponse;
import org.opends.ldap.GenericMessage;
import org.opends.ldap.requests.AbandonRequest;
import org.opends.ldap.requests.AddRequest;
import org.opends.ldap.requests.CompareRequest;
import org.opends.ldap.requests.DeleteRequest;
import org.opends.ldap.requests.GenericBindRequest;
import org.opends.ldap.requests.ModifyDNRequest;
import org.opends.ldap.requests.ModifyRequest;
import org.opends.ldap.requests.SASLBindRequest;
import org.opends.ldap.requests.SearchRequest;
import org.opends.ldap.requests.SimpleBindRequest;
import org.opends.ldap.requests.UnbindRequest;
import org.opends.ldap.responses.AddResponse;
import org.opends.ldap.responses.BindResponse;
import org.opends.ldap.responses.CompareResponse;
import org.opends.ldap.responses.DeleteResponse;
import org.opends.ldap.responses.ModifyDNResponse;
import org.opends.ldap.responses.ModifyResponse;
import org.opends.ldap.responses.SearchResultDone;
import org.opends.ldap.responses.SearchResultEntry;
import org.opends.ldap.responses.SearchResultReference;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 16, 2009 Time:
 * 11:57:26 AM To change this template use File | Settings | File
 * Templates.
 */
public abstract class AbstractLDAPMessageHandler implements
    LDAPMessageHandler
{
  public void handleMessage(int messageID, GenericMessage unknownMessage)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, unknownMessage);
  }



  public void handleRequest(int messageID, AbandonRequest abandonRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, abandonRequest);
  }



  public void handleRequest(int messageID, AddRequest addRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, addRequest);
  }



  public void handleRequest(int messageID, CompareRequest compareRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, compareRequest);
  }



  public void handleRequest(int messageID, DeleteRequest deleteRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, deleteRequest);
  }



  public void handleRequest(int messageID,
      GenericExtendedRequest extendedRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, extendedRequest);
  }



  public void handleRequest(int messageID, int version,
      GenericBindRequest bindRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, bindRequest);
  }



  public void handleRequest(int messageID, int version,
      SASLBindRequest bindRequest) throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, bindRequest);
  }



  public void handleRequest(int messageID, int version,
      SimpleBindRequest bindRequest) throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, bindRequest);
  }



  public void handleRequest(int messageID,
      ModifyDNRequest modifyDNRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, modifyDNRequest);
  }



  public void handleRequest(int messageID, ModifyRequest modifyRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, modifyRequest);
  }



  public void handleRequest(int messageID, SearchRequest searchRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, searchRequest);
  }



  public void handleRequest(int messageID, UnbindRequest unbindRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, unbindRequest);
  }



  public void handleResponse(int messageID, AddResponse addResponse)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, addResponse);
  }



  public void handleResponse(int messageID, BindResponse bindResponse)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, bindResponse);
  }



  public void handleResponse(int messageID,
      CompareResponse compareResponse)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, compareResponse);
  }



  public void handleResponse(int messageID,
      DeleteResponse deleteResponse) throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, deleteResponse);
  }



  public void handleResponse(int messageID,
      GenericExtendedResponse extendedResponse)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, extendedResponse);
  }



  public void handleResponse(int messageID,
      GenericIntermediateResponse intermediateResponse)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID,
        intermediateResponse);
  }



  public void handleResponse(int messageID,
      ModifyDNResponse modifyDNResponse)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, modifyDNResponse);
  }



  public void handleResponse(int messageID,
      ModifyResponse modifyResponse) throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, modifyResponse);
  }



  public void handleResponse(int messageID,
      SearchResultDone searchResultDone)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, searchResultDone);
  }



  public void handleResponse(int messageID,
      SearchResultEntry searchResultEntry)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, searchResultEntry);
  }



  public void handleResponse(int messageID,
      SearchResultReference searchResultReference)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID,
        searchResultReference);
  }
}
