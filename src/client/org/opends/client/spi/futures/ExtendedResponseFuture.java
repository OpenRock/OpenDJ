package org.opends.client.spi.futures;

import org.opends.common.api.extended.ExtendedResponse;
import org.opends.common.api.extended.ExtendedRequest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:40:02 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ExtendedResponseFuture extends ResponseFuture
{
  public ExtendedRequest getRequest();

  public ExtendedResponse get()
      throws InterruptedException, ExecutionException;

  public ExtendedResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ExecutionException ;

    /**
   * Retrieves the number of intermediate responses received from
   * the server.
   *
   * @return
   */
  public int getNumIntermediateResponse();
}
