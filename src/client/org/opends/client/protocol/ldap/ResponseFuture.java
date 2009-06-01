package org.opends.client.protocol.ldap;

import org.opends.common.api.raw.response.RawResponse;
import org.opends.common.api.raw.request.RawRequest;
import org.opends.client.api.ResponseHandler;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 29, 2009 Time: 11:56:23
 * AM To change this template use File | Settings | File Templates.
 */
public abstract class ResponseFuture<R extends RawResponse> implements Future<R>
{
  protected final CountDownLatch latch = new CountDownLatch(1);

  private final int messageID;
  private final RawRequest orginalRequest;
  private final LDAPConnection connection;

  protected volatile R result;
  protected volatile boolean isCancelled;
  protected volatile Throwable failure;

  public ResponseFuture(int messageID, RawRequest orginalRequest,
                        LDAPConnection connection)
  {
    this.messageID = messageID;
    this.orginalRequest = orginalRequest;
    this.connection = connection;

    isCancelled = false;
    failure = null;
  }

  /**
   * Get current result value without any blocking.
   *
   * @return current result value without any blocking.
   */
  public R getResult() {
    return result;
  }

  /**
   * Set the result value and notify about operation completion.
   *
   * @param result the result value
   */
  public abstract void setResult(R result);

  /**
   * Notify about the failure, occured during asynchronous operation execution.
   *
   * @param failure
   */
  public abstract void failure(Throwable failure);

  public RawRequest getOrginalRequest()
  {
    return orginalRequest;
  }

  public int getMessageID()
  {
    return messageID;
  }

  public synchronized boolean cancel(boolean b)
  {
    // TODO: Send cancel extended op.
    return true;
  }

  public synchronized void abandon() throws IOException
  {
    if(latch.getCount() > 0)
    {
      this.isCancelled = true;
      connection.abandonRequest(messageID);
      latch.countDown();
    }
  }

  public boolean isCancelled()
  {
    return this.isCancelled;
  }

  public boolean isDone()
  {
    return latch.getCount() == 0;
  }

  public R get() throws InterruptedException, ExecutionException
  {
    latch.await();

    if(failure != null)
    {
      throw new ExecutionException(failure);
    }
    if(isCancelled)
    {
      throw new CancellationException();
    }

    return result;
  }

  public R get(long l, TimeUnit timeUnit)
      throws InterruptedException, ExecutionException, TimeoutException
  {
    if(!latch.await(l, timeUnit))
    {
      throw new TimeoutException();
    }
    if(failure != null)
    {
      throw new ExecutionException(failure);
    }
    if(isCancelled)
    {
      throw new CancellationException();
    }

    return result;
  }

  protected void invokeHandler(Runnable runnable)
  {
    connection.getConnFactory().getHandlerInvokers().submit(runnable);
  }
}
