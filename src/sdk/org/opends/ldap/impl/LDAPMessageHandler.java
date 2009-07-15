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
 * Created by IntelliJ IDEA. User: digitalperk Date: May 25, 2009 Time:
 * 9:39:07 AM To change this template use File | Settings | File
 * Templates.
 */
public interface LDAPMessageHandler
{
  public void handleException(Throwable throwable);



  public void handleMessage(int messageID, byte messageTag,
      ByteString messageBytes) throws UnsupportedMessageException;



  public void handleRequest(int messageID, AbandonRequest abandonRequest)
      throws UnexpectedRequestException;



  public void handleRequest(int messageID, AddRequest addRequest)
      throws UnexpectedRequestException;



  public void handleRequest(int messageID, CompareRequest compareRequest)
      throws UnexpectedRequestException;



  public void handleRequest(int messageID, DeleteRequest deleteRequest)
      throws UnexpectedRequestException;



  public void handleRequest(int messageID,
      GenericExtendedRequest extendedRequest)
      throws UnexpectedRequestException;



  public void handleRequest(int messageID, int version,
      GenericBindRequest bindRequest) throws UnexpectedRequestException;



  public void handleRequest(int messageID, int version,
      SASLBindRequest bindRequest) throws UnexpectedRequestException;



  public void handleRequest(int messageID, int version,
      SimpleBindRequest bindRequest) throws UnexpectedRequestException;



  public void handleRequest(int messageID,
      ModifyDNRequest modifyDNRequest)
      throws UnexpectedRequestException;



  public void handleRequest(int messageID, ModifyRequest modifyRequest)
      throws UnexpectedRequestException;



  public void handleRequest(int messageID, SearchRequest searchRequest)
      throws UnexpectedRequestException;



  public void handleRequest(int messageID, UnbindRequest unbindRequest)
      throws UnexpectedRequestException;



  public void handleResponse(int messageID, AddResult addResponse)
      throws UnexpectedResponseException;



  public void handleResponse(int messageID, BindResult bindResponse)
      throws UnexpectedResponseException;



  public void handleResponse(int messageID,
      CompareResult compareResponse) throws UnexpectedResponseException;



  public void handleResponse(int messageID, DeleteResult deleteResponse)
      throws UnexpectedResponseException;



  public void handleResponse(int messageID,
      GenericExtendedResult extendedResponse)
      throws UnexpectedResponseException;



  public void handleResponse(int messageID,
      GenericIntermediateResponse intermediateResponse)
      throws UnexpectedResponseException;



  public void handleResponse(int messageID,
      ModifyDNResult modifyDNResponse)
      throws UnexpectedResponseException;



  public void handleResponse(int messageID, ModifyResult modifyResponse)
      throws UnexpectedResponseException;



  public void handleResponse(int messageID,
      SearchResult searchResultDone) throws UnexpectedResponseException;



  public void handleResponse(int messageID,
      SearchResultEntry searchResultEntry)
      throws UnexpectedResponseException;



  public void handleResponse(int messageID,
      SearchResultReference searchResultReference)
      throws UnexpectedResponseException;
}
