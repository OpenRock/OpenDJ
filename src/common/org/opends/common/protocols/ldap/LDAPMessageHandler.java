package org.opends.common.protocols.ldap;

import org.opends.common.api.request.*;
import org.opends.common.api.response.*;
import org.opends.common.api.GenericMessage;
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
  public void handleRequest(int messageID, AbandonRequest abandonRequest)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, AddRequest addRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID, AddResponse addResponse)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, int version,
                            SimpleBindRequest bindRequest)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, int version,
                            SASLBindRequest bindRequest)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, int version,
                            GenericBindRequest bindRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID, BindResponse bindResponse)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, CompareRequest compareRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID, CompareResponse compareResponse)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, DeleteRequest deleteRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID, DeleteResponse deleteResponse)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, GenericExtendedRequest extendedRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID,
                             GenericExtendedResponse extendedResponse)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, ModifyDNRequest modifyDNRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID,
                             ModifyDNResponse modifyDNResponse)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, ModifyRequest modifyRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID, ModifyResponse modifyResponse)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, SearchRequest searchRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID,
                             SearchResultEntry searchResultEntry)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID,
                             SearchResultReference searchResultReference)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID,
                             SearchResultDone searchResultDone)
      throws UnsupportedMessageException;

  public void handleRequest(int messageID, UnbindRequest unbindRequest)
      throws UnsupportedMessageException;

  public void handleResponse(int messageID,
                             GenericIntermediateResponse intermediateResponse)
      throws UnsupportedMessageException;

  public void handleMessage(int messageID, GenericMessage unknownMessage)
      throws UnsupportedMessageException;

  public void handleException(Throwable throwable);
}
