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

package org.opends.sdk.sasl;



import org.opends.sdk.util.Validator;
import org.opends.sdk.util.ByteString;



/**
 * Generic SASL bind request.
 */
public class GenericSASLBindRequest extends
    SASLBindRequest<GenericSASLBindRequest>
{
  // The SASL credentials.
  private ByteString saslCredentials;

  // The SASL mechanism.
  private String saslMechanism;



  public GenericSASLBindRequest(String saslMechanism,
      ByteString saslCredentials)
  {
    Validator.ensureNotNull(saslMechanism);
    this.saslCredentials = saslCredentials;
    this.saslMechanism = saslMechanism;
  }



  /**
   * Returns the SASL credentials for this bind request.
   *
   * @return The SASL credentials for this bind request, or {@code null}
   *         if there are none or if the bind does not use SASL
   *         authentication.
   */
  @Override
  public ByteString getSASLCredentials()
  {
    return saslCredentials;
  }



  /**
   * Returns the SASL mechanism for this bind request.
   *
   * @return The SASL mechanism for this bind request, or {@code null}
   *         if there are none or if the bind does not use SASL
   *         authentication.
   */
  @Override
  public String getSASLMechanism()
  {
    return saslMechanism;
  }



  /**
   * Sets the SASL credentials for this bind request.
   *
   * @param saslCredentials
   *          The SASL credentials for this bind request, or {@code
   *          null} if there are none or if the bind does not use SASL
   *          authentication.
   * @return This raw bind request.
   */
  public GenericSASLBindRequest setSASLCredentials(
      ByteString saslCredentials)
  {
    this.saslCredentials = saslCredentials;
    return this;
  }



  /**
   * Sets The SASL mechanism for this bind request.
   *
   * @param saslMechanism
   *          The SASL mechanism for this bind request, or {@code null}
   *          if there are none or if the bind does not use SASL
   *          authentication.
   * @return This raw bind request.
   */
  public GenericSASLBindRequest setSASLMechanism(String saslMechanism)
  {
    Validator.ensureNotNull(saslMechanism);
    this.saslMechanism = saslMechanism;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("SASLBindRequest(bindDN=");
    builder.append(getName());
    builder.append(", authentication=SASL");
    builder.append(", saslMechanism=");
    builder.append(saslMechanism);
    builder.append(", saslCredentials=");
    builder.append(saslCredentials);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder.toString();
  }
}
