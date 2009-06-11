package org.opends.client.protocol.ldap;

import org.opends.common.api.raw.response.RawSearchResultDone;
import org.opends.common.api.raw.response.RawSearchResultEntry;
import org.opends.common.api.raw.response.RawSearchResultReference;
import org.opends.common.api.raw.request.RawRequest;
import org.opends.client.api.SearchResponseHandler;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.Semaphore;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 1, 2009 Time: 3:15:40
 * PM To change this template use File | Settings | File Templates.
 */
public final class SearchResponseFuture 
    extends ResultResponseFuture<RawSearchResultDone>
{
  private final Semaphore invokerLock;
  private final SearchResultReferenceInvoker referenceInvoker =
      new SearchResultReferenceInvoker();
  private final SearchResultEntryInvoker entryInvoker =
      new SearchResultEntryInvoker();

  private SearchResponseHandler handler;

  public SearchResponseFuture(int messageID, RawRequest orginalRequest,
                              SearchResponseHandler searchResponseHandler,
                              LDAPConnection connection)
  {
    super(messageID, orginalRequest, searchResponseHandler, connection);
    this.invokerLock = new Semaphore(1);
    this.handler = searchResponseHandler;
  }

  private class SearchResultEntryInvoker implements Runnable
  {
    RawSearchResultEntry entry;

    public void run()
    {
      handler.handleSearchResultEntry(entry);
      invokerLock.release();
    }
  }

  private class SearchResultReferenceInvoker implements Runnable
  {
    RawSearchResultReference reference;

    public void run()
    {
      handler.handleSearchResultReference(reference);
      invokerLock.release();
    }
  }

  @Override
  public synchronized void setResult(RawSearchResultDone result)
  {
    if(latch.getCount() > 0)
    {
      this.result = result;
      latch.countDown();
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
    }
  }

  synchronized void setResult(RawSearchResultEntry entry)
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

  synchronized void setResult(RawSearchResultReference reference)
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
      latch.countDown();
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
    }
  }

  @Override
  public void run()
  {
    super.run();
    invokerLock.release();
  }
}
