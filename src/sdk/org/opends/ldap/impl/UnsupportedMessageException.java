package org.opends.ldap.impl;



import java.io.IOException;

import org.opends.ldap.Message;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 16, 2009 Time:
 * 4:05:39 PM To change this template use File | Settings | File
 * Templates.
 */
@SuppressWarnings("serial")
public final class UnsupportedMessageException extends IOException
{
  private final int messageID;
  private final Message ldapMessage;



  public UnsupportedMessageException(int messageID, Message ldapMessage)
  {
    super(org.opends.messages.Message.raw(
        "Unsupported LDAP message: id=%d, message=%s", messageID,
        ldapMessage).toString());
    this.messageID = messageID;
    this.ldapMessage = ldapMessage;
  }



  public Message getLDAPMessage()
  {
    return ldapMessage;
  }



  public int getMessageID()
  {
    return messageID;
  }
}
