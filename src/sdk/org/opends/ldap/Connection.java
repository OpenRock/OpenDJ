package org.opends.ldap;



import java.io.Closeable;

import org.opends.ldap.requests.AbandonRequest;
import org.opends.ldap.requests.AddRequest;
import org.opends.ldap.requests.BindRequest;
import org.opends.ldap.requests.CompareRequest;
import org.opends.ldap.requests.DeleteRequest;
import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.requests.ModifyDNRequest;
import org.opends.ldap.requests.ModifyRequest;
import org.opends.ldap.requests.SearchRequest;
import org.opends.ldap.responses.AddResponse;
import org.opends.ldap.responses.BindResponse;
import org.opends.ldap.responses.BindResponseFuture;
import org.opends.ldap.responses.CompareResponse;
import org.opends.ldap.responses.CompareResponseFuture;
import org.opends.ldap.responses.DeleteResponse;
import org.opends.ldap.responses.ExtendedResponseFuture;
import org.opends.ldap.responses.ModifyDNResponse;
import org.opends.ldap.responses.ModifyResponse;
import org.opends.ldap.responses.ResponseFuture;
import org.opends.ldap.responses.SearchResponseFuture;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time:
 * 11:21:02 AM To change this template use File | Settings | File
 * Templates.
 */
public interface Connection extends Closeable
{
  void abandon(AbandonRequest request);



  ResponseFuture add(AddRequest request);



  ResponseFuture add(AddRequest request,
      ResponseHandler<AddResponse> handler);



  BindResponseFuture bind(BindRequest request);



  BindResponseFuture bind(BindRequest request,
      ResponseHandler<BindResponse> handler);



  CompareResponseFuture compare(CompareRequest request);



  CompareResponseFuture compare(CompareRequest request,
      ResponseHandler<CompareResponse> handler);



  ResponseFuture delete(DeleteRequest request);



  ResponseFuture delete(DeleteRequest request,
      ResponseHandler<DeleteResponse> handler);



  ExtendedResponseFuture extendedRequest(ExtendedRequest request);



  ExtendedResponseFuture extendedRequest(ExtendedRequest request,
      ExtendedResponseHandler handler);



  ResponseFuture modify(ModifyRequest request);



  ResponseFuture modify(ModifyRequest request,
      ResponseHandler<ModifyResponse> handler);



  ResponseFuture modifyDN(ModifyDNRequest request);



  ResponseFuture modifyDN(ModifyDNRequest request,
      ResponseHandler<ModifyDNResponse> handler);



  SearchResponseFuture search(SearchRequest request);



  SearchResponseFuture search(SearchRequest request,
      SearchResponseHandler handler);



  void close();
}
