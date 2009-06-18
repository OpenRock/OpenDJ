package org.opends.common.api.raw.request;

import org.opends.server.types.ByteString;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.ResultCode;
import org.opends.server.util.Validator;
import org.opends.server.util.StaticUtils;
import org.opends.server.core.operations.BindRequest;
import org.opends.server.core.operations.Schema;
import org.opends.messages.Message;
import static org.opends.messages.ProtocolMessages.
    ERR_LDAP_BIND_REQUEST_DECODE_INVALID_CRED_TYPE;
import org.opends.common.api.DN;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 4, 2009 Time: 4:23:35
 * PM To change this template use File | Settings | File Templates.
 */
public class RawUnknownBindRequest extends RawBindRequest
{
  private byte authenticationType;
  private ByteString authenticationBytes;

  public RawUnknownBindRequest(String bindDN, byte authenticationType,
                               ByteString authenticationBytes)
  {
    super(bindDN);
    Validator.ensureNotNull(authenticationType, authenticationBytes);
    this.authenticationType = authenticationType;
    this.authenticationBytes = authenticationBytes;
  }


  public RawUnknownBindRequest(DN bindDN, byte authenticationType,
                               ByteString authenticationBytes)
  {
    super(bindDN.toString());
    Validator.ensureNotNull(authenticationType, authenticationBytes);
    this.authenticationType = authenticationType;
    this.authenticationBytes = authenticationBytes;
  }

  public byte getAuthenticationType()
  {
    return authenticationType;
  }

  public RawUnknownBindRequest setAuthenticationType(byte authenticationType)
  {
    Validator.ensureNotNull(authenticationType);
    this.authenticationType = authenticationType;
    return this;
  }

  public ByteString getAuthenticationBytes()
  {
    return authenticationBytes;
  }

  public RawUnknownBindRequest setAuthenticationBytes(ByteString unknownAuthBytes)
  {
    Validator.ensureNotNull(unknownAuthBytes);
    this.authenticationBytes = unknownAuthBytes;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public BindRequest toRequest(Schema schema) throws DirectoryException
  {
    // TODO: Use this error somewhere!
    Message message =
        ERR_LDAP_BIND_REQUEST_DECODE_INVALID_CRED_TYPE.get(authenticationType);
    throw new DirectoryException(ResultCode.AUTH_METHOD_NOT_SUPPORTED,
                                 message);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("UnkownBindRequest(bindDN=");
    buffer.append(getBindDN());
    buffer.append(", authenticationType=");
    buffer.append(StaticUtils.byteToHex(authenticationType));
    buffer.append(", authenticationBytes=");
    buffer.append(authenticationBytes.toHex());
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
