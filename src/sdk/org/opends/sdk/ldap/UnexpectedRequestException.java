package org.opends.sdk.ldap;



import java.io.IOException;

import org.opends.messages.Message;
import org.opends.sdk.Request;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 16, 2009 Time:
 * 4:05:39 PM To change this template use File | Settings | File
 * Templates.
 */
@SuppressWarnings("serial")
public final class UnexpectedRequestException extends IOException
{
  private final int messageID;
  private final Request request;



  public UnexpectedRequestException(int messageID, Request request)
  {
    super(Message.raw("Unexpected LDAP request: id=%d, message=%s",
        messageID, request).toString());
    this.messageID = messageID;
    this.request = request;
  }



  public int getMessageID()
  {
    return messageID;
  }



  public Request getRequest()
  {
    return request;
  }
}
