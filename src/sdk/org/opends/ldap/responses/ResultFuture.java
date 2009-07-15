package org.opends.ldap.responses;



import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 7, 2009 Time: 1:42:16
 * PM To change this template use File | Settings | File Templates.
 */
public interface ResultFuture extends Future<Result>
{
  Result get() throws InterruptedException, ExecutionException;



  Result get(long timeout, TimeUnit unit) throws InterruptedException,
      TimeoutException, ExecutionException;

}
