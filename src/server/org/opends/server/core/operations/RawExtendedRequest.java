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

package org.opends.server.core.operations;



import org.opends.server.types.ByteString;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.OperationType;
import org.opends.server.util.Validator;



/**
 * A raw extended request.
 */
public final class RawExtendedRequest extends RawRequest
{
  // The extended request name OID.
  private String requestName;

  // The extended request value.
  private ByteString requestValue = null;



  /**
   * Creates a new raw extended request using the provided OID.
   * <p>
   * The new raw extended request will contain an empty list of
   * controls, and no value.
   * 
   * @param requestName
   *          The extended request name OID.
   */
  public RawExtendedRequest(String requestName)
  {
    super(OperationType.EXTENDED);
    Validator.ensureNotNull(requestName);
    this.requestName = requestName;
  }



  /**
   * Returns the name OID for this extended request.
   * 
   * @return The name OID for this extended request.
   */
  public String getRequestName()
  {
    return requestName;
  }



  /**
   * Returns the request value for this extended request.
   * 
   * @return The request value for this extended request, or {@code
   *         null} if this extended request does not have a request
   *         value.
   */
  public ByteString getRequestValue()
  {
    return requestValue;
  }



  /**
   * Sets the name OID for this extended request.
   * 
   * @param requestName
   *          The name OID for this extended request.
   * @return This raw extended request.
   */
  public RawExtendedRequest setRequestName(String requestName)
  {
    Validator.ensureNotNull(requestName);
    this.requestName = requestName;
    return this;
  }



  /**
   * Sets the request value for this extended request.
   * 
   * @param requestValue
   *          The request value for this extended request, or {@code
   *          null} if this extended request does not have a request
   *          value.
   * @return This raw extended request.
   */
  public RawExtendedRequest setRequestValue(ByteString requestValue)
  {
    this.requestValue = requestValue;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public ExtendedRequest toRequest(Schema schema)
      throws DirectoryException
  {
    // TODO: not yet implemented.
    return null;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("ExtendedRequest(requestName=");
    buffer.append(requestName);
    buffer.append(", requestValue=");
    buffer.append(String.valueOf(requestValue));
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
