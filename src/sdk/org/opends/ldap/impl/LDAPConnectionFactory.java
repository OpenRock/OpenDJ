package org.opends.ldap.impl;



import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.opends.ldap.Connection;
import org.opends.ldap.ConnectionFactory;
import org.opends.ldap.ConnectionOptions;

import com.sun.grizzly.attributes.Attribute;
import com.sun.grizzly.filterchain.Filter;
import com.sun.grizzly.nio.transport.TCPNIOTransport;
import com.sun.grizzly.ssl.BlockingSSLHandshaker;
import com.sun.grizzly.ssl.SSLEngineConfigurator;
import com.sun.grizzly.ssl.SSLFilter;
import com.sun.grizzly.ssl.SSLHandshaker;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 27, 2009 Time:
 * 9:50:43 AM To change this template use File | Settings | File
 * Templates.
 */
public class LDAPConnectionFactory extends AbstractLDAPTransport
    implements ConnectionFactory
{
  public static final String LDAP_CONNECTION_OBJECT_ATTR =
      "LDAPConnAtr";
  public final Attribute<LDAPConnection> ldapConnectionAttr;

  private final SSLEngineConfigurator sslEngineConfigurator;
  private final SSLFilter sslFilter;
  private final SSLHandshaker sslHandshaker;

  private final TCPNIOTransport transport;
  private final InetSocketAddress socketAddress;



  public LDAPConnectionFactory(String host, int port,
      ConnectionOptions options, TCPNIOTransport transport)
      throws KeyManagementException
  {
    super(options, transport);

    this.transport = transport;
    this.ldapConnectionAttr =
        transport.getAttributeBuilder().createAttribute(
            LDAP_CONNECTION_OBJECT_ATTR);
    this.socketAddress = new InetSocketAddress(host, port);

    this.sslEngineConfigurator =
        new SSLEngineConfigurator(getSSLContext(), true, false, false);
    this.sslHandshaker = new BlockingSSLHandshaker();
    this.sslFilter =
        new SSLFilter(sslEngineConfigurator, sslHandshaker);

    if (options.useSSL())
    {
      // Install the SSLFilter in the default filter chain
      Filter oldFilter =
          getDefaultFilterChainFactory().getFilterChainPattern()
              .remove(2);
      getDefaultFilterChainFactory().getFilterChainPattern().add(
          getSSLFilter());
      if (!(oldFilter instanceof SSLFilter))
      {
        getDefaultFilterChainFactory().getFilterChainPattern().add(
            oldFilter);
      }
    }
  }



  public Connection getConnection() throws IOException
  {
    Future<com.sun.grizzly.Connection> connFuture =
        transport.connect(socketAddress);

    com.sun.grizzly.Connection connection;
    try
    {
      connection = connFuture.get();
    }
    catch (InterruptedException ie)
    {
      throw new IOException("Interrupted!");
    }
    catch (ExecutionException ee)
    {
      if (ee.getCause() instanceof IOException)
      {
        throw (IOException) ee.getCause();
      }

      throw new IOException("Got an error:" + ee.getCause());
    }
    catch (CancellationException ce)
    {
      throw new IOException("Cancelled!");
    }

    // Test shows that its much faster with non block writes but risk
    // running out of memory if the server is slow.
    connection.configureBlocking(true);
    connection.getStreamReader().setBlocking(true);
    connection.getStreamWriter().setBlocking(true);

    LDAPConnection ldapConnection =
        new LDAPConnection(connection, socketAddress, this);
    ldapConnectionAttr.set(connection, ldapConnection);
    return ldapConnection;
  }



  public ExecutorService getHandlerInvokers()
  {
    return transport.getWorkerThreadPool();
  }



  public SSLEngineConfigurator getSSLEngineConfigurator()
  {
    return sslEngineConfigurator;
  }



  public SSLFilter getSSLFilter()
  {
    return sslFilter;
  }



  public SSLHandshaker getSSLHandshaker()
  {
    return sslHandshaker;
  }



  @Override
  protected LDAPMessageHandler getMessageHandler(
      com.sun.grizzly.Connection connection)
  {
    return ldapConnectionAttr.get(connection);
  }



  @Override
  protected LDAPMessageHandler removeMessageHandler(
      com.sun.grizzly.Connection connection)
  {
    return ldapConnectionAttr.remove(connection);
  }
}
