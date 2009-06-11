package org.opends.client.protocol.ldap;

import org.opends.common.api.raw.response.RawAddResponse;
import org.opends.common.api.raw.response.RawBindResponse;
import org.opends.common.api.raw.response.RawExtendedResponse;
import org.opends.common.api.raw.response.RawSearchResultDone;
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
      throws IOException, InvalidConnectionException;

  public ResponseFuture<RawBindResponse> bindRequest(
      RawSimpleBindRequest bindRequest,
      ResponseHandler<RawBindResponse> responseHandler)
      throws IOException, InvalidConnectionException;

  public ResponseFuture<RawBindResponse> bindRequest(
      RawSASLBindRequest bindRequest,
      ResponseHandler<RawBindResponse> responseHandler)
      throws IOException, InvalidConnectionException;

  public ResponseFuture<RawExtendedResponse> extendedRequest(
      RawExtendedRequest extendedRequest,
      ResponseHandler<RawExtendedResponse> responseHandler)
      throws IOException, InvalidConnectionException;

  public ResponseFuture<RawSearchResultDone> searchRequest(
      RawSearchRequest searchRequest, SearchResponseHandler responseHandler)
      throws IOException, InvalidConnectionException;
}
