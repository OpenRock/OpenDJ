package org.opends.ldap.impl;



import org.opends.ldap.GenericMessage;
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
import org.opends.ldap.responses.AddResponse;
import org.opends.ldap.responses.BindResponse;
import org.opends.ldap.responses.CompareResponse;
import org.opends.ldap.responses.DeleteResponse;
import org.opends.ldap.responses.GenericExtendedResponse;
import org.opends.ldap.responses.GenericIntermediateResponse;
import org.opends.ldap.responses.ModifyDNResponse;
import org.opends.ldap.responses.ModifyResponse;
import org.opends.ldap.responses.SearchResultDone;
import org.opends.ldap.responses.SearchResultEntry;
import org.opends.ldap.responses.SearchResultReference;
import org.opends.ldap.sasl.SASLBindRequest;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 25, 2009 Time:
 * 9:39:07 AM To change this template use File | Settings | File
 * Templates.
 */
public interface LDAPMessageHandler
{
  public void handleException(Throwable throwable);



  public void handleMessage(int messageID, GenericMessage unknownMessage)
      throws UnsupportedMessageException;



  public void handleRequest(int messageID, AbandonRequest abandonRequest)
      throws UnsupportedMessageException;



  public void handleRequest(int messageID, AddRequest addRequest)
      throws UnsupportedMessageException;



  public void handleRequest(int messageID, CompareRequest compareRequest)
      throws UnsupportedMessageException;



  public void handleRequest(int messageID, DeleteRequest deleteRequest)
      throws UnsupportedMessageException;



  public void handleRequest(int messageID,
      GenericExtendedRequest extendedRequest)
      throws UnsupportedMessageException;



  public void handleRequest(int messageID, int version,
      GenericBindRequest bindRequest)
      throws UnsupportedMessageException;



  public void handleRequest(int messageID, int version,
      SASLBindRequest bindRequest) throws UnsupportedMessageException;



  public void handleRequest(int messageID, int version,
      SimpleBindRequest bindRequest) throws UnsupportedMessageException;



  public void handleRequest(int messageID,
      ModifyDNRequest modifyDNRequest)
      throws UnsupportedMessageException;



  public void handleRequest(int messageID, ModifyRequest modifyRequest)
      throws UnsupportedMessageException;



  public void handleRequest(int messageID, SearchRequest searchRequest)
      throws UnsupportedMessageException;



  public void handleRequest(int messageID, UnbindRequest unbindRequest)
      throws UnsupportedMessageException;



  public void handleResponse(int messageID, AddResponse addResponse)
      throws UnsupportedMessageException;



  public void handleResponse(int messageID, BindResponse bindResponse)
      throws UnsupportedMessageException;



  public void handleResponse(int messageID,
      CompareResponse compareResponse)
      throws UnsupportedMessageException;



  public void handleResponse(int messageID,
      DeleteResponse deleteResponse) throws UnsupportedMessageException;



  public void handleResponse(int messageID,
      GenericExtendedResponse extendedResponse)
      throws UnsupportedMessageException;



  public void handleResponse(int messageID,
      GenericIntermediateResponse intermediateResponse)
      throws UnsupportedMessageException;



  public void handleResponse(int messageID,
      ModifyDNResponse modifyDNResponse)
      throws UnsupportedMessageException;



  public void handleResponse(int messageID,
      ModifyResponse modifyResponse) throws UnsupportedMessageException;



  public void handleResponse(int messageID,
      SearchResultDone searchResultDone)
      throws UnsupportedMessageException;



  public void handleResponse(int messageID,
      SearchResultEntry searchResultEntry)
      throws UnsupportedMessageException;



  public void handleResponse(int messageID,
      SearchResultReference searchResultReference)
      throws UnsupportedMessageException;
}
