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

import org.opends.sdk.ConnectionFactory;
import org.opends.sdk.ConnectionFuture;
import org.opends.sdk.ConnectionResultHandler;
import org.opends.sdk.InitializationException;
import org.opends.sdk.spi.ConnectionFactoryProvider;
import org.opends.sdk.util.Validator;

import com.sun.grizzly.TransportFactory;
import com.sun.grizzly.nio.transport.TCPNIOTransport;



/**
 * This class contains various methods for creating connections to
 * Directory Servers.
 */
public final class Connections
{
  private static ConnectionFactoryProvider INSTANCE = null;



  /**
   * Sets the connection factory provider which should be used for LDAP
   * SDK.
   *
   * @param provider
   *          The connection factory provider to use.
   * @throws IllegalStateException
   *           If the connection factory provider has already been set.
   * @throws NullPointerException
   *           If {@code provider} was {@code null}.
   */
  public static synchronized void setProvider(
      ConnectionFactoryProvider provider) throws IllegalStateException,
      NullPointerException
  {
    Validator.ensureNotNull(provider);

    if (INSTANCE != null)
    {
      throw new IllegalStateException(
          "ConnectionFactoryProvider already set");
    }

    INSTANCE = provider;
  }



  private static synchronized ConnectionFactoryProvider getProvider()
  {
    if (INSTANCE == null)
    {
      // Create a default provider using the Grizzly framework.
      //
      // Different SDK implementations would provide a different
      // implementation of this method.
      //
      final TCPNIOTransport transport =
          TransportFactory.getInstance().createTCPTransport();
      try
      {
        transport.start();
      }
      catch (IOException e)
      {
        throw new RuntimeException(
            "Unable to create default connection factory provider", e);
      }

      INSTANCE = new LDAPConnectionFactoryProvider(transport);
      Runtime.getRuntime().addShutdownHook(new Thread()
      {

        public void run()
        {
          try
          {
            transport.stop();
          }
          catch (Exception e)
          {
            // Ignore.
          }

          try
          {
            transport.getWorkerThreadPool().shutdown();
          }
          catch (Exception e)
          {
            // Ignore.
          }
        }

      });
    }

    return INSTANCE;
  }



  /**
   * Connects to the Directory Server at the provided host and port
   * address using default connection options.
   *
   * @param host
   *          The host name.
   * @param port
   *          The port number.
   * @param handler
   *          A completion handler which can be used to asynchronously
   *          process the connection when it is successfully connects,
   *          may be {@code null}.
   * @return A future representing the connection.
   * @throws InitializationException
   *           If a problem occurred while configuring the connection
   *           parameters using the default options.
   * @throws NullPointerException
   *           If {@code host} was {@code null}.
   */
  public static ConnectionFuture connect(String host, int port,
      ConnectionResultHandler handler)
      throws InitializationException, NullPointerException
  {
    return newConnectionFactory(host, port).connect(handler);
  }



  /**
   * Connects to the Directory Server at the provided host and port
   * address using the provided connection options.
   *
   * @param host
   *          The host name.
   * @param port
   *          The port number.
   * @param options
   *          The connection options to use when creating the
   *          connection.
   * @param handler
   *          A completion handler which can be used to asynchronously
   *          process the connection when it is successfully connects,
   *          may be {@code null}.
   * @return A future representing the connection.
   * @throws InitializationException
   *           If a problem occurred while configuring the connection
   *           parameters using the provided options.
   * @throws NullPointerException
   *           If {@code host} was {@code null}.
   */
  public static ConnectionFuture connect(String host, int port,
      LDAPConnectionOptions options, ConnectionResultHandler handler)
      throws InitializationException, NullPointerException
  {
    return newConnectionFactory(host, port, options).connect(handler);
  }



  /**
   * Creates a new connection factory which can be used to create
   * connections to the Directory Server at the provided host and port
   * address using default connection options.
   *
   * @param host
   *          The host name.
   * @param port
   *          The port number.
   * @return The new connection factory.
   * @throws InitializationException
   *           If a problem occurred while configuring the connection
   *           factory using the default options.
   * @throws NullPointerException
   *           If {@code host} was {@code null}.
   */
  public static ConnectionFactory newConnectionFactory(String host,
      int port) throws InitializationException, NullPointerException
  {
    return newConnectionFactory(host, port, null);
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
   * @return The new connection factory.
   * @throws InitializationException
   *           If a problem occurred while configuring the connection
   *           factory using the provided options.
   * @throws NullPointerException
   *           If {@code host} was {@code null}.
   */
  public static ConnectionFactory newConnectionFactory(String host,
      int port, LDAPConnectionOptions options)
      throws InitializationException, NullPointerException
  {
    Validator.ensureNotNull(host);

    if (options == null)
    {
      options = LDAPConnectionOptions.defaultOptions();
    }

    // FIXME: how should we handle unsupported options?
    return getProvider().newConnectionFactory(host, port, options);
  }

}
