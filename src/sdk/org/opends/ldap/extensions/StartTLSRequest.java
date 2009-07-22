package org.opends.ldap.extensions;



import static org.opends.server.util.ServerConstants.*;

import org.opends.ldap.DecodeException;
import org.opends.ldap.ExtendedOperation;
import org.opends.ldap.ResultCode;
import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.responses.Result;
import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 22, 2009 Time: 6:21:44
 * PM To change this template use File | Settings | File Templates.
 */
public final class StartTLSRequest extends
    ExtendedRequest<StartTLSRequest, Result>
{

  public StartTLSRequest()
  {
    super(OID_START_TLS_REQUEST);
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
    buffer.append("StartTLSExtendedRequest(requestName=");
    buffer.append(getRequestName());
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }



  private static final class Operation implements
      ExtendedOperation<StartTLSRequest, Result>
  {

    public StartTLSRequest decodeRequest(String requestName,
        ByteString requestValue) throws DecodeException
    {
      return new StartTLSRequest();
    }



    public Result decodeResponse(ResultCode resultCode,
        String matchedDN, String diagnosticMessage,
        String responseName, ByteString responseValue)
        throws DecodeException
    {
      // TODO: Should we check oid is NOT null and matches but
      // value is null?
      return new Result(resultCode, matchedDN, diagnosticMessage);
    }



    public Result decodeResponse(ResultCode resultCode,
        String matchedDN, String diagnosticMessage)
    {
      return new Result(resultCode, matchedDN, diagnosticMessage);
    }
  }



  // Singleton instance.
  private static final Operation OPERATION = new Operation();

}
