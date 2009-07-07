package org.opends.client.api.futures;

import org.opends.common.api.request.BindRequest;
import org.opends.common.api.request.CompareRequest;
import org.opends.common.api.response.BindResponse;
import org.opends.common.api.response.CompareResponse;
import org.opends.client.api.BindRequestException;
import org.opends.client.api.CompareRequestException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:28:25 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CompareResponseFuture 
    extends ResponseFuture<CompareRequest, CompareResponse>
{
  public CompareResponse get()
      throws InterruptedException, CompareRequestException;

  public CompareResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      CompareRequestException;
}
