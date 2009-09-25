package org.opends.sdk.schema;

import org.opends.messages.Message;
import org.opends.sdk.util.LocalizableException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 16, 2009
 * Time: 6:11:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class SchemaException extends
    IllegalArgumentException implements LocalizableException
{
    // The I18N message associated with this exception.
  private final Message message;



  /**
   * Creates a new localized illegal argument exception with the
   * provided message.
   *
   * @param message
   *          The message that explains the problem that occurred.
   */
  public SchemaException(Message message)
  {
    super(String.valueOf(message));
    this.message = message;
  }



  /**
   * Creates a new localized illegal argument exception with the
   * provided message and cause.
   *
   * @param message
   *          The message that explains the problem that occurred.
   * @param cause
   *          The cause which may be later retrieved by the
   *          {@link #getCause} method. A {@code null} value is
   *          permitted, and indicates that the cause is nonexistent or
   *          unknown.
   */
  public SchemaException(Message message,
      Throwable cause)
  {
    super(String.valueOf(message), cause);
    this.message = message;
  }



  /**
   * {@inheritDoc}
   */
  public Message getMessageObject()
  {
    return this.message;
  }
}
