package org.opends.ldap.futures;



import java.util.concurrent.ExecutorService;

import org.opends.ldap.Connection;
import org.opends.ldap.ResponseHandler;
import org.opends.ldap.requests.BindRequest;
import org.opends.ldap.responses.BindResponse;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 1:52:17
 * PM To change this template use File | Settings | File Templates.
 */
public class DefaultBindResponseFuture extends
    AbstractResponseFuture<BindRequest, BindResponse> implements
    BindResponseFuture
{
  public DefaultBindResponseFuture(int messageID, BindRequest request,
      ResponseHandler<BindResponse> addResponseHandler,
      Connection connection, ExecutorService handlerExecutor)
  {
    super(messageID, request, addResponseHandler, connection,
        handlerExecutor);
  }
}
