package org.opends.examples;



import java.util.concurrent.ExecutionException;

import org.opends.admin.ads.util.BlindTrustManager;
import org.opends.ldap.Connection;
import org.opends.ldap.ConnectionOptions;
import org.opends.ldap.Connections;
import org.opends.ldap.SearchResponseHandler;
import org.opends.ldap.extensions.CancelRequest;
import org.opends.ldap.extensions.GetConnectionIDRequest;
import org.opends.ldap.extensions.GetConnectionIDResult;
import org.opends.ldap.extensions.PasswordPolicyStateExtendedOperation;
import org.opends.ldap.extensions.StartTLSRequest;
import org.opends.ldap.requests.AddRequest;
import org.opends.ldap.requests.CompareRequest;
import org.opends.ldap.requests.DeleteRequest;
import org.opends.ldap.requests.ModifyDNRequest;
import org.opends.ldap.requests.ModifyRequest;
import org.opends.ldap.requests.SearchRequest;
import org.opends.ldap.requests.SimpleBindRequest;
import org.opends.ldap.responses.BindResult;
import org.opends.ldap.responses.BindResultFuture;
import org.opends.ldap.responses.CompareResultFuture;
import org.opends.ldap.responses.ErrorResultException;
import org.opends.ldap.responses.ExtendedResultFuture;
import org.opends.ldap.responses.Result;
import org.opends.ldap.responses.ResultFuture;
import org.opends.ldap.responses.SearchResult;
import org.opends.ldap.responses.SearchResultEntry;
import org.opends.ldap.responses.SearchResultFuture;
import org.opends.ldap.responses.SearchResultReference;
import org.opends.server.types.ByteString;
import org.opends.types.ModificationType;
import org.opends.types.SearchScope;
import org.opends.types.filter.Filter;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 15, 2009 Time:
 * 4:58:02 PM To change this template use File | Settings | File
 * Templates.
 */
public class SimpleBind
{
  private static class SearchHandler implements SearchResponseHandler
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



    public void handleSearchResultEntry(SearchResultEntry entry)
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



    public void handleSearchResultReference(
        SearchResultReference reference)
    {
      System.out.println(Thread.currentThread() + " " + reference);
    }



    /**
     * {@inheritDoc}
     */
    public void handleErrorResult(ErrorResultException result)
    {
      handleException(result);
    }
  }



  public static final void main(String[] args)
  {
    Connection connection = null;
    try
    {
      ConnectionOptions options =
          new ConnectionOptions()
              .setTrustManager(new BlindTrustManager());
      connection = Connections.connect("localhost", 1389, options);

      StartTLSRequest extendedRequest = new StartTLSRequest();
      ExtendedResultFuture<Result> tlsFuture =
          connection.extendedRequest(extendedRequest);
      System.out.println(tlsFuture.get());

      SimpleBindRequest bindRequest =
          new SimpleBindRequest("cn=directory manager", ByteString
              .valueOf("password"));
      BindResultFuture future = connection.bind(bindRequest, null);

      BindResult response = future.get();
      System.out.println(response);

      /*
       * DigestMD5SASLBindRequest bindRequest = new
       * DigestMD5SASLBindRequest("dn:cn=directory manager",
       * ByteString.valueOf("password")); ResponseFuture<BindResponse>
       * future = connection.bindRequest(bindRequest, null);
       * BindResponse response = future.get();
       * System.out.println(response);
       */
      DeleteRequest deleteRequest =
          new DeleteRequest("ou=test.new,dc=example,dc=com");

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
          new AddRequest("ou=test,dc=example,dc=com");
      addRequest.addAttribute("objectClass", ByteString.valueOf("top"),
          ByteString.valueOf("organizationalUnit"));
      addRequest.addAttribute("ou", ByteString.valueOf("test"));

      ResultFuture addFuture = connection.add(addRequest);

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
          new CompareRequest("uid=user.0,ou=people,dc=example,dc=com",
              "uid", ByteString.valueOf("user.0"));
      CompareResultFuture compareFuture =
          connection.compare(compareRequest);

      Filter filter =
          Filter.newEqualityMatchFilter("uid", ByteString
              .valueOf("user.0"));
      // new RawAndFilter(new RawPresenceFilter("objectClass"));
      // filter.addComponent(new RawEqualFilter("uid",
      // ByteString.valueOf("user.0")));

      SearchRequest searchRequest =
          new SearchRequest("dc=example,dc=com",
              SearchScope.WHOLE_SUBTREE, filter);
      SearchResultFuture searchFuture1 = null;
      SearchResponseHandler handler = new SearchHandler();
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
          connection.extendedRequest(request);

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
          connection.extendedRequest(ppser);
      System.out.println(ppse.get());

      GetConnectionIDRequest gcier = new GetConnectionIDRequest();
      ExtendedResultFuture<GetConnectionIDResult> gcie =
          connection.extendedRequest(gcier);
      System.out.println(gcie.get());

      ModifyDNRequest modifyDNRequest =
          new ModifyDNRequest("ou=test,dc=example,dc=com",
              "ou=test.new");
      modifyDNRequest.setDeleteOldRDN(true);
      ResultFuture modifyDNResponse =
          connection.modifyDN(modifyDNRequest);

      ModifyRequest modifyRequest =
          new ModifyRequest("uid=user.0,ou=people,dc=example,dc=com");
      modifyRequest.addChange(ModificationType.REPLACE, "description",
          ByteString.valueOf("new description"));
      ResultFuture modifyResponse = connection.modify(modifyRequest);

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
