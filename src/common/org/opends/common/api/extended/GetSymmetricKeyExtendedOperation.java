package org.opends.common.api.extended;

import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;
import org.opends.server.types.DebugLogLevel;
import static org.opends.server.util.ServerConstants.OID_GET_SYMMETRIC_KEY_EXTENDED_OP;
import org.opends.server.protocols.asn1.ASN1Writer;
import org.opends.server.protocols.asn1.ASN1;
import org.opends.server.protocols.asn1.ASN1Reader;
import org.opends.server.protocols.asn1.ASN1Exception;
import org.opends.server.loggers.debug.DebugLogger;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.common.api.ResultCode;
import org.opends.common.api.DecodeException;
import org.opends.messages.Message;
import static org.opends.messages.ExtensionMessages.ERR_GET_SYMMETRIC_KEY_NO_VALUE;
import static org.opends.messages.ExtensionMessages.ERR_GET_SYMMETRIC_KEY_ASN1_DECODE_EXCEPTION;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jun 23, 2009
 * Time: 12:10:59 PM
 * To change this template use File | Settings | File Templates.
 */
public final class GetSymmetricKeyExtendedOperation
    extends AbstractExtendedOperation
{
   /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = DebugLogger.getTracer();

  private static final GetSymmetricKeyExtendedOperation SINGLETON =
      new GetSymmetricKeyExtendedOperation();

  /**
   * The BER type value for the symmetric key element of the operation value.
   */
  private static final byte TYPE_SYMMETRIC_KEY_ELEMENT = (byte) 0x80;



  /**
   * The BER type value for the instance key ID element of the operation value.
   */
  private static final byte TYPE_INSTANCE_KEY_ID_ELEMENT = (byte) 0x81;

  private GetSymmetricKeyExtendedOperation() {
    super();
    // We could register the result codes here if they are not
    // already included in the default set.
  }

  public static class GetSymmetricKeyExtendedRequest extends
      ExtendedRequest<GetSymmetricKeyExtendedOperation>
  {
    private String requestSymmetricKey = null;
    private String instanceKeyID       = null;

    public GetSymmetricKeyExtendedRequest() {
      super(OID_GET_SYMMETRIC_KEY_EXTENDED_OP);
    }

    public GetSymmetricKeyExtendedOperation getExtendedOperation() {
      return SINGLETON;
    }

    public String getRequestSymmetricKey() {
      return requestSymmetricKey;
    }

    public GetSymmetricKeyExtendedRequest setRequestSymmetricKey(
        String requestSymmetricKey) {
      this.requestSymmetricKey = requestSymmetricKey;
      return this;
    }

    public String getInstanceKeyID() {
      return instanceKeyID;
    }

    public GetSymmetricKeyExtendedRequest setInstanceKeyID(
        String instanceKeyID) {
      this.instanceKeyID = instanceKeyID;
      return this;
    }

    public ByteString getRequestValue() {
      ByteStringBuilder buffer = new ByteStringBuilder();
      ASN1Writer writer = ASN1.getWriter(buffer);

      try
      {
        writer.writeStartSequence();
        if(requestSymmetricKey != null)
        {
          writer.writeOctetString(TYPE_SYMMETRIC_KEY_ELEMENT,
              requestSymmetricKey);
        }
        if(instanceKeyID != null)
        {
          writer.writeOctetString(TYPE_INSTANCE_KEY_ID_ELEMENT,
              instanceKeyID);
        }
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
      buffer.append("GetSymmetricKeyExtendedRequest(requestName=");
      buffer.append(requestName);
      buffer.append(", requestSymmetricKey=");
      buffer.append(requestSymmetricKey);
      buffer.append(", instanceKeyID=");
      buffer.append(instanceKeyID);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }

  public static class GetSymmetricKeyExtendedResponse extends
      ExtendedResponse<GetSymmetricKeyExtendedOperation>
  {
    public GetSymmetricKeyExtendedResponse(ResultCode resultCode,
                                           String matchedDN,
                                           String diagnosticMessage)
    {
      super(resultCode, matchedDN, diagnosticMessage);
      this.responseName = OID_GET_SYMMETRIC_KEY_EXTENDED_OP;
    }

    public GetSymmetricKeyExtendedOperation getExtendedOperation() {
      return SINGLETON;
    }

    public ByteString getResponseValue() {
      return null;
    }

    public void toString(StringBuilder buffer) {
      buffer.append("GetSymmetricKeyExtendedResponse(resultCode=");
      buffer.append(resultCode);
      buffer.append(", matchedDN=");
      buffer.append(matchedDN);
      buffer.append(", diagnosticMessage=");
      buffer.append(diagnosticMessage);
      buffer.append(", referrals=");
      buffer.append(referrals);
      buffer.append(", responseName=");
      buffer.append(responseName);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }



  @Override
  public GetSymmetricKeyExtendedRequest decodeRequest(String requestName,
                                                      ByteString requestValue)
      throws DecodeException
  {
    if (requestValue == null)
    {
      // The request must always have a value.
      Message message = ERR_GET_SYMMETRIC_KEY_NO_VALUE.get();
      throw new DecodeException(message);
    }

    String requestSymmetricKey = null;
    String instanceKeyID       = null;

    try
    {
      ASN1Reader reader = ASN1.getReader(requestValue);
      reader.readStartSequence();
      if(reader.hasNextElement() &&
          reader.peekType() == TYPE_SYMMETRIC_KEY_ELEMENT)
      {
        requestSymmetricKey = reader.readOctetStringAsString();
      }
      if(reader.hasNextElement() &&
          reader.peekType() == TYPE_INSTANCE_KEY_ID_ELEMENT)
      {
        instanceKeyID = reader.readOctetStringAsString();
      }
      reader.readEndSequence();
      return new GetSymmetricKeyExtendedRequest().
          setRequestSymmetricKey(requestSymmetricKey).
          setInstanceKeyID(instanceKeyID);
    }
    catch (ASN1Exception ae)
    {
      if (DebugLogger.debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, ae);
      }

      Message message = ERR_GET_SYMMETRIC_KEY_ASN1_DECODE_EXCEPTION.get(
           ae.getMessage());
      throw new DecodeException(message, ae);
    }
  }

  @Override
  public GetSymmetricKeyExtendedResponse decodeResponse(
      ResultCode resultCode, String matchedDN,
      String diagnosticMessage, String responseName,
      ByteString responseValue)
      throws DecodeException
  {
    // TODO: Should we check to make sure OID and value is null?
    return new GetSymmetricKeyExtendedResponse(resultCode, matchedDN,
        diagnosticMessage);
  }
}
