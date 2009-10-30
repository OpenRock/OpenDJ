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



import java.util.LinkedList;
import java.util.List;

import org.opends.sdk.ResultCode;
import org.opends.sdk.util.Validator;



/**
 * An abstract result which can be used as the basis for implementing
 * new results.
 *
 * @param <S>
 *          The type of result.
 */
public abstract class AbstractResult<S extends Result> extends
    AbstractMessage<S> implements Result
{
  // For local errors caused by internal exceptions.
  private Throwable cause = null;

  private String diagnosticMessage = "";
  private String matchedDN = "";
  private final List<String> referralURIs = new LinkedList<String>();
  private ResultCode resultCode;



  /**
   * Creates a new abstract result using the provided result code.
   *
   * @param resultCode
   *          The result code.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  protected AbstractResult(ResultCode resultCode)
      throws NullPointerException
  {
    Validator.ensureNotNull(resultCode);

    this.resultCode = resultCode;
  }



  /**
   * {@inheritDoc}
   */
  public final S addReferralURI(String uri) throws NullPointerException
  {
    Validator.ensureNotNull(uri);

    referralURIs.add(uri);
    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public final S clearReferralURIs()
  {
    referralURIs.clear();
    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public final Throwable getCause()
  {
    return cause;
  }



  /**
   * {@inheritDoc}
   */
  public final String getDiagnosticMessage()
  {
    return diagnosticMessage;
  }



  /**
   * {@inheritDoc}
   */
  public final String getMatchedDN()
  {
    return matchedDN;
  }



  /**
   * {@inheritDoc}
   */
  public final Iterable<String> getReferralURIs()
  {
    return referralURIs;
  }



  /**
   * {@inheritDoc}
   */
  public final ResultCode getResultCode()
  {
    return resultCode;
  }



  /**
   * {@inheritDoc}
   */
  public final boolean hasReferralURIs()
  {
    return !referralURIs.isEmpty();
  }



  /**
   * {@inheritDoc}
   */
  public final S setCause(Throwable cause)
  {
    this.cause = cause;
    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public final S setDiagnosticMessage(String message)
  {
    if (message == null)
    {
      this.diagnosticMessage = "";
    }
    else
    {
      this.diagnosticMessage = message;
    }

    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public final S setMatchedDN(String dn)
  {
    if (dn == null)
    {
      this.matchedDN = "";
    }
    else
    {
      this.matchedDN = dn;
    }

    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public final S setResultCode(ResultCode resultCode)
      throws NullPointerException
  {
    Validator.ensureNotNull(resultCode);

    this.resultCode = resultCode;
    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("Result(resultCode=");
    builder.append(getResultCode());
    builder.append(", matchedDN=");
    builder.append(getMatchedDN());
    builder.append(", diagnosticMessage=");
    builder.append(getDiagnosticMessage());
    builder.append(", referralURIs=");
    builder.append(getReferralURIs());
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder.toString();
  }



  /**
   * Returns a type-safe reference to this result.
   *
   * @return This message as a R.
   */
  @SuppressWarnings("unchecked")
  private final S getThis()
  {
    return (S) this;
  }



  /**
   * {@inheritDoc}
   */
  public final boolean isReferral()
  {
    ResultCode code = getResultCode();
    return code.equals(ResultCode.REFERRAL);
  }



  /**
   * {@inheritDoc}
   */
  public final boolean isSuccess()
  {
    ResultCode code = getResultCode();
    return !code.isExceptional();
  }

}
