package org.opends.ldap.futures;



import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

import org.opends.ldap.ErrorResultException;
import org.opends.ldap.LDAPConnection;
import org.opends.ldap.SearchResponseHandler;
import org.opends.ldap.requests.SearchRequest;
import org.opends.ldap.responses.SearchResultDone;
import org.opends.ldap.responses.SearchResultEntry;
import org.opends.ldap.responses.SearchResultReference;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 2:19:38
 * PM To change this template use File | Settings | File Templates.
 */
public final class DefaultSearchResponseFuture extends
    AbstractResponseFuture<SearchRequest, SearchResultDone> implements
    SearchResponseFuture
{
  private class SearchResultEntryInvoker implements Runnable
  {
    SearchResultEntry entry;



    public void run()
    {
      ((SearchResponseHandler) handler).handleSearchResultEntry(entry);
      invokerLock.release();
    }
  }

  private class SearchResultReferenceInvoker implements Runnable
  {
    SearchResultReference reference;



    public void run()
    {
      ((SearchResponseHandler) handler)
          .handleSearchResultReference(reference);
      invokerLock.release();
    }
  }



  private final Semaphore invokerLock;
  private final SearchResultReferenceInvoker referenceInvoker =
      new SearchResultReferenceInvoker();
  private final SearchResultEntryInvoker entryInvoker =
      new SearchResultEntryInvoker();

  private int numSearchResultEntries;

  private int numSearchResultReferences;



  public DefaultSearchResponseFuture(int messageID,
      SearchRequest orginalRequest,
      SearchResponseHandler searchResponseHandler,
      LDAPConnection connection, ExecutorService handlerExecutor)
  {
    super(messageID, orginalRequest, searchResponseHandler, connection,
        handlerExecutor);
    this.invokerLock = new Semaphore(1);
  }



  @Override
  public synchronized void failure(Throwable failure)
  {
    if (latch.getCount() > 0)
    {
      if (failure instanceof ExecutionException)
      {
        this.failure = (ExecutionException) failure;
      }
      else
      {
        this.failure = new ExecutionException(failure);
      }
      if (handler != null)
      {
        try
        {
          invokerLock.acquire();
          invokeHandler(this);
        }
        catch (InterruptedException ie)
        {
          // TODO: What should we do now?
        }
      }
      latch.countDown();
    }
  }



  public synchronized int getNumSearchResultEntries()
  {
    return numSearchResultEntries;
  }



  public synchronized int getNumSearchResultReferences()
  {
    return numSearchResultReferences;
  }



  @Override
  public void run()
  {
    super.run();
    invokerLock.release();
  }



  @Override
  public synchronized void setResult(SearchResultDone result)
  {
    if (latch.getCount() > 0)
    {
      if (result.getResultCode().isExceptional())
      {
        this.failure = new ErrorResultException(result);
      }
      else
      {
        this.result = result;
      }
      if (handler != null)
      {
        try
        {
          invokerLock.acquire();
          invokeHandler(this);
        }
        catch (InterruptedException ie)
        {
          // TODO: What should we do now?
        }
      }
      latch.countDown();
    }
  }



  public synchronized void setResult(SearchResultEntry entry)
  {
    numSearchResultEntries++;
    if ((latch.getCount() > 0) && (handler != null))
    {
      try
      {
        invokerLock.acquire();
        entryInvoker.entry = entry;
        invokeHandler(entryInvoker);
      }
      catch (InterruptedException ie)
      {
        // TODO: What should we do now?
      }
    }
  }



  public synchronized void setResult(SearchResultReference reference)
  {
    numSearchResultReferences++;
    if ((latch.getCount() > 0) && (handler != null))
    {
      try
      {
        invokerLock.acquire();
        referenceInvoker.reference = reference;
        invokeHandler(referenceInvoker);
      }
      catch (InterruptedException ie)
      {
        // TODO: What should we do now?
      }
    }
  }
}
