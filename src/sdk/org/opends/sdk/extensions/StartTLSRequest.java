package org.opends.sdk.extensions;



import static org.opends.server.util.ServerConstants.OID_START_TLS_REQUEST;

import org.opends.sdk.DecodeException;
import org.opends.sdk.Responses;
import org.opends.sdk.Result;
import org.opends.sdk.ResultCode;
import org.opends.sdk.spi.AbstractExtendedRequest;
import org.opends.sdk.spi.ExtendedOperation;
import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 22, 2009 Time: 6:21:44
 * PM To change this template use File | Settings | File Templates.
 */
public final class StartTLSRequest extends
    AbstractExtendedRequest<StartTLSRequest, Result>
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



  public StringBuilder toString(StringBuilder builder)
  {
    builder.append("StartTLSExtendedRequest(requestName=");
    builder.append(getRequestName());
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder;
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
      return Responses.newResult(resultCode).setMatchedDN(matchedDN)
          .setDiagnosticMessage(diagnosticMessage);
    }



    public Result decodeResponse(ResultCode resultCode,
        String matchedDN, String diagnosticMessage)
    {
      return Responses.newResult(resultCode).setMatchedDN(matchedDN)
          .setDiagnosticMessage(diagnosticMessage);
    }
  }



  // Singleton instance.
  private static final Operation OPERATION = new Operation();

}
