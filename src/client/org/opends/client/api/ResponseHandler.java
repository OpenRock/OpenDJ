package org.opends.client.api;

import org.opends.common.api.response.ResultResponse;
import org.opends.client.spi.ErrorResultException;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 1, 2009 Time: 3:16:58
 * PM To change this template use File | Settings | File Templates.
 */
public interface ResponseHandler<R extends ResultResponse>
{
  public void handleResult(R result);

  public void handleErrorResult(ErrorResultException result);
  
  public void handleException(ExecutionException e);
}
