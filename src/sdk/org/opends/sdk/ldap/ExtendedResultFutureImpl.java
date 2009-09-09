package org.opends.sdk.ldap;



import java.util.concurrent.ExecutorService;

import org.opends.sdk.DecodeException;
import org.opends.sdk.ExtendedRequest;
import org.opends.sdk.ExtendedResultFuture;
import org.opends.sdk.Result;
import org.opends.sdk.ResultCode;
import org.opends.sdk.ResultHandler;
import org.opends.sdk.spi.ExtendedOperation;
import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 2:12:46
 * PM To change this template use File | Settings | File Templates.
 */
final class ExtendedResultFutureImpl<R extends Result> extends
    AbstractResultFutureImpl<R> implements ExtendedResultFuture<R>
{
  private final ExtendedOperation<?, R> operation;



  ExtendedResultFutureImpl(int messageID,
      ExtendedRequest<R> request, ResultHandler<R> handler,
      LDAPConnection connection, ExecutorService handlerExecutor)
  {
    super(messageID, handler, connection, handlerExecutor);
    operation = request.getExtendedOperation();
  }



  R decodeResponse(ResultCode resultCode, String matchedDN,
      String diagnosticMessage, String responseName,
      ByteString responseValue) throws DecodeException
  {
    return operation.decodeResponse(resultCode, matchedDN,
        diagnosticMessage, responseName, responseValue);
  }



  /**
   * {@inheritDoc}
   */
  R newErrorResult(ResultCode resultCode, String diagnosticMessage,
      Throwable cause)
  {
    return operation.decodeResponse(resultCode, "", diagnosticMessage);
  }
}
