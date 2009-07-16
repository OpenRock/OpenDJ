package org.opends.ldap.responses;



import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 7, 2009 Time: 3:40:02
 * PM To change this template use File | Settings | File Templates.
 */
public interface ExtendedResultFuture extends ResultFuture
{
  ExtendedResult get() throws InterruptedException, ErrorResultException;



  ExtendedResult get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException, ErrorResultException;



  int getNumIntermediateResponse();

}
