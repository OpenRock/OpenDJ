package org.opends.common.api.extended;

import org.opends.common.api.ResultCode;
import org.opends.common.api.DecodeException;
import org.opends.common.protocols.asn1.ASN1Writer;
import org.opends.common.protocols.asn1.ASN1;
import org.opends.common.protocols.asn1.ASN1Reader;
import static org.opends.server.util.ServerConstants.OID_GET_CONNECTION_ID_EXTOP;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;
import org.opends.messages.Message;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jun 23, 2009
 * Time: 11:43:53 AM
 * To change this template use File | Settings | File Templates.
 */
public final class GetConnectionIDExtendedOperation
    extends AbstractExtendedOperation
{
  private static final GetConnectionIDExtendedOperation SINGLETON =
      new GetConnectionIDExtendedOperation();

  private GetConnectionIDExtendedOperation() {
    super();
    // We could register the result codes here if they are not
    // already included in the default set.
  }

  public static class Request extends
      ExtendedRequest<GetConnectionIDExtendedOperation>
  {
    public Request() {
      super(OID_GET_CONNECTION_ID_EXTOP);
    }

    public GetConnectionIDExtendedOperation getExtendedOperation() {
      return SINGLETON;
    }

    public ByteString getRequestValue() {
      return null;
    }

    public void toString(StringBuilder buffer) {
      buffer.append("GetConnectionIDExtendedRequest(requestName=");
      buffer.append(requestName);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }

  public static class Response extends
      ExtendedResponse<GetConnectionIDExtendedOperation>
  {
    private int connectionID;

    public Response(ResultCode resultCode,
                                  String matchedDN,
                                  String diagnosticMessage,
                                  int connectionID)
    {
      super(resultCode, matchedDN, diagnosticMessage);
      this.responseName = OID_GET_CONNECTION_ID_EXTOP;
      this.connectionID = connectionID;
    }

    public int getConnectionID() {
      return connectionID;
    }

    public Response setConnectionID(
        int connectionID) {
      this.connectionID = connectionID;
      return this;
    }

    public GetConnectionIDExtendedOperation getExtendedOperation() {
      return SINGLETON;
    }

    public ByteString getResponseValue() {
      ByteStringBuilder buffer = new ByteStringBuilder(6);
      ASN1Writer writer = ASN1.getWriter(buffer);

      try
      {
        writer.writeInteger(connectionID);
      }
      catch(IOException ioe)
      {
        // This should never happen unless there is a bug somewhere.
        throw new RuntimeException(ioe);
      }

      return buffer.toByteString();
    }

    public void toString(StringBuilder buffer) {
      buffer.append("GetConnectionIDExtendedResponse(resultCode=");
      buffer.append(resultCode);
      buffer.append(", matchedDN=");
      buffer.append(matchedDN);
      buffer.append(", diagnosticMessage=");
      buffer.append(diagnosticMessage);
      buffer.append(", referrals=");
      buffer.append(referrals);
      buffer.append(", responseName=");
      buffer.append(responseName);
      buffer.append(", connectionID=");
      buffer.append(connectionID);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }



  @Override
  public Request decodeRequest(String requestName,
                                             ByteString requestValue)
      throws DecodeException
  {
    return new Request();
  }

  @Override
  public Response decodeResponse(
      ResultCode resultCode, String matchedDN,
      String diagnosticMessage, String responseName,
      ByteString responseValue)
      throws DecodeException
  {
    if(responseValue == null || responseValue.length() <= 0)
    {
      throw new DecodeException(Message.raw("Empty response value"));
    }

    try
    {
      ASN1Reader reader = ASN1.getReader(responseValue);
      int connectionID = (int)reader.readInteger();
      return new Response(resultCode,
          matchedDN, diagnosticMessage, connectionID);
    }
    catch(IOException e)
    {
      throw new DecodeException(Message.raw("Error decoding response value"), e);
    }
  }
}
