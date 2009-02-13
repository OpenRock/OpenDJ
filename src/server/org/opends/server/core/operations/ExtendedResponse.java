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



import java.util.List;

import org.opends.messages.Message;
import org.opends.server.types.ByteString;
import org.opends.server.types.Control;
import org.opends.server.types.DN;
import org.opends.server.types.ResultCode;



/**
 * An extended operation response.
 */
public final class ExtendedResponse extends Response
{

  // The extended response name OID.
  private final String responseName;

  // The optional extended response value.
  private final ByteString responseValue;



  /**
   * Creates a new extended response.
   * <p>
   * Package private - prevents instantiation outside of this package.
   * 
   * @param resultCode
   *          The result code.
   * @param responseName
   *          The extended response name OID.
   * @param responseValue
   *          The optional extended response value.
   * @param errorMessage
   *          The optional error message.
   * @param additionalLogMessage
   *          The optional additional log message.
   * @param matchedDN
   *          The optional matched DN.
   * @param referralURLs
   *          The unmodifiable list of referral URLs (may be empty).
   * @param controls
   *          The unmodifiable list of controls (may be empty).
   */
  ExtendedResponse(ResultCode resultCode, String responseName,
      ByteString responseValue, Message errorMessage,
      Message additionalLogMessage, DN matchedDN,
      List<String> referralURLs, List<Control> controls)
  {
    super(resultCode, errorMessage, additionalLogMessage, matchedDN,
        referralURLs, controls);
    this.responseName = responseName;
    this.responseValue = responseValue;
  }



  /**
   * Returns the name OID for this extended response.
   * 
   * @return The name OID for this extended response.
   */
  public String getResponseName()
  {
    return responseName;
  }



  /**
   * Returns the response value for this extended response.
   * 
   * @return The response value for this extended response, or {@code
   *         null} if this extended response does not have a response
   *         value.
   */
  public ByteString getResponseValue()
  {
    return responseValue;
  }



  /**
   * Appends a string representation of this response to the provided
   * buffer.
   * 
   * @param buffer
   *          The buffer into which a string representation of this
   *          response should be appended.
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("ExtendedResponse(resultCode=");
    buffer.append(getResultCode());
    buffer.append(", responseName=");
    buffer.append(responseName);
    buffer.append(", responseValue=");
    buffer.append(String.valueOf(responseValue));
    buffer.append(", errorMessage=");
    buffer.append(String.valueOf(getErrorMessage()));
    buffer.append(", matchedDN=");
    buffer.append(String.valueOf(getMatchedDN()));
    buffer.append(", referralURLs=");
    buffer.append(getReferralURLs());
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
