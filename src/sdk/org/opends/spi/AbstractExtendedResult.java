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

package org.opends.spi;



import java.util.LinkedList;
import java.util.List;

import org.opends.ldap.responses.ExtendedResult;
import org.opends.server.types.ByteString;
import org.opends.types.ResultCode;



/**
 * An abstract LDAP extended result response message implementation,
 * which can be used as the basis for implementing new extended results.
 *
 * @param <R>
 *          The type of extended result.
 */
public abstract class AbstractExtendedResult<R extends ExtendedResult>
    extends AbstractMessage<R> implements ExtendedResult<R>
{
  private Throwable cause;
  private String diagnosticMessage;
  private String matchedDN;
  private String name = null;
  private final List<String> referrals = new LinkedList<String>();
  private ResultCode resultCode;



  /**
   * Creates a new extended result using the provided result code.
   *
   * @param resultCode
   *          The result code.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  protected AbstractExtendedResult(ResultCode resultCode)
      throws NullPointerException
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
  public final String getResponseName()
  {
    return name;
  }



  /**
   * {@inheritDoc}
   */
  public abstract ByteString getResponseValue();



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
  public final R setResponseName(String name)
  {
    this.name = name;
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
  public StringBuilder toString(StringBuilder builder)
  {
    builder.append("ExtendedResult(resultCode=");
    builder.append(getResultCode());
    builder.append(", matchedDN=");
    builder.append(getMatchedDN());
    builder.append(", diagnosticMessage=");
    builder.append(getDiagnosticMessage());
    builder.append(", referrals=");
    builder.append(getReferralURIs());
    builder.append(", responseName=");
    builder.append(name == null ? "" : name);
    builder.append(", responseValue=");
    ByteString value = getResponseValue();
    builder.append(value == null ? ByteString.empty() : value);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder;
  }



  /**
   * Returns a type-safe reference to this response.
   *
   * @return This response as a T.
   */
  @SuppressWarnings("unchecked")
  private final R getThis()
  {
    return (R) this;
  }
}
