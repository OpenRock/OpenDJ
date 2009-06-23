package org.opends.client.examples;

import com.sun.grizzly.nio.transport.TCPNIOTransport;
import com.sun.grizzly.TransportFactory;
import org.opends.client.protocol.ldap.LDAPConnectionFactory;
import org.opends.client.protocol.ldap.RawConnection;
import org.opends.client.protocol.ldap.ResponseFuture;
import org.opends.client.api.SearchResponseHandler;
import org.opends.admin.ads.util.BlindTrustManager;
import org.opends.common.api.raw.request.*;
import org.opends.common.api.raw.request.filter.RawFilter;
import org.opends.common.api.raw.request.filter.RawEqualFilter;
import org.opends.common.api.raw.response.*;
import org.opends.common.api.raw.RawPartialAttribute;
import org.opends.server.types.ByteString;
import org.opends.server.types.SearchScope;
import org.opends.server.types.ModificationType;

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

    try
    {
      RawConnection connection = factory.getConnection();



      //RawExtendedRequest extendedRequest =
       //   new RawExtendedRequest(OID_START_TLS_REQUEST);
      //ResponseFuture<RawExtendedResponse> tlsFuture =
      //    connection.extendedRequest(extendedRequest, null);
      //System.out.println(tlsFuture.get());

      RawSimpleBindRequest bindRequest =
          new RawSimpleBindRequest("cn=directory manager",
                                   ByteString.valueOf("password"));
      ResponseFuture<RawBindResponse> future =
          connection.bindRequest(bindRequest, null);

      RawBindResponse response = future.get();
      System.out.println(response);

      RawDeleteRequest deleteRequest = new RawDeleteRequest("ou=test.new,dc=example,dc=com");
      System.out.println(connection.deleteRequest(deleteRequest, null).get());

      RawPartialAttribute attribute = new RawPartialAttribute("objectClass", ByteString.valueOf("top"));
      attribute.addAttributeValue(ByteString.valueOf("organizationalUnit"));
      RawAddRequest addRequest = new RawAddRequest("ou=test,dc=example,dc=com");
      addRequest.addAttribute(attribute);
      addRequest.addAttribute(new RawPartialAttribute("ou", ByteString.valueOf("test")));

      ResponseFuture<RawAddResponse> addFuture = connection.addRequest(addRequest, null);

      RawCompareRequest compareRequest = new RawCompareRequest(
          "uid=user.0,ou=people,dc=example,dc=com", "uid",
          ByteString.valueOf("user.0"));
      ResponseFuture<RawCompareResponse> compareFuture = connection.compareRequest(compareRequest, null);

      RawFilter filter = new RawEqualFilter("uid", ByteString.valueOf("user.0"));
      //new RawAndFilter(new RawPresenceFilter("objectClass"));
      //filter.addComponent(new RawEqualFilter("uid", ByteString.valueOf("user.0")));

      RawSearchRequest searchRequest = new RawSearchRequest("dc=example,dc=com",
                                                            SearchScope.WHOLE_SUBTREE.intValue(),
                                                            filter);
      ResponseFuture<RawSearchResultDone> searchFuture1 = null;
      SearchResponseHandler handler = new SearchHandler();
      for(int i = 0; i < 100000; i++)
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

      System.out.println(addFuture.get());
      RawModifyDNRequest modifyDNRequest = new RawModifyDNRequest("ou=test,dc=example,dc=com", "ou=test.new");
      modifyDNRequest.setDeleteOldRDN(true);
      ResponseFuture<RawModifyDNResponse> modifyDNResponse = connection.modifyDNRequest(modifyDNRequest, null);

      RawModifyRequest modifyRequest = new RawModifyRequest("uid=user.0,ou=people,dc=example,dc=com");
      RawPartialAttribute attr = new RawPartialAttribute("description");
      attr.addAttributeValue(ByteString.valueOf("new description"));
      modifyRequest.addChange(new RawModifyRequest.Change(ModificationType.REPLACE.intValue(), attr));
      ResponseFuture<RawModifyResponse> modifyResponse = connection.modifyRequest(modifyRequest, null);

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


      connection.close();
    }
    catch(Exception ioe)
    {
      System.out.println(ioe);
    }
    finally
    {
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

  private static class SlowSearchHandler implements SearchResponseHandler
  {
    public void handleSearchResultEntry(RawSearchResultEntry entry)
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

    public void handleSearchResultReference(RawSearchResultReference reference)
    {
      System.out.println(Thread.currentThread() + " " + reference);
    }

    public void handleResult(RawSearchResultDone result)
    {
      System.out.println(Thread.currentThread() + " " + result);
    }

    public void handleException(Throwable t)
    {
      System.out.println(t);
    }
  }

  private static class SearchHandler implements SearchResponseHandler
  {
    long start = System.currentTimeMillis();
    int count = 0;
    public void handleSearchResultEntry(RawSearchResultEntry entry)
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

    public void handleSearchResultReference(RawSearchResultReference reference)
    {
      System.out.println(Thread.currentThread() + " " + reference);
    }

    public void handleResult(RawSearchResultDone result)
    {
      //System.out.println(Thread.currentThread() + " " + result);
    }

    public void handleException(Throwable t)
    {
      System.out.println(t);
    }
  }
}
