package org.opends.client.spi.futures;

import org.opends.common.api.request.ModifyRequest;
import org.opends.common.api.response.ModifyResponse;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:36:23 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ModifyResponseFuture extends ResponseFuture
{
  public ModifyRequest getRequest();
  
  public ModifyResponse get()
      throws InterruptedException, ExecutionException;

  public ModifyResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ExecutionException;
}
