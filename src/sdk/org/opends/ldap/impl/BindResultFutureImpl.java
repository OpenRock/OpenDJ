package org.opends.ldap.impl;



import java.util.concurrent.ExecutorService;

import org.opends.ldap.Connection;
import org.opends.ldap.ResponseHandler;
import org.opends.ldap.requests.BindRequest;
import org.opends.ldap.responses.BindResult;
import org.opends.ldap.responses.BindResultFuture;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 1:52:17
 * PM To change this template use File | Settings | File Templates.
 */
public class BindResultFutureImpl extends
    ResultFutureImpl<BindRequest, BindResult> implements
    BindResultFuture
{
  public BindResultFutureImpl(int messageID, BindRequest request,
      ResponseHandler<BindResult> addResponseHandler,
      Connection connection, ExecutorService handlerExecutor)
  {
    super(messageID, request, addResponseHandler, connection,
        handlerExecutor);
  }
}
