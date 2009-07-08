package org.opends.client.spi.futures;

import org.opends.common.api.request.DeleteRequest;
import org.opends.common.api.response.DeleteResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:31:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DeleteResponseFuture extends ResponseFuture
{
  public DeleteRequest getRequest();

  public DeleteResponse get()
      throws InterruptedException, ExecutionException;

  public DeleteResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ExecutionException;
}

