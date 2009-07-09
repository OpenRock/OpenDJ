package org.opends.ldap.futures;



import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opends.ldap.requests.Request;
import org.opends.ldap.responses.ResultResponse;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 7, 2009 Time: 1:42:16
 * PM To change this template use File | Settings | File Templates.
 */
public interface ResponseFuture extends Future<ResultResponse>
{
  /**
   * @return
   * @throws InterruptedException
   * @throws org.opends.ldap.ErrorResultException
   * @throws ExecutionException
   */
  public ResultResponse get() throws InterruptedException,
      ExecutionException;



  /**
   * @param timeout
   * @param unit
   * @return
   * @throws InterruptedException
   * @throws TimeoutException
   * @throws org.opends.ldap.ErrorResultException
   * @throws ExecutionException
   */
  public ResultResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException, ExecutionException;



  public Request getRequest();
}
