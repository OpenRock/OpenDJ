package org.opends.schema;

import org.opends.messages.Message;
import org.opends.server.types.IdentifiedException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 16, 2009
 * Time: 6:11:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class SchemaException extends IdentifiedException
{
  /**
   * Creates a new decode exception with the provided message.
   *
   * @param message
   *          The message that explains the problem that occurred.
   */
  public SchemaException(Message message)
  {
    super(message);
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
  public SchemaException(Message message, Throwable cause)
  {
    super(message, cause);
  }
}
