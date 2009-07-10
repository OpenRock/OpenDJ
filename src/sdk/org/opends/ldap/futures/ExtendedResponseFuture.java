package org.opends.ldap.futures;



import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.responses.ExtendedResponse;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 7, 2009 Time: 3:40:02
 * PM To change this template use File | Settings | File Templates.
 */
public interface ExtendedResponseFuture extends ResponseFuture
{
  public ExtendedResponse get() throws InterruptedException,
      ExecutionException;



  public ExtendedResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException, ExecutionException;



  /**
   * Retrieves the number of intermediate responses received from the
   * server.
   * 
   * @return
   */
  public int getNumIntermediateResponse();



  public ExtendedRequest getRequest();
}
