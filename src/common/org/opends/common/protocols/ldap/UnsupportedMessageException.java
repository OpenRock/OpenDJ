package org.opends.common.protocols.ldap;

import org.opends.common.api.Message;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 16, 2009 Time: 4:05:39
 * PM To change this template use File | Settings | File Templates.
 */
public final class UnsupportedMessageException extends IOException
{
  private int messageID;
  private Message ldapMessage;

  public UnsupportedMessageException(int messageID, Message ldapMessage)
  {
    super(org.opends.messages.Message.raw("Unsupported LDAP message: id=%d, message=%s",
                      messageID, ldapMessage).toString());
    this.messageID = messageID;
    this.ldapMessage = ldapMessage;
  }

  public int getMessageID()
  {
    return messageID;
  }

  public Message getLDAPMessage()
  {
    return ldapMessage;
  }
}
