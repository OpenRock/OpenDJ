package org.opends.client.protocol.ldap;

import org.opends.common.api.response.SearchResultDone;
import org.opends.common.api.response.SearchResultEntry;
import org.opends.common.api.response.SearchResultReference;
import org.opends.common.api.request.Request;
import org.opends.client.api.SearchResponseHandler;

import java.util.concurrent.Semaphore;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 1, 2009 Time: 3:15:40
 * PM To change this template use File | Settings | File Templates.
 */
public final class SearchResponseFuture 
    extends ResultResponseFuture<SearchResultDone>
{
  private final Semaphore invokerLock;
  private final SearchResultReferenceInvoker referenceInvoker =
      new SearchResultReferenceInvoker();
  private final SearchResultEntryInvoker entryInvoker =
      new SearchResultEntryInvoker();

  private SearchResponseHandler handler;

  public SearchResponseFuture(int messageID, Request orginalRequest,
                              SearchResponseHandler searchResponseHandler,
                              LDAPConnection connection)
  {
    super(messageID, orginalRequest, searchResponseHandler, connection);
    this.invokerLock = new Semaphore(1);
    this.handler = searchResponseHandler;
  }

  private class SearchResultEntryInvoker implements Runnable
  {
    SearchResultEntry entry;

    public void run()
    {
      handler.handleSearchResultEntry(entry);
      invokerLock.release();
    }
  }

  private class SearchResultReferenceInvoker implements Runnable
  {
    SearchResultReference reference;

    public void run()
    {
      handler.handleSearchResultReference(reference);
      invokerLock.release();
    }
  }

  @Override
  public synchronized void setResult(SearchResultDone result)
  {
    if(latch.getCount() > 0)
    {
      this.result = result;
      if(handler != null)
      {
        try
        {
          invokerLock.acquire();
          invokeHandler(this);
        }
        catch(InterruptedException ie)
        {
          // TODO: What should we do now?
        }
      }
      latch.countDown();
    }
  }

  synchronized void setResult(SearchResultEntry entry)
  {
    if(latch.getCount() > 0 && handler != null)
    {
      try
      {
        invokerLock.acquire();
        entryInvoker.entry = entry;
        invokeHandler(entryInvoker);
      }
      catch(InterruptedException ie)
      {
        // TODO: What should we do now?
      }
    }
  }

  synchronized void setResult(SearchResultReference reference)
  {
    if(latch.getCount() > 0 && handler != null)
    {
      try
      {
        invokerLock.acquire();
        referenceInvoker.reference = reference;
        invokeHandler(referenceInvoker);
      }
      catch(InterruptedException ie)
      {
        // TODO: What should we do now?
      }
    }
  }

  @Override
  public synchronized void failure(Throwable failure)
  {
    if(latch.getCount() > 0)
    {
      this.failure = failure;
      if(handler != null)
      {
        try
        {
          invokerLock.acquire();
          invokeHandler(this);
        }
        catch(InterruptedException ie)
        {
          // TODO: What should we do now?
        }
      }
      latch.countDown();
    }
  }

  @Override
  public void run()
  {
    super.run();
    invokerLock.release();
  }
}
