package org.opends.common.protocols.ldap;

import org.opends.server.types.IdentifiedException;
import org.opends.server.types.ResultCode;
import org.opends.messages.Message;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 28, 2009 Time: 10:43:56
 * AM To change this template use File | Settings | File Templates.
 */
public class LDAPProtocolException extends IdentifiedException
{
  private ResultCode resultCode;
  private boolean disconnect;

  public LDAPProtocolException(Message message, ResultCode resultCode,
                               boolean disconnect)
  {
    super(message);
    this.resultCode = resultCode;
    this.disconnect = disconnect;
  }

  public LDAPProtocolException(Message message, Throwable cause,
                               ResultCode resultCode, boolean disconnect)
  {
    super(message, cause);
    this.resultCode = resultCode;
    this.disconnect = disconnect;
  }

  public LDAPProtocolException(Message message, boolean disconnect)
  {
    this(message, ResultCode.PROTOCOL_ERROR, disconnect);
  }

  public LDAPProtocolException(Message message, Throwable cause,
                                boolean disconnect)
  {
    this(message, cause, ResultCode.PROTOCOL_ERROR, disconnect);
  }

  public boolean isDisconnect()
  {
    return disconnect;
  }

  public ResultCode getResultCode()
  {
    return resultCode;
  }
}
