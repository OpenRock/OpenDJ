package org.opends.client.protocol.ldap;

import org.opends.server.types.IdentifiedException;
import org.opends.messages.Message;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 10:37:10
 * AM To change this template use File | Settings | File Templates.
 */
public class InvalidConnectionException extends IdentifiedException
{
  public InvalidConnectionException(Message message)
  {
    super(message);
  }

  public InvalidConnectionException(Message message, Throwable cause)
  {
    super(message, cause);
  }
}
