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
import org.opends.ldap.responses.Result;
import org.opends.ldap.responses.Responses;
import org.opends.messages.Message;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;
import org.opends.spi.AbstractExtendedRequest;
import org.opends.spi.ExtendedOperation;
import org.opends.types.ResultCode;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 25, 2009 Time: 2:14:58
 * PM To change this template use File | Settings | File Templates.
 */
public final class PasswordModifyRequest extends
    AbstractExtendedRequest<PasswordModifyRequest, Result>
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



  public StringBuilder toString(StringBuilder builder)
  {
    builder.append("PasswordModifyExtendedRequest(requestName=");
    builder.append(getRequestName());
    builder.append(", userIdentity=");
    builder.append(userIdentity);
    builder.append(", oldPassword=");
    builder.append(oldPassword);
    builder.append(", newPassword=");
    builder.append(newPassword);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder;
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
