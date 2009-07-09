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

package org.opends.types.filter;



import org.opends.messages.Message;
import org.opends.util.LocalizableException;



/**
 * Thrown to indicate that the application has attempted to do one of
 * the following:
 * <ul>
 * <li>Convert a {@code String} to a {@code Filter}, but that the
 * {@code String} does not conform to the LDAP string representation for
 * filters.
 * <li>Use an inappropriate type of {@code Filter}, for example when
 * creating controls that only accept certain types of filter.
 * </ul>
 */
@SuppressWarnings("serial")
public final class IllegalFilterException extends
    IllegalArgumentException implements LocalizableException
{
  // The I18N message associated with this exception.
  private final Message message;



  /**
   * Creates a new illegal filter exception with the provided message.
   * 
   * @param message
   *          The message that explains the problem that occurred.
   */
  public IllegalFilterException(Message message)
  {
    super(String.valueOf(message));
    this.message = message;
  }



  /**
   * {@inheritDoc}
   */
  public Message getMessageObject()
  {
    return this.message;
  }
}
