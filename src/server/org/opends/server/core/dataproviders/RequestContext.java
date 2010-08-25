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
 *       Copyright year Sun Microsystems, Inc.
 */

package org.opends.server.core.dataproviders;

import org.opends.sdk.*;
import org.opends.sdk.requests.BindRequest;
import org.opends.server.api.AccessControlHandler;
import org.opends.server.types.CanceledOperationException;
import org.opends.server.types.Privilege;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * The context in which a request is to be processed.
 * <p>
 * Implementations may query the context in order to:
 * <ul>
 * <li>query the schema associated with the request (attribute types,
 * decode DNs, etc)
 * <li>perform internal operations
 * <li>query information regarding client performing the request
 * </ul>
 * Context implementations take care of correctly routing internal
 * requests.
 * <p>
 * In addition, the context acts as a transaction manager, coordinating
 * any resources accessed during the processing of a request and any
 * subsequent requests forming part of the same logical transaction.
 */
public interface RequestContext extends ConnectionFactory, AttachmentHolder
{
  /**
   * Retrieves the operation ID for this operation.
   *
   * @return  The operation ID for this operation.
   */
  public long getOperationID();

  /**
   * Retrieves the message ID assigned to this operation.
   *
   * @return  The message ID assigned to this operation.
   */
  public int getMessageID();

  /**
   * Sets the entry for the user that should be considered the
   * authorization identity for this operation.
   *
   * @param  authorizationEntry  The entry for the user that should be
   *                             considered the authorization identity
   *                             for this operation, or {@code null}
   *                             if it should be the unauthenticated
   *                             user.
   */
  public void setAuthorizationEntry(Entry authorizationEntry);

  /**
   * Retrieves the entry for the user that should be considered the
   * authorization identity for this operation.  In many cases, it
   * will be the same as the authorization entry for the underlying
   * client connection, or {@code null} if no authentication has been
   * performed on that connection.  However, it may be some other
   * value if special processing has been requested (e.g., the
   * operation included a proxied authorization control).
   *
   * @return  The entry for the user that should be considered the
   *          authorization identity for this operation, or
   *          {@code null} if the authorization identity should be the
   *          unauthenticated  user.
   */
  public Entry getAuthorizationEntry();


  /**
   * Checks to see if this operation requested to cancel in which case
   * CanceledOperationException will be thrown.
   *
   * @param signalTooLate <code>true</code> to signal that any further
   *                      cancel requests will be too late after
   *                      return from this call or <code>false</code>
   *                      otherwise.
   *
   * @throws CanceledOperationException if this operation should
   * be cancelled.
   */
  public void checkIfCanceled(boolean signalTooLate)
      throws CanceledOperationException;

  /**
   * Retrieves an asynchronous connection for performing internal operations.
   *
   * @param handler
   *          The completion handler, or {@code null} if no handler is to be
   *          used.
   * @return A future which can be used to retrieve the asynchronous connection.
   */
  public FutureResult<AsynchronousConnection> getAsynchronousConnection(
      ResultHandler<? super AsynchronousConnection> handler);

  /**
   * Returns a connection for performing internal operations.
   *
   * @return A connection to the Directory Server associated with this
   *         connection factory.
   * @throws ErrorResultException
   *           If the connection request failed for some reason.
   * @throws InterruptedException
   *           If the current thread was interrupted while waiting.
   */
  public Connection getConnection()
      throws ErrorResultException, InterruptedException;

  /**
   * Get the active access control handler.
   * <p>
   * When access control is disabled, this method returns a default access
   * control implementation which permits all operations.
   *
   * @return   The active access control handler (never {@code null}).
   */
  public AccessControlHandler<?> getAccessControlHandler();

  /**
   * Indicates whether the authenticated client has the specified
   * privilege.
   *
   * @param  privilege  The privilege for which to make the
   *                    determination.
   *
   * @return  {@code true} if the authenticated client has the
   *          specified privilege, or {@code false} if not.
   */
  public boolean hasPrivilege(Privilege privilege);

    /**
   * Indicates whether the authenticate client has all of the
   * specified privileges.
   *
   * @param  privileges  The array of privileges for which to make the
   *                     determination.
   *
   * @return  {@code true} if the authenticated client has all of the
   *          specified privileges, or {@code false} if not.
   */
  public boolean hasAllPrivileges(Privilege[] privileges);

   /**
   * Retrieves the unique identifier that is assigned to the client
   * connection that submitted this operation.
   *
   * @return  The unique identifier that is assigned to the client
   *          connection that submitted this operation.
   */
  long getConnectionID();

  /**
   * Retrieves the entry for the user as whom the client is
   * authenticated.
   *
   * @return  The entry for the user as whom the client is
   *          authenticated, or {@code null} if the client is
   *          unauthenticated.
   */
  Entry getAuthenticationEntry();

  /**
   * Retrieves the last successful bind request from the client.
   *
   * @return The last successful bind request or {@code null} if the
   *         client have not yet successfully bind.
   */
  BindRequest getBindRequest();

  /**
   * Returns the {@code InetSocketAddress} associated with the local system.
   *
   * @return The {@code InetSocketAddress} associated with the local system.
   */
  InetSocketAddress getLocalAddress();

  /**
   * Returns the {@code InetSocketAddress} associated with the remote system.
   *
   * @return The {@code InetSocketAddress} associated with the remote system.
   */
  InetSocketAddress getPeerAddress();

  /**
   * Retrieves the protocol that the client is using to communicate
   * with the Directory Server.
   *
   * @return  The protocol that the client is using to communicate
   *          with the Directory Server.
   */
  String getProtocol();

  /**
   * Returns the strongest cipher strength currently in use by the underlying
   * connection.
   *
   * @return The strongest cipher strength currently in use by the underlying
   *         connection.
   */
  int getSecurityStrengthFactor();

  /**
   * Retrieves the size limit that will be enforced for searches
   * performed using this client connection.
   *
   * @return  The size limit that will be enforced for searches
   *          performed using this client connection.
   */
  int getSizeLimit();

  /**
   * Retrieves the time limit that will be enforced for searches
   * performed using this client connection.
   *
   * @return  The time limit that will be enforced for searches
   *          performed using this client connection.
   */
  int getTimeLimit();

  /**
   * Retrieves the default maximum number of entries that should
   * checked for matches during a search.
   *
   * @return  The default maximum number of entries that should
   *          checked for matches during a search.
   */
  int getLookthroughLimit();
}
