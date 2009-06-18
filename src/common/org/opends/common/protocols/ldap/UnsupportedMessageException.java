package org.opends.common.protocols.ldap;

import org.opends.common.api.raw.RawMessage;
import org.opends.server.types.IdentifiedException;
import org.opends.server.types.ResultCode;
import org.opends.messages.Message;
import static org.opends.messages.ProtocolMessages.ERR_LDAP_BIND_REQUEST_DECODE_INVALID_CRED_TYPE;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 16, 2009 Time: 4:05:39
 * PM To change this template use File | Settings | File Templates.
 */
public final class UnsupportedMessageException extends IOException
{
  private int messageID;
  private RawMessage ldapMessage;

  public UnsupportedMessageException(int messageID, RawMessage ldapMessage)
  {
    super(Message.raw("Unsupported LDAP message: id=%d, message=%s",
                      messageID, ldapMessage).toString());
    this.messageID = messageID;
    this.ldapMessage = ldapMessage;
  }

  public int getMessageID()
  {
    return messageID;
  }

  public RawMessage getLDAPMessage()
  {
    return ldapMessage;
  }
}
