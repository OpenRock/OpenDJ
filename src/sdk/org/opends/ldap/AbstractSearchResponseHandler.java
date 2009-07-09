package org.opends.ldap;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 4:28:12
 * PM To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSearchResponseHandler implements
    SearchResponseHandler
{
  public void handleErrorResult(ErrorResultException result)
  {
    handleException(result);
  }
}
