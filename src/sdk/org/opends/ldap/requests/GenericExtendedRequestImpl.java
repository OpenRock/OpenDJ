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

package org.opends.ldap.requests;



import org.opends.ldap.extensions.GenericExtendedOperation;
import org.opends.ldap.responses.GenericExtendedResult;
import org.opends.server.types.ByteString;
import org.opends.spi.AbstractExtendedRequest;



/**
 * Generic extended request implementation.
 */
final class GenericExtendedRequestImpl
    extends
    AbstractExtendedRequest<GenericExtendedRequest, GenericExtendedResult>
    implements GenericExtendedRequest
{
  private ByteString value = ByteString.empty();



  /**
   * Creates a new generic extended request using the provided name.
   * 
   * @param oid
   *          The dotted-decimal representation of the unique OID
   *          corresponding to this extended request.
   * @throws NullPointerException
   *           If {@code oid} was {@code null}.
   */
  GenericExtendedRequestImpl(String oid) throws NullPointerException
  {
    super(oid);
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public GenericExtendedOperation getExtendedOperation()
  {
    return GenericExtendedOperation.getInstance();
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public ByteString getRequestValue()
  {
    return value;
  }



  /**
   * {@inheritDoc}
   */
  public GenericExtendedRequest setRequestValue(ByteString bytes)
  {
    this.value = bytes;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public StringBuilder toString(StringBuilder builder)
      throws NullPointerException
  {
    builder.append("GenericExtendedRequest(requestName=");
    builder.append(getRequestName());
    builder.append(", requestValue=");
    builder.append(value);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder;
  }
}
