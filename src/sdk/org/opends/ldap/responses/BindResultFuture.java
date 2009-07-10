package org.opends.ldap.responses;



import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 7, 2009 Time: 3:04:48
 * PM To change this template use File | Settings | File Templates.
 */
public interface BindResultFuture extends ResultFuture
{
  BindResult get() throws InterruptedException, ExecutionException;



  BindResult get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException, ExecutionException;

}
