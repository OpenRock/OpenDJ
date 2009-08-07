package org.opends.ldap.sasl;



import org.opends.server.types.ByteString;
import org.opends.spi.AbstractBindRequest;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 1, 2009 Time: 12:52:51
 * PM To change this template use File | Settings | File Templates.
 */
public abstract class SASLBindRequest extends AbstractBindRequest
{
  public abstract ByteString getSASLCredentials();



  public abstract String getSASLMechanism();
}
