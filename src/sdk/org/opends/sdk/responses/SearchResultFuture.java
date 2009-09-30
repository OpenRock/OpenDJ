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

package org.opends.sdk.responses;



import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opends.sdk.ErrorResultException;



/**
 * A handle which can be used to retrieve the Search result of an
 * asynchronous Search request.
 * <p>
 * TODO: this is a place holder - we could have methods for retrieving
 * the first entry, or iterating through the entries as they are
 * returned.
 */
public interface SearchResultFuture extends ResultFuture
{
  /**
   * Returns the current number of search result entries received from
   * the server.
   *
   * @return The current number of search result entries.
   */
  int getNumSearchResultEntries();



  /**
   * Returns the current number of search result references received
   * from the server.
   *
   * @return The current number of search result references.
   */
  int getNumSearchResultReferences();



  /**
   * {@inheritDoc}
   */
  SearchResult get() throws InterruptedException, ErrorResultException;



  /**
   * {@inheritDoc}
   */
  SearchResult get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ErrorResultException;



  /**
   * {@inheritDoc}
   */
  boolean cancel(boolean mayInterruptIfRunning);



  /**
   * {@inheritDoc}
   */
  int getMessageID();



  /**
   * {@inheritDoc}
   */
  boolean isCancelled();



  /**
   * {@inheritDoc}
   */
  boolean isDone();

}
