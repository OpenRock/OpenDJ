package org.opends.ldap;



import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time:
 * 5:45:36 PM To change this template use File | Settings | File
 * Templates.
 */
public class GenericMessage extends Message
{
  private byte messageTag;
  private ByteString messageBytes;



  public GenericMessage(byte messageTag, ByteString messageBytes)
  {
    Validator.ensureNotNull(messageTag, messageBytes);
    this.messageBytes = messageBytes;
  }



  public ByteString getMessageBytes()
  {
    return messageBytes;
  }



  public byte getMessageTag()
  {
    return messageTag;
  }



  public GenericMessage setMessageBytes(ByteString messageBytes)
  {
    Validator.ensureNotNull(messageBytes);
    this.messageBytes = messageBytes;
    return this;
  }



  public GenericMessage setMessageTag(byte messageTag)
  {
    Validator.ensureNotNull(messageTag);
    this.messageTag = messageTag;
    return this;
  }



  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("UnknownMessage(messageTag=");
    buffer.append(messageTag);
    buffer.append(", messageBytes=");
    buffer.append(messageBytes.toHex());
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
