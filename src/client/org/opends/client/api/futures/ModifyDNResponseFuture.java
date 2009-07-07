package org.opends.client.api.futures;

import org.opends.common.api.request.ModifyDNRequest;
import org.opends.common.api.response.ModifyDNResponse;
import org.opends.client.api.ModifyDNRequestException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:35:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ModifyDNResponseFuture
    extends ResponseFuture<ModifyDNRequest, ModifyDNResponse>
{
  public ModifyDNResponse get()
      throws InterruptedException, ModifyDNRequestException;

  public ModifyDNResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ModifyDNRequestException;
}
