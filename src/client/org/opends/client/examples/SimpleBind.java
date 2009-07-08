package org.opends.client.examples;

import com.sun.grizzly.nio.transport.TCPNIOTransport;
import com.sun.grizzly.TransportFactory;
import org.opends.client.protocol.ldap.*;
import org.opends.client.api.SearchResponseHandler;
import org.opends.client.api.AbstractSearchResponseHandler;
import org.opends.client.spi.Connection;
import org.opends.client.spi.ErrorResultException;
import org.opends.client.spi.futures.*;
import org.opends.admin.ads.util.BlindTrustManager;
import org.opends.common.api.request.*;
import org.opends.common.api.filter.Filter;
import org.opends.common.api.response.*;
import org.opends.common.api.SearchScope;
import org.opends.common.api.ModificationType;
import org.opends.common.api.ResultCode;
import org.opends.common.api.extended.CancelExtendedOperation;
import org.opends.common.api.extended.PasswordPolicyStateExtendedOperation;
import org.opends.common.api.extended.GetConnectionIDExtendedOperation;
import org.opends.common.api.extended.StartTLSExtendedOperation;
import org.opends.server.types.ByteString;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 15, 2009 Time: 4:58:02
 * PM To change this template use File | Settings | File Templates.
 */
public class SimpleBind
{
  public static final void main(String[] args)
  {
    TCPNIOTransport transport =
        TransportFactory.getInstance().createTCPTransport();
    LDAPConnectionFactory factory =
        new LDAPConnectionFactory("localhost", 11389, transport);

    try
    {
    factory.setTrustManager(new BlindTrustManager());
    factory.start();
    }
    catch(Exception e)
    {
      System.out.println(e);
    }

    Connection connection = null;
    try
    {
      connection = factory.getConnection();

      StartTLSExtendedOperation.Request extendedRequest =
          new StartTLSExtendedOperation.Request();
      ExtendedResponseFuture tlsFuture =
          connection.extendedRequest(extendedRequest, null);
      System.out.println(tlsFuture.get());

      SimpleBindRequest bindRequest =
          new SimpleBindRequest("cn=directory manager",
                                   ByteString.valueOf("password"));
      BindResponseFuture future =
          connection.bindRequest(bindRequest, null);

      BindResponse response = future.get();
      System.out.println(response);

      /*
      DigestMD5SASLBindRequest bindRequest =
          new DigestMD5SASLBindRequest("dn:cn=directory manager",
              ByteString.valueOf("password"));
      ResponseFuture<BindResponse> future =
          connection.bindRequest(bindRequest, null);
      BindResponse response = future.get();
      System.out.println(response);
      */
      DeleteRequest deleteRequest = new DeleteRequest("ou=test.new,dc=example,dc=com");

      try
      {
        System.out.println(connection.deleteRequest(deleteRequest, null).get());
      }
      catch(ErrorResultException ere)
      {
        // We don't care if the server returned an error on this one.
        // However, all other exceptions will stop the test.
        System.out.println("WARNING: Delete failed: " + ere);
      }

      AddRequest addRequest = new AddRequest("ou=test,dc=example,dc=com");
      addRequest.addAttribute("objectClass", ByteString.valueOf("top"), ByteString.valueOf("organizationalUnit"));
      addRequest.addAttribute("ou", ByteString.valueOf("test"));

      AddResponseFuture addFuture = connection.addRequest(addRequest, null);

      try
      {
        System.out.println(addFuture.get());
      }
      catch(ErrorResultException ere)
      {
        // We don't care if the server returned an error on this one.
        // However, all other exceptions will stop the test.
        System.out.println("WARNING: Add failed: " + ere);
      }
      CompareRequest compareRequest = new CompareRequest(
          "uid=user.0,ou=people,dc=example,dc=com", "uid",
          ByteString.valueOf("user.0"));
      CompareResponseFuture compareFuture = connection.compareRequest(compareRequest, null);

      Filter filter = Filter.newEqualityMatchFilter("uid", ByteString.valueOf("user.0"));
      //new RawAndFilter(new RawPresenceFilter("objectClass"));
      //filter.addComponent(new RawEqualFilter("uid", ByteString.valueOf("user.0")));

      SearchRequest searchRequest = new SearchRequest("dc=example,dc=com",
                                                            SearchScope.WHOLE_SUBTREE,
                                                            filter);
      SearchResponseFuture searchFuture1 = null;
      SearchResponseHandler handler = new SearchHandler();
      for(int i = 0; i < 10000; i++)
      {
          searchFuture1 = connection.searchRequest(searchRequest, handler);
      }

          //  ResponseFuture<RawSearchResultDone> searchFuture2 =
         // connection.searchRequest(searchRequest, new SlowSearchHandler());

         //   ResponseFuture<RawSearchResultDone> searchFuture3 =
         // connection.searchRequest(searchRequest, new SearchHandler());

           //       ResponseFuture<RawSearchResultDone> searchFuture4 =
          //connection.searchRequest(searchRequest, new SearchHandler());

      //searchFuture1.get();
      //searchFuture2.get();
      //searchFuture3.get();
      //searchFuture4.get();

      CancelExtendedOperation.Request request =
          new CancelExtendedOperation.Request(10);

      ExtendedResponseFuture cancel =
          connection.extendedRequest(request, null);

      try
      {
        System.out.println(cancel.get());
      }
      catch(ErrorResultException ere)
      {
        // We don't care if the server returned an error on this one.
        // However, all other exceptions will stop the test.
        System.out.println("WARNING: Cancel failed: " + ere);
      }


      PasswordPolicyStateExtendedOperation.Request ppser =
          new PasswordPolicyStateExtendedOperation.Request(
              "uid=user.0,ou=people,dc=example,dc=com");
      ExtendedResponseFuture
          ppse = connection.extendedRequest(ppser, null);
      System.out.println(ppse.get());

      GetConnectionIDExtendedOperation.Request gcier =
          new GetConnectionIDExtendedOperation.Request();
      ExtendedResponseFuture gcie =
          connection.extendedRequest(gcier, null);
      System.out.println(gcie.get());



      ModifyDNRequest modifyDNRequest = new ModifyDNRequest("ou=test,dc=example,dc=com", "ou=test.new");
      modifyDNRequest.setDeleteOldRDN(true);
      ModifyDNResponseFuture modifyDNResponse = connection.modifyDNRequest(modifyDNRequest, null);

      ModifyRequest modifyRequest = new ModifyRequest("uid=user.0,ou=people,dc=example,dc=com");
      modifyRequest.addChange(ModificationType.REPLACE, "description", ByteString.valueOf("new description"));
      ModifyResponseFuture modifyResponse = connection.modifyRequest(modifyRequest, null);

      System.out.println(compareFuture.get());

      try
      {
      System.out.println(modifyDNResponse.get());
      System.out.println(modifyResponse.get());
        System.out.println(searchFuture1.get());
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
    catch(Exception ioe)
    {
      System.out.println(ioe);
      ioe.printStackTrace();
    }
    finally
    {
      if(connection != null)
      {
        connection.close();
      }
      try
      {
            transport.stop();
      }
      catch(Exception e)
      {
        System.out.println(e);
      }
      transport.getWorkerThreadPool().shutdown();
    }


  }

  private static class SlowSearchHandler extends AbstractSearchResponseHandler
  {
    public void handleSearchResultEntry(SearchResultEntry entry)
    {
      try
      {
      Thread.sleep(1000);
      }
      catch(Exception e)
      {

      }
      System.out.println(Thread.currentThread() + " " + entry);
    }

    public void handleSearchResultReference(SearchResultReference reference)
    {
      System.out.println(Thread.currentThread() + " " + reference);
    }

    public void handleResult(SearchResultDone result)
    {
      System.out.println(Thread.currentThread() + " " + result);
    }

    public void handleException(ExecutionException t)
    {
      System.out.println(t);
    }
  }

  private static class SearchHandler extends AbstractSearchResponseHandler
  {
    long start = System.currentTimeMillis();
    int count = 0;
    public void handleSearchResultEntry(SearchResultEntry entry)
    {
      if(System.currentTimeMillis() > (start + 1000))
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

    public void handleSearchResultReference(SearchResultReference reference)
    {
      System.out.println(Thread.currentThread() + " " + reference);
    }

    public void handleResult(SearchResultDone result)
    {
      //System.out.println(Thread.currentThread() + " " + result);
    }

    public void handleException(ExecutionException t)
    {
      System.out.println(t);
    }
  }
}
