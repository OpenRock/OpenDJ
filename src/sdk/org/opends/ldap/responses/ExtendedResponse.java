package org.opends.ldap.responses;



import org.opends.ldap.ExtendedOperation;
import org.opends.ldap.ResultCode;
import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 19, 2009 Time:
 * 8:40:31 PM To change this template use File | Settings | File
 * Templates.
 */
public abstract class ExtendedResponse<T extends ExtendedOperation>
    extends AbstractResultResponse
{
  protected String responseName;



  protected ExtendedResponse(ResultCode resultCode, String matchedDN,
      String diagnosticMessage)
  {
    super(resultCode, matchedDN, diagnosticMessage);
  }



  public abstract T getExtendedOperation();



  /**
   * Get the response name OID of this extended response or
   * <code>NULL</code> if it is not available.
   * 
   * @return The response name OID or <code>NULL</code>
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
