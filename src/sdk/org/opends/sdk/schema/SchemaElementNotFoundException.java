package org.opends.sdk.schema;

import org.opends.sdk.util.LocalizableException;
import org.opends.messages.Message;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Sep 2, 2009
 * Time: 5:24:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class SchemaElementNotFoundException extends
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
  public SchemaElementNotFoundException(Message message)
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
  public SchemaElementNotFoundException(Message message,
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
