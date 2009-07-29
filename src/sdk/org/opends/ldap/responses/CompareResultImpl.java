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



/**
 * LDAP compare result response message implementation.
 */
final class CompareResultImpl extends ResultImpl<CompareResult>
    implements CompareResult
{

  /**
   * Creates a new compare result using the provided result code.
   *
   * @param resultCode
   *          The result code.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  CompareResultImpl(ResultCode resultCode) throws NullPointerException
  {
    super(resultCode);
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("CompareResult(resultCode=");
    buffer.append(getResultCode());
    buffer.append(", matchedDN=");
    buffer.append(getMatchedDN());
    buffer.append(", diagnosticMessage=");
    buffer.append(getDiagnosticMessage());
    buffer.append(", referrals=");
    buffer.append(getReferralURIs());
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
