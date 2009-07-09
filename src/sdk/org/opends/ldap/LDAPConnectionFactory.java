package org.opends.ldap;



import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.opends.ldap.impl.AbstractLDAPTransport;
import org.opends.ldap.impl.LDAPMessageHandler;

import com.sun.grizzly.attributes.Attribute;
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
    this.ldapConnectionAttr =
        tcpTransport.getAttributeBuilder().createAttribute(
            LDAP_CONNECTION_OBJECT_ATTR);
    this.socketAddress = new InetSocketAddress(host, port);
  }



  public Connection getConnection() throws IOException
  {
    Future<com.sun.grizzly.Connection> connFuture =
        tcpTransport.connect(socketAddress);

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
    // running
    // out of memory if the server is slow.
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
    return tcpTransport.getWorkerThreadPool();
  }



  public SSLEngineConfigurator getSSLEngineConfigurator()
  {
    getSSLFilter();
    return sslEngineConfigurator;
  }



  public SSLFilter getSSLFilter()
  {
    if (sslFilter == null)
    {
      sslEngineConfigurator =
          new SSLEngineConfigurator(getSSLContext(), true, false, false);
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



  public void start() throws IOException
  {
    tcpTransport.start();
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
