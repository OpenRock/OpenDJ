package org.opends.ldap.extensions;



import static org.opends.server.util.ServerConstants.*;

import org.opends.ldap.DecodeException;
import org.opends.ldap.ExtendedOperation;
import org.opends.ldap.ResultCode;
import org.opends.ldap.requests.ExtendedRequest;
import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 22, 2009 Time: 6:40:06
 * PM To change this template use File | Settings | File Templates.
 */
public final class WhoAmIRequest extends
    ExtendedRequest<WhoAmIRequest, WhoAmIResult>
{
  public WhoAmIRequest()
  {
    super(OID_WHO_AM_I_REQUEST);
  }



  public Operation getExtendedOperation()
  {
    return OPERATION;
  }



  public ByteString getRequestValue()
  {
    return null;
  }



  public void toString(StringBuilder buffer)
  {
    buffer.append("WhoAmIExtendedRequest(requestName=");
    buffer.append(getRequestName());
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }



  private static final class Operation implements
      ExtendedOperation<WhoAmIRequest, WhoAmIResult>
  {

    public WhoAmIRequest decodeRequest(String requestName,
        ByteString requestValue) throws DecodeException
    {
      return new WhoAmIRequest();
    }



    public WhoAmIResult decodeResponse(ResultCode resultCode,
        String matchedDN, String diagnosticMessage,
        String responseName, ByteString responseValue)
        throws DecodeException
    {
      // TODO: Should we check oid is null?
      String authzId = null;
      if (responseValue != null)
      {
        authzId = responseValue.toString();
      }
      return new WhoAmIResult(resultCode, matchedDN, diagnosticMessage)
          .setAuthzId(authzId);
    }



    public WhoAmIResult decodeResponse(ResultCode resultCode,
        String matchedDN, String diagnosticMessage)
    {
      return new WhoAmIResult(resultCode, matchedDN, diagnosticMessage);
    }
  }



  // Singleton instance.
  private static final Operation OPERATION = new Operation();
}
