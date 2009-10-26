/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2009 Sun Microsystems, Inc.
 */

package org.opends.sdk.examples;



import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opends.messages.Message;
import org.opends.sdk.Connection;
import org.opends.sdk.ErrorResultException;
import org.opends.sdk.RootDSE;
import org.opends.sdk.ldap.LDAPConnection;
import org.opends.sdk.ldap.LDAPConnectionOptions;
import org.opends.sdk.requests.Requests;
import org.opends.sdk.requests.SimpleBindRequest;
import org.opends.sdk.responses.BindResult;
import org.opends.sdk.responses.BindResultFuture;
import org.opends.sdk.responses.SearchResult;
import org.opends.sdk.responses.SearchResultEntry;
import org.opends.sdk.responses.SearchResultHandler;
import org.opends.sdk.responses.SearchResultReference;
import org.opends.sdk.schema.Schema;


/**
 * Sample code demonstrating usage of the SDK.
 */
public class SimpleBind
{
  private static class SearchHandler implements SearchResultHandler
  {
    long start = System.currentTimeMillis();
    int count = 0;



    public void handleException(ExecutionException t)
    {
      System.out.println(t);
    }



    public void handleResult(SearchResult result)
    {
      // System.out.println(Thread.currentThread() + " " + result);
    }



    public void handleEntry(SearchResultEntry entry)
    {
      if (System.currentTimeMillis() > (start + 1000))
      {
        System.out.println(count);
        start = System.currentTimeMillis();
        count = 0;
      }
      else
      {
        count++;
      }
    }



    public void handleReference(SearchResultReference reference)
    {
      System.out.println(Thread.currentThread() + " " + reference);
    }



    /**
     * {@inheritDoc}
     */
    public void handleError(ErrorResultException result)
    {
      handleException(result);
    }
  }



