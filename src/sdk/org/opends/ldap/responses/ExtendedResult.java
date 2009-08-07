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



import org.opends.ldap.controls.Control;
import org.opends.server.types.ByteString;
import org.opends.types.ResultCode;



/**
 * An LDAP extended result response message.
 *
 * @param <R>
 *          The type of extended result.
 */
public interface ExtendedResult<R extends ExtendedResult> extends
    Result
{

  /**
   * {@inheritDoc}
   */
  R addControl(Control control) throws UnsupportedOperationException,
      NullPointerException;



  /**
   * {@inheritDoc}
   */
  R addReferralURI(String uri) throws UnsupportedOperationException,
      NullPointerException;



  /**
   * {@inheritDoc}
   */
  R clearControls() throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  R clearReferralURIs() throws UnsupportedOperationException;



  /**
   * Returns the response name associated with this extended result.
   *
   * @return The response name associated with this extended result,
   *         which may be {@code null} if none was provided.
   */
  String getResponseName();



  /**
   * Returns the response value associated with this extended result.
   *
   * @return The response value associated with this extended result,
   *         which may be {@code null} if none was provided.
   */
  ByteString getResponseValue();



  /**
   * {@inheritDoc}
   */
  R setCause(Throwable cause) throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  R setDiagnosticMessage(String message)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  R setMatchedDN(String dn) throws UnsupportedOperationException;



  /**
   * Sets the response name associated with this extended result.
   *
   * @param name
   *          The response name associated with this extended result,
   *          which may be {@code null} indicating that none was
   *          provided.
   * @return This extended result.
   * @throws UnsupportedOperationException
   *           If this extended result does not permit the response name
   *           to be set.
   */
  R setResponseName(String name) throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  R setResultCode(ResultCode resultCode)
      throws UnsupportedOperationException, NullPointerException;
}