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
import java.security.KeyManagementException;

import org.opends.server.util.Validator;
import org.opends.spi.ConnectionFactoryProvider;



/**
 *
 */
public final class Connections
{

  public static Connection connect(String host, int port)
      throws IOException, KeyManagementException
  {
    return newConnectionFactory(host, port).getConnection();
  }



  public static Connection connect(String host, int port,
      ConnectionOptions options) throws IOException,
      KeyManagementException
  {
    return newConnectionFactory(host, port, options).getConnection();
  }



  public static ConnectionFactory newConnectionFactory(String host,
      int port) throws KeyManagementException
  {
    return newConnectionFactory(host, port, null);
  }



  public static ConnectionFactory newConnectionFactory(String host,
      int port, ConnectionOptions options)
      throws KeyManagementException
  {
    Validator.ensureNotNull(host);

    if (options == null)
    {
      options = new ConnectionOptions();
    }

    // FIXME: how should we handle unsupported options?
    ConnectionFactory impl =
        ConnectionFactoryProvider.getConnectionFactory(host, port,
            options);

    return new Factory(impl);
  }



  private static final class Factory implements ConnectionFactory
  {

    private final ConnectionFactory pimpl;



    private Factory(ConnectionFactory pimpl)
    {
      this.pimpl = pimpl;
    }



    /**
     * {@inheritDoc}
     */
    public Connection getConnection() throws IOException
    {
      return pimpl.getConnection();
    }
  }

}
