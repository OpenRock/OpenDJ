package org.opends.client.api.futures;

import org.opends.common.api.response.Response;
import org.opends.common.api.request.Request;
import org.opends.client.protocol.ldap.LDAPConnection;
import org.opends.client.api.RequestException;

import java.util.concurrent.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 1:42:16 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ResponseFuture
    <Q extends Request, R extends Response> extends Future<R>
{
  public Q getRequest();

  public R get()
      throws InterruptedException, RequestException;

  public R get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException, RequestException;
}
