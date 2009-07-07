package org.opends.client.api.futures;

import org.opends.common.api.extended.ExtendedResponse;
import org.opends.common.api.extended.ExtendedRequest;
import org.opends.client.api.ExtendedRequestException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:40:02 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ExtendedResponseFuture
    extends ResponseFuture<ExtendedRequest, ExtendedResponse>
{
  public ExtendedResponse get()
      throws InterruptedException, ExtendedRequestException;

  public ExtendedResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ExtendedRequestException;
}
