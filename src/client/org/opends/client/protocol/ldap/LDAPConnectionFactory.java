package org.opends.client.protocol.ldap;

import org.opends.common.protocols.ldap.AbstractLDAPTransport;
import org.opends.common.protocols.ldap.LDAPMessageHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.security.GeneralSecurityException;

import com.sun.grizzly.Connection;
import com.sun.grizzly.filterchain.Filter;
import com.sun.grizzly.streams.StreamReader;
import com.sun.grizzly.streams.StreamWriter;
import com.sun.grizzly.ssl.*;
import com.sun.grizzly.nio.transport.TCPNIOTransport;
import com.sun.grizzly.attributes.Attribute;

import javax.net.ssl.*;

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

  public RawConnection getConnection()
      throws IOException
  {
    Future<Connection> connFuture = tcpTransport.connect(socketAddress);

    Connection connection;
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

  protected LDAPMessageHandler getMessageHandler(Connection connection)
  {
    return ldapConnectionAttr.get(connection);
  }

  protected LDAPMessageHandler removeMessageHandler(Connection connection)
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
