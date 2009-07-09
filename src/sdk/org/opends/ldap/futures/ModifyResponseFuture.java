package org.opends.ldap.futures;



import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opends.ldap.requests.ModifyRequest;
import org.opends.ldap.responses.ModifyResponse;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 7, 2009 Time: 3:36:23
 * PM To change this template use File | Settings | File Templates.
 */
public interface ModifyResponseFuture extends ResponseFuture
{
  public ModifyResponse get() throws InterruptedException,
      ExecutionException;



  public ModifyResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException, ExecutionException;



  public ModifyRequest getRequest();
}
