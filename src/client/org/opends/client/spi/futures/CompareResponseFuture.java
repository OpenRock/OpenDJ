package org.opends.client.spi.futures;

import org.opends.common.api.request.CompareRequest;
import org.opends.common.api.response.CompareResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:28:25 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CompareResponseFuture extends ResponseFuture
{
  public CompareRequest getRequest();

  public CompareResponse get()
      throws InterruptedException, ExecutionException;

  public CompareResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ExecutionException;
}
