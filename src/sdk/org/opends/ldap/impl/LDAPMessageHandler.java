package org.opends.ldap.impl;



import org.opends.ldap.requests.AbandonRequestImpl;
import org.opends.ldap.requests.AddRequestImpl;
import org.opends.ldap.requests.CompareRequestImpl;
import org.opends.ldap.requests.DeleteRequestImpl;
import org.opends.ldap.requests.GenericBindRequestImpl;
import org.opends.ldap.requests.GenericExtendedRequestImpl;
import org.opends.ldap.requests.ModifyDNRequestImpl;
import org.opends.ldap.requests.ModifyRequestImpl;
import org.opends.ldap.requests.SearchRequestImpl;
import org.opends.ldap.requests.SimpleBindRequestImpl;
import org.opends.ldap.requests.UnbindRequestImpl;
import org.opends.ldap.responses.BindResult;
import org.opends.ldap.responses.CompareResult;
import org.opends.ldap.responses.GenericExtendedResult;
import org.opends.ldap.responses.GenericIntermediateResponse;
import org.opends.ldap.responses.Result;
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
  void handleException(Throwable throwable);



  void handleUnrecognizedMessage(int messageID, byte messageTag,
      ByteString messageBytes) throws UnsupportedMessageException;



  void handleAbandonRequest(int messageID, AbandonRequestImpl request)
      throws UnexpectedRequestException;



  void handleAddRequest(int messageID, AddRequestImpl request)
      throws UnexpectedRequestException;



  void handleCompareRequest(int messageID, CompareRequestImpl request)
      throws UnexpectedRequestException;



  void handleDeleteRequest(int messageID, DeleteRequestImpl request)
      throws UnexpectedRequestException;



  void handleExtendedRequest(int messageID,
      GenericExtendedRequestImpl request) throws UnexpectedRequestException;



  void handleBindRequest(int messageID, int version,
      GenericBindRequestImpl request) throws UnexpectedRequestException;



  void handleBindRequest(int messageID, int version,
      SASLBindRequest request) throws UnexpectedRequestException;



  void handleBindRequest(int messageID, int version,
      SimpleBindRequestImpl request) throws UnexpectedRequestException;



  void handleModifyDNRequest(int messageID, ModifyDNRequestImpl request)
      throws UnexpectedRequestException;



  void handleModifyRequest(int messageID, ModifyRequestImpl request)
      throws UnexpectedRequestException;



  void handleSearchRequest(int messageID, SearchRequestImpl request)
      throws UnexpectedRequestException;



  void handleUnbindRequest(int messageID, UnbindRequestImpl request)
      throws UnexpectedRequestException;



  void handleAddResult(int messageID, Result result)
      throws UnexpectedResponseException;



  void handleBindResult(int messageID, BindResult result)
      throws UnexpectedResponseException;



  void handleCompareResult(int messageID, CompareResult result)
      throws UnexpectedResponseException;



  void handleDeleteResult(int messageID, Result result)
      throws UnexpectedResponseException;



  void handleExtendedResult(int messageID, GenericExtendedResult result)
      throws UnexpectedResponseException;



  void handleIntermediateResponse(int messageID,
      GenericIntermediateResponse response)
      throws UnexpectedResponseException;



  void handleModifyDNResult(int messageID, Result result)
      throws UnexpectedResponseException;



  void handleModifyResult(int messageID, Result result)
      throws UnexpectedResponseException;



  void handleSearchResult(int messageID, SearchResult result)
      throws UnexpectedResponseException;



  void handleSearchResultEntry(int messageID, SearchResultEntry entry)
      throws UnexpectedResponseException;



  void handleSearchResultReference(int messageID,
      SearchResultReference reference)
      throws UnexpectedResponseException;
}
