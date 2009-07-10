package org.opends.ldap.extensions;



import static org.opends.messages.ExtensionMessages.*;
import static org.opends.server.util.ServerConstants.*;

import java.io.IOException;

import org.opends.asn1.ASN1;
import org.opends.asn1.ASN1Reader;
import org.opends.asn1.ASN1Writer;
import org.opends.ldap.AbstractExtendedOperation;
import org.opends.ldap.DecodeException;
import org.opends.ldap.ResultCode;
import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.responses.ExtendedResult;
import org.opends.messages.Message;
import org.opends.server.loggers.debug.DebugLogger;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;
import org.opends.server.types.DebugLogLevel;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 23, 2009 Time:
 * 12:10:59 PM To change this template use File | Settings | File
 * Templates.
 */
public final class GetSymmetricKeyExtendedOperation extends
    AbstractExtendedOperation
{
  public static class Request extends
      ExtendedRequest<GetSymmetricKeyExtendedOperation>
  {
    private String requestSymmetricKey = null;
    private String instanceKeyID = null;



    public Request()
    {
      super(OID_GET_SYMMETRIC_KEY_EXTENDED_OP);
    }



    @Override
    public GetSymmetricKeyExtendedOperation getExtendedOperation()
    {
      return SINGLETON;
    }



    public String getInstanceKeyID()
    {
      return instanceKeyID;
    }



    public String getRequestSymmetricKey()
    {
      return requestSymmetricKey;
    }



    @Override
    public ByteString getRequestValue()
    {
      ByteStringBuilder buffer = new ByteStringBuilder();
      ASN1Writer writer = ASN1.getWriter(buffer);

      try
      {
        writer.writeStartSequence();
        if (requestSymmetricKey != null)
        {
          writer.writeOctetString(TYPE_SYMMETRIC_KEY_ELEMENT,
              requestSymmetricKey);
        }
        if (instanceKeyID != null)
        {
          writer.writeOctetString(TYPE_INSTANCE_KEY_ID_ELEMENT,
              instanceKeyID);
        }
        writer.writeEndSequence();
      }
      catch (IOException ioe)
      {
        // This should never happen unless there is a bug somewhere.
        throw new RuntimeException(ioe);
      }

      return buffer.toByteString();
    }



    public Request setInstanceKeyID(String instanceKeyID)
    {
      this.instanceKeyID = instanceKeyID;
      return this;
    }



    public Request setRequestSymmetricKey(String requestSymmetricKey)
    {
      this.requestSymmetricKey = requestSymmetricKey;
      return this;
    }



    @Override
    public void toString(StringBuilder buffer)
    {
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

  public static class Response extends
      ExtendedResult<GetSymmetricKeyExtendedOperation>
  {
    public Response(ResultCode resultCode, String matchedDN,
        String diagnosticMessage)
    {
      super(resultCode, matchedDN, diagnosticMessage);
      this.responseName = OID_GET_SYMMETRIC_KEY_EXTENDED_OP;
    }



    @Override
    public GetSymmetricKeyExtendedOperation getExtendedOperation()
    {
      return SINGLETON;
    }



    @Override
    public ByteString getResponseValue()
    {
      return null;
    }



    @Override
    public void toString(StringBuilder buffer)
    {
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



  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = DebugLogger.getTracer();

  private static final GetSymmetricKeyExtendedOperation SINGLETON =
      new GetSymmetricKeyExtendedOperation();

  /**
   * The BER type value for the symmetric key element of the operation
   * value.
   */
  private static final byte TYPE_SYMMETRIC_KEY_ELEMENT = (byte) 0x80;

  /**
   * The BER type value for the instance key ID element of the operation
   * value.
   */
  private static final byte TYPE_INSTANCE_KEY_ID_ELEMENT = (byte) 0x81;



  private GetSymmetricKeyExtendedOperation()
  {
    super();
    // We could register the result codes here if they are not
    // already included in the default set.
  }



  @Override
  public Request decodeRequest(String requestName,
      ByteString requestValue) throws DecodeException
  {
    if (requestValue == null)
    {
      // The request must always have a value.
      Message message = ERR_GET_SYMMETRIC_KEY_NO_VALUE.get();
      throw new DecodeException(message);
    }

    String requestSymmetricKey = null;
    String instanceKeyID = null;

    try
    {
      ASN1Reader reader = ASN1.getReader(requestValue);
      reader.readStartSequence();
      if (reader.hasNextElement()
          && (reader.peekType() == TYPE_SYMMETRIC_KEY_ELEMENT))
      {
        requestSymmetricKey = reader.readOctetStringAsString();
      }
      if (reader.hasNextElement()
          && (reader.peekType() == TYPE_INSTANCE_KEY_ID_ELEMENT))
      {
        instanceKeyID = reader.readOctetStringAsString();
      }
      reader.readEndSequence();
      return new Request().setRequestSymmetricKey(requestSymmetricKey)
          .setInstanceKeyID(instanceKeyID);
    }
    catch (IOException ae)
    {
      if (DebugLogger.debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, ae);
      }

      Message message =
          ERR_GET_SYMMETRIC_KEY_ASN1_DECODE_EXCEPTION.get(ae
              .getMessage());
      throw new DecodeException(message, ae);
    }
  }



  @Override
  public Response decodeResponse(ResultCode resultCode,
      String matchedDN, String diagnosticMessage, String responseName,
      ByteString responseValue) throws DecodeException
  {
    // TODO: Should we check to make sure OID and value is null?
    return new Response(resultCode, matchedDN, diagnosticMessage);
  }
}
