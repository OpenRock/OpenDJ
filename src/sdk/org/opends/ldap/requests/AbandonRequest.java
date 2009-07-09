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



import org.opends.ldap.Message;



/**
 * A raw abandon request.
 */
public final class AbandonRequest extends Message implements Request
{
  // The message ID of the request that should be abandoned.
  private int messageID;



  /**
   * Creates a new raw abandon request using the provided message ID.
   * <p>
   * The new raw abandon request will contain an empty list of controls.
   * 
   * @param messageID
   *          The message ID of the request that should be abandoned.
   */
  public AbandonRequest(int messageID)
  {
    this.messageID = messageID;
  }



  /**
   * Returns the message ID of the request that should be abandoned.
   * 
   * @return The message ID of the request that should be abandoned.
   */
  public int getMessageID()
  {
    return messageID;
  }



  /**
   * Sets the message ID of the request that should be abandoned.
   * 
   * @param messageID
   *          The message ID of the request that should be abandoned.
   * @return This raw abandon request.
   */
  public AbandonRequest setMessageID(int messageID)
  {
    this.messageID = messageID;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("AbandonRequest(messageID=");
    buffer.append(messageID);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
