package org.opends.client.protocol.ldap;

import org.opends.common.api.raw.response.RawSearchResultDone;
import org.opends.common.api.raw.response.RawSearchResultEntry;
import org.opends.common.api.raw.response.RawSearchResultReference;
import org.opends.common.api.raw.request.RawRequest;
import org.opends.client.api.SearchResponseHandler;

import java.util.concurrent.SynchronousQueue;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 1, 2009 Time: 3:15:40
 * PM To change this template use File | Settings | File Templates.
 */
public final class SearchResponseFuture extends ResponseFuture<RawSearchResultDone>
{
  private static final Object INTERRUPT_RESPONSE = new Object();

  private volatile SearchResultHandlerInvoker invoker;
  private SearchResponseHandler handler;

  public SearchResponseFuture(int messageID, RawRequest orginalRequest,
                              SearchResponseHandler searchResponseHandler,
                              LDAPConnection connection)
  {
    super(messageID, orginalRequest, connection);
    this.handler = searchResponseHandler;
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
        if(invoker == null)
        {
          invoker = new SearchResultHandlerInvoker();
          invokeHandler(invoker);
        }
        else
        {
          try
          {
            invoker.searchResultSync.put(result);
          }
          catch(InterruptedException ie)
          {
            // TODO: What should we do now?
          }
        }
      }
    }
  }

  synchronized void setResult(RawSearchResultEntry entry)
  {
    if(latch.getCount() > 0 && handler != null)
    {
      if(invoker == null)
      {
        invoker = new SearchResultHandlerInvoker();
        invokeHandler(invoker);
      }
      try
      {
        invoker.searchResultSync.put(entry);
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
      if(invoker == null)
      {
        invoker = new SearchResultHandlerInvoker();
        invokeHandler(invoker);
      }
      try
      {
        invoker.searchResultSync.put(reference);
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
        if(invoker == null)
        {
          invoker = new SearchResultHandlerInvoker();
          invokeHandler(invoker);
        }
        else
        {
          invoker.searchResultSync.offer(INTERRUPT_RESPONSE);
        }
      }
    }
  }

  @Override
  public synchronized boolean cancel(boolean b)
  {
    return super.cancel(b);
  }

  @Override
  public synchronized void abandon() throws IOException
  {
    super.abandon();
    if(invoker != null)
    {
      invoker.searchResultSync.offer(INTERRUPT_RESPONSE);
    }
  }


  private class SearchResultHandlerInvoker implements Runnable
  {
    SynchronousQueue<Object> searchResultSync = new SynchronousQueue<Object>();

    public void run()
    {
      // First see if we already have a result
      if(result != null)
      {
        handler.handleResult(result);
      }
      else while(failure == null && !isCancelled)
      {
        // No result so go take results from the queue.
        try
        {
          Object result = searchResultSync.take();
          if(result instanceof RawSearchResultEntry)
          {
            handler.handleSearchResultEntry((RawSearchResultEntry)result);
          }
          else if(result instanceof RawSearchResultDone)
          {
            handler.handleResult((RawSearchResultDone)result);
            break;
          }
          else if(result instanceof RawSearchResultReference)
          {
            handler.handleSearchResultReference(
                (RawSearchResultReference)result);
          }
          else if(result == INTERRUPT_RESPONSE)
          {
            // Hacky interrupt signal
          }
        }
        catch(InterruptedException ie)
        {
          // Maybe stop requested. Go around the loop check.
        }
      }
      if(failure != null)
      {
        handler.handleException(failure);
      }
    }
  }
}
