package org.opends.ldap;



import org.opends.ldap.responses.AddResponse;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 1:53:38
 * PM To change this template use File | Settings | File Templates.
 */
public abstract class AbstractAddResponseHandler implements
    ResponseHandler<AddResponse>
{
  public void handleErrorResult(ErrorResultException result)
  {
    handleException(result);
  }
}
