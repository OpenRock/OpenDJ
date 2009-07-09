package org.opends.ldap.futures;



import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opends.ldap.requests.ModifyDNRequest;
import org.opends.ldap.responses.ModifyDNResponse;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 7, 2009 Time: 3:35:37
 * PM To change this template use File | Settings | File Templates.
 */
public interface ModifyDNResponseFuture extends ResponseFuture
{
  public ModifyDNResponse get() throws InterruptedException,
      ExecutionException;



  public ModifyDNResponse get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException, ExecutionException;



  public ModifyDNRequest getRequest();
}
