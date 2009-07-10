package org.opends.ldap.responses;



import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opends.ldap.requests.CompareRequest;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 7, 2009 Time: 3:28:25
 * PM To change this template use File | Settings | File Templates.
 */
public interface CompareResponseFuture extends ResponseFuture
{
  public CompareResponse get() throws InterruptedException,
      ExecutionException;



  public CompareResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException, ExecutionException;



  public CompareRequest getRequest();
}
