package org.opends.ldap.impl;



import java.util.concurrent.ExecutorService;

import org.opends.ldap.Connection;
import org.opends.ldap.ResponseHandler;
import org.opends.ldap.requests.CompareRequest;
import org.opends.ldap.responses.CompareResult;
import org.opends.ldap.responses.CompareResultFuture;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 2:11:10
 * PM To change this template use File | Settings | File Templates.
 */
public class DefaultCompareResponseFuture extends
    ResultFutureImpl<CompareRequest, CompareResult> implements
    CompareResultFuture
{
  public DefaultCompareResponseFuture(int messageID,
      CompareRequest request,
      ResponseHandler<CompareResult> addResponseHandler,
      Connection connection, ExecutorService handlerExecutor)
  {
    super(messageID, request, addResponseHandler, connection,
        handlerExecutor);
  }
}
