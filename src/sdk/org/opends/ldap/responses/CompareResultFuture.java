package org.opends.ldap.responses;



import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 7, 2009 Time: 3:28:25
 * PM To change this template use File | Settings | File Templates.
 */
public interface CompareResultFuture extends ResultFuture
{
  CompareResult get() throws InterruptedException, ExecutionException;



  CompareResult get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException, ExecutionException;

}
