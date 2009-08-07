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
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 16, 2009 Time:
 * 11:57:26 AM To change this template use File | Settings | File
 * Templates.
 */
public abstract class AbstractLDAPMessageHandler implements
    LDAPMessageHandler
{
  public void handleUnrecognizedMessage(int messageID, byte messageTag,
      ByteString messageBytes) throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, messageTag,
        messageBytes);
  }



  public void handleAbandonRequest(int messageID, AbandonRequestImpl request)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, request);
  }



  public void handleAddRequest(int messageID, AddRequestImpl request)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, request);
  }



  public void handleCompareRequest(int messageID, CompareRequestImpl request)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, request);
  }



  public void handleDeleteRequest(int messageID, DeleteRequestImpl request)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, request);
  }



  public void handleExtendedRequest(int messageID,
      GenericExtendedRequestImpl request) throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, request);
  }



  public void handleBindRequest(int messageID, int version,
      GenericBindRequestImpl request) throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, request);
  }



  public void handleBindRequest(int messageID, int version,
      SASLBindRequest request) throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, request);
  }



  public void handleBindRequest(int messageID, int version,
      SimpleBindRequestImpl request) throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, request);
  }



  public void handleModifyDNRequest(int messageID,
      ModifyDNRequestImpl request) throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, request);
  }



  public void handleModifyRequest(int messageID, ModifyRequestImpl request)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, request);
  }



  public void handleSearchRequest(int messageID, SearchRequestImpl request)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, request);
  }



  public void handleUnbindRequest(int messageID, UnbindRequestImpl request)
      throws UnexpectedRequestException
  {
    throw new UnexpectedRequestException(messageID, request);
  }



  public void handleAddResult(int messageID, Result result)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, result);
  }



  public void handleBindResult(int messageID, BindResult result)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, result);
  }



  public void handleCompareResult(int messageID, CompareResult result)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, result);
  }



  public void handleDeleteResult(int messageID, Result result)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, result);
  }



  public void handleExtendedResult(int messageID,
      GenericExtendedResult result) throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, result);
  }



  public void handleIntermediateResponse(int messageID,
      GenericIntermediateResponse response)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, response);
  }



  public void handleModifyDNResult(int messageID, Result result)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, result);
  }



  public void handleModifyResult(int messageID, Result result)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, result);
  }



  public void handleSearchResult(int messageID, SearchResult result)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, result);
  }



  public void handleSearchResultEntry(int messageID,
      SearchResultEntry entry) throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, entry);
  }



  public void handleSearchResultReference(int messageID,
      SearchResultReference reference)
      throws UnexpectedResponseException
  {
    throw new UnexpectedResponseException(messageID, reference);
  }
}
