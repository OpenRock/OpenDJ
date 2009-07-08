package org.opends.client.spi.futures;

import org.opends.common.api.response.BindResponse;
import org.opends.common.api.request.BindRequest;
import org.opends.client.api.ResponseHandler;
import org.opends.client.spi.Connection;

import java.util.concurrent.ExecutorService;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 8, 2009
 * Time: 1:52:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultBindResponseFuture
    extends AbstractResponseFuture<BindRequest, BindResponse>
    implements BindResponseFuture
{
  public DefaultBindResponseFuture(int messageID, BindRequest request,
                                   ResponseHandler<BindResponse> addResponseHandler,
                                   Connection connection,
                                   ExecutorService handlerExecutor)
  {
    super(messageID, request, addResponseHandler, connection,
        handlerExecutor);
  }
}

