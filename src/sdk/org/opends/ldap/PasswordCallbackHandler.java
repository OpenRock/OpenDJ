package org.opends.ldap;



import javax.security.auth.callback.PasswordCallback;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 1, 2009 Time: 3:15:51
 * PM To change this template use File | Settings | File Templates.
 */
public interface PasswordCallbackHandler
{
  public void handle(PasswordCallback callback);
}
