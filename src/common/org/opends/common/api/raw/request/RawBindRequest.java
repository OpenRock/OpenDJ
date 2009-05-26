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

package org.opends.common.api.raw.request;



import org.opends.server.types.AuthenticationType;
import org.opends.server.types.ByteString;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.OperationType;
import org.opends.server.util.Validator;
import org.opends.server.core.operations.BindRequest;
import org.opends.server.core.operations.Schema;


/**
 * A raw bind request.
 */
public final class RawBindRequest extends RawRequest
{

  // The authentication type.
  private AuthenticationType authenticationType =
      AuthenticationType.SIMPLE;

  // The bind DN.
  private ByteString bindDN = ByteString.empty();

  // The string representation of the protocol version.
  private String protocolVersion;

  // The SASL credentials.
  private ByteString saslCredentials = null;

  // The SASL mechanism.
  private String saslMechanism = null;

  // The simple password.
  private ByteString simplePassword = null;



  /**
   * Creates a new raw bind request using the provided protocol version.
   * <p>
   * The new raw bind request will contain an empty list of controls and
   * default to anonymous authentication.
   *
   * @param protocolVersion
   *          The string representation of the protocol version.
   */
  public RawBindRequest(String protocolVersion)
  {
    super(OperationType.BIND);
    Validator.ensureNotNull(protocolVersion);
    this.protocolVersion = protocolVersion;
  }



  /**
   * Returns the authentication type for this bind request.
   *
   * @return The authentication type for this bind request.
   */
  public AuthenticationType getAuthenticationType()
  {
    return authenticationType;
  }



  /**
   * Returns the raw, unprocessed bind DN for this bind request as
   * contained in the client request.
   * <p>
   * The value may not actually contain a valid DN, as no validation
   * will have been performed.
   *
   * @return The raw, unprocessed bind DN for this bind request as
   *         contained in the client request.
   */
  public ByteString getBindDN()
  {
    return bindDN;
  }



  /**
   * Returns a string representation of the protocol version associated
   * with this bind request.
   *
   * @return A string representation of the protocol version associated
   *         with this bind request.
   */
  public String getProtocolVersion()
  {
    return protocolVersion;
  }



  /**
   * Returns the SASL credentials for this bind request.
   *
   * @return The SASL credentials for this bind request, or {@code null}
   *         if there are none or if the bind does not use SASL
   *         authentication.
   */
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
  public String getSASLMechanism()
  {
    return saslMechanism;
  }



  /**
   * Returns the simple authentication password for this bind request.
   *
   * @return The simple authentication password for this bind request,
   *         or {@code null} if there is no password.
   */
  public ByteString getSimplePassword()
  {
    return simplePassword;
  }



  /**
   * Sets the raw, unprocessed bind DN for this bind request.
   * <p>
   * This may or may not contain a valid DN.
   *
   * @param bindDN
   *          The raw, unprocessed bind DN for this bind request.
   * @return This raw bind request.
   */
  public RawBindRequest setBindDN(ByteString bindDN)
  {
    Validator.ensureNotNull(bindDN);
    this.bindDN = bindDN;
    return this;
  }



  /**
   * Sets the string representation of the protocol version associated
   * with this bind request.
   *
   * @param protocolVersion
   *          The string representation of the protocol version
   *          associated with this bind request.
   * @return This raw bind request.
   */
  public RawBindRequest setProtocolVersion(String protocolVersion)
  {
    Validator.ensureNotNull(protocolVersion);
    this.protocolVersion = protocolVersion;
    return this;
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
  public RawBindRequest setSASLCredentials(ByteString saslCredentials)
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
  public RawBindRequest setSASLMechanism(String saslMechanism)
  {
    this.saslMechanism = saslMechanism;
    return this;
  }



  /**
   * Sets the simple authentication password for this bind request.
   *
   * @param simplePassword
   *          The simple authentication password for this bind request,
   *          or {@code null} if there is no password.
   * @return This raw bind request.
   */
  public RawBindRequest setSimplePassword(ByteString simplePassword)
  {
    this.simplePassword = simplePassword;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public BindRequest toRequest(Schema schema) throws DirectoryException
  {
    // TODO: not yet implemented.
    return null;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("BindRequest(version=");
    buffer.append(protocolVersion);
    buffer.append(", name=");
    buffer.append(bindDN);
    buffer.append(", authentication=");
    buffer.append(authenticationType);
    buffer.append(", simplePassword=");
    buffer.append(String.valueOf(simplePassword));
    buffer.append(", saslMechanism=");
    buffer.append(String.valueOf(saslMechanism));
    buffer.append(", saslCredentials=");
    buffer.append(String.valueOf(saslCredentials));
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
