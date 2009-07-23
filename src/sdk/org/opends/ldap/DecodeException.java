package org.opends.ldap;



import org.opends.messages.Message;
import org.opends.util.LocalizableException;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 22, 2009 Time: 5:31:40
 * PM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public final class DecodeException extends Exception implements
    LocalizableException
{
  private final Message message;



  /**
   * Creates a new decode exception with the provided message.
   *
   * @param message
   *          The message that explains the problem that occurred.
   */
  public DecodeException(Message message)
  {
    this(message, null);
  }



  /**
   * Creates a new decode exception with the provided message and root
   * cause.
   *
   * @param message
   *          The message that explains the problem that occurred.
   * @param cause
   *          The exception that was caught to trigger this exception.
   */
  public DecodeException(Message message, Throwable cause)
  {
    super(message.toString(), cause);
    this.message = message;
  }



  /**
   * Returns the message that explains the problem that occurred.
   *
   * @return Message of the problem
   */
  public Message getMessageObject()
  {
    return message;
  }
}
