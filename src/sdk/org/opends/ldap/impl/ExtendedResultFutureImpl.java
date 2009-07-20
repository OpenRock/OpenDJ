package org.opends.ldap.impl;



import java.util.concurrent.ExecutorService;

import org.opends.ldap.ExtendedResponseHandler;
import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.responses.ExtendedResult;
import org.opends.ldap.responses.ExtendedResultFuture;
import org.opends.ldap.responses.IntermediateResponse;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 2:12:46
 * PM To change this template use File | Settings | File Templates.
 */
public final class ExtendedResultFutureImpl extends
    ResultFutureImpl<ExtendedRequest, ExtendedResult> implements
    ExtendedResultFuture
{

  private int numIntermediateResponses = 0;

  private final ExtendedResponseHandler handler;



  public ExtendedResultFutureImpl(int messageID,
      ExtendedRequest request, ExtendedResponseHandler handler,
      LDAPConnection connection, ExecutorService handlerExecutor)
  {
    super(messageID, request, handler, connection, handlerExecutor);
    this.handler = handler;
  }



  public synchronized int getNumIntermediateResponse()
  {
    return numIntermediateResponses;
  }



  public synchronized void handleIntermediateResponse(
      final IntermediateResponse response)
  {
    numIntermediateResponses++;
    if (!isDone())
    {
      invokeHandler(new Runnable()
      {
        public void run()
        {
          handler.handleIntermediateResponse(response);
        }
      });
    }
  }
}
