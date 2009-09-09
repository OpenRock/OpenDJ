package org.opends.sdk.ldap;



import java.util.concurrent.ExecutorService;

import org.opends.sdk.Connection;
import org.opends.sdk.Request;
import org.opends.sdk.Responses;
import org.opends.sdk.Result;
import org.opends.sdk.ResultCode;
import org.opends.sdk.ResultFuture;
import org.opends.sdk.ResultHandler;



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
