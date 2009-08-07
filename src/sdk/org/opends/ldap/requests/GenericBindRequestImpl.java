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

package org.opends.ldap.requests;



import org.opends.server.types.ByteString;
import org.opends.spi.AbstractBindRequest;
import org.opends.util.Validator;



/**
 * Generic Bind request implementation.
 */
class GenericBindRequestImpl extends
    AbstractBindRequest<GenericBindRequest> implements
    GenericBindRequest
{
  private ByteString authenticationBytes;
  private byte authenticationType;



  /**
   * Creates a new generic bind request using the provided bind DN,
   * authentication type, and authentication information.
   *
   * @param dn
   *          The name of the Directory object that the client wishes to
   *          bind as (may be empty).
   * @param type
   *          The authentication mechanism identifier for this generic
   *          bind request.
   * @param bytes
   *          The authentication information for this generic bind
   *          request in a form defined by the authentication mechanism.
   * @throws NullPointerException
   *           If {@code authenticationType} or {@code
   *           authenticationBytes} was {@code null}.
   */
  GenericBindRequestImpl(String dn, byte type, ByteString bytes)
      throws NullPointerException
  {
    Validator.ensureNotNull(dn, type, bytes);

    setBindDN(dn);
    this.authenticationType = type;
    this.authenticationBytes = bytes;
  }



  /**
   * {@inheritDoc}
   */
  public ByteString getAuthenticationBytes()
  {
    return authenticationBytes;
  }



  /**
   * {@inheritDoc}
   */
  public byte getAuthenticationType()
  {
    return authenticationType;
  }



  /**
   * {@inheritDoc}
   */
  public GenericBindRequestImpl setAuthenticationBytes(ByteString bytes)
      throws NullPointerException
  {
    Validator.ensureNotNull(bytes);

    this.authenticationBytes = bytes;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public GenericBindRequestImpl setAuthenticationType(byte type)
  {
    this.authenticationType = type;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public StringBuilder toString(StringBuilder builder)
      throws NullPointerException
  {
    builder.append("GenericBindRequest(bindDN=");
    builder.append(getBindDN());
    builder.append(", authenticationType=");
    builder.append(authenticationType);
    builder.append(", authenticationBytes=");
    builder.append(authenticationBytes);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder;
  }
}
