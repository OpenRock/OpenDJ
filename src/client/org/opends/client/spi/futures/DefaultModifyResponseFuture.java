package org.opends.client.spi.futures;

import org.opends.client.api.ResponseHandler;
import org.opends.client.spi.Connection;
import org.opends.common.api.response.ModifyResponse;
import org.opends.common.api.request.ModifyRequest;

import java.util.concurrent.ExecutorService;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 8, 2009
 * Time: 2:19:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultModifyResponseFuture
    extends AbstractResponseFuture<ModifyRequest, ModifyResponse>
    implements ModifyResponseFuture
{
  public DefaultModifyResponseFuture(int messageID, ModifyRequest request,
                                   ResponseHandler<ModifyResponse> addResponseHandler,
                                   Connection connection,
                                   ExecutorService handlerExecutor)
  {
    super(messageID, request, addResponseHandler, connection,
        handlerExecutor);
  }
}
