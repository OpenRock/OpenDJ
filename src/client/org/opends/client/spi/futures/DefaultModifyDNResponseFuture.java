package org.opends.client.spi.futures;

import org.opends.client.api.ResponseHandler;
import org.opends.client.spi.Connection;
import org.opends.common.api.response.ModifyDNResponse;
import org.opends.common.api.request.ModifyDNRequest;

import java.util.concurrent.ExecutorService;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 8, 2009
 * Time: 2:17:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultModifyDNResponseFuture
    extends AbstractResponseFuture<ModifyDNRequest, ModifyDNResponse>
    implements ModifyDNResponseFuture
{
  public DefaultModifyDNResponseFuture(int messageID, ModifyDNRequest request,
                                   ResponseHandler<ModifyDNResponse> addResponseHandler,
                                   Connection connection,
                                   ExecutorService handlerExecutor)
  {
    super(messageID, request, addResponseHandler, connection,
        handlerExecutor);
  }
}
