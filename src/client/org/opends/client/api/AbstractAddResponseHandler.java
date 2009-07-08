package org.opends.client.api;

import org.opends.common.api.response.AddResponse;
import org.opends.client.spi.ErrorResultException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 8, 2009
 * Time: 1:53:38 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractAddResponseHandler
    implements ResponseHandler<AddResponse>
{
  public void handleErrorResult(ErrorResultException result) {
    handleException(result);
  }
}
