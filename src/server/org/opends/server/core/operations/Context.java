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



import org.opends.server.types.DN;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.Entry;



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
public interface Context
{

  /**
   * Indicates whether the specified entry exists in this request
   * context.
   * <p>
   * TODO: locking, isolation?
   *
   * @param dn
   *          The DN of the entry for which to make the determination.
   * @return {@code true} if the specified entry exists in one of this
   *         request context, or {@code false} if it does not.
   * @throws DirectoryException
   *           If a problem occurs while attempting to make the
   *           determination.
   */
  boolean entryExists(DN dn) throws DirectoryException;



  /**
   * Searches this request context using the provided request and
   * response handler.
   * <p>
   * TODO: we need to support asynchronous internal requests. It would
   * be nice to make this as simple as possible and transparent to
   * implementations that want a simple synchronous model. Maybe use the
   * IOU pattern - Future?
   *
   * @param request
   *          The search request.
   * @param handler
   *          The search response handler.
   * @throws DirectoryException
   *           If a problem occurs while attempting to perform the
   *           search.
   */
  void executeSearch(SearchRequest request,
      SearchResponseHandler handler) throws DirectoryException;



  /**
   * Returns the named entry from this request context.
   * <p>
   * TODO: locking, isolation?
   *
   * @param dn
   *          The DN of the entry to return.
   * @return The requested entry, or {@code null} if it does not exist.
   * @throws DirectoryException
   *           If a problem occurs while attempting to retrieve the
   *           entry.
   */
  Entry getEntry(DN dn) throws DirectoryException;



  /**
   * Returns the message ID assigned to this request context.
   *
   * @return The message ID assigned to this request context.
   */
  int getMessageID();



  /**
   * Returns the request ID assigned to this request context.
   *
   * @return The request ID assigned to this request context.
   */
  long getRequestID();



  /**
   * Gets the schema associated with this context.
   * <p>
   * The returned schema will create new object classes, attribute types
   * and syntaxes on demand.
   *
   * @return The schema.
   */
  Schema getSchema();



  /**
   * Gets the strict schema associated with this context.
   * <p>
   * The returned schema will not create new object classes, attribute
   * types and syntaxes on demand.
   *
   * @return The strict schema.
   */
  Schema getStrictSchema();



  /**
   * Indicates whether the request originated from an external client.
   *
   * @return {@code true} if the request originated from an external
   *         client.
   */
  boolean isClientRequest();



  /**
   * Indicates whether the request originated internally.
   *
   * @return {@code true} if the request originated internally.
   */
  boolean isInternalRequest();



  /**
   * Indicates whether the request needs to be synchronized to other
   * copies of the data.
   *
   * @return {@code true} if the request should be synchronized to other
   *         copies of the data.
   */
  boolean isSynchronizationNeeded();



  /**
   * Indicates whether the is a synchronization request.
   *
   * @return {@code true} if the is a synchronization request.
   */
  boolean isSynchronizationRequest();
}
