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


import javax.security.sasl.SaslException;

import org.opends.sdk.util.Validator;
import org.opends.sdk.util.ByteString;



/**
 * Anonymous SASL bind request.
 */
public final class AnonymousSASLBindRequest extends
    AbstractSASLBindRequest<AnonymousSASLBindRequest>
{
  /**
   * The name of the SASL mechanism that does not provide any authentication but
   * rather uses anonymous access.
   */
  public static final String SASL_MECHANISM_ANONYMOUS = "ANONYMOUS";

  private String traceString;



  public AnonymousSASLBindRequest()
  {
    this.traceString = "".intern();
  }



  public AnonymousSASLBindRequest(String traceString)
  {
    Validator.ensureNotNull(traceString);
    this.traceString = traceString;
  }



  public void dispose() throws SaslException
  {
    // Nothing needed.
  }



  public boolean evaluateCredentials(ByteString incomingCredentials)
      throws SaslException
  {
    // This is a single stage SASL bind.
    return true;
  }



  @Override
  public ByteString getSASLCredentials()
  {
    return ByteString.valueOf(traceString);
  }



  @Override
  public String getSASLMechanism()
  {
    return SASL_MECHANISM_ANONYMOUS;
  }



  public String getTraceString()
  {
    return traceString;
  }



  public void initialize(String serverName) throws SaslException
  {
    // Nothing to initialize.
  }



  public boolean isComplete()
  {
    return true;
  }



  public boolean isSecure()
  {
    return false;
  }



  public AnonymousSASLBindRequest setTraceString(String traceString)
  {
    Validator.ensureNotNull(traceString);
    this.traceString = traceString;
    return this;
  }



  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("AnonymousSASLBindRequest(bindDN=");
    builder.append(getName());
    builder.append(", authentication=SASL");
    builder.append(", saslMechanism=");
    builder.append(getSASLMechanism());
    builder.append(", traceString=");
    builder.append(traceString);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder.toString();
  }
}
