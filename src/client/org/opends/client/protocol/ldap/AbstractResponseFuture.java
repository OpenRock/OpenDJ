package org.opends.client.protocol.ldap;

import org.opends.common.api.request.Request;
import org.opends.common.api.response.Response;
import org.opends.client.protocol.ldap.LDAPConnection;
import org.opends.client.api.futures.ResponseFuture;
import org.opends.client.api.ResponseHandler;

import java.util.concurrent.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 2:13:22 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractResponseFuture
    <Q extends Request, R extends Response> implements Runnable
{
  protected final CountDownLatch latch = new CountDownLatch(1);

  private final int messageID;
  private final LDAPConnection connection;
  protected final ResponseHandler<R> handler;
  protected final Q request;

  protected volatile R result;
  protected volatile boolean isCancelled;
  protected volatile Throwable failure;

  public AbstractResponseFuture(int messageID, Q request,
                        ResponseHandler<R> responseHandler,
                        LDAPConnection connection)
  {
    this.messageID = messageID;
    this.handler = responseHandler;
    this.connection = connection;
    this.request = request;

    isCancelled = false;
    failure = null;
  }

  public Q getRequest() {
    return request;
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
  public synchronized void setResult(R result)
  {
    if(latch.getCount() > 0)
    {
      this.result = result;
      if(handler != null)
      {
        invokeHandler(this);
      }
      latch.countDown();
    }
  }

  /**
   * Notify about the failure, occured during asynchronous operation execution.
   *
   * @param failure
   */
  public synchronized void failure(Throwable failure) {
    if(latch.getCount() > 0)
    {
      this.failure = failure;
      if(handler != null)
      {
        invokeHandler(this);
      }
      latch.countDown();
    }
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

  protected void invokeHandler(Runnable runnable)
  {
    connection.getConnFactory().getHandlerInvokers().submit(runnable);
  }

  public void run()
  {
    if(failure != null)
    {
      handler.handleException(failure);
    }
    else if(result != null)
    {
      handler.handleResult(result);
    }
  }
}
