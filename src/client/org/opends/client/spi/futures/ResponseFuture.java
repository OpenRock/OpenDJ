package org.opends.client.spi.futures;

import org.opends.common.api.response.ResultResponse;
import org.opends.common.api.request.Request;

import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 1:42:16 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ResponseFuture extends Future<ResultResponse>
{
  public Request getRequest();

  /**
   *
   * @return
   * @throws InterruptedException
   * @throws org.opends.client.spi.ErrorResultException
   * @throws ExecutionException
   */
  public ResultResponse get()
      throws InterruptedException, ExecutionException;

  /**
   *
   * @param timeout
   * @param unit
   * @return
   * @throws InterruptedException
   * @throws TimeoutException
   * @throws org.opends.client.spi.ErrorResultException
   * @throws ExecutionException
   */
  public ResultResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ExecutionException;
}
