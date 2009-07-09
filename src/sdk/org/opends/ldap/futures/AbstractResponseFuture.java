package org.opends.ldap.futures;



import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opends.ldap.Connection;
import org.opends.ldap.ErrorResultException;
import org.opends.ldap.ResponseHandler;
import org.opends.ldap.requests.AbandonRequest;
import org.opends.ldap.requests.Request;
import org.opends.ldap.responses.ResultResponse;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 1:25:07
 * PM To change this template use File | Settings | File Templates.
 */
public abstract class AbstractResponseFuture<Q extends Request, R extends ResultResponse>
    implements ResponseFuture, Runnable
{
  protected final CountDownLatch latch = new CountDownLatch(1);

  private final int messageID;
  private final Connection connection;
  private final ExecutorService handlerExecutor;
  protected final ResponseHandler<R> handler;
  protected final Q request;

  protected volatile R result;
  protected volatile boolean isCancelled;
  protected volatile ExecutionException failure;



  public AbstractResponseFuture(int messageID, Q request,
      ResponseHandler<R> responseHandler, Connection connection,
      ExecutorService handlerExecutor)
  {
    this.messageID = messageID;
    this.handler = responseHandler;
    this.connection = connection;
    this.handlerExecutor = handlerExecutor;
    this.request = request;

    isCancelled = false;
    failure = null;
  }



  public synchronized boolean cancel(boolean b)
  {
    if (latch.getCount() > 0)
    {
      this.isCancelled = true;
      connection.abandonRequest(new AbandonRequest(messageID));
      latch.countDown();
    }
    return true;
  }



  /**
   * Notify about the failure, occured during asynchronous operation
   * execution.
   * 
   * @param failure
   */
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
        invokeHandler(this);
      }
      latch.countDown();
    }
  }



  public R get() throws InterruptedException, ExecutionException
  {
    latch.await();

    if (failure != null)
    {
      throw failure;
    }
    if (isCancelled)
    {
      throw new CancellationException();
    }

    return result;
  }



  public R get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException, ExecutionException
  {
    if (!latch.await(timeout, unit))
    {
      throw new TimeoutException();
    }
    if (failure != null)
    {
      throw new ExecutionException(failure);
    }
    if (isCancelled)
    {
      throw new CancellationException();
    }

    return result;
  }



  public int getMessageID()
  {
    return messageID;
  }



  public Q getRequest()
  {
    return request;
  }



  /**
   * Get current result value without any blocking.
   * 
   * @return current result value without any blocking.
   */
  public R getResult()
  {
    return result;
  }



  public boolean isCancelled()
  {
    return this.isCancelled;
  }



  public boolean isDone()
  {
    return latch.getCount() == 0;
  }



  public void run()
  {
    if (failure != null)
    {
      if (failure instanceof ErrorResultException)
      {
        handler.handleErrorResult((ErrorResultException) failure);
      }
      else
      {
        handler.handleException(failure);
      }
    }
    else if (result != null)
    {
      handler.handleResult(result);
    }
  }



  /**
   * Set the result value and notify about operation completion.
   * 
   * @param result
   *          the result value
   */
  public synchronized void setResult(R result)
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
        invokeHandler(this);
      }
      latch.countDown();
    }
  }



  protected void invokeHandler(Runnable runnable)
  {
    handlerExecutor.submit(runnable);
  }
}
