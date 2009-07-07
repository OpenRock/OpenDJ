package org.opends.client.api.futures;

import org.opends.common.api.response.AddResponse;
import org.opends.common.api.request.AddRequest;
import org.opends.client.api.AddRequestException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 1:43:39 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AddResponseFuture
    extends ResponseFuture<AddRequest, AddResponse>
{
  public AddResponse get()
      throws InterruptedException, AddRequestException;

  public AddResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      AddRequestException;
}
