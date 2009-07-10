package org.opends.ldap.extensions;



import static org.opends.messages.ExtensionMessages.*;
import static org.opends.server.util.ServerConstants.*;
import static org.opends.server.util.StaticUtils.*;

import java.io.IOException;

import org.opends.asn1.ASN1;
import org.opends.asn1.ASN1Reader;
import org.opends.asn1.ASN1Writer;
import org.opends.ldap.AbstractExtendedOperation;
import org.opends.ldap.DecodeException;
import org.opends.ldap.ResultCode;
import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.responses.ExtendedResponse;
import org.opends.messages.Message;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 22, 2009 Time: 4:44:51
 * PM To change this template use File | Settings | File Templates.
 */
public final class CancelExtendedOperation extends
    AbstractExtendedOperation
{
  public static class Request extends
      ExtendedRequest<CancelExtendedOperation>
  {
    int cancelID;



    public Request(int cancelID)
    {
      super(OID_CANCEL_REQUEST);
      this.cancelID = cancelID;
    }



    public int getCancelID()
    {
      return cancelID;
    }



    @Override
    public CancelExtendedOperation getExtendedOperation()
    {
      return SINGLETON;
    }



    @Override
    public ByteString getRequestValue()
    {
      ByteStringBuilder buffer = new ByteStringBuilder(6);
      ASN1Writer writer = ASN1.getWriter(buffer);

      try
      {
        writer.writeStartSequence();
        writer.writeInteger(cancelID);
        writer.writeEndSequence();
      }
      catch (IOException ioe)
      {
        // This should never happen unless there is a bug somewhere.
        throw new RuntimeException(ioe);
      }

      return buffer.toByteString();
    }



    public Request setCancelID(int cancelID)
    {
      this.cancelID = cancelID;
      return this;
    }



    @Override
    public void toString(StringBuilder buffer)
    {
      buffer.append("CancelExtendedRequest(requestName=");
      buffer.append(requestName);
      buffer.append(", cancelID=");
      buffer.append(cancelID);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }

  public static class Response extends
      ExtendedResponse<CancelExtendedOperation>
  {
    public Response(ResultCode resultCode, String matchedDN,
        String diagnosticMessage)
    {
      super(resultCode, matchedDN, diagnosticMessage);
    }



    @Override
    public CancelExtendedOperation getExtendedOperation()
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



  private static final CancelExtendedOperation SINGLETON =
      new CancelExtendedOperation();



  private CancelExtendedOperation()
  {
    super();
    // We could register the result codes here if they are not
    // already included in the default set.
  }



  @Override
  public Request decodeRequest(String requestName,
      ByteString requestValue) throws DecodeException
  {
    if ((requestValue == null) || (requestValue.length() <= 0))
    {
      throw new DecodeException(ERR_EXTOP_CANCEL_NO_REQUEST_VALUE.get());
    }

    try
    {
      ASN1Reader reader = ASN1.getReader(requestValue);
      reader.readStartSequence();
      int idToCancel = (int) reader.readInteger();
      reader.readEndSequence();
      return new Request(idToCancel);
    }
    catch (IOException e)
    {
      Message message =
          ERR_EXTOP_CANCEL_CANNOT_DECODE_REQUEST_VALUE
              .get(getExceptionMessage(e));
      throw new DecodeException(message, e);
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
