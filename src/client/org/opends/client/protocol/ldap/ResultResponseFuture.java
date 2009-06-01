package org.opends.client.protocol.ldap;

import org.opends.common.api.raw.response.RawResultResponse;
import org.opends.common.api.raw.request.RawRequest;
import org.opends.client.api.ResponseHandler;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 1, 2009 Time: 4:40:56
 * PM To change this template use File | Settings | File Templates.
 */
public final class ResultResponseFuture<R extends RawResultResponse>
    extends ResponseFuture<R>
{
  protected final ResponseHandler<R> handler;

   public ResultResponseFuture(int messageID, RawRequest orginalRequest,
                               ResponseHandler<R> responseHandler,
                               LDAPConnection connection)
  {
    super(messageID, orginalRequest, connection);
    this.handler = responseHandler;
  }

    private class ResponseHandlerInvoker implements Runnable
  {
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
      latch.countDown();
      if(handler != null)
      {
        invokeHandler(new ResponseHandlerInvoker());
      }
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
      latch.countDown();
      if(handler != null)
      {
        invokeHandler(new ResponseHandlerInvoker());
      }
    }
  }
}
