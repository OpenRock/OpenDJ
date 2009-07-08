package org.opends.client.spi;

import org.opends.common.api.response.*;
import org.opends.common.api.request.*;
import org.opends.common.api.extended.ExtendedRequest;
import org.opends.client.api.ResponseHandler;
import org.opends.client.api.SearchResponseHandler;
import org.opends.client.api.ExtendedResponseHandler;
import org.opends.client.api.request.AbstractSASLBindRequest;
import org.opends.client.spi.futures.*;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 11:21:02
 * AM To change this template use File | Settings | File Templates.
 */
public interface Connection extends Closeable
{
  public AddResponseFuture addRequest(
      AddRequest addRequest,
      ResponseHandler<AddResponse> responseHandler);

  public BindResponseFuture bindRequest(
      BindRequest bindRequest,
      ResponseHandler<BindResponse> responseHandler);

  public CompareResponseFuture compareRequest(
      CompareRequest compareRequest,
      ResponseHandler<CompareResponse> responseHandler);

  public DeleteResponseFuture deleteRequest(
      DeleteRequest deleteRequest,
      ResponseHandler<DeleteResponse> responseHandler);

  public ExtendedResponseFuture extendedRequest(
      ExtendedRequest extendedRequest,
      ExtendedResponseHandler responseHandler);

  public ModifyDNResponseFuture modifyDNRequest(
      ModifyDNRequest modifyDNRequest,
      ResponseHandler<ModifyDNResponse> responseHandler);

  public ModifyResponseFuture modifyRequest(
      ModifyRequest modifyRequest,
      ResponseHandler<ModifyResponse> responseHandler);

  public SearchResponseFuture searchRequest(
      SearchRequest searchRequest,
      SearchResponseHandler responseHandler);

  void abandonRequest(AbandonRequest abandonRequest);

  void close();
}
