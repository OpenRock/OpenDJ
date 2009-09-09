package org.opends.sdk.ldap;



import java.util.concurrent.ExecutorService;

import org.opends.sdk.Responses;
import org.opends.sdk.ResultCode;
import org.opends.sdk.SearchResult;
import org.opends.sdk.SearchResultEntry;
import org.opends.sdk.SearchResultFuture;
import org.opends.sdk.SearchResultHandler;
import org.opends.sdk.SearchResultReference;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 2:19:38
 * PM To change this template use File | Settings | File Templates.
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
