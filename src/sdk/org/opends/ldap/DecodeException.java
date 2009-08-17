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

package org.opends.ldap;



import org.opends.messages.Message;
import org.opends.util.LocalizableException;



/**
 * Thrown when a control or extended operation could not be decoded
 * because it was malformed.
 * <p>
 * TODO: should we combine this with DecodeException?
 */
@SuppressWarnings("serial")
public final class DecodeException extends Exception implements
    LocalizableException
{
  private final Message message;



  /**
   * Creates a new decode exception with the provided message.
   *
   * @param message
   *          The message that explains the problem that occurred.
   */
  public DecodeException(Message message)
  {
    this(message, null);
  }



  /**
   * Creates a new decode exception with the provided message and root
   * cause.
   *
   * @param message
   *          The message that explains the problem that occurred.
   * @param cause
   *          The exception that was caught to trigger this exception.
   */
  public DecodeException(Message message, Throwable cause)
  {
    super(message.toString(), cause);
    this.message = message;
  }



  /**
   * Returns the message that explains the problem that occurred.
   *
   * @return Message of the problem
   */
  public Message getMessageObject()
  {
    return message;
  }
}
