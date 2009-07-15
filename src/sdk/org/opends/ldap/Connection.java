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
import org.opends.ldap.responses.BindResult;
import org.opends.ldap.responses.BindResultFuture;
import org.opends.ldap.responses.CompareResult;
import org.opends.ldap.responses.CompareResultFuture;
import org.opends.ldap.responses.ExtendedResultFuture;
import org.opends.ldap.responses.Result;
import org.opends.ldap.responses.ResultFuture;
import org.opends.ldap.responses.SearchResultFuture;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time:
 * 11:21:02 AM To change this template use File | Settings | File
 * Templates.
 */
public interface Connection extends Closeable
{
  void abandon(AbandonRequest request);



  ResultFuture add(AddRequest request);



  ResultFuture add(AddRequest request, ResponseHandler<Result> handler);



  BindResultFuture bind(BindRequest request);



  BindResultFuture bind(BindRequest request,
      ResponseHandler<BindResult> handler);



  void close();



  CompareResultFuture compare(CompareRequest request);



  CompareResultFuture compare(CompareRequest request,
      ResponseHandler<CompareResult> handler);



  ResultFuture delete(DeleteRequest request);



  ResultFuture delete(DeleteRequest request,
      ResponseHandler<Result> handler);



  ExtendedResultFuture extendedRequest(ExtendedRequest request);



  ExtendedResultFuture extendedRequest(ExtendedRequest request,
      ExtendedResponseHandler handler);



  ResultFuture modify(ModifyRequest request);



  ResultFuture modify(ModifyRequest request,
      ResponseHandler<Result> handler);



  ResultFuture modifyDN(ModifyDNRequest request);



  ResultFuture modifyDN(ModifyDNRequest request,
      ResponseHandler<Result> handler);



  SearchResultFuture search(SearchRequest request);



  SearchResultFuture search(SearchRequest request,
      SearchResponseHandler handler);
}
