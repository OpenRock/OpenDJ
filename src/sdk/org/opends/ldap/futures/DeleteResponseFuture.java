package org.opends.ldap.futures;



import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opends.ldap.requests.DeleteRequest;
import org.opends.ldap.responses.DeleteResponse;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 7, 2009 Time: 3:31:36
 * PM To change this template use File | Settings | File Templates.
 */
public interface DeleteResponseFuture extends ResponseFuture
{
  public DeleteResponse get() throws InterruptedException,
      ExecutionException;



  public DeleteResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException, ExecutionException;



  public DeleteRequest getRequest();
}
