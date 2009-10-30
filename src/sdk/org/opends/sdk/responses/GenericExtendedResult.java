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
import org.opends.sdk.requests.GenericExtendedRequest;
import org.opends.sdk.util.ByteString;



/**
 * A generic Extended result indicates the status of a generic Extended
 * operation (see {@link GenericExtendedRequest}) and any additional
 * information associated with the Extended operation, including the
 * optional response name and value. These can be retrieved using the
 * {@link #getResponseName} and {@link #getResponseValue} methods
 * respectively.
 */
public interface GenericExtendedResult extends ExtendedResult
{

  /**
   * {@inheritDoc}
   */
  GenericExtendedResult addControl(Control control)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  GenericExtendedResult clearControls()
      throws UnsupportedOperationException;



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
  GenericExtendedResult addReferralURI(String uri)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  GenericExtendedResult clearReferralURIs()
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
  GenericExtendedResult setCause(Throwable cause)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  GenericExtendedResult setDiagnosticMessage(String message)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  GenericExtendedResult setMatchedDN(String dn)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  GenericExtendedResult setResultCode(ResultCode resultCode)
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
   * {@inheritDoc}
   */
  String getResponseName();



  /**
   * {@inheritDoc}
   */
  ByteString getResponseValue();



  /**
   * {@inheritDoc}
   */
  GenericExtendedResult setResponseName(String name)
      throws UnsupportedOperationException;



  /**
   * Sets the content of this generic extended result in a form defined
   * by the extended result.
   *
   * @param bytes
   *          The content of this generic extended result in a form
   *          defined by the extended result, or {@code null} if there
   *          is no content.
   * @return This generic extended result.
   * @throws UnsupportedOperationException
   *           If this generic extended result does not permit the
   *           request value to be set.
   */
  GenericExtendedResult setResponseValue(ByteString bytes)
      throws UnsupportedOperationException;

}