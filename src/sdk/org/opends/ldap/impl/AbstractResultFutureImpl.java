package org.opends.ldap.impl;



import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opends.ldap.Connection;
import org.opends.ldap.CompletionHandler;
import org.opends.ldap.requests.Requests;
import org.opends.ldap.responses.ErrorResultException;
import org.opends.ldap.responses.Result;
import org.opends.ldap.responses.ResultFuture;
import org.opends.types.ResultCode;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 1:25:07
 * PM To change this template use File | Settings | File Templates.
 */
abstract class AbstractResultFutureImpl<R extends Result> implements
    ResultFuture, Runnable
{
  private final Connection connection;
  private final CompletionHandler<R> handler;
  private final ExecutorService handlerExecutor;
  private final int messageID;
  private final Semaphore invokerLock = new Semaphore(1);
  private final CountDownLatch latch = new CountDownLatch(1);

  private volatile boolean isCancelled = false;
  private volatile R result = null;



  AbstractResultFutureImpl(int messageID, CompletionHandler<R> handler,
      Connection connection, ExecutorService handlerExecutor)
  {
    this.messageID = messageID;
    this.handler = handler;
    this.connection = connection;
    this.handlerExecutor = handlerExecutor;
  }



  public synchronized boolean cancel(boolean b)
  {
    if (!isDone())
    {
      isCancelled = true;
      connection.abandon(Requests.newAbandonRequest(messageID));
      latch.countDown();
      return true;
    }
    else
    {
      return false;
    }
  }



  public R get() throws InterruptedException, ErrorResultException
  {
    latch.await();
    return get0();
  }



  public R get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ErrorResultException
  {
    if (!latch.await(timeout, unit))
    {
      throw new TimeoutException();
    }
    return get0();
  }



  public int getMessageID()
  {
    return messageID;
  }



  public boolean isCancelled()
  {
    return isCancelled;
  }



  public boolean isDone()
  {
    return latch.getCount() == 0;
  }



  public void run()
  {
    if (result.getResultCode().isExceptional())
    {
      ErrorResultException e =
          ErrorResultException.wrap(result);
      handler.failed(e);
    }
    else
    {
      handler.completed(result);
    }
  }



  synchronized void handleErrorResult(Result result)
  {
    R errorResult =
        newErrorResult(result.getResultCode(), result
            .getDiagnosticMessage(), result.getCause());
    handleResult(errorResult);
  }



  abstract R newErrorResult(ResultCode resultCode,
      String diagnosticMessage, Throwable cause);



  void handleResult(R result)
  {
    if (!isDone())
    {
      this.result = result;
      latch.countDown();
      invokeHandler(this);
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



  private R get0() throws CancellationException, ErrorResultException
  {
    if (isCancelled())
    {
      throw new CancellationException();
    }
    else if (result.getResultCode().isExceptional())
    {
      throw ErrorResultException.wrap(result);
    }
    else
    {
      return result;
    }
  }
}
