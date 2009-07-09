package org.opends.ldap;



import javax.security.auth.callback.NameCallback;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 1, 2009 Time: 3:13:43
 * PM To change this template use File | Settings | File Templates.
 */
public interface NameCallbackHandler
{
  public void handle(NameCallback callback);
}
