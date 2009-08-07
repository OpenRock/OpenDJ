package org.opends.ldap.impl;



import java.util.concurrent.ExecutorService;

import org.opends.ldap.Connection;
import org.opends.ldap.ResponseHandler;
import org.opends.ldap.responses.BindResult;
import org.opends.ldap.responses.BindResultFuture;
import org.opends.ldap.responses.Responses;
import org.opends.spi.AbstractBindRequest;
import org.opends.types.ResultCode;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 1:52:17
 * PM To change this template use File | Settings | File Templates.
 */
class BindResultFutureImpl extends AbstractResultFutureImpl<BindResult>
    implements BindResultFuture
{
  private final AbstractBindRequest request;



  BindResultFutureImpl(int messageID, AbstractBindRequest request,
      ResponseHandler<BindResult> handler, Connection connection,
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



  AbstractBindRequest getRequest()
  {
    return request;
  }

}
