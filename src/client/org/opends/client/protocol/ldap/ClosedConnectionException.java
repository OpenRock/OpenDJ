package org.opends.client.protocol.ldap;

import org.opends.server.types.IdentifiedException;
import org.opends.messages.Message;
import org.opends.common.api.LocalizableException;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 10:37:10
 * AM To change this template use File | Settings | File Templates.
 */
public class ClosedConnectionException extends IOException
    implements LocalizableException
{
  // The I18N message associated with this exception.
  private final Message message;

  public ClosedConnectionException(Message message)
  {
    super();
    this.message = message;
  }

  public ClosedConnectionException(Message message, Throwable cause)
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
