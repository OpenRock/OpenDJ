package org.opends.sdk.ldap;



import java.util.concurrent.ExecutorService;

import org.opends.sdk.BindRequest;
import org.opends.sdk.BindResult;
import org.opends.sdk.BindResultFuture;
import org.opends.sdk.Connection;
import org.opends.sdk.Responses;
import org.opends.sdk.ResultCode;
import org.opends.sdk.ResultHandler;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 1:52:17
 * PM To change this template use File | Settings | File Templates.
 */
class BindResultFutureImpl extends AbstractResultFutureImpl<BindResult>
    implements BindResultFuture
{
  private final BindRequest request;



  BindResultFutureImpl(int messageID, BindRequest request,
      ResultHandler<BindResult> handler, Connection connection,
      ExecutorService handlerExecutor)
  {
    super(messageID, handler, connection, handlerExecutor);
    this.request = request;
  }



  /**
   * {@inheritDoc}
   */
  BindResult newErrorResult(ResultCode resultCode,
      String diagnosticMessage, Throwable cause)
  {
    return Responses.newBindResult(resultCode).setDiagnosticMessage(
        diagnosticMessage).setCause(cause);
  }



  BindRequest getRequest()
  {
    return request;
  }

}
