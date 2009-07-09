package org.opends.ldap.requests;



import org.opends.server.types.ByteString;
import org.opends.server.util.StaticUtils;
import org.opends.server.util.Validator;
import org.opends.types.DN;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 4, 2009 Time:
 * 4:23:35 PM To change this template use File | Settings | File
 * Templates.
 */
public class GenericBindRequest extends BindRequest
{
  private byte authenticationType;
  private ByteString authenticationBytes;



  public GenericBindRequest(DN bindDN, byte authenticationType,
      ByteString authenticationBytes)
  {
    super(bindDN.toString());
    Validator.ensureNotNull(authenticationType, authenticationBytes);
    this.authenticationType = authenticationType;
    this.authenticationBytes = authenticationBytes;
  }



  public GenericBindRequest(String bindDN, byte authenticationType,
      ByteString authenticationBytes)
  {
    super(bindDN);
    Validator.ensureNotNull(authenticationType, authenticationBytes);
    this.authenticationType = authenticationType;
    this.authenticationBytes = authenticationBytes;
  }



  public ByteString getAuthenticationBytes()
  {
    return authenticationBytes;
  }



  public byte getAuthenticationType()
  {
    return authenticationType;
  }



  public GenericBindRequest setAuthenticationBytes(
      ByteString unknownAuthBytes)
  {
    Validator.ensureNotNull(unknownAuthBytes);
    this.authenticationBytes = unknownAuthBytes;
    return this;
  }



  public GenericBindRequest setAuthenticationType(
      byte authenticationType)
  {
    Validator.ensureNotNull(authenticationType);
    this.authenticationType = authenticationType;
    return this;
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
