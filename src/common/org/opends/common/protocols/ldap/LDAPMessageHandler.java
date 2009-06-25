package org.opends.common.protocols.ldap;

import org.opends.common.api.request.*;
import org.opends.common.api.response.*;
import org.opends.common.api.RawUnknownMessage;
import org.opends.common.api.extended.GenericExtendedRequest;
import org.opends.common.api.extended.GenericExtendedResponse;
import org.opends.common.api.extended.GenericIntermediateResponse;

/**
 * Created by IntelliJ IDEA.
 * User: digitalperk
 * Date: May 25, 2009
 * Time: 9:39:07 AM
 * To change this template use File | Settings | File Templates.
 */
public interface LDAPMessageHandler {
  public void handleRequest(int messageID, RawAbandonRequest abandonRequest)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, RawAddRequest addRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID, RawAddResponse addResponse)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, int version,
                            RawSimpleBindRequest bindRequest)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, int version,
                            RawSASLBindRequest bindRequest)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, int version,
                            RawUnknownBindRequest bindRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID, RawBindResponse bindResponse)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, RawCompareRequest compareRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID, RawCompareResponse compareResponse)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, RawDeleteRequest deleteRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID, RawDeleteResponse deleteResponse)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, GenericExtendedRequest extendedRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID,
                             GenericExtendedResponse extendedResponse)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, RawModifyDNRequest modifyDNRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID,
                             RawModifyDNResponse modifyDNResponse)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, RawModifyRequest modifyRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID, RawModifyResponse modifyResponse)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, RawSearchRequest searchRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID,
                             RawSearchResultEntry searchResultEntry)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID,
                             RawSearchResultReference searchResultReference)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID,
                             RawSearchResultDone searchResultDone)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, RawUnbindRequest unbindRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID,
                             GenericIntermediateResponse intermediateResponse)
      throws UnsupportedMessageException;

  public void handleMessage(int messageID, RawUnknownMessage unknownMessage)
      throws UnsupportedMessageException;

  public void handleException(Throwable throwable);
}
