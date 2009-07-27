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



import java.security.KeyManagementException;

import org.opends.ldap.ConnectionFactory;
import org.opends.ldap.ConnectionOptions;
import org.opends.spi.ConnectionFactoryProvider;

import com.sun.grizzly.nio.transport.TCPNIOTransport;



/**
 *
 */
public final class LDAPConnectionFactoryProvider extends
    ConnectionFactoryProvider
{

  private final TCPNIOTransport transport;



  public LDAPConnectionFactoryProvider(TCPNIOTransport transport)
  {
    this.transport = transport;
  }



  /**
   * {@inheritDoc}
   */
  protected ConnectionFactory newConnectionFactory(String host,
      int port, ConnectionOptions options)
      throws KeyManagementException
  {
    return new LDAPConnectionFactory(host, port, options, transport);
  }

}
