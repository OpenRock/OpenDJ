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
import org.opends.types.ResultCode;



/**
 * A Compare result indicates the status of a Compare operation.
 * <p>
 * If the attribute value assertion in the Compare request matched a
 * value of the attribute or sub-type according to the attribute's
 * equality matching rule then the result code is set to
 * {@link ResultCode#COMPARE_TRUE} and can be determined by invoking the
 * {@link #matched} method.
 */
public interface CompareResult extends Result
{

  /**
   * {@inheritDoc}
   */
  CompareResult addControl(Control control)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  CompareResult clearControls() throws UnsupportedOperationException;



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
  String toString();



  /**
   * {@inheritDoc}
   */
  StringBuilder toString(StringBuilder builder)
      throws NullPointerException;



  /**
   * {@inheritDoc}
   */
  CompareResult addReferralURI(String uri)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  CompareResult clearReferralURIs()
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
  CompareResult setCause(Throwable cause)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  CompareResult setDiagnosticMessage(String message)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  CompareResult setMatchedDN(String dn)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  CompareResult setResultCode(ResultCode resultCode)
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
   * Indicates whether or not the attribute value assertion in the
   * Compare request matched a value of the attribute or sub-type
   * according to the attribute's equality matching rule.
   * <p>
   * Specifically, this method returns {@code true} if the result code
   * is equal to {@link ResultCode#COMPARE_TRUE}.
   *
   * @return {@code true} if the attribute value assertion matched,
   *         otherwise {@code false}.
   */
  boolean matched();

}