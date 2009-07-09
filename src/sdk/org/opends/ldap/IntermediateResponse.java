package org.opends.ldap;



import org.opends.ldap.responses.Response;
import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 22, 2009 Time: 3:48:40
 * PM To change this template use File | Settings | File Templates.
 */
public abstract class IntermediateResponse<T extends ExtendedOperation>
    extends Message implements Response
{
  protected String responseName;



  public abstract T getExtendedOperation();



  /**
   * Get the response name OID of this intermediate response or
   * <code>NULL</code> if it is not available.
   * 
   * @return The response name OID or <code>NULL</code>.
   */
  public String getResponseName()
  {
    return responseName;
  }



  /**
   * Get the response value of this intermediate response or
   * <code>NULL</code> if it is not available.
   * 
   * @return the response value or <code>NULL</code>.
   */
  public abstract ByteString getResponseValue();
}
