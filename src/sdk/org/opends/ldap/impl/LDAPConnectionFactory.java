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

package org.opends.ldap.impl;



import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opends.ldap.CompletionHandler;
import org.opends.ldap.Connection;
import org.opends.ldap.ConnectionFactory;
import org.opends.ldap.ConnectionFuture;
import org.opends.ldap.ConnectionOptions;
import org.opends.ldap.responses.ErrorResultException;
import org.opends.ldap.responses.Responses;
import org.opends.ldap.responses.Result;
import org.opends.types.ResultCode;

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
    private final CompletionHandler<Connection> handler;



    private CompletionHandlerAdapter(
        CompletionHandler<Connection> handler)
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
        handler.completed(adaptConnection(connection));
      }
      catch (IOException e)
      {
        handler.failed(adaptConnectionException(e));
      }
    }



    /**
     * {@inheritDoc}
     */
    public void failed(com.sun.grizzly.Connection connection,
        Throwable throwable)
    {
      handler.failed(adaptConnectionException(throwable));
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



  public static final String LDAP_CONNECTION_OBJECT_ATTR =
      "LDAPConnAtr";
  public final Attribute<LDAPConnection> ldapConnectionAttr;
  private final InetSocketAddress socketAddress;

  private final SSLEngineConfigurator sslEngineConfigurator;
  private final SSLFilter sslFilter;

  private final SSLHandshaker sslHandshaker;

  private final TCPNIOTransport transport;



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



  public ConnectionFuture connect(CompletionHandler<Connection> handler)
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
