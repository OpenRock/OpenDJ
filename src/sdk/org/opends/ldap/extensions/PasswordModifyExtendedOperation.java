package org.opends.ldap.extensions;



import static org.opends.messages.ExtensionMessages.*;
import static org.opends.server.extensions.ExtensionsConstants.*;
import static org.opends.server.util.ServerConstants.*;
import static org.opends.server.util.StaticUtils.*;

import java.io.IOException;

import org.opends.asn1.ASN1;
import org.opends.asn1.ASN1Reader;
import org.opends.asn1.ASN1Writer;
import org.opends.ldap.DecodeException;
import org.opends.ldap.ExtendedOperation;
import org.opends.ldap.ResultCode;
import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.responses.ExtendedResult;
import org.opends.messages.Message;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 25, 2009 Time: 2:14:58
 * PM To change this template use File | Settings | File Templates.
 */
public final class PasswordModifyExtendedOperation
{
  public static class Request extends
      ExtendedRequest<Request, Response>
  {
    private String userIdentity;
    private ByteString oldPassword;
    private ByteString newPassword;



    public Request()
    {
      super(OID_PASSWORD_MODIFY_REQUEST);
    }



    public Operation getExtendedOperation()
    {
      return OPERATION;
    }



    public ByteString getNewPassword()
    {
      return newPassword;
    }



    public ByteString getOldPassword()
    {
      return oldPassword;
    }



    public ByteString getRequestValue()
    {
      ByteStringBuilder buffer = new ByteStringBuilder();
      ASN1Writer writer = ASN1.getWriter(buffer);

      try
      {
        writer.writeStartSequence();
        if (userIdentity != null)
        {
          writer.writeOctetString(TYPE_PASSWORD_MODIFY_USER_ID,
              userIdentity);
        }
        if (oldPassword != null)
        {
          writer.writeOctetString(TYPE_PASSWORD_MODIFY_OLD_PASSWORD,
              oldPassword);
        }
        if (newPassword != null)
        {
          writer.writeOctetString(TYPE_PASSWORD_MODIFY_NEW_PASSWORD,
              newPassword);
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



    public String getUserIdentity()
    {
      return userIdentity;
    }



    public void setNewPassword(ByteString newPassword)
    {
      this.newPassword = newPassword;
    }



    public void setOldPassword(ByteString oldPassword)
    {
      this.oldPassword = oldPassword;
    }



    public void setUserIdentity(String userIdentity)
    {
      this.userIdentity = userIdentity;
    }



    public void toString(StringBuilder buffer)
    {
      buffer.append("PasswordModifyExtendedRequest(requestName=");
      buffer.append(getRequestName());
      buffer.append(", userIdentity=");
      buffer.append(userIdentity);
      buffer.append(", oldPassword=");
      buffer.append(oldPassword);
      buffer.append(", newPassword=");
      buffer.append(newPassword);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }

  public static class Response extends ExtendedResult<Response>
  {
    public Response(ResultCode resultCode, String matchedDN,
        String diagnosticMessage)
    {
      super(resultCode, matchedDN, diagnosticMessage);
    }



    public ByteString getResponseValue()
    {
      return null;
    }



    public void toString(StringBuilder buffer)
    {
      buffer.append("PasswordModifyExtendedResponse(resultCode=");
      buffer.append(getResultCode());
      buffer.append(", matchedDN=");
      buffer.append(getMatchedDN());
      buffer.append(", diagnosticMessage=");
      buffer.append(getDiagnosticMessage());
      buffer.append(", referrals=");
      buffer.append(getReferrals());
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }

  private static final class Operation implements
      ExtendedOperation<Request, Response>
  {

    public Request decodeRequest(String requestName,
        ByteString requestValue) throws DecodeException
    {
      Request request = new Request();
      if (requestValue != null)
      {
        try
        {
          ASN1Reader reader = ASN1.getReader(requestValue);
          reader.readStartSequence();
          if (reader.hasNextElement()
              && (reader.peekType() == TYPE_PASSWORD_MODIFY_USER_ID))
          {
            request.setUserIdentity(reader.readOctetStringAsString());
          }
          if (reader.hasNextElement()
              && (reader.peekType() == TYPE_PASSWORD_MODIFY_OLD_PASSWORD))
          {
            request.setOldPassword(reader.readOctetString());
          }
          if (reader.hasNextElement()
              && (reader.peekType() == TYPE_PASSWORD_MODIFY_NEW_PASSWORD))
          {
            request.setNewPassword(reader.readOctetString());
          }
          reader.readEndSequence();
        }
        catch (IOException e)
        {
          Message message =
              ERR_EXTOP_PASSMOD_CANNOT_DECODE_REQUEST
                  .get(getExceptionMessage(e));
          throw new DecodeException(message, e);
        }
      }
      return request;
    }



    public Response decodeResponse(ResultCode resultCode,
        String matchedDN, String diagnosticMessage,
        String responseName, ByteString responseValue)
        throws DecodeException
    {
      // TODO: Should we check to make sure OID and value is null?
      return new Response(resultCode, matchedDN, diagnosticMessage);
    }



    public Response decodeResponse(ResultCode resultCode,
        String matchedDN, String diagnosticMessage)
    {
      return new Response(resultCode, matchedDN, diagnosticMessage);
    }
  }



  // Singleton instance.
  private static final Operation OPERATION = new Operation();
}
