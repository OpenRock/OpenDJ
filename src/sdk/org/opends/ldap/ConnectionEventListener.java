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



import java.util.EventListener;

import org.opends.ldap.responses.ErrorResultException;
import org.opends.ldap.responses.GenericExtendedResult;



/**
 * An object that registers to be notified when a {@link Connection} is
 * closed by the application, receives an unsolicited notification, or
 * experiences a fatal error.
 * <p>
 * TODO: isolate fatal connection errors as a sub-type of
 * ErrorResultException.
 */
public interface ConnectionEventListener extends EventListener
{
  /**
   * Notifies this connection event listener that the application has
   * called {@link Connection#close} on the connection. The connection
   * event listener will be notified immediately after the application
   * calls the {@link Connection#close} method on the associated
   * connection.
   *
   * @param connection
   *          The connection that has just been closed by the
   *          application.
   */
  void connectionClosed(Connection connection);



  /**
   * Notifies this connection event listener that the connection has
   * just received the provided unsolicited notification from the
   * server.
   * <p>
   * <b>Note:</b> disconnect notifications are treated as fatal
   * connection errors and are handled by the
   * {@link #connectionErrorOccurred} method.
   *
   * @param connection
   *          The connection that has just received the unsolicited
   *          notification.
   * @param notification
   *          The unsolicited notification
   */
  void connectionReceivedUnsolicitedNotification(Connection connection,
      GenericExtendedResult notification);



  /**
   * Notifies this connection event listener that a fatal error has
   * occurred and the connection can no longer be used - the server has
   * crashed, for example. The connection implementation makes this
   * notification just before it throws the provided
   * {@link ErrorResultException} to the application.
   * <p>
   * <b>Note:</b> disconnect notifications are treated as fatal
   * connection errors and are handled by this method. In this case
   * {@code isDisconnectNotification} will be {@code true} and {@code
   * error} will contain the result code and any diagnostic information
   * contained in the notification message.
   *
   * @param connection
   *          The connection which has just experienced a fatal error
   *          and can no longer be used.
   * @param isDisconnectNotification
   *          {@code true} if the error was triggered by a disconnect
   *          notification sent by the server, otherwise {@code false}.
   * @param error
   *          The exception that is about to be thrown to the
   *          application.
   */
  void connectionErrorOccurred(Connection connection,
      boolean isDisconnectNotification, ErrorResultException error);
}
