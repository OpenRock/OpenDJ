package org.opends.client.spi.futures;

import org.opends.common.api.request.ModifyDNRequest;
import org.opends.common.api.response.ModifyDNResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:35:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ModifyDNResponseFuture extends ResponseFuture
{
  public ModifyDNRequest getRequest();

  public ModifyDNResponse get()
      throws InterruptedException, ExecutionException;

  public ModifyDNResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ExecutionException;
}
