package org.opends.ldap.impl;



import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opends.ldap.Connection;
import org.opends.ldap.ResponseHandler;
import org.opends.ldap.requests.AbandonRequest;
import org.opends.ldap.requests.Request;
import org.opends.ldap.responses.ErrorResultException;
import org.opends.ldap.responses.Result;
import org.opends.ldap.responses.ResultFuture;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 1:25:07
 * PM To change this template use File | Settings | File Templates.
 */
public class ResultFutureImpl<Q extends Request, R extends Result>
    implements ResultFuture, Runnable
{
  private final CountDownLatch latch = new CountDownLatch(1);

  private final int messageID;
  private final Connection connection;
  private final ExecutorService handlerExecutor;
  private final ResponseHandler<R> handler;
  private final Q request;

  private volatile R result = null;
  private volatile boolean isCancelled = false;
  private volatile ExecutionException failure = null;

  private final Semaphore invokerLock = new Semaphore(1);



  public ResultFutureImpl(int messageID, Q request,
      ResponseHandler<R> handler, Connection connection,
      ExecutorService handlerExecutor)
  {
    this.messageID = messageID;
    this.handler = handler;
    this.connection = connection;
    this.handlerExecutor = handlerExecutor;
    this.request = request;
  }



  public synchronized boolean cancel(boolean b)
  {
    if (!isDone())
    {
      this.isCancelled = true;
      connection.abandon(new AbandonRequest(messageID));
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
    if (!isDone())
    {
      if (failure instanceof ExecutionException)
      {
        this.failure = (ExecutionException) failure;
      }
      else
      {
        this.failure = new ExecutionException(failure);
      }
      invokeHandler(this);
      latch.countDown();
    }
  }



  public R get() throws InterruptedException, ErrorResultException
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
      throws InterruptedException, TimeoutException,
      ErrorResultException
  {
    if (!latch.await(timeout, unit))
    {
      throw new TimeoutException();
    }
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
  public synchronized void handleResult(R result)
  {
    if (!isDone())
    {
      if (result.getResultCode().isExceptional())
      {
        this.failure = new ErrorResultException(result);
      }
      else
      {
        this.result = result;
      }
      invokeHandler(this);
      latch.countDown();
    }
  }



  protected void invokeHandler(final Runnable runnable)
  {
    if (handler == null)
    {
      return;
    }

    try
    {
      invokerLock.acquire();

      handlerExecutor.submit(new Runnable()
      {
        public void run()
        {
          runnable.run();
          invokerLock.release();
        }
      });
    }
    catch (InterruptedException e)
    {
      // TODO: what should we do now?
    }
  }
}
