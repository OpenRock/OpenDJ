package org.opends.client.spi.futures;

import org.opends.common.api.response.CompareResponse;
import org.opends.common.api.request.CompareRequest;
import org.opends.client.api.ResponseHandler;
import org.opends.client.spi.Connection;

import java.util.concurrent.ExecutorService;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 8, 2009
 * Time: 2:11:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultCompareResponseFuture
    extends AbstractResponseFuture<CompareRequest, CompareResponse>
    implements CompareResponseFuture
{
  public DefaultCompareResponseFuture(int messageID, CompareRequest request,
                                   ResponseHandler<CompareResponse> addResponseHandler,
                                   Connection connection,
                                   ExecutorService handlerExecutor)
  {
    super(messageID, request, addResponseHandler, connection,
        handlerExecutor);
  }
}
