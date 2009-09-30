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

package org.opends.sdk.ldap;



import java.util.concurrent.ExecutorService;

import org.opends.sdk.ResultCode;
import org.opends.sdk.responses.Responses;
import org.opends.sdk.responses.SearchResult;
import org.opends.sdk.responses.SearchResultEntry;
import org.opends.sdk.responses.SearchResultFuture;
import org.opends.sdk.responses.SearchResultHandler;
import org.opends.sdk.responses.SearchResultReference;



/**
 * Search result future implementation.
 */
final class SearchResultFutureImpl extends
    AbstractResultFutureImpl<SearchResult> implements
    SearchResultFuture
{

  private int numSearchResultEntries = 0;

  private int numSearchResultReferences = 0;

  private final SearchResultHandler handler;



  SearchResultFutureImpl(int messageID, SearchResultHandler handler,
      LDAPConnection connection, ExecutorService handlerExecutor)
  {
    super(messageID, handler, connection, handlerExecutor);
    this.handler = handler;
  }



  public synchronized int getNumSearchResultEntries()
  {
    return numSearchResultEntries;
  }



  public synchronized int getNumSearchResultReferences()
  {
    return numSearchResultReferences;
  }



  synchronized void handleSearchResultEntry(
      final SearchResultEntry entry)
  {
    numSearchResultEntries++;
    if (!isDone())
    {
      invokeHandler(new Runnable()
      {
        public void run()
        {
          handler.handleEntry(entry);
        }
      });
    }
  }



  synchronized void handleSearchResultReference(
      final SearchResultReference reference)
  {
    numSearchResultReferences++;
    if (!isDone())
    {
      invokeHandler(new Runnable()
      {
        public void run()
        {
          handler.handleReference(reference);
        }
      });
    }
  }



  /**
   * {@inheritDoc}
   */
  SearchResult newErrorResult(ResultCode resultCode,
      String diagnosticMessage, Throwable cause)
  {
    return Responses.newSearchResult(resultCode).setDiagnosticMessage(
        diagnosticMessage).setCause(cause);
  }
}
