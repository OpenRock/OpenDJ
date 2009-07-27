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

package org.opends.spi;



import java.security.KeyManagementException;

import org.opends.ldap.ConnectionFactory;
import org.opends.ldap.ConnectionOptions;



/**
 *
 */
public abstract class ConnectionFactoryProvider
{
  private static ConnectionFactoryProvider INSTANCE = null;



  public static synchronized void setInstance(
      ConnectionFactoryProvider provider)
  {
    INSTANCE = provider;
  }



  public static synchronized ConnectionFactory getConnectionFactory(
      String host, int port, ConnectionOptions options)
      throws KeyManagementException
  {
    if (INSTANCE == null)
    {
      throw new IllegalStateException(
          "No LDAPConnectionFactoryProvider registered");
    }

    // FIXME: how should we handle unsupported options?
    return INSTANCE.newConnectionFactory(host, port, options);
  }



  protected abstract ConnectionFactory newConnectionFactory(
      String host, int port, ConnectionOptions options)
      throws KeyManagementException;

}
