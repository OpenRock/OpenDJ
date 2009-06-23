package org.opends.common.api;

import org.opends.server.types.IdentifiedException;
import org.opends.messages.Message;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jun 22, 2009
 * Time: 5:31:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class DecodeException extends IdentifiedException
{
    /**
   * Creates a new decode exception with the provided message.
   *
   * @param  message    The message that explains the problem that occurred.
   */
  public DecodeException(Message message)
  {
    super(message);
  }



  /**
   * Creates a new decode exception with the provided message and root
   * cause.
   *
   * @param  message    The message that explains the problem that occurred.
   * @param  cause      The exception that was caught to trigger this exception.
   */
  public DecodeException(Message message, Throwable cause)
  {
    super(message, cause);
  }
}
