package org.opends.client.protocol.ldap;

import org.opends.client.api.ExtendedResponseHandler;
import org.opends.client.api.ExtendedRequestException;
import org.opends.client.api.futures.ExtendedResponseFuture;
import org.opends.common.api.extended.ExtendedResponse;
import org.opends.common.api.extended.ExtendedOperation;
import org.opends.common.api.extended.ExtendedRequest;
import org.opends.common.api.extended.IntermediateResponse;

import java.util.concurrent.Semaphore;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 11, 2009 Time: 11:32:30
 * AM To change this template use File | Settings | File Templates.
 */
public final class ExtendedResponseFutureImpl
    extends AbstractResponseFuture<ExtendedRequest, ExtendedResponse>
    implements ExtendedResponseFuture
{
  private final Semaphore invokerLock;
  private final IntermediateResultInvoker intermediateInvoker =
      new IntermediateResultInvoker();

  public ExtendedResponseFutureImpl(int messageID, ExtendedRequest orginalRequest,
                                    ExtendedResponseHandler extendedResponseHandler,
                                    LDAPConnection connection)
  {
    super(messageID, orginalRequest, extendedResponseHandler, connection);
    this.invokerLock = new Semaphore(1);
  }

  private class IntermediateResultInvoker implements Runnable
  {
    IntermediateResponse intermediateResult;

    public void run()
    {
      ((ExtendedResponseHandler)handler).handleIntermediateResponse(
          intermediateResult);
      invokerLock.release();
    }
  }

  @Override
  public synchronized void setResult(ExtendedResponse result)
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

  synchronized void setResult(IntermediateResponse intermediateResponse)
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

  public ExtendedResponse get()
      throws InterruptedException, ExtendedRequestException
  {
    latch.await();

    if(failure != null)
    {
      throw new ExtendedRequestException(failure);
    }
    if(isCancelled)
    {
      throw new CancellationException();
    }

    return result;
  }

  public ExtendedResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ExtendedRequestException
  {
    if(!latch.await(timeout, unit))
    {
      throw new TimeoutException();
    }
    if(failure != null)
    {
      throw new ExtendedRequestException(failure);
    }
    if(isCancelled)
    {
      throw new CancellationException();
    }

    return result;
  }

  @Override
  public void run()
  {
    super.run();
    invokerLock.release();
  }
}
