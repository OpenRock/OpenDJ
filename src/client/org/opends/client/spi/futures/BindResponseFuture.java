package org.opends.client.spi.futures;

import org.opends.common.api.response.BindResponse;
import org.opends.common.api.request.BindRequest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:04:48 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BindResponseFuture extends ResponseFuture
{
  public BindRequest getRequest();

  public BindResponse get()
      throws InterruptedException, ExecutionException;

  public BindResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ExecutionException;
}
