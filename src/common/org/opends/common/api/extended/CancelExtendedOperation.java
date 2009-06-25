package org.opends.common.api.extended;

import static org.opends.server.util.ServerConstants.OID_CANCEL_REQUEST;
import static org.opends.server.util.StaticUtils.getExceptionMessage;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;
import org.opends.server.protocols.asn1.ASN1Reader;
import org.opends.server.protocols.asn1.ASN1;
import org.opends.server.protocols.asn1.ASN1Exception;
import org.opends.server.protocols.asn1.ASN1Writer;
import org.opends.common.api.DecodeException;
import org.opends.common.api.ResultCode;
import org.opends.messages.Message;
import static org.opends.messages.ExtensionMessages.ERR_EXTOP_CANCEL_CANNOT_DECODE_REQUEST_VALUE;
import static org.opends.messages.ExtensionMessages.ERR_EXTOP_CANCEL_NO_REQUEST_VALUE;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jun 22, 2009
 * Time: 4:44:51 PM
 * To change this template use File | Settings | File Templates.
 */
public final class CancelExtendedOperation
    extends AbstractExtendedOperation
{
  private static final CancelExtendedOperation SINGLETON =
      new CancelExtendedOperation();

  private CancelExtendedOperation() {
    super();
    // We could register the result codes here if they are not
    // already included in the default set.
  }

  public static class CancelExtendedRequest extends
      ExtendedRequest<CancelExtendedOperation>
  {
    int cancelID;

    public CancelExtendedRequest(int cancelID) {
      super(OID_CANCEL_REQUEST);
      this.cancelID = cancelID;
    }

    public CancelExtendedOperation getExtendedOperation() {
      return SINGLETON;
    }

    public int getCancelID() {
      return cancelID;
    }

    public CancelExtendedRequest setCancelID(int cancelID) {
      this.cancelID = cancelID;
      return this;
    }

    public ByteString getRequestValue() {
      ByteStringBuilder buffer = new ByteStringBuilder(6);
      ASN1Writer writer = ASN1.getWriter(buffer);

      try
      {
        writer.writeStartSequence();
        writer.writeInteger(cancelID);
        writer.writeEndSequence();
      }
      catch(IOException ioe)
      {
        // This should never happen unless there is a bug somewhere.
        throw new RuntimeException(ioe);
      }

      return buffer.toByteString();
    }

    public void toString(StringBuilder buffer) {
      buffer.append("CancelExtendedRequest(requestName=");
      buffer.append(requestName);
      buffer.append(", cancelID=");
      buffer.append(cancelID);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }

  public static class CancelExtendedResponse extends
      ExtendedResponse<CancelExtendedOperation>
  {
    public CancelExtendedResponse(ResultCode resultCode,
                                  String matchedDN,
                                  String diagnosticMessage)
    {
      super(resultCode, matchedDN, diagnosticMessage);
    }

    public CancelExtendedOperation getExtendedOperation() {
      return SINGLETON;
    }

    public ByteString getResponseValue() {
      return null;
    }

    public void toString(StringBuilder buffer) {
      buffer.append("CancelExtendedResponse(resultCode=");
      buffer.append(resultCode);
      buffer.append(", matchedDN=");
      buffer.append(matchedDN);
      buffer.append(", diagnosticMessage=");
      buffer.append(diagnosticMessage);
      buffer.append(", referrals=");
      buffer.append(referrals);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }



  @Override
  public CancelExtendedRequest decodeRequest(String requestName,
                                             ByteString requestValue)
      throws DecodeException
  {
    if(requestValue == null || requestValue.length() <= 0)
    {
      throw new DecodeException(ERR_EXTOP_CANCEL_NO_REQUEST_VALUE.get());
    }
    
    try
    {
      ASN1Reader reader = ASN1.getReader(requestValue);
      reader.readStartSequence();
      int idToCancel = (int)reader.readInteger();
      reader.readEndSequence();
      return new CancelExtendedRequest(idToCancel);
    }
    catch(ASN1Exception e)
    {
      Message message =
          ERR_EXTOP_CANCEL_CANNOT_DECODE_REQUEST_VALUE.get(
          getExceptionMessage(e));
      throw new DecodeException(message, e);
    }
  }

  @Override
  public CancelExtendedResponse decodeResponse(
      ResultCode resultCode, String matchedDN,
      String diagnosticMessage, String responseName,
      ByteString responseValue)
      throws DecodeException
  {
    // TODO: Should we check to make sure OID and value is null?
    return new CancelExtendedResponse(resultCode, matchedDN,
        diagnosticMessage);
  }
}
