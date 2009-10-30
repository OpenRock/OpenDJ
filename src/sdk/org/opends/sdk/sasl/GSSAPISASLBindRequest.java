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



import static org.opends.messages.ExtensionMessages.ERR_SASL_CONTEXT_CREATE_ERROR;
import static org.opends.messages.ExtensionMessages.ERR_SASL_PROTOCOL_ERROR;
import static org.opends.sdk.util.StaticUtils.getExceptionMessage;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

import org.opends.messages.Message;
import org.opends.sdk.DN;
import org.opends.sdk.util.Validator;
import org.opends.sdk.util.ByteString;



/**
 * GSSAPI SASL bind request.
 */
public final class GSSAPISASLBindRequest extends
    AbstractSASLBindRequest<GSSAPISASLBindRequest>
{
  /**
   * The name of the SASL mechanism based on GSS-API authentication.
   */
  static final String SASL_MECHANISM_GSSAPI = "GSSAPI";

  private SaslClient saslClient;
  private ByteString outgoingCredentials = null;

  private final Subject subject;
  private String authorizationID;

  private ByteString incomingCredentials;
  private String serverName;

  PrivilegedExceptionAction<Boolean> evaluateAction =
      new PrivilegedExceptionAction<Boolean>()
      {
        public Boolean run() throws Exception
        {
          byte[] bytes =
              saslClient.evaluateChallenge(incomingCredentials
                  .toByteArray());
          if (bytes != null)
          {
            outgoingCredentials = ByteString.wrap(bytes);
          }
          else
          {
            outgoingCredentials = null;
          }

          return isComplete();
        }
      };

  PrivilegedExceptionAction<Object> invokeAction =
      new PrivilegedExceptionAction<Object>()
      {
        public Object run() throws Exception
        {
          saslClient =
              Sasl.createSaslClient(
                  new String[] { SASL_MECHANISM_GSSAPI },
                  authorizationID, SASL_DEFAULT_PROTOCOL, serverName,
                  null, GSSAPISASLBindRequest.this);

          if (saslClient.hasInitialResponse())
          {
            byte[] bytes = saslClient.evaluateChallenge(new byte[0]);
            if (bytes != null)
            {
              outgoingCredentials = ByteString.wrap(bytes);
            }
          }
          return null;
        }
      };



  public GSSAPISASLBindRequest(Subject subject)
  {
    Validator.ensureNotNull(subject);
    this.subject = subject;
  }



  public GSSAPISASLBindRequest(Subject subject, DN authorizationDN)
  {
    Validator.ensureNotNull(subject, authorizationDN);
    this.subject = subject;
  }



  public GSSAPISASLBindRequest(Subject subject, String authorizationID)
  {
    Validator.ensureNotNull(subject, authorizationID);
    this.subject = subject;
  }



  public void dispose() throws SaslException
  {
    saslClient.dispose();
  }



  /**
   * Override so the Sasl client can be used as the subject.
   *
   * @param incomingCredentials
   * @return
   * @throws SaslException
   */
  public boolean evaluateCredentials(ByteString incomingCredentials)
      throws SaslException
  {
    this.incomingCredentials = incomingCredentials;
    try
    {
      return Subject.doAs(subject, evaluateAction);
    }
    catch (PrivilegedActionException e)
    {
      if (e.getCause() instanceof SaslException)
      {
        throw (SaslException) e.getCause();
      }

      // This should not happen. Must be a bug.
      Message msg =
          ERR_SASL_PROTOCOL_ERROR.get(SASL_MECHANISM_GSSAPI,
              getExceptionMessage(e));
      throw new SaslException(msg.toString(), e.getCause());
    }
  }



  public String getAuthorizationID()
  {
    return authorizationID;
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



  /**
   * Override so the Sasl client can be initialized as the subject.
   *
   * @param serverName
   * @throws SaslException
   */
  public void initialize(String serverName) throws SaslException
  {
    this.serverName = serverName;

    try
    {
      Subject.doAs(subject, invokeAction);
    }
    catch (PrivilegedActionException e)
    {
      if (e.getCause() instanceof SaslException)
      {
        throw (SaslException) e.getCause();
      }

      // This should not happen. Must be a bug.
      Message msg =
          ERR_SASL_CONTEXT_CREATE_ERROR.get(SASL_MECHANISM_GSSAPI,
              getExceptionMessage(e));
      throw new SaslException(msg.toString(), e.getCause());
    }
  }



  public boolean isComplete()
  {
    return saslClient.isComplete();
  }



  public boolean isSecure()
  {
    String qop = (String) saslClient.getNegotiatedProperty(Sasl.QOP);
    return (qop.equalsIgnoreCase("auth-int") || qop
        .equalsIgnoreCase("auth-conf"));
  }



  public GSSAPISASLBindRequest setAuthorizationID(String authorizationID)
  {
    this.authorizationID = authorizationID;
    return this;
  }



  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("GSSAPISASLBindRequest(bindDN=");
    builder.append(getName());
    builder.append(", authentication=SASL");
    builder.append(", saslMechanism=");
    builder.append(getSASLMechanism());
    builder.append(", subject=");
    builder.append(subject);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder.toString();
  }
}
