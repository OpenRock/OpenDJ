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

package org.opends.sdk.responses;



import org.opends.sdk.ResultCode;
import org.opends.sdk.controls.Control;
import org.opends.sdk.util.ByteString;



/**
 * A Extended result indicates the status of an Extended operation and
 * any additional information associated with the Extended operation,
 * including the optional response name and value. These can be
 * retrieved using the {@link #getResponseName} and
 * {@link #getResponseValue} methods respectively.
 */
public interface ExtendedResult extends Result
{

  /**
   * {@inheritDoc}
   */
  ExtendedResult addControl(Control control)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  ExtendedResult clearControls() throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  Control getControl(String oid) throws NullPointerException;



  /**
   * {@inheritDoc}
   */
  Iterable<Control> getControls();



  /**
   * {@inheritDoc}
   */
  boolean hasControls();



  /**
   * {@inheritDoc}
   */
  Control removeControl(String oid)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  ExtendedResult addReferralURI(String uri)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  ExtendedResult clearReferralURIs()
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  Throwable getCause();



  /**
   * {@inheritDoc}
   */
  String getDiagnosticMessage();



  /**
   * {@inheritDoc}
   */
  String getMatchedDN();



  /**
   * {@inheritDoc}
   */
  Iterable<String> getReferralURIs();



  /**
   * {@inheritDoc}
   */
  ResultCode getResultCode();



  /**
   * {@inheritDoc}
   */
  boolean hasReferralURIs();



  /**
   * {@inheritDoc}
   */
  ExtendedResult setCause(Throwable cause)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  ExtendedResult setDiagnosticMessage(String message)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  ExtendedResult setMatchedDN(String dn)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  ExtendedResult setResultCode(ResultCode resultCode)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  boolean isSuccess();



  /**
   * {@inheritDoc}
   */
  boolean isReferral();



  /**
   * Returns the dotted-decimal representation of the unique OID
   * corresponding to this extended result.
   *
   * @return The dotted-decimal representation of the unique OID, or
   *         {@code null} if none was provided.
   */
  String getResponseName();



  /**
   * Returns the content of this extended result in a form defined by
   * the extended result.
   *
   * @return The content of this extended result, or {@code null} if
   *         there is no content.
   */
  ByteString getResponseValue();



  /**
   * Sets the response name associated with this extended result.
   *
   * @param oid
   *          The dotted-decimal representation of the unique OID
   *          corresponding to this extended result, which may be
   *          {@code null} indicating that none was provided.
   * @return This extended result.
   * @throws UnsupportedOperationException
   *           If this extended result does not permit the response name
   *           to be set.
   */
  ExtendedResult setResponseName(String oid)
      throws UnsupportedOperationException;
}