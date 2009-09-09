package org.opends.sdk.ldap;



import org.opends.sdk.AbandonRequest;
import org.opends.sdk.AddRequest;
import org.opends.sdk.BindResult;
import org.opends.sdk.CompareRequest;
import org.opends.sdk.CompareResult;
import org.opends.sdk.DeleteRequest;
import org.opends.sdk.GenericBindRequest;
import org.opends.sdk.GenericExtendedRequest;
import org.opends.sdk.GenericExtendedResult;
import org.opends.sdk.GenericIntermediateResponse;
import org.opends.sdk.ModifyDNRequest;
import org.opends.sdk.ModifyRequest;
import org.opends.sdk.Result;
import org.opends.sdk.SearchRequest;
import org.opends.sdk.SearchResult;
import org.opends.sdk.SearchResultEntry;
import org.opends.sdk.SearchResultReference;
import org.opends.sdk.SimpleBindRequest;
import org.opends.sdk.UnbindRequest;
import org.opends.sdk.sasl.SASLBindRequest;
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



  void handleAbandonRequest(int messageID, AbandonRequest request)
      throws UnexpectedRequestException;



  void handleAddRequest(int messageID, AddRequest request)
      throws UnexpectedRequestException;



  void handleCompareRequest(int messageID, CompareRequest request)
      throws UnexpectedRequestException;



  void handleDeleteRequest(int messageID, DeleteRequest request)
      throws UnexpectedRequestException;



  void handleExtendedRequest(int messageID,
      GenericExtendedRequest request) throws UnexpectedRequestException;



  void handleBindRequest(int messageID, int version,
      GenericBindRequest request) throws UnexpectedRequestException;



  void handleBindRequest(int messageID, int version,
      SASLBindRequest<?> request) throws UnexpectedRequestException;



  void handleBindRequest(int messageID, int version,
      SimpleBindRequest request) throws UnexpectedRequestException;



  void handleModifyDNRequest(int messageID, ModifyDNRequest request)
      throws UnexpectedRequestException;



  void handleModifyRequest(int messageID, ModifyRequest request)
      throws UnexpectedRequestException;



  void handleSearchRequest(int messageID, SearchRequest request)
      throws UnexpectedRequestException;



  void handleUnbindRequest(int messageID, UnbindRequest request)
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
