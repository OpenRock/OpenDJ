package org.opends.ldap.futures;



import java.util.concurrent.ExecutorService;

import org.opends.ldap.Connection;
import org.opends.ldap.ResponseHandler;
import org.opends.ldap.requests.ModifyDNRequest;
import org.opends.ldap.responses.ModifyDNResponse;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 2:17:59
 * PM To change this template use File | Settings | File Templates.
 */
public class DefaultModifyDNResponseFuture extends
    AbstractResponseFuture<ModifyDNRequest, ModifyDNResponse> implements
    ModifyDNResponseFuture
{
  public DefaultModifyDNResponseFuture(int messageID,
      ModifyDNRequest request,
      ResponseHandler<ModifyDNResponse> addResponseHandler,
      Connection connection, ExecutorService handlerExecutor)
  {
    super(messageID, request, addResponseHandler, connection,
        handlerExecutor);
  }
}
