package org.opends.ldap.impl;



import java.util.concurrent.ExecutorService;

import org.opends.ldap.Connection;
import org.opends.ldap.ResponseHandler;
import org.opends.ldap.requests.DeleteRequest;
import org.opends.ldap.responses.DeleteResponse;
import org.opends.ldap.responses.ResponseFuture;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 2:12:02
 * PM To change this template use File | Settings | File Templates.
 */
public class DefaultDeleteResponseFuture extends
    AbstractResponseFuture<DeleteRequest, DeleteResponse> implements
    ResponseFuture
{
  public DefaultDeleteResponseFuture(int messageID,
      DeleteRequest request,
      ResponseHandler<DeleteResponse> addResponseHandler,
      Connection connection, ExecutorService handlerExecutor)
  {
    super(messageID, request, addResponseHandler, connection,
        handlerExecutor);
  }
}
