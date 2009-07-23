package org.opends.ldap.controls;



import static org.opends.messages.ProtocolMessages.*;
import static org.opends.server.loggers.debug.DebugLogger.*;
import static org.opends.server.util.ServerConstants.*;
import static org.opends.server.util.StaticUtils.*;

import org.opends.ldap.DecodeException;
import org.opends.messages.Message;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.types.ByteString;
import org.opends.server.types.DebugLogLevel;
import org.opends.spi.ControlDecoder;



/**
 * This class implements the Netscape password expiring control, which
 * serves as a warning to clients that the user's password is about to
 * expire. The only element contained in the control value is a string
 * representation of the number of seconds until expiration.
 */
public class PasswordExpiringControl extends Control
{
  /**
   * ControlDecoder implentation to decode this control from a
   * ByteString.
   */
  private final static class Decoder implements
      ControlDecoder<PasswordExpiringControl>
  {
    /**
     * {@inheritDoc}
     */
    public PasswordExpiringControl decode(boolean isCritical,
        ByteString value) throws DecodeException
    {
      if (value == null)
      {
        Message message = ERR_PWEXPIRING_NO_CONTROL_VALUE.get();
        throw new DecodeException(message);
      }

      int secondsUntilExpiration;
      try
      {
        secondsUntilExpiration = Integer.parseInt(value.toString());
      }
      catch (Exception e)
      {
        if (debugEnabled())
        {
          TRACER.debugCaught(DebugLogLevel.ERROR, e);
        }

        Message message =
            ERR_PWEXPIRING_CANNOT_DECODE_SECONDS_UNTIL_EXPIRATION
                .get(getExceptionMessage(e));
        throw new DecodeException(message);
      }

      return new PasswordExpiringControl(isCritical,
          secondsUntilExpiration);
    }



    public String getOID()
    {
      return OID_NS_PASSWORD_EXPIRING;
    }

  }



  /**
   * The Control Decoder that can be used to decode this control.
   */
  public static final ControlDecoder<PasswordExpiringControl> DECODER =
      new Decoder();

  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  // The length of time in seconds until the password actually expires.
  private final int secondsUntilExpiration;



  /**
   * Creates a new instance of the password expiring control with the
   * provided information.
   * 
   * @param isCritical
   *          Indicates whether support for this control should be
   *          considered a critical part of the client processing.
   * @param secondsUntilExpiration
   *          The length of time in seconds until the password actually
   *          expires.
   */
  public PasswordExpiringControl(boolean isCritical,
      int secondsUntilExpiration)
  {
    super(OID_NS_PASSWORD_EXPIRING, isCritical);

    this.secondsUntilExpiration = secondsUntilExpiration;
  }



  /**
   * Creates a new instance of the password expiring control with the
   * provided information.
   * 
   * @param secondsUntilExpiration
   *          The length of time in seconds until the password actually
   *          expires.
   */
  public PasswordExpiringControl(int secondsUntilExpiration)
  {
    this(false, secondsUntilExpiration);
  }



  /**
   * Retrieves the length of time in seconds until the password actually
   * expires.
   * 
   * @return The length of time in seconds until the password actually
   *         expires.
   */
  public int getSecondsUntilExpiration()
  {
    return secondsUntilExpiration;
  }



  @Override
  public ByteString getValue()
  {
    return ByteString.valueOf(String.valueOf(secondsUntilExpiration));
  }



  @Override
  public boolean hasValue()
  {
    return true;
  }



  /**
   * Appends a string representation of this password expiring control
   * to the provided buffer.
   * 
   * @param buffer
   *          The buffer to which the information should be appended.
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("PasswordExpiringControl(oid=");
    buffer.append(getOID());
    buffer.append(", criticality=");
    buffer.append(isCritical());
    buffer.append(", secondsUntilExpiration=");
    buffer.append(secondsUntilExpiration);
    buffer.append(")");
  }
}
