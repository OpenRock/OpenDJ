package org.opends.common.api.extended;

import static org.opends.server.util.ServerConstants.OID_PASSWORD_MODIFY_REQUEST;
import static org.opends.server.util.StaticUtils.getExceptionMessage;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;
import org.opends.server.protocols.asn1.ASN1Writer;
import org.opends.server.protocols.asn1.ASN1;
import org.opends.server.protocols.asn1.ASN1Reader;
import org.opends.server.protocols.asn1.ASN1Exception;
import static org.opends.server.extensions.ExtensionsConstants.TYPE_PASSWORD_MODIFY_USER_ID;
import static org.opends.server.extensions.ExtensionsConstants.TYPE_PASSWORD_MODIFY_OLD_PASSWORD;
import static org.opends.server.extensions.ExtensionsConstants.TYPE_PASSWORD_MODIFY_NEW_PASSWORD;
import org.opends.common.api.ResultCode;
import org.opends.common.api.DecodeException;
import static org.opends.messages.ExtensionMessages.ERR_EXTOP_PASSMOD_CANNOT_DECODE_REQUEST;
import org.opends.messages.Message;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jun 25, 2009
 * Time: 2:14:58 PM
 * To change this template use File | Settings | File Templates.
 */
public final class PasswordModifyExtendedOperation
    extends AbstractExtendedOperation
{
  private static final PasswordModifyExtendedOperation SINGLETON =
      new PasswordModifyExtendedOperation();

  private PasswordModifyExtendedOperation() {
    super();
    // We could register the result codes here if they are not
    // already included in the default set.
  }

  public static class PasswordModifyExtendedRequest extends
      ExtendedRequest<PasswordModifyExtendedOperation>
  {
    private String userIdentity;
    private ByteString oldPassword;
    private ByteString newPassword;

    public PasswordModifyExtendedRequest() {
      super(OID_PASSWORD_MODIFY_REQUEST);
    }

    public PasswordModifyExtendedOperation getExtendedOperation() {
      return SINGLETON;
    }

    public String getUserIdentity() {
      return userIdentity;
    }

    public void setUserIdentity(String userIdentity) {
      this.userIdentity = userIdentity;
    }

    public ByteString getOldPassword() {
      return oldPassword;
    }

    public void setOldPassword(ByteString oldPassword) {
      this.oldPassword = oldPassword;
    }

    public ByteString getNewPassword() {
      return newPassword;
    }

    public void setNewPassword(ByteString newPassword) {
      this.newPassword = newPassword;
    }

    public ByteString getRequestValue() {
      ByteStringBuilder buffer = new ByteStringBuilder();
      ASN1Writer writer = ASN1.getWriter(buffer);

      try
      {
        writer.writeStartSequence();
        if(userIdentity != null)
        {
          writer.writeOctetString(TYPE_PASSWORD_MODIFY_USER_ID,
              userIdentity);
        }
        if(oldPassword != null)
        {
          writer.writeOctetString(TYPE_PASSWORD_MODIFY_OLD_PASSWORD,
              oldPassword);
        }
        if(newPassword != null)
        {
          writer.writeOctetString(TYPE_PASSWORD_MODIFY_NEW_PASSWORD,
              newPassword);
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
      buffer.append("PasswordModifyExtendedRequest(requestName=");
      buffer.append(requestName);
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

  public static class PasswordModifyExtendedResponse extends
      ExtendedResponse<PasswordModifyExtendedOperation>
  {
    public PasswordModifyExtendedResponse(ResultCode resultCode,
                                  String matchedDN,
                                  String diagnosticMessage)
    {
      super(resultCode, matchedDN, diagnosticMessage);
    }

    public PasswordModifyExtendedOperation getExtendedOperation() {
      return SINGLETON;
    }

    public ByteString getResponseValue() {
      return null;
    }

    public void toString(StringBuilder buffer) {
      buffer.append("PasswordModifyExtendedResponse(resultCode=");
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
  public PasswordModifyExtendedRequest decodeRequest(String requestName,
                                             ByteString requestValue)
      throws DecodeException
  {
    PasswordModifyExtendedRequest request =
        new PasswordModifyExtendedRequest();
    if(requestValue != null)
    {
      try
      {
        ASN1Reader reader = ASN1.getReader(requestValue);
        reader.readStartSequence();
        if(reader.hasNextElement() &&
            reader.peekType() == TYPE_PASSWORD_MODIFY_USER_ID)
        {
          request.setUserIdentity(reader.readOctetStringAsString());
        }
        if(reader.hasNextElement() &&
            reader.peekType() == TYPE_PASSWORD_MODIFY_OLD_PASSWORD)
        {
          request.setOldPassword(reader.readOctetString());
        }
        if(reader.hasNextElement() &&
            reader.peekType() == TYPE_PASSWORD_MODIFY_NEW_PASSWORD)
        {
          request.setNewPassword(reader.readOctetString());
        }
        reader.readEndSequence();
      }
      catch(ASN1Exception e)
      {
        Message message =
            ERR_EXTOP_PASSMOD_CANNOT_DECODE_REQUEST.get(
                getExceptionMessage(e));
        throw new DecodeException(message, e);
      }
    }
    return request;
  }

  @Override
  public PasswordModifyExtendedResponse decodeResponse(
      ResultCode resultCode, String matchedDN,
      String diagnosticMessage, String responseName,
      ByteString responseValue)
      throws DecodeException
  {
    // TODO: Should we check to make sure OID and value is null?
    return new PasswordModifyExtendedResponse(resultCode, matchedDN,
        diagnosticMessage);
  }
}
