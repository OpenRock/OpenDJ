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
import org.opends.ldap.requests.ExtendedRequest;
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
  void abandon(AbandonRequest request);



  AddResponseFuture add(AddRequest request);



  AddResponseFuture add(AddRequest request,
      ResponseHandler<AddResponse> handler);



  BindResponseFuture bind(BindRequest request);



  BindResponseFuture bind(BindRequest request,
      ResponseHandler<BindResponse> handler);



  CompareResponseFuture compare(CompareRequest request);



  CompareResponseFuture compare(CompareRequest request,
      ResponseHandler<CompareResponse> handler);



  DeleteResponseFuture delete(DeleteRequest request);



  DeleteResponseFuture delete(DeleteRequest request,
      ResponseHandler<DeleteResponse> handler);



  ExtendedResponseFuture extendedRequest(ExtendedRequest request);



  ExtendedResponseFuture extendedRequest(ExtendedRequest request,
      ExtendedResponseHandler handler);



  ModifyResponseFuture modify(ModifyRequest request);



  ModifyResponseFuture modify(ModifyRequest request,
      ResponseHandler<ModifyResponse> handler);



  ModifyDNResponseFuture modifyDN(ModifyDNRequest request);



  ModifyDNResponseFuture modifyDN(ModifyDNRequest request,
      ResponseHandler<ModifyDNResponse> handler);



  SearchResponseFuture search(SearchRequest request);



  SearchResponseFuture search(SearchRequest request,
      SearchResponseHandler handler);



  void close();
}
