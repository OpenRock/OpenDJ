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
import java.util.concurrent.*;

import org.opends.sdk.Connection;
import org.opends.sdk.ConnectionFactory;
import org.opends.sdk.ConnectionFuture;
import org.opends.sdk.ConnectionResultHandler;
import org.opends.sdk.ErrorResultException;
import org.opends.sdk.InitializationException;
import org.opends.sdk.ResultCode;
import org.opends.sdk.extensions.StartTLSRequest;
import org.opends.sdk.responses.*;
import org.opends.sdk.util.Validator;

import com.sun.grizzly.TransportFactory;
import com.sun.grizzly.filterchain.Filter;
import com.sun.grizzly.ssl.SSLHandshaker;
import com.sun.grizzly.ssl.SSLFilter;
import com.sun.grizzly.ssl.SSLEngineConfigurator;
import com.sun.grizzly.ssl.BlockingSSLHandshaker;
import com.sun.grizzly.attributes.Attribute;
import com.sun.grizzly.nio.transport.TCPNIOTransport;

import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;


/**
 * LDAP connection factory implementation.
 */
public final class LDAPConnectionFactory extends AbstractLDAPTransport
    implements ConnectionFactory
{
  private static class FailedImpl implements ConnectionFuture
  {
    private volatile ErrorResultException exception;

    private FailedImpl(ErrorResultException exception) {
      this.exception = exception;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
    }

    public Connection get() throws InterruptedException, ErrorResultException {
      throw exception;
    }

    public Connection get(long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException, ErrorResultException {
      throw exception;
    }

    public boolean isCancelled() {
      return false;
    }

    public boolean isDone() {
      return false;
    }
  }

  private class ConnectionFutureImpl implements ConnectionFuture,
      com.sun.grizzly.CompletionHandler<com.sun.grizzly.Connection>,
      ResultHandler<Result>
  {
    private volatile Connection connection;
    private volatile ErrorResultException exception;
    private volatile Future<com.sun.grizzly.Connection> connectFuture;
    private volatile Future sslFuture;
    private final CountDownLatch latch = new CountDownLatch(1);
    private final ConnectionResultHandler handler;
    private boolean cancelled;

    private ConnectionFutureImpl(ConnectionResultHandler handler) {
      this.handler = handler;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
      cancelled = connectFuture.cancel(mayInterruptIfRunning) ||
          sslFuture != null && sslFuture.cancel(mayInterruptIfRunning);
      if(cancelled)
      {
        latch.countDown();
      }
      return cancelled;
    }

    public Connection get() throws InterruptedException, ErrorResultException {
      latch.await();
      if(cancelled)
      {
        throw new CancellationException();
      }
      if(exception != null)
      {
        throw exception;
      }
      return connection;
    }

    public Connection get(long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException, ErrorResultException {
      latch.await(timeout, unit);
      if(cancelled)
      {
        throw new CancellationException();
      }
      if(exception != null)
      {
        throw exception;
      }
      return connection;
    }

    public boolean isCancelled() {
      return cancelled;
    }

    public boolean isDone() {
      return latch.getCount() == 0;
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
      LDAPConnection ldapConn = adaptConnection(connection);
      this.connection = ldapConn;

      if(sslContext != null && useStartTLS)
      {
        StartTLSRequest startTLS =
            new StartTLSRequest(sslContext);
        sslFuture = ldapConn.extendedRequest(startTLS, this);
      }
      else if(sslContext != null)
      {
        try
        {
          ldapConn.installFilter(sslFilter);
          ldapConn.performSSLHandshake(sslHandshaker, sslEngineConfigurator);
          latch.countDown();
          if(handler != null)
          {
            handler.handleConnection(ldapConn);
          }
        }
        catch(CancellationException ce)
        {
          // Handshake cancelled.
          latch.countDown();
        }
        catch(ErrorResultException throwable)
        {
          exception = throwable;
          latch.countDown();
          if(handler != null)
          {
            handler.handleConnectionError(exception);
          }
        }
      }
      else
      {
        latch.countDown();
        if(handler != null)
        {
          handler.handleConnection(ldapConn);
        }
      }
    }



    /**
     * {@inheritDoc}
     */
    public void failed(com.sun.grizzly.Connection connection,
                       Throwable throwable)
    {
      exception = adaptConnectionException(throwable);
      latch.countDown();
      if(handler != null)
      {
        handler.handleConnectionError(exception);
      }
    }



    /**
     * {@inheritDoc}
     */
    public void updated(com.sun.grizzly.Connection connection,
                        com.sun.grizzly.Connection result)
    {
      // Ignore this.
    }

    // This is called when the StartTLS request is successful
    public void handleResult(Result result) {
      latch.countDown();
      if(handler != null)
      {
        handler.handleConnection(connection);
      }
    }

    // This is called when the StartTLS request is not successful
    public void handleError(ErrorResultException error) {
      exception = error;
      latch.countDown();
      if(handler != null)
      {
        handler.handleConnectionError(exception);
      }
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

  private final TCPNIOTransport transport;

  private final boolean useStartTLS;

  private final SSLHandshaker sslHandshaker = new BlockingSSLHandshaker();
  private final SSLEngineConfigurator sslEngineConfigurator;
  private final SSLContext sslContext;
  private final SSLFilter sslFilter;

  /**
   * Creates a new connection factory which can be used to create
   * connections to the Directory Server at the provided host and port
   * address using provided connection options.
   *
   * @param host
   *          The host name.
   * @param port
   *          The port number.
   *          The connection options to use when creating connections.
   * @throws InitializationException
   *           If a problem occurred while configuring the connection
   *           factory using the provided options.
   * @throws NullPointerException
   *           If {@code host} was {@code null}.
   */
  public LDAPConnectionFactory(String host, int port)
      throws InitializationException, NullPointerException
  {
    this(host, port, getTCPNIOTransport());
  }



  private LDAPConnectionFactory(String host, int port,
                                TCPNIOTransport transport)
      throws InitializationException
  {
    super();

    Validator.ensureNotNull(host);

    this.transport = transport;
    this.ldapConnectionAttr =
        transport.getAttributeBuilder().createAttribute(
            LDAP_CONNECTION_OBJECT_ATTR);
    this.socketAddress = new InetSocketAddress(host, port);
    this.sslContext = null;
    this.sslFilter = null;
    this.sslEngineConfigurator = null;
    this.useStartTLS = false;
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
   *          The connection options to use when creating connections.
   * @throws InitializationException
   *           If a problem occurred while configuring the connection
   *           factory using the provided options.
   * @throws NullPointerException
   *           If {@code host} was {@code null}.
   */
  public LDAPConnectionFactory(String host, int port, SSLContext sslContext,
                               boolean useStartTLS)
      throws InitializationException, NullPointerException
  {
    this(host, port, getTCPNIOTransport(), sslContext, useStartTLS);
  }



  private LDAPConnectionFactory(String host, int port,
                                TCPNIOTransport transport,
                                SSLContext sslContext,
                                boolean useStartTLS)
      throws InitializationException
  {
    super();

    Validator.ensureNotNull(host, transport, sslContext);

    this.transport = transport;
    this.ldapConnectionAttr =
        transport.getAttributeBuilder().createAttribute(
            LDAP_CONNECTION_OBJECT_ATTR);
    this.socketAddress = new InetSocketAddress(host, port);
    this.sslContext = sslContext;
    sslEngineConfigurator =
        new SSLEngineConfigurator(sslContext, true, false, false);
    sslFilter = new SSLFilter(sslEngineConfigurator, sslHandshaker);
    this.useStartTLS = useStartTLS;
  }



  public ConnectionFuture connect(ConnectionResultHandler handler)
  {
    ConnectionFutureImpl future = new ConnectionFutureImpl(handler);

    try
    {
      future.connectFuture = transport.connect(socketAddress, future);
      return future;
    }
    catch (IOException e)
    {
      ErrorResultException result = adaptConnectionException(e);
      return new FailedImpl(result);
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



  void removeMessageHandler(com.sun.grizzly.Connection connection)
  {
    ldapConnectionAttr.remove(connection);
  }

  SSLHandshaker getSslHandshaker() {
    return sslHandshaker;
  }

  SSLFilter getSSlFilter() {
    return sslFilter;
  }

  SSLContext getSSLContext() {
    return sslContext;
  }

  SSLEngineConfigurator getSSlEngineConfigurator() {
    return sslEngineConfigurator;
  }

  private LDAPConnection adaptConnection(
      com.sun.grizzly.Connection connection)
  {
    // Test shows that its much faster with non block writes but risk
    // running out of memory if the server is slow.
    connection.configureBlocking(true);
    connection.getStreamReader().setBlocking(true);
    connection.getStreamWriter().setBlocking(true);
    connection.setProcessor(
        getDefaultFilterChainFactory().getFilterChainPattern());

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
