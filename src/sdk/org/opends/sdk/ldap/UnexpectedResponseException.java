package org.opends.sdk.ldap;



import java.io.IOException;

import org.opends.messages.Message;
import org.opends.sdk.Response;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 16, 2009 Time:
 * 4:05:39 PM To change this template use File | Settings | File
 * Templates.
 */
@SuppressWarnings("serial")
public final class UnexpectedResponseException extends IOException
{
  private final int messageID;
  private final Response response;



  public UnexpectedResponseException(int messageID, Response response)
  {
    super(Message.raw("Unexpected LDAP response: id=%d, message=%s",
        messageID, response).toString());
    this.messageID = messageID;
    this.response = response;
  }



  public int getMessageID()
  {
    return messageID;
  }



  public Response getResponse()
  {
    return response;
  }
}
