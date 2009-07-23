package org.opends.ldap.extensions;



import static org.opends.server.util.ServerConstants.*;

import java.io.IOException;

import org.opends.asn1.ASN1;
import org.opends.asn1.ASN1Reader;
import org.opends.ldap.DecodeException;
import org.opends.ldap.ResultCode;
import org.opends.ldap.requests.ExtendedRequest;
import org.opends.messages.Message;
import org.opends.server.types.ByteString;
import org.opends.spi.ExtendedOperation;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 23, 2009 Time:
 * 11:43:53 AM To change this template use File | Settings | File
 * Templates.
 */
public final class GetConnectionIDRequest
    extends
    ExtendedRequest<GetConnectionIDRequest, GetConnectionIDResult>
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



  public void toString(StringBuilder buffer)
  {
    buffer.append("GetConnectionIDExtendedRequest(requestName=");
    buffer.append(getRequestName());
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
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
        return new GetConnectionIDResult(resultCode, matchedDN, diagnosticMessage,
            connectionID);
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
      return new GetConnectionIDResult(resultCode, matchedDN, diagnosticMessage, -1);
    }
  }



  // Singleton instance.
  private static final Operation OPERATION = new Operation();
}
