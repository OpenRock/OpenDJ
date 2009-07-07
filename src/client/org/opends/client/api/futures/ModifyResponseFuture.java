package org.opends.client.api.futures;

import org.opends.common.api.request.ModifyRequest;
import org.opends.common.api.response.ModifyResponse;
import org.opends.client.api.ModifyRequestException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:36:23 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ModifyResponseFuture
    extends ResponseFuture<ModifyRequest, ModifyResponse>
{
  public ModifyResponse get()
      throws InterruptedException, ModifyRequestException;

  public ModifyResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ModifyRequestException;
}
