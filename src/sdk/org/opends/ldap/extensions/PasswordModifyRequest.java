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
import org.opends.ldap.responses.Result;
import org.opends.messages.Message;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 25, 2009 Time: 2:14:58
 * PM To change this template use File | Settings | File Templates.
 */
public final class PasswordModifyRequest extends
    ExtendedRequest<PasswordModifyRequest, Result>
{

  private String userIdentity;
  private ByteString oldPassword;
  private ByteString newPassword;



  public PasswordModifyRequest()
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



  public PasswordModifyRequest setNewPassword(ByteString newPassword)
  {
    this.newPassword = newPassword;
    return this;
  }



  public PasswordModifyRequest setOldPassword(ByteString oldPassword)
  {
    this.oldPassword = oldPassword;
    return this;
  }



  public PasswordModifyRequest setUserIdentity(String userIdentity)
  {
    this.userIdentity = userIdentity;
    return this;
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



  private static final class Operation implements
      ExtendedOperation<PasswordModifyRequest, Result>
  {

    public PasswordModifyRequest decodeRequest(String requestName,
        ByteString requestValue) throws DecodeException
    {
      PasswordModifyRequest request = new PasswordModifyRequest();
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



    public Result decodeResponse(ResultCode resultCode,
        String matchedDN, String diagnosticMessage,
        String responseName, ByteString responseValue)
        throws DecodeException
    {
      // TODO: Should we check to make sure OID and value is null?
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
