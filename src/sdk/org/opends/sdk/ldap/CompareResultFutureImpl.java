package org.opends.sdk.ldap;



import java.util.concurrent.ExecutorService;

import org.opends.sdk.CompareRequest;
import org.opends.sdk.CompareResult;
import org.opends.sdk.CompareResultFuture;
import org.opends.sdk.Connection;
import org.opends.sdk.Responses;
import org.opends.sdk.ResultCode;
import org.opends.sdk.ResultHandler;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 2:11:10
 * PM To change this template use File | Settings | File Templates.
 */
class CompareResultFutureImpl extends
    AbstractResultFutureImpl<CompareResult> implements
    CompareResultFuture
{
  CompareResultFutureImpl(int messageID, CompareRequest request,
      ResultHandler<CompareResult> handler, Connection connection,
      ExecutorService handlerExecutor)
  {
    super(messageID, handler, connection, handlerExecutor);
  }



  /**
   * {@inheritDoc}
   */
  CompareResult newErrorResult(ResultCode resultCode,
      String diagnosticMessage, Throwable cause)
  {
    return Responses.newCompareResult(resultCode).setDiagnosticMessage(
        diagnosticMessage).setCause(cause);
  }
}
