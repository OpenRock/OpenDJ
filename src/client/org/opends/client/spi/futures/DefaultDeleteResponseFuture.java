package org.opends.client.spi.futures;

import org.opends.client.api.ResponseHandler;
import org.opends.client.spi.Connection;
import org.opends.common.api.response.DeleteResponse;
import org.opends.common.api.request.DeleteRequest;

import java.util.concurrent.ExecutorService;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 8, 2009
 * Time: 2:12:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultDeleteResponseFuture
    extends AbstractResponseFuture<DeleteRequest, DeleteResponse>
    implements DeleteResponseFuture
{
  public DefaultDeleteResponseFuture(int messageID, DeleteRequest request,
                                   ResponseHandler<DeleteResponse> addResponseHandler,
                                   Connection connection,
                                   ExecutorService handlerExecutor)
  {
    super(messageID, request, addResponseHandler, connection,
        handlerExecutor);
  }
}
