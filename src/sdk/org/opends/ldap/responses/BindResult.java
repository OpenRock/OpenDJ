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

package org.opends.ldap.responses;



import org.opends.ldap.ResultCode;
import org.opends.ldap.controls.Control;
import org.opends.server.types.ByteString;
import org.opends.types.DN;



/**
 * An LDAP bind result response message.
 */
public interface BindResult extends Result
{

  /**
   * {@inheritDoc}
   */
  BindResult addControl(Control control)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  BindResult addReferralURI(String referralURL)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  BindResult clearControls() throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  BindResult clearReferralURIs() throws UnsupportedOperationException;



  /**
   * Returns the server SASL credentials associated with this bind
   * result.
   *
   * @return The server SASL credentials associated with this bind
   *         result, which may be empty if none was provided.
   */
  ByteString getServerSASLCredentials();



  /**
   * {@inheritDoc}
   */
  BindResult setCause(Throwable cause)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  BindResult setDiagnosticMessage(String diagnosticMessage)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  BindResult setMatchedDN(DN matchedDN)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  BindResult setMatchedDN(String matchedDN)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  BindResult setResultCode(ResultCode resultCode)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Sets the server SASL credentials associated with this bind result.
   *
   * @param credentials
   *          The server SASL credentials associated with this bind
   *          result, which may be empty or {@code null} indicating that
   *          none was provided.
   * @return This bind result.
   * @throws UnsupportedOperationException
   *           If this bind result does not permit the server SASL
   *           credentials to be set.
   */
  BindResult setServerSASLCredentials(ByteString credentials)
      throws UnsupportedOperationException;

}