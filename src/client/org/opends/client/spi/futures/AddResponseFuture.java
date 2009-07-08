package org.opends.client.spi.futures;

import org.opends.common.api.response.AddResponse;
import org.opends.common.api.request.AddRequest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 1:43:39 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AddResponseFuture extends ResponseFuture
{
  public AddRequest getRequest();

  public AddResponse get()
      throws InterruptedException, ExecutionException;

  public AddResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ExecutionException;
}
