package org.opends.ldap.impl;



import java.util.concurrent.ExecutorService;

import org.opends.ldap.Connection;
import org.opends.ldap.ResultHandler;
import org.opends.ldap.requests.Request;
import org.opends.ldap.responses.Responses;
import org.opends.ldap.responses.Result;
import org.opends.ldap.responses.ResultFuture;
import org.opends.types.ResultCode;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 1:52:17
 * PM To change this template use File | Settings | File Templates.
 */
class ResultFutureImpl extends AbstractResultFutureImpl<Result>
    implements ResultFuture
{
  private final Request request;



  ResultFutureImpl(int messageID, Request request,
      ResultHandler<Result> handler, Connection connection,
      ExecutorService handlerExecutor)
  {
    super(messageID, handler, connection, handlerExecutor);
    this.request = request;
  }



  Request getRequest()
  {
    return request;
  }



  /**
   * {@inheritDoc}
   */
  Result newErrorResult(ResultCode resultCode,
      String diagnosticMessage, Throwable cause)
  {
    return Responses.newResult(resultCode).setDiagnosticMessage(
        diagnosticMessage).setCause(cause);
  }
}
