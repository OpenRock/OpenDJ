package org.opends.util;



import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 27, 2009 Time:
 * 12:51:25 PM To change this template use File | Settings | File
 * Templates.
 */
public class PendingFuture<R> implements Future<R>
{
  private final CountDownLatch latch = new CountDownLatch(1);
  private volatile R result;
  private volatile boolean isCancelled;
  private volatile Throwable failure;



  public boolean cancel(boolean b)
  {
    this.isCancelled = true;
    latch.countDown();
    return true;
  }



  /**
   * Notify about the failure, occured during asynchronous operation
   * execution.
   * 
   * @param failure
   */
  public void failure(Throwable failure)
  {
    this.failure = failure;
    latch.countDown();
  }



  public R get() throws InterruptedException, ExecutionException
  {
    latch.await();

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



  public R get(long l, TimeUnit timeUnit) throws InterruptedException,
      ExecutionException, TimeoutException
  {
    if (!latch.await(l, timeUnit))
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
    return isCancelled();
  }



  public boolean isDone()
  {
    return latch.getCount() == 0;
  }



  /**
   * Set the result value and notify about operation completion.
   * 
   * @param result
   *          the result value
   */
  public void setResult(R result)
  {
    this.result = result;
    latch.countDown();
  }

}
