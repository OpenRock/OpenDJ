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

package org.opends.sdk.ldap;



import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opends.sdk.Connection;
import org.opends.sdk.ConnectionFactory;
import org.opends.sdk.ConnectionFuture;
import org.opends.sdk.ConnectionResultHandler;
import org.opends.sdk.ErrorResultException;
import org.opends.sdk.InitializationException;
import org.opends.sdk.ResultCode;
import org.opends.sdk.responses.Responses;
import org.opends.sdk.responses.Result;
import org.opends.sdk.util.Validator;

import com.sun.grizzly.TransportFactory;
import com.sun.grizzly.attributes.Attribute;
import com.sun.grizzly.filterchain.Filter;
import com.sun.grizzly.nio.transport.TCPNIOTransport;
import com.sun.grizzly.ssl.BlockingSSLHandshaker;
import com.sun.grizzly.ssl.SSLEngineConfigurator;
import com.sun.grizzly.ssl.SSLFilter;
import com.sun.grizzly.ssl.SSLHandshaker;



/**
 * LDAP connection factory implementation.
 */
public final class LDAPConnectionFactory extends AbstractLDAPTransport
    implements ConnectionFactory
{
  private class CompletionHandlerAdapter implements
      com.sun.grizzly.CompletionHandler<com.sun.grizzly.Connection>
  {
    private final ConnectionResultHandler handler;



    private CompletionHandlerAdapter(ConnectionResultHandler handler)
    {
      this.handler = handler;
    }



    /**
     * {@inheritDoc}
     */
    public void cancelled(com.sun.grizzly.Connection connection)
    {
      // Ignore this.
    }



    /**
     * {@inheritDoc}
     */
    public void completed(com.sun.grizzly.Connection connection,
        com.sun.grizzly.Connection result)
    {
      try
      {
        handler.handleConnection(adaptConnection(connection));
      }
      catch (IOException e)
      {
        handler.handleConnectionError(adaptConnectionException(e));
      }
    }



    /**
     * {@inheritDoc}
     */
    public void failed(com.sun.grizzly.Connection connection,
        Throwable throwable)
    {
      handler
          .handleConnectionError(adaptConnectionException(throwable));
    }



    /**
     * {@inheritDoc}
     */
    public void updated(com.sun.grizzly.Connection connection,
        com.sun.grizzly.Connection result)
    {
      // Ignore this.
    }

  }



  private final class ConnectionFutureImpl implements ConnectionFuture
  {
    private final ErrorResultException error;
    private final Future<com.sun.grizzly.Connection> future;



    private ConnectionFutureImpl(ErrorResultException error)
    {
      this.future = null;
      this.error = error;
    }



    private ConnectionFutureImpl(
        Future<com.sun.grizzly.Connection> future)
    {
      this.future = future;
      this.error = null;
    }



    /**
     * {@inheritDoc}
     */
    public boolean cancel(boolean mayInterruptIfRunning)
    {
      if (error != null)
      {
        return false;
      }

      return future.cancel(mayInterruptIfRunning);
    }



    /**
     * {@inheritDoc}
     */
    public Connection get() throws InterruptedException,
        ErrorResultException
    {
      if (error != null)
      {
        throw error;
      }

      try
      {
        return adaptConnection(future.get());
      }
      catch (Exception e)
      {
        throw adaptConnectionException(e);
      }
    }



    /**
     * {@inheritDoc}
     */
    public Connection get(long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException,
        ErrorResultException
    {
      if (error != null)
      {
        throw error;
      }

      try
      {
        return adaptConnection(future.get(timeout, unit));
      }
      catch (Exception e)
      {
        throw adaptConnectionException(e);
      }
    }



    /**
     * {@inheritDoc}
     */
    public boolean isCancelled()
    {
      if (error != null)
      {
        return false;
      }

      return future.isCancelled();
    }



    /**
     * {@inheritDoc}
     */
    public boolean isDone()
    {
      if (error != null)
      {
        return true;
      }

      return future.isDone();
    }

  }

  private static final String LDAP_CONNECTION_OBJECT_ATTR =
      "LDAPConnAtr";

  private static TCPNIOTransport TCP_NIO_TRANSPORT = null;



  // FIXME: Need to figure out how this can be configured without
  // exposing internal implementation details to application.
  private static synchronized TCPNIOTransport getTCPNIOTransport()
  {
    if (TCP_NIO_TRANSPORT == null)
    {
      // Create a default transport using the Grizzly framework.
      //
      TCP_NIO_TRANSPORT =
          TransportFactory.getInstance().createTCPTransport();
      try
      {
        TCP_NIO_TRANSPORT.start();
      }
      catch (IOException e)
      {
        throw new RuntimeException(
            "Unable to create default connection factory provider", e);
      }

      Runtime.getRuntime().addShutdownHook(new Thread()
      {

        public void run()
        {
          try
          {
            TCP_NIO_TRANSPORT.stop();
          }
          catch (Exception e)
          {
            // Ignore.
          }

          try
          {
            TCP_NIO_TRANSPORT.getWorkerThreadPool().shutdown();
          }
          catch (Exception e)
          {
            // Ignore.
          }
        }

      });
    }
    return TCP_NIO_TRANSPORT;
  }

  private final Attribute<LDAPConnection> ldapConnectionAttr;

  private final InetSocketAddress socketAddress;

  private final SSLEngineConfigurator sslEngineConfigurator;
  private final SSLFilter sslFilter;

  private final SSLHandshaker sslHandshaker;

  private final TCPNIOTransport transport;



  /**
   * Creates a new connection factory which can be used to create
   * connections to the Directory Server at the provided host and port
   * address using default connection options.
   *
   * @param host
   *          The host name.
   * @param port
   *          The port number.
   * @throws InitializationException
   *           If a problem occurred while configuring the connection
   *           factory using the default options.
   * @throws NullPointerException
   *           If {@code host} was {@code null}.
   */
  public LDAPConnectionFactory(String host, int port)
      throws InitializationException, NullPointerException
  {
    this(host, port, null);
  }



  /**
   * Creates a new connection factory which can be used to create
   * connections to the Directory Server at the provided host and port
   * address using provided connection options.
   *
   * @param host
   *          The host name.
   * @param port
   *          The port number.
   * @param options
   *          The connection options to use when creating connections.
   * @throws InitializationException
   *           If a problem occurred while configuring the connection
   *           factory using the provided options.
   * @throws NullPointerException
   *           If {@code host} was {@code null}.
   */
  public LDAPConnectionFactory(String host, int port,
      LDAPConnectionOptions options) throws InitializationException,
      NullPointerException
  {
    this(host, port, options == null ? LDAPConnectionOptions
        .defaultOptions() : options, getTCPNIOTransport());
  }



  private LDAPConnectionFactory(String host, int port,
      LDAPConnectionOptions options, TCPNIOTransport transport)
      throws InitializationException
  {
    super(options, transport);

    Validator.ensureNotNull(host);

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



  public ConnectionFuture connect(ConnectionResultHandler handler)
  {
    CompletionHandlerAdapter adapter = null;
    if (handler != null)
    {
      adapter = new CompletionHandlerAdapter(handler);
    }

    try
    {
      return new ConnectionFutureImpl(transport.connect(socketAddress,
          adapter));
    }
    catch (IOException e)
    {
      ErrorResultException result = adaptConnectionException(e);
      return new ConnectionFutureImpl(result);
    }
  }



  ExecutorService getHandlerInvokers()
  {
    return transport.getWorkerThreadPool();
  }



  @Override
  LDAPMessageHandler getMessageHandler(
      com.sun.grizzly.Connection connection)
  {
    return ldapConnectionAttr.get(connection).getLDAPMessageHandler();
  }



  SSLEngineConfigurator getSSLEngineConfigurator()
  {
    return sslEngineConfigurator;
  }



  SSLFilter getSSLFilter()
  {
    return sslFilter;
  }



  SSLHandshaker getSSLHandshaker()
  {
    return sslHandshaker;
  }



  void removeMessageHandler(com.sun.grizzly.Connection connection)
  {
    ldapConnectionAttr.remove(connection);
  }



  private Connection adaptConnection(
      com.sun.grizzly.Connection connection) throws IOException
  {
    // Test shows that its much faster with non block writes but risk
    // running out of memory if the server is slow.
    connection.configureBlocking(true);
    connection.getStreamReader().setBlocking(true);
    connection.getStreamWriter().setBlocking(true);

    LDAPConnection ldapConnection =
        new LDAPConnection(connection, socketAddress,
            LDAPConnectionFactory.this);
    ldapConnectionAttr.set(connection, ldapConnection);
    return ldapConnection;
  }



  private ErrorResultException adaptConnectionException(Throwable t)
  {
    if (t instanceof ExecutionException)
    {
      t = t.getCause();
    }

    Result result =
        Responses.newResult(ResultCode.CLIENT_SIDE_CONNECT_ERROR)
            .setCause(t).setDiagnosticMessage(t.getMessage());
    return ErrorResultException.wrap(result);
  }
}
