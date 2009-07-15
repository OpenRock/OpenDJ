package org.opends.ldap.impl;



import java.io.IOException;

import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 16, 2009 Time:
 * 4:05:39 PM To change this template use File | Settings | File
 * Templates.
 */
@SuppressWarnings("serial")
public final class UnsupportedMessageException extends IOException
{
  private final int id;
  private final byte tag;
  private final ByteString content;



  public UnsupportedMessageException(int id, byte tag,
      ByteString content)
  {
    super(org.opends.messages.Message.raw(
        "Unsupported LDAP message: id=%d, tag=%d, content=%s", id, tag,
        content).toString());
    this.id = id;
    this.tag = tag;
    this.content = content;
  }



  public ByteString getContent()
  {
    return content;
  }



  public int getID()
  {
    return id;
  }



  public byte getTag()
  {
    return tag;
  }
}
