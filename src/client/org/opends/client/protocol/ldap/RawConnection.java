package org.opends.client.protocol.ldap;

import org.opends.common.api.raw.response.*;
import org.opends.common.api.raw.request.*;
import org.opends.client.api.ResponseHandler;
import org.opends.client.api.SearchResponseHandler;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 11:21:02
 * AM To change this template use File | Settings | File Templates.
 */
public interface RawConnection extends Closeable
{
  public ResponseFuture<RawAddResponse> addRequest(
      RawAddRequest addRequest,
      ResponseHandler<RawAddResponse> responseHandler)
      throws InvalidConnectionException;

  public ResponseFuture<RawBindResponse> bindRequest(
      RawSimpleBindRequest bindRequest,
      ResponseHandler<RawBindResponse> responseHandler)
      throws InvalidConnectionException;

  public ResponseFuture<RawBindResponse> bindRequest(
      RawSASLBindRequest bindRequest,
      ResponseHandler<RawBindResponse> responseHandler)
      throws InvalidConnectionException;

  public ResponseFuture<RawCompareResponse> compareRequest(
      RawCompareRequest compareRequest,
      ResponseHandler<RawCompareResponse> responseHandler)
      throws InvalidConnectionException;

  public ResponseFuture<RawDeleteResponse> deleteRequest(
      RawDeleteRequest deleteRequest,
      ResponseHandler<RawDeleteResponse> responseHandler)
      throws InvalidConnectionException;

  public ResponseFuture<RawExtendedResponse> extendedRequest(
      RawExtendedRequest extendedRequest,
      ResponseHandler<RawExtendedResponse> responseHandler)
      throws InvalidConnectionException; 

  public ResponseFuture<RawModifyDNResponse> modifyDNRequest(
      RawModifyDNRequest modifyDNRequest,
      ResponseHandler<RawModifyDNResponse> responseHandler)
      throws InvalidConnectionException;

  public ResponseFuture<RawModifyResponse> modifyRequest(
      RawModifyRequest modifyRequest,
      ResponseHandler<RawModifyResponse> responseHandler)
      throws InvalidConnectionException;

  public ResponseFuture<RawSearchResultDone> searchRequest(
      RawSearchRequest searchRequest, SearchResponseHandler responseHandler)
      throws InvalidConnectionException;
}
