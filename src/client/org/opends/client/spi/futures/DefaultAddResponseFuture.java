package org.opends.client.spi.futures;

import org.opends.common.api.request.AddRequest;
import org.opends.common.api.response.AddResponse;
import org.opends.client.api.ResponseHandler;
import org.opends.client.spi.Connection;

import java.util.concurrent.ExecutorService;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 8, 2009
 * Time: 1:50:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultAddResponseFuture
    extends AbstractResponseFuture<AddRequest, AddResponse>
    implements AddResponseFuture
{
  public DefaultAddResponseFuture(int messageID, AddRequest request,
                                   ResponseHandler<AddResponse> addResponseHandler,
                                   Connection connection,
                                   ExecutorService handlerExecutor)
  {
    super(messageID, request, addResponseHandler, connection,
        handlerExecutor);
  }
}
