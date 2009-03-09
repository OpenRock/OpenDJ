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
import org.opends.server.controls.ControlDecoder;
import org.opends.server.protocols.ldap.LDAPControl;
import org.opends.server.types.Control;
import org.opends.server.types.DN;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.ResultCode;



/**
 * A generic operation response. This class defines methods common to
 * all types of response.
 */
public class Response
{
  // The optional additional log message.
  private final Message additionalLogMessage;

  // The unmodifiable list of controls (may be empty).
  private final List<Control> controls;

  // The optional error message.
  private final Message errorMessage;

  // The optional matched DN.
  private final DN matchedDN;

  // The unmodifiable list of referral URLs (may be empty).
  private final List<String> referralURLs;

  // The result code.
  private final ResultCode resultCode;



  /**
   * Creates a new response.
   * <p>
   * Package private - prevents sub-classing and instantiation outside
   * of this package.
   *
   * @param resultCode
   *          The result code.
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
  Response(ResultCode resultCode, Message errorMessage,
      Message additionalLogMessage, DN matchedDN,
      List<String> referralURLs, List<Control> controls)
  {
    this.resultCode = resultCode;
    this.errorMessage = errorMessage;
    this.additionalLogMessage = additionalLogMessage;
    this.matchedDN = matchedDN;
    this.referralURLs = referralURLs;
    this.controls = controls;
  }



  /**
   * Returns the additional log message for this response, which should
   * be written to the log but not included in the response to the
   * client.
   *
   * @return The additional log message for this response, or {@code
   *         null} if there is no additional log message.
   */
  public final Message getAdditionalLogMessage()
  {
    return additionalLogMessage;
  }



  /**
   * Returns the specified control included with this response, decoding
   * it using the specified decoder.
   *
   * @param <T>
   *          The type of the requested control.
   * @param d
   *          The requested control's decoder.
   * @return The decoded control, or {@code null} if the control is not
   *         included with this response.
   * @throws DirectoryException
   *           If the control was found but it could not be decoded.
   */
  @SuppressWarnings("unchecked")
  public final <T extends Control> T getControl(ControlDecoder<T> d)
      throws DirectoryException
  {
    String oid = d.getOID();
    for (int i = 0; i < controls.size(); i++)
    {
      Control c = controls.get(i);
      if (c.getOID().equals(oid))
      {
        if (c instanceof LDAPControl)
        {
          T decodedControl =
              d.decode(c.isCritical(), ((LDAPControl) c).getValue());
          controls.set(i, decodedControl);
          return decodedControl;
        }
        else
        {
          return (T) c;
        }
      }
    }
    return null;
  }



  /**
   * Returns an unmodifiable list containing the controls included with
   * this response. The returned list may be empty (but never {@code
   * null}) if there are no controls associated with this response.
   *
   * @return The unmodifiable list containing the controls included with
   *         this response.
   */
  public final List<Control> getControls()
  {
    return controls;
  }



  /**
   * Returns the error message for this response.
   *
   * @return The error message for this response, or {@code null} if
   *         there is no error message.
   */
  public final Message getErrorMessage()
  {
    return errorMessage;
  }



  /**
   * Returns the matched DN for this response.
   *
   * @return The matched DN for this response, or {@code null} if there
   *         is no matched DN.
   */
  public final DN getMatchedDN()
  {
    return matchedDN;
  }



  /**
   * Returns an unmodifiable list containing the referral URLs included
   * with this response. The returned list may be empty (but never
   * {@code null}) if there are no referral URLs associated with this
   * response.
   *
   * @return The unmodifiable list containing the referral URLs included
   *         with this response.
   */
  public final List<String> getReferralURLs()
  {
    return referralURLs;
  }



  /**
   * Returns the result code for this response.
   *
   * @return The result code for this response.
   */
  public final ResultCode getResultCode()
  {
    return resultCode;
  }



  /**
   * Returns a string representation of this response.
   *
   * @return A string representation of this response.
   */
  @Override
  public final String toString()
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
    buffer.append("Response(resultCode=");
    buffer.append(resultCode);
    buffer.append(", errorMessage=");
    buffer.append(String.valueOf(errorMessage));
    buffer.append(", matchedDN=");
    buffer.append(String.valueOf(matchedDN));
    buffer.append(", referralURLs=");
    buffer.append(referralURLs);
    buffer.append(", controls=");
    buffer.append(controls);
    buffer.append(")");
  }
}
