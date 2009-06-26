package org.opends.client.api;

import org.opends.common.api.response.Response;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 1, 2009 Time: 3:16:58
 * PM To change this template use File | Settings | File Templates.
 */
public interface ResponseHandler<R extends Response>
{
  public void handleResult(R result);

  public void handleException(Throwable t);
}
