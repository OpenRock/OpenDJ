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



/**
 * An intermediate response.
 */
public final class IntermediateResponse
{

  // The optional intermediate response name OID.
  private final String responseName;

  // The optional intermediate response value.
  private final ByteString responseValue;



  /**
   * Creates a new intermediate response.
   *
   * @param responseName
   *          The optional intermediate response name OID.
   * @param responseValue
   *          The optional intermediate response value.
   */
  public IntermediateResponse(String responseName,
      ByteString responseValue)
  {
    this.responseName = responseName;
    this.responseValue = responseValue;
  }



  /**
   * Returns the name OID for this intermediate response.
   *
   * @return The name OID for this intermediate response, or {@code
   *         null} if this intermediate response does not have a
   *         response name OID.
   */
  public String getResponseName()
  {
    return responseName;
  }



  /**
   * Returns the response value for this intermediate response.
   *
   * @return The response value for this intermediate response, or
   *         {@code null} if this intermediate response does not have a
   *         response value.
   */
  public ByteString getResponseValue()
  {
    return responseValue;
  }



  /**
   * Returns a string representation of this response.
   *
   * @return A string representation of this response.
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    toString(builder);
    return builder.toString();
  }



  /**
   * Appends a string representation of this response to the provided
   * buffer.
   *
   * @param buffer
   *          The buffer into which a string representation of this
   *          response should be appended.
   */
  public void toString(StringBuilder buffer)
  {
    buffer.append("IntermediateResponse(responseName=");
    buffer.append(String.valueOf(responseName));
    buffer.append(", responseValue=");
    buffer.append(String.valueOf(responseValue));
    buffer.append(")");
  }
}
