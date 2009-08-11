package org.opends.ldap.impl;



import java.util.concurrent.ExecutorService;

import org.opends.ldap.DecodeException;
import org.opends.ldap.ResponseHandler;
import org.opends.ldap.responses.ExtendedResultFuture;
import org.opends.ldap.responses.Result;
import org.opends.server.types.ByteString;
import org.opends.ldap.requests.ExtendedRequest;
import org.opends.spi.ExtendedOperation;
import org.opends.types.ResultCode;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 2:12:46
 * PM To change this template use File | Settings | File Templates.
 */
final class ExtendedResultFutureImpl<R extends Result> extends
    AbstractResultFutureImpl<R> implements ExtendedResultFuture<R>
{
  private final ExtendedOperation<?, R> operation;



  ExtendedResultFutureImpl(int messageID,
      ExtendedRequest<R> request, ResponseHandler<R> handler,
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
