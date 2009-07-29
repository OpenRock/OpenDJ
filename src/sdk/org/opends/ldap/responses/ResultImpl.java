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



import java.util.LinkedList;
import java.util.List;

import org.opends.ldap.ResultCode;
import org.opends.types.DN;



/**
 * LDAP result response message implementation.
 *
 * @param <R>
 *          The type of response.
 */
class ResultImpl<R extends Result> extends ResponseImpl<R> implements
    Result
{
  // For local errors caused by internal exceptions.
  private Throwable cause = null;

  private String diagnosticMessage = "";
  private String matchedDN = "";
  private final List<String> referrals = new LinkedList<String>();
  private ResultCode resultCode;



  /**
   * Creates a new result using the provided result code.
   *
   * @param resultCode
   *          The result code.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  ResultImpl(ResultCode resultCode) throws NullPointerException
  {
    if (resultCode == null)
    {
      throw new NullPointerException();
    }
    this.resultCode = resultCode;
  }



  /**
   * {@inheritDoc}
   */
  public final R addReferralURI(String referralURL)
      throws NullPointerException
  {
    if (referralURL == null)
    {
      throw new NullPointerException();
    }

    referrals.add(referralURL);
    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public final R clearReferralURIs()
  {
    referrals.clear();
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
    return referrals;
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
    return !referrals.isEmpty();
  }



  /**
   * {@inheritDoc}
   */
  public final R setCause(Throwable cause)
  {
    this.cause = cause;
    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public final R setDiagnosticMessage(String diagnosticMessage)
  {
    if (diagnosticMessage == null)
    {
      this.diagnosticMessage = "";
    }
    else
    {
      this.diagnosticMessage = diagnosticMessage;
    }

    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public final R setMatchedDN(DN matchedDN)
  {
    if (matchedDN == null)
    {
      this.matchedDN = "";
    }
    else
    {
      this.matchedDN = matchedDN.toString();
    }

    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public final R setMatchedDN(String matchedDN)
  {
    if (matchedDN == null)
    {
      this.matchedDN = "";
    }
    else
    {
      this.matchedDN = matchedDN;
    }

    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public final R setResultCode(ResultCode resultCode)
      throws NullPointerException
  {
    if (resultCode == null)
    {
      throw new NullPointerException();
    }
    this.resultCode = resultCode;

    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public void toString(StringBuilder buffer)
  {
    buffer.append("Result(resultCode=");
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