  public static final void main(String[] args)
  {
    Connection connection = null;
    try
    {
      LDAPConnectionOptions options =
          LDAPConnectionOptions.defaultOptions();
      connection =
          LDAPConnection.connect("localhost", 1389, null)
              .get();

      //StartTLSRequest extendedRequest = new StartTLSRequest();
      //ExtendedResultFuture<Result> tlsFuture =
      //    connection.extendedRequest(extendedRequest, null);
      //System.out.println(tlsFuture.get());

      SimpleBindRequest bindRequest =
          Requests.newSimpleBindRequest("cn=directory manager",
              "password");
      BindResultFuture future = connection.bind(bindRequest, null);

      BindResult response = future.get();
      System.out.println(response);

      List<Message> warnings = new LinkedList<Message>();
      Schema schema = Schema.getSchema(connection, "", warnings);
      System.out.println(schema);
      System.out.println(warnings);
      RootDSE rootDSE = RootDSE.getRootDSE(connection, schema);
      System.out.println(rootDSE);

      /*
       * DigestMD5SASLBindRequest bindRequest = new
       * DigestMD5SASLBindRequest("dn:cn=directory manager",
       * ByteString.valueOf("password")); ResponseFuture<BindResponse>
       * future = connection.bindRequest(bindRequest, null);
       * BindResponse response = future.get();
       * System.out.println(response);
       */
      /*
      DeleteRequest deleteRequest =
          Requests.newDeleteRequest("ou=test.new,dc=example,dc=com");

      try
      {
        System.out
            .println(connection.delete(deleteRequest, null).get());
      }
      catch (ErrorResultException ere)
      {
        // We don't care if the server returned an error on this one.
        // However, all other exceptions will stop the test.
        System.out.println("WARNING: Delete failed: " + ere);
      }

      AddRequest addRequest =
          Requests.newAddRequest("ou=test,dc=example,dc=com");
      addRequest.addAttribute("objectClass", ByteString.valueOf("top"),
          ByteString.valueOf("organizationalUnit"));
      addRequest.addAttribute("ou", ByteString.valueOf("test"));

      ResultFuture addFuture = connection.add(addRequest, null);

      try
      {
        System.out.println(addFuture.get());
      }
      catch (ErrorResultException ere)
      {
        // We don't care if the server returned an error on this one.
        // However, all other exceptions will stop the test.
        System.out.println("WARNING: Add failed: " + ere);
      }
      CompareRequest compareRequest =
          Requests.newCompareRequest(
              "uid=user.0,ou=people,dc=example,dc=com", "uid",
              ByteString.valueOf("user.0"));
      CompareResultFuture compareFuture =
          connection.compare(compareRequest, null);

      Filter filter =
          Filter.newEqualityMatchFilter("uid", ByteString
              .valueOf("user.0"));
      // new RawAndFilter(new RawPresenceFilter("objectClass"));
      // filter.addComponent(new RawEqualFilter("uid",
      // ByteString.valueOf("user.0")));

      SearchRequest searchRequest =
          Requests.newSearchRequest("dc=example,dc=com",
              SearchScope.WHOLE_SUBTREE, filter);
      SearchResultFuture searchFuture1 = null;
      SearchResultHandler handler = new SearchHandler();
      for (int i = 0; i < 10000; i++)
      {
        searchFuture1 = connection.search(searchRequest, handler);
      }

      // ResponseFuture<RawSearchResultDone> searchFuture2 =
      // connection.searchRequest(searchRequest, new
      // SlowSearchHandler());

      // ResponseFuture<RawSearchResultDone> searchFuture3 =
      // connection.searchRequest(searchRequest, new SearchHandler());

      // ResponseFuture<RawSearchResultDone> searchFuture4 =
      // connection.searchRequest(searchRequest, new SearchHandler());

      // searchFuture1.get();
      // searchFuture2.get();
      // searchFuture3.get();
      // searchFuture4.get();

      CancelRequest request = new CancelRequest(10);

      ExtendedResultFuture<Result> cancel =
          connection.extendedRequest(request, null);

      try
      {
        System.out.println(cancel.get());
      }
      catch (ErrorResultException ere)
      {
        // We don't care if the server returned an error on this one.
        // However, all other exceptions will stop the test.
        System.out.println("WARNING: Cancel failed: " + ere);
      }

      PasswordPolicyStateExtendedOperation.Request ppser =
          new PasswordPolicyStateExtendedOperation.Request(
              "uid=user.0,ou=people,dc=example,dc=com");
      ExtendedResultFuture<PasswordPolicyStateExtendedOperation.Response> ppse =
          connection.extendedRequest(ppser, null);
      System.out.println(ppse.get());

      GetConnectionIDRequest gcier = new GetConnectionIDRequest();
      ExtendedResultFuture<GetConnectionIDResult> gcie =
          connection.extendedRequest(gcier, null);
      System.out.println(gcie.get());

      ModifyDNRequest modifyDNRequest =
          Requests.newModifyDNRequest("ou=test,dc=example,dc=com",
              "ou=test.new");
      modifyDNRequest.setDeleteOldRDN(true);
      ResultFuture modifyDNResponse =
          connection.modifyDN(modifyDNRequest, null);

      ModifyRequest modifyRequest =
          Requests
              .newModifyRequest("uid=user.0,ou=people,dc=example,dc=com");
      modifyRequest.addChange(ModificationType.REPLACE, "description",
          ByteString.valueOf("new description"));
      ResultFuture modifyResponse =
          connection.modify(modifyRequest, null);

      System.out.println(compareFuture.get());

      try
      {
        System.out.println(modifyDNResponse.get());
        System.out.println(modifyResponse.get());
        System.out.println(searchFuture1.get());
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      */
    }
    catch (Exception ioe)
    {
      System.out.println(ioe);
      ioe.printStackTrace();
    }
    finally
    {
      if (connection != null)
      {
        connection.close();
      }
    }

  }
}
