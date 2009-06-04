package org.opends.common.api.raw.request;

import org.opends.server.types.ByteString;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.ResultCode;
import org.opends.server.util.Validator;
import org.opends.server.core.operations.BindRequest;
import org.opends.server.core.operations.Schema;
import org.opends.messages.Message;
import static org.opends.messages.ProtocolMessages.
    ERR_LDAP_BIND_REQUEST_DECODE_INVALID_CRED_TYPE;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 4, 2009 Time: 4:23:35
 * PM To change this template use File | Settings | File Templates.
 */
public class RawUnknownBindRequest extends RawBindRequest
{
  private byte unknownAuthType;
  private ByteString unknownAuthBytes;

  public RawUnknownBindRequest(String bindDN, byte unknownAuthType,
                               ByteString unknownAuthBytes)
  {
    super(bindDN);
    Validator.ensureNotNull(unknownAuthType);
    Validator.ensureNotNull(unknownAuthBytes);
    this.unknownAuthType = unknownAuthType;
    this.unknownAuthBytes = unknownAuthBytes;
  }

  public ByteString getUnknownAuthBytes()
  {
    return unknownAuthBytes;
  }

  public RawUnknownBindRequest setUnknownAuthBytes(ByteString unknownAuthBytes)
  {
    Validator.ensureNotNull(unknownAuthBytes);
    this.unknownAuthBytes = unknownAuthBytes;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public BindRequest toRequest(Schema schema) throws DirectoryException
  {
    Message message =
        ERR_LDAP_BIND_REQUEST_DECODE_INVALID_CRED_TYPE.get(unknownAuthType);
    throw new DirectoryException(ResultCode.AUTH_METHOD_NOT_SUPPORTED,
                                 message);
  }




  /**
   * {@inheritDoc}
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("BindRequest(bindDN=");
    buffer.append(getBindDN());
    buffer.append(", authentication=unknown");
    buffer.append(", unknownAuthBytes=");
    buffer.append(unknownAuthBytes);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
