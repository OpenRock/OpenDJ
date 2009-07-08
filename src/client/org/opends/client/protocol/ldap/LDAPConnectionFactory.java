package org.opends.client.protocol.ldap;

import org.opends.common.protocols.ldap.AbstractLDAPTransport;
import org.opends.common.protocols.ldap.LDAPMessageHandler;
import org.opends.client.spi.Connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;

import com.sun.grizzly.ssl.*;
import com.sun.grizzly.nio.transport.TCPNIOTransport;
import com.sun.grizzly.attributes.Attribute;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 27, 2009 Time: 9:50:43
 * AM To change this template use File | Settings | File Templates.
 */
public class LDAPConnectionFactory extends AbstractLDAPTransport 
    implements ConnectionFactory
{
  public static final String LDAP_CONNECTION_OBJECT_ATTR = "LDAPConnAtr";
  public final Attribute<LDAPConnection> ldapConnectionAttr;

  private SSLEngineConfigurator sslEngineConfigurator;
  private SSLFilter sslFilter;
  private SSLHandshaker sslHandshaker;

  protected TCPNIOTransport tcpTransport;
  protected InetSocketAddress socketAddress;

  public LDAPConnectionFactory(String host, int port,
                               TCPNIOTransport tcpTransport)
  {
    super(tcpTransport);
    this.tcpTransport = tcpTransport;
    this.ldapConnectionAttr = tcpTransport.getAttributeBuilder().
        createAttribute(LDAP_CONNECTION_OBJECT_ATTR);
    this.socketAddress = new InetSocketAddress(host, port);
  }

  public void start() throws IOException
  {
    tcpTransport.start();
  }

  public Connection getConnection()
      throws IOException
  {
    Future<com.sun.grizzly.Connection> connFuture = tcpTransport.connect(socketAddress);

    com.sun.grizzly.Connection connection;
    try
    {
      connection = connFuture.get();
    }
    catch(InterruptedException ie)
    {
      throw new IOException("Interrupted!");
    }
    catch(ExecutionException ee)
    {
      if(ee.getCause() instanceof IOException)
      {
        throw (IOException)ee.getCause();
      }

      throw new IOException("Got an error:" + ee.getCause());
    }
    catch(CancellationException ce)
    {
      throw new IOException("Cancelled!");
    }

    // Test shows that its much faster with non block writes but risk running
    // out of memory if the server is slow.
    connection.configureBlocking(true);
    connection.getStreamReader().setBlocking(true);
    connection.getStreamWriter().setBlocking(true);

    LDAPConnection ldapConnection =
        new LDAPConnection(connection, socketAddress, this);
    ldapConnectionAttr.set(connection, ldapConnection);
    return ldapConnection;
  }

  protected LDAPMessageHandler getMessageHandler(com.sun.grizzly.Connection connection)
  {
    return ldapConnectionAttr.get(connection);
  }

  protected LDAPMessageHandler removeMessageHandler(com.sun.grizzly.Connection connection)
  {
    return ldapConnectionAttr.remove(connection);
  }

  public ExecutorService getHandlerInvokers()
  {
    return tcpTransport.getWorkerThreadPool();
  }

  public SSLFilter getSSLFilter()
  {
    if(sslFilter == null)
    {
      sslEngineConfigurator = new SSLEngineConfigurator(getSSLContext(),
                                                        true, false, false);
      sslFilter = new SSLFilter(sslEngineConfigurator, sslHandshaker);
      sslHandshaker = new BlockingSSLHandshaker();
    }

    return sslFilter;
  }

  public SSLHandshaker getSSLHandshaker()
  {
    getSSLFilter();
    return sslHandshaker;
  }

  public SSLEngineConfigurator getSSLEngineConfigurator()
  {
    getSSLFilter();
    return sslEngineConfigurator;
  }
}
