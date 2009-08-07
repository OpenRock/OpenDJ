package org.opends.ldap.impl;



import java.util.concurrent.ExecutorService;

import org.opends.ldap.SearchResponseHandler;
import org.opends.ldap.responses.Responses;
import org.opends.ldap.responses.SearchResult;
import org.opends.ldap.responses.SearchResultEntry;
import org.opends.ldap.responses.SearchResultFuture;
import org.opends.ldap.responses.SearchResultReference;
import org.opends.types.ResultCode;



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

  private final SearchResponseHandler handler;



  SearchResultFutureImpl(int messageID, SearchResponseHandler handler,
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
          handler.handleSearchResultEntry(entry);
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
          handler.handleSearchResultReference(reference);
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
