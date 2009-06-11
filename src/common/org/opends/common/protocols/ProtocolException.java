package org.opends.common.protocols;

import org.opends.messages.Message;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 9, 2009 Time: 7:19:56
 * PM To change this template use File | Settings | File Templates.
 */
public final class ProtocolException extends IOException
{
  private Message message;

  /**
   * Creates a new identified exception with the provided information.
   *
   * @param  message  The message that explains the problem that
   *                  occurred.
   */
  public ProtocolException(Message message)
  {
    super(message != null ? message.toString() : null);
    if (message != null) {
      this.message = message;
    }
  }



  /**
   * Returns the message that explains the problem that occurred.
   *
   * @return Message of the problem
   */
  public Message getMessageObject() {
    return this.message;
  }

}
