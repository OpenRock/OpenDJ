package org.opends.ldap.requests;



import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 1, 2009 Time: 12:52:51
 * PM To change this template use File | Settings | File Templates.
 */
public abstract class SASLBindRequest extends BindRequest
{
  public abstract ByteString getSASLCredentials();



  public abstract String getSASLMechanism();
}
