package org.opends.client.protocol.ldap;

import org.opends.client.api.ExtendedResponseHandler;
import org.opends.common.api.extended.ExtendedResponse;
import org.opends.common.api.extended.ExtendedOperation;
import org.opends.common.api.extended.ExtendedRequest;
import org.opends.common.api.extended.IntermediateResponse;

import java.util.concurrent.Semaphore;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 11, 2009 Time: 11:32:30
 * AM To change this template use File | Settings | File Templates.
 */
public final class ExtendedResponseFuture<T extends ExtendedOperation> extends
    ResultResponseFuture<ExtendedResponse<T>>
{
  private final Semaphore invokerLock;
  private final IntermediateResultInvoker intermediateInvoker =
      new IntermediateResultInvoker();

  private ExtendedResponseHandler<T> handler;

  public ExtendedResponseFuture(int messageID, ExtendedRequest<T> orginalRequest,
                              ExtendedResponseHandler<T> extendedResponseHandler,
                              LDAPConnection connection)
  {
    super(messageID, orginalRequest, extendedResponseHandler, connection);
    this.invokerLock = new Semaphore(1);
    this.handler = extendedResponseHandler;
  }

  private class IntermediateResultInvoker implements Runnable
  {
    IntermediateResponse<T> intermediateResult;

    public void run()
    {
      handler.handleIntermediateResponse(intermediateResult);
      invokerLock.release();
    }
  }

  @Override
  public synchronized void setResult(ExtendedResponse<T> result)
  {
    if(latch.getCount() > 0)
    {
      this.result = result;
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
      latch.countDown();
    }
  }

  synchronized void setResult(IntermediateResponse<T> intermediateResponse)
  {
    if(latch.getCount() > 0 && handler != null)
    {
      try
      {
        invokerLock.acquire();
        intermediateInvoker.intermediateResult = intermediateResponse;
        invokeHandler(intermediateInvoker);
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
      latch.countDown();
    }
  }

  @Override
  public void run()
  {
    super.run();
    invokerLock.release();
  }
}
