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

package org.opends.sdk.requests;



import org.opends.sdk.util.Validator;
import org.opends.sdk.util.ByteString;



/**
 * Generic Bind request implementation.
 */
class GenericBindRequestImpl extends
    AbstractBindRequest<GenericBindRequest> implements
    GenericBindRequest
{
  private ByteString authenticationValue;
  private byte authenticationType;



  /**
   * Creates a new generic bind request using the provided bind DN,
   * authentication type, and authentication information.
   *
   * @param name
   *          The distinguished name of the Directory object that the
   *          client wishes to bind as (may be empty).
   * @param authenticationType
   *          The authentication mechanism identifier for this generic
   *          bind request.
   * @param authenticationBytes
   *          The authentication information for this generic bind
   *          request in a form defined by the authentication mechanism.
   * @throws NullPointerException
   *           If {@code name}, {@code authenticationType}, or {@code
   *           authenticationBytes} was {@code null}.
   */
  GenericBindRequestImpl(String name, byte authenticationType,
      ByteString authenticationBytes) throws NullPointerException
  {
    Validator.ensureNotNull(name, authenticationType,
        authenticationBytes);

    setName(name);
    this.authenticationType = authenticationType;
    this.authenticationValue = authenticationBytes;
  }



  /**
   * {@inheritDoc}
   */
  public ByteString getAuthenticationValue()
  {
    return authenticationValue;
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
  public GenericBindRequestImpl setAuthenticationValue(ByteString bytes)
      throws NullPointerException
  {
    Validator.ensureNotNull(bytes);

    this.authenticationValue = bytes;
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
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("GenericBindRequest(name=");
    builder.append(getName());
    builder.append(", authenticationType=");
    builder.append(authenticationType);
    builder.append(", authenticationValue=");
    builder.append(authenticationValue);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder.toString();
  }
}
