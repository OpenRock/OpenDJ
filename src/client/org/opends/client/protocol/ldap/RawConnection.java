package org.opends.client.protocol.ldap;

import org.opends.common.api.response.*;
import org.opends.common.api.request.*;
import org.opends.common.api.extended.ExtendedRequest;
import org.opends.common.api.extended.ExtendedOperation;
import org.opends.client.api.ResponseHandler;
import org.opends.client.api.SearchResponseHandler;
import org.opends.client.api.ExtendedResponseHandler;
import org.opends.client.api.request.AbstractSASLBindRequest;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 11:21:02
 * AM To change this template use File | Settings | File Templates.
 */
public interface RawConnection extends Closeable
{
  public ResponseFuture<AddResponse> addRequest(
      AddRequest addRequest,
      ResponseHandler<AddResponse> responseHandler)
      throws IOException;

  public ResponseFuture<BindResponse> bindRequest(
      SimpleBindRequest bindRequest,
      ResponseHandler<BindResponse> responseHandler)
      throws IOException;

  public ResponseFuture<BindResponse> bindRequest(
      AbstractSASLBindRequest bindRequest,
      ResponseHandler<BindResponse> responseHandler)
      throws IOException;

  public ResponseFuture<CompareResponse> compareRequest(
      CompareRequest compareRequest,
      ResponseHandler<CompareResponse> responseHandler)
      throws IOException;

  public ResponseFuture<DeleteResponse> deleteRequest(
      DeleteRequest deleteRequest,
      ResponseHandler<DeleteResponse> responseHandler)
      throws IOException;

  public <T extends ExtendedOperation>
  ExtendedResponseFuture<T> extendedRequest(
      ExtendedRequest<T> extendedRequest,
      ExtendedResponseHandler<T> responseHandler)
      throws IOException;

  public ResponseFuture<ModifyDNResponse> modifyDNRequest(
      ModifyDNRequest modifyDNRequest,
      ResponseHandler<ModifyDNResponse> responseHandler)
      throws IOException;

  public ResponseFuture<ModifyResponse> modifyRequest(
      ModifyRequest modifyRequest,
      ResponseHandler<ModifyResponse> responseHandler)
      throws IOException;

  public SearchResponseFuture searchRequest(
      SearchRequest searchRequest, SearchResponseHandler responseHandler)
      throws IOException;
}
