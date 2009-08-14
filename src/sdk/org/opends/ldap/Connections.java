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

package org.opends.ldap;



import java.io.IOException;

import org.opends.ldap.impl.LDAPConnectionFactoryProvider;
import org.opends.server.util.Validator;
import org.opends.spi.ConnectionFactoryProvider;

import com.sun.grizzly.TransportFactory;
import com.sun.grizzly.nio.transport.TCPNIOTransport;



/**
 *
 */
public final class Connections
{
  private static ConnectionFactoryProvider INSTANCE = null;



  public static synchronized void setProvider(
      ConnectionFactoryProvider provider) throws IllegalStateException
  {
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



  public static ConnectionFuture connect(String host, int port,
      CompletionHandler<Connection> handler)
      throws InitializationException
  {
    return newConnectionFactory(host, port).connect(handler);
  }



  public static ConnectionFuture connect(String host, int port,
      ConnectionOptions options, CompletionHandler<Connection> handler)
      throws InitializationException
  {
    return newConnectionFactory(host, port, options).connect(handler);
  }



  public static ConnectionFactory newConnectionFactory(String host,
      int port) throws InitializationException
  {
    return newConnectionFactory(host, port, null);
  }



  public static ConnectionFactory newConnectionFactory(String host,
      int port, ConnectionOptions options)
      throws InitializationException
  {
    Validator.ensureNotNull(host);

    if (options == null)
    {
      options = ConnectionOptions.defaultOptions();
    }

    // FIXME: how should we handle unsupported options?
    return getProvider().newConnectionFactory(host, port, options);
  }

}
