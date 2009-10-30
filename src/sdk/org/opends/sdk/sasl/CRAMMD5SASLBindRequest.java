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

import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

import org.opends.sdk.DN;
import org.opends.sdk.util.Validator;
import org.opends.sdk.util.ByteString;



/**
 * CRAM-MD5 SASL bind request.
 */
public final class CRAMMD5SASLBindRequest extends
    AbstractSASLBindRequest<CRAMMD5SASLBindRequest>
{
  /**
   * The name of the SASL mechanism based on CRAM-MD5 authentication.
   */
  static final String SASL_MECHANISM_CRAM_MD5 = "CRAM-MD5";

  private SaslClient saslClient;
  private ByteString outgoingCredentials = null;

  private String authenticationID;
  private ByteString password;

  private NameCallbackHandler authIDHandler;
  private PasswordCallbackHandler passHandler;



  public CRAMMD5SASLBindRequest(DN authenticationDN, ByteString password)
  {
    Validator.ensureNotNull(authenticationDN, password);
    this.authenticationID = "dn:" + authenticationDN.toString();
    this.password = password;
  }



  public CRAMMD5SASLBindRequest(String authenticationID,
      ByteString password)
  {
    Validator.ensureNotNull(authenticationID, password);
    this.authenticationID = authenticationID;
    this.password = password;
  }



  public void dispose() throws SaslException
  {
    saslClient.dispose();
  }



  public boolean evaluateCredentials(ByteString incomingCredentials)
      throws SaslException
  {
    byte[] bytes =
        saslClient.evaluateChallenge(incomingCredentials.toByteArray());
    if (bytes != null)
    {
      this.outgoingCredentials = ByteString.wrap(bytes);
    }
    else
    {
      this.outgoingCredentials = null;
    }

    return isComplete();
  }



  public String getAuthenticationID()
  {
    return authenticationID;
  }



  public NameCallbackHandler getAuthIDHandler()
  {
    return authIDHandler;
  }



  public PasswordCallbackHandler getPassHandler()
  {
    return passHandler;
  }



  public ByteString getPassword()
  {
    return password;
  }



  @Override
  public ByteString getSASLCredentials()
  {
    return outgoingCredentials;
  }



  @Override
  public String getSASLMechanism()
  {
    return saslClient.getMechanismName();
  }



  public void initialize(String serverName) throws SaslException
  {
    saslClient =
        Sasl.createSaslClient(new String[] { SASL_MECHANISM_CRAM_MD5 },
            null, SASL_DEFAULT_PROTOCOL, serverName, null, this);

    if (saslClient.hasInitialResponse())
    {
      byte[] bytes = saslClient.evaluateChallenge(new byte[0]);
      if (bytes != null)
      {
        this.outgoingCredentials = ByteString.wrap(bytes);
      }
    }
  }



  public boolean isComplete()
  {
    return saslClient.isComplete();
  }



  public boolean isSecure()
  {
    return false;
  }



  public CRAMMD5SASLBindRequest setAuthenticationID(
      String authenticationID)
  {
    Validator.ensureNotNull(authenticationID);
    this.authenticationID = authenticationID;
    return this;
  }



  public CRAMMD5SASLBindRequest setAuthIDHandler(
      NameCallbackHandler authIDHandler)
  {
    this.authIDHandler = authIDHandler;
    return this;
  }



  public CRAMMD5SASLBindRequest setPassHandler(
      PasswordCallbackHandler passHandler)
  {
    this.passHandler = passHandler;
    return this;
  }



  public CRAMMD5SASLBindRequest setPassword(ByteString password)
  {
    Validator.ensureNotNull(password);
    this.password = password;
    return this;
  }



  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("CRAMMD5SASLBindRequest(bindDN=");
    builder.append(getName());
    builder.append(", authentication=SASL");
    builder.append(", saslMechanism=");
    builder.append(getSASLMechanism());
    builder.append(", authenticationID=");
    builder.append(authenticationID);
    builder.append(", password=");
    builder.append(password);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder.toString();
  }



  @Override
  protected void handle(NameCallback callback)
      throws UnsupportedCallbackException
  {
    if (authIDHandler == null)
    {
      callback.setName(authenticationID);
    }
    else
    {
      authIDHandler.handle(callback);
    }
  }



  @Override
  protected void handle(PasswordCallback callback)
      throws UnsupportedCallbackException
  {
    if (passHandler == null)
    {
      callback.setPassword(password.toString().toCharArray());
    }
    else
    {
      passHandler.handle(callback);
    }
  }
}
