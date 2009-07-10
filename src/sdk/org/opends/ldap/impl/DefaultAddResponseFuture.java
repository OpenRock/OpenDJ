package org.opends.ldap.impl;



import java.util.concurrent.ExecutorService;

import org.opends.ldap.Connection;
import org.opends.ldap.ResponseHandler;
import org.opends.ldap.requests.AddRequest;
import org.opends.ldap.responses.AddResponse;
import org.opends.ldap.responses.ResponseFuture;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 1:50:34
 * PM To change this template use File | Settings | File Templates.
 */
public class DefaultAddResponseFuture extends
    AbstractResponseFuture<AddRequest, AddResponse> implements
    ResponseFuture
{
  public DefaultAddResponseFuture(int messageID, AddRequest request,
      ResponseHandler<AddResponse> addResponseHandler,
      Connection connection, ExecutorService handlerExecutor)
  {
    super(messageID, request, addResponseHandler, connection,
        handlerExecutor);
  }
}
