package org.opends.sdk.extensions;



import static org.opends.server.util.ServerConstants.OID_GET_CONNECTION_ID_EXTOP;

import java.io.IOException;

import org.opends.messages.Message;
import org.opends.sdk.DecodeException;
import org.opends.sdk.ResultCode;
import org.opends.sdk.asn1.ASN1;
import org.opends.sdk.asn1.ASN1Reader;
import org.opends.sdk.spi.AbstractExtendedRequest;
import org.opends.sdk.spi.ExtendedOperation;
import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 23, 2009 Time:
 * 11:43:53 AM To change this template use File | Settings | File
 * Templates.
 */
public final class GetConnectionIDRequest
    extends
    AbstractExtendedRequest<GetConnectionIDRequest, GetConnectionIDResult>
{
  public GetConnectionIDRequest()
  {
    super(OID_GET_CONNECTION_ID_EXTOP);
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
    builder.append("GetConnectionIDExtendedRequest(requestName=");
    builder.append(getRequestName());
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder;
  }



  private static final class Operation implements
      ExtendedOperation<GetConnectionIDRequest, GetConnectionIDResult>
  {

    public GetConnectionIDRequest decodeRequest(String requestName,
        ByteString requestValue) throws DecodeException
    {
      return new GetConnectionIDRequest();
    }



    public GetConnectionIDResult decodeResponse(ResultCode resultCode,
        String matchedDN, String diagnosticMessage,
        String responseName, ByteString responseValue)
        throws DecodeException
    {
      if (!resultCode.isExceptional()
          && ((responseValue == null) || (responseValue.length() <= 0)))
      {
        throw new DecodeException(Message.raw("Empty response value"));
      }

      try
      {
        ASN1Reader reader = ASN1.getReader(responseValue);
        int connectionID = (int) reader.readInteger();
        return new GetConnectionIDResult(resultCode, connectionID)
            .setMatchedDN(matchedDN).setDiagnosticMessage(
                diagnosticMessage);
      }
      catch (IOException e)
      {
        throw new DecodeException(Message
            .raw("Error decoding response value"), e);
      }
    }



    /**
     * {@inheritDoc}
     */
    public GetConnectionIDResult decodeResponse(ResultCode resultCode,
        String matchedDN, String diagnosticMessage)
    {
      if (!resultCode.isExceptional())
      {
        // A successful response must contain a response name and
        // value.
        throw new IllegalArgumentException(
            "No response name and value for result code "
                + resultCode.intValue());
      }
      return new GetConnectionIDResult(resultCode, -1).setMatchedDN(
          matchedDN).setDiagnosticMessage(diagnosticMessage);
    }
  }



  // Singleton instance.
  private static final Operation OPERATION = new Operation();
}
