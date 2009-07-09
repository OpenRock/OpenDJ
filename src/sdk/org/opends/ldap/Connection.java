package org.opends.ldap;



import java.io.Closeable;

import org.opends.ldap.futures.AddResponseFuture;
import org.opends.ldap.futures.BindResponseFuture;
import org.opends.ldap.futures.CompareResponseFuture;
import org.opends.ldap.futures.DeleteResponseFuture;
import org.opends.ldap.futures.ExtendedResponseFuture;
import org.opends.ldap.futures.ModifyDNResponseFuture;
import org.opends.ldap.futures.ModifyResponseFuture;
import org.opends.ldap.futures.SearchResponseFuture;
import org.opends.ldap.requests.AbandonRequest;
import org.opends.ldap.requests.AddRequest;
import org.opends.ldap.requests.BindRequest;
import org.opends.ldap.requests.CompareRequest;
import org.opends.ldap.requests.DeleteRequest;
import org.opends.ldap.requests.ModifyDNRequest;
import org.opends.ldap.requests.ModifyRequest;
import org.opends.ldap.requests.SearchRequest;
import org.opends.ldap.responses.AddResponse;
import org.opends.ldap.responses.BindResponse;
import org.opends.ldap.responses.CompareResponse;
import org.opends.ldap.responses.DeleteResponse;
import org.opends.ldap.responses.ModifyDNResponse;
import org.opends.ldap.responses.ModifyResponse;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time:
 * 11:21:02 AM To change this template use File | Settings | File
 * Templates.
 */
public interface Connection extends Closeable
{
  public AddResponseFuture addRequest(AddRequest addRequest,
      ResponseHandler<AddResponse> responseHandler);



  public BindResponseFuture bindRequest(BindRequest bindRequest,
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
      SearchRequest searchRequest, SearchResponseHandler responseHandler);



  void abandonRequest(AbandonRequest abandonRequest);



  void close();
}
