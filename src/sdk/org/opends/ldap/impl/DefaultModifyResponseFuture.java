package org.opends.ldap.impl;



import java.util.concurrent.ExecutorService;

import org.opends.ldap.Connection;
import org.opends.ldap.ResponseHandler;
import org.opends.ldap.requests.ModifyRequest;
import org.opends.ldap.responses.ModifyResponse;
import org.opends.ldap.responses.ResponseFuture;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 2:19:00
 * PM To change this template use File | Settings | File Templates.
 */
public class DefaultModifyResponseFuture extends
    AbstractResponseFuture<ModifyRequest, ModifyResponse> implements
    ResponseFuture
{
  public DefaultModifyResponseFuture(int messageID,
      ModifyRequest request,
      ResponseHandler<ModifyResponse> addResponseHandler,
      Connection connection, ExecutorService handlerExecutor)
  {
    super(messageID, request, addResponseHandler, connection,
        handlerExecutor);
  }
}
