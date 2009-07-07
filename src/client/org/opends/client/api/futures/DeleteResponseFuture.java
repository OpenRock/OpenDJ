package org.opends.client.api.futures;

import org.opends.common.api.request.CompareRequest;
import org.opends.common.api.request.DeleteRequest;
import org.opends.common.api.response.CompareResponse;
import org.opends.common.api.response.DeleteResponse;
import org.opends.client.api.CompareRequestException;
import org.opends.client.api.DeleteRequestException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:31:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DeleteResponseFuture 
    extends ResponseFuture<DeleteRequest, DeleteResponse>
{
  public DeleteResponse get()
      throws InterruptedException, DeleteRequestException;

  public DeleteResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      DeleteRequestException;
}

