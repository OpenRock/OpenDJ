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



import org.opends.types.ResultCode;



/**
 * A Result response.
 */
public interface Result extends Response<Result>
{

  /**
   * Adds the provided referral URI to this result.
   *
   * @param uri
   *          The referral URI to be added.
   * @return This result.
   * @throws UnsupportedOperationException
   *           If this result does not permit referrals to be added.
   * @throws NullPointerException
   *           If {@code uri} was {@code null}.
   */
  Result addReferralURI(String uri)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Removes all the referral URIs included with this result.
   *
   * @return This result.
   * @throws UnsupportedOperationException
   *           If this result does not permit referral URIs to be
   *           removed.
   */
  Result clearReferralURIs() throws UnsupportedOperationException;



  /**
   * Returns the {@code Throwable} associated with this result if
   * available. A cause may be provided in cases where a result
   * indicates a failure due to a client-side error.
   *
   * @return The {@code Throwable} associated with this result, or
   *         {@code null} if none was provided.
   */
  Throwable getCause();



  /**
   * Returns the diagnostic message associated with this result.
   *
   * @return The diagnostic message associated with this result, which
   *         may be empty if none was provided.
   */
  String getDiagnosticMessage();



  /**
   * Returns the matched DN associated with this result.
   *
   * @return The matched DN associated with this result, which may be
   *         empty if none was provided.
   */
  String getMatchedDN();



  /**
   * Returns an {@code Iterable} containing the referral URIs included
   * with this result. The returned {@code Iterable} may be used to
   * remove referral URIs if permitted by this result.
   *
   * @return An {@code Iterable} containing the referral URIs included
   *         with this result.
   */
  Iterable<String> getReferralURIs();



  /**
   * Returns the result code associated with this result.
   *
   * @return The result code associated with this result.
   */
  ResultCode getResultCode();



  /**
   * Indicates whether or not this result has any referral URIs.
   *
   * @return {@code true} if this result has any referral URIs,
   *         otherwise {@code false}.
   */
  boolean hasReferralURIs();



  /**
   * Sets the {@code Throwable} associated with this result if
   * available. A cause may be provided in cases where a result
   * indicates a failure due to a client-side error.
   *
   * @param cause
   *          The cause associated with this result, which may be
   *          {@code null} indicating that none was provided.
   * @return This result.
   * @throws UnsupportedOperationException
   *           If this result does not permit the cause to be set.
   */
  Result setCause(Throwable cause) throws UnsupportedOperationException;



  /**
   * Sets the diagnostic message associated with this result.
   *
   * @param message
   *          The diagnostic message associated with this result, which
   *          may be empty or {@code null} indicating that none was
   *          provided.
   * @return This result.
   * @throws UnsupportedOperationException
   *           If this result does not permit the diagnostic message to
   *           be set.
   */
  Result setDiagnosticMessage(String message)
      throws UnsupportedOperationException;



  /**
   * Sets the matched DN associated with this result.
   *
   * @param dn
   *          The matched DN associated with this result, which may be
   *          empty or {@code null} indicating that none was provided.
   * @return This result.
   * @throws UnsupportedOperationException
   *           If this result does not permit the matched DN to be set.
   */
  Result setMatchedDN(String dn) throws UnsupportedOperationException;



  /**
   * Sets the result code associated with this result.
   *
   * @param resultCode
   *          The result code associated with this result.
   * @return This result.
   * @throws UnsupportedOperationException
   *           If this result does not permit the result code to be set.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  Result setResultCode(ResultCode resultCode)
      throws UnsupportedOperationException, NullPointerException;

}