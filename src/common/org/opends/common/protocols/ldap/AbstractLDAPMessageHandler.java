package org.opends.common.protocols.ldap;

import org.opends.common.api.request.*;
import org.opends.common.api.response.*;
import org.opends.common.api.RawUnknownMessage;
import org.opends.common.api.extended.GenericExtendedRequest;
import org.opends.common.api.extended.GenericExtendedResponse;
import org.opends.common.api.extended.GenericIntermediateResponse;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 16, 2009 Time: 11:57:26
 * AM To change this template use File | Settings | File Templates.
 */
public abstract class AbstractLDAPMessageHandler implements LDAPMessageHandler
{
  public void handleRequest(int messageID, RawAbandonRequest abandonRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, abandonRequest);
  }

  public void handleRequest(int messageID, RawAddRequest addRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, addRequest);
  }

  public void handleResponse(int messageID, RawAddResponse addResponse)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, addResponse);
  }

  public void handleRequest(int messageID, int version, RawSimpleBindRequest bindRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, bindRequest);
  }

  public void handleRequest(int messageID, int version, RawSASLBindRequest bindRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, bindRequest);
  }

  public void handleRequest(int messageID, int version, RawUnknownBindRequest bindRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, bindRequest);
  }

  public void handleResponse(int messageID, RawBindResponse bindResponse)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, bindResponse);
  }

  public void handleRequest(int messageID, RawCompareRequest compareRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, compareRequest);
  }

  public void handleResponse(int messageID, RawCompareResponse compareResponse)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, compareResponse);
  }

  public void handleRequest(int messageID, RawDeleteRequest deleteRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, deleteRequest);
  }

  public void handleResponse(int messageID, RawDeleteResponse deleteResponse)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, deleteResponse);
  }

  public void handleRequest(int messageID, GenericExtendedRequest extendedRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, extendedRequest);
  }

  public void handleResponse(int messageID, GenericExtendedResponse extendedResponse)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, extendedResponse);
  }

  public void handleRequest(int messageID, RawModifyDNRequest modifyDNRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, modifyDNRequest);
  }

  public void handleResponse(int messageID, RawModifyDNResponse modifyDNResponse)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, modifyDNResponse);
  }

  public void handleRequest(int messageID, RawModifyRequest modifyRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, modifyRequest);
  }

  public void handleResponse(int messageID, RawModifyResponse modifyResponse)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, modifyResponse);
  }

  public void handleRequest(int messageID, RawSearchRequest searchRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, searchRequest);
  }

  public void handleResponse(int messageID, RawSearchResultEntry searchResultEntry)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, searchResultEntry);
  }

  public void handleResponse(int messageID, RawSearchResultReference searchResultReference)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, searchResultReference);
  }

  public void handleResponse(int messageID, RawSearchResultDone searchResultDone)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, searchResultDone);
  }

  public void handleRequest(int messageID, RawUnbindRequest unbindRequest)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, unbindRequest);
  }

  public void handleResponse(int messageID, GenericIntermediateResponse intermediateResponse)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, intermediateResponse);
  }

  public void handleMessage(int messageID, RawUnknownMessage unknownMessage)
      throws UnsupportedMessageException
  {
    throw new UnsupportedMessageException(messageID, unknownMessage);
  }
}
