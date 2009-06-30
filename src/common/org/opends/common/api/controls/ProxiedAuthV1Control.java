package org.opends.common.api.controls;

import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import org.opends.server.loggers.debug.DebugTracer;
import static org.opends.server.util.StaticUtils.getExceptionMessage;
import static org.opends.server.util.ServerConstants.OID_PROXIED_AUTH_V1;
import org.opends.server.util.Validator;
import org.opends.server.types.ByteString;
import org.opends.server.types.DebugLogLevel;
import org.opends.messages.Message;
import static org.opends.messages.ProtocolMessages.*;
import org.opends.common.api.DecodeException;
import org.opends.common.api.DN;
import org.opends.common.protocols.asn1.ASN1Reader;
import org.opends.common.protocols.asn1.ASN1;

import java.io.IOException;

/**
 * This class implements version 1 of the proxied authorization control as
 * defined in early versions of draft-weltman-ldapv3-proxy (this implementation
 * is based on the "-04" revision).  It makes it possible for one user to
 * request that an operation be performed under the authorization of another.
 * The target user is specified as a DN in the control value, which
 * distinguishes it from later versions of the control (which used a different
 * OID) in which the target user was specified using an authorization ID.
 */
public class ProxiedAuthV1Control extends Control
{
  /**
   * ControlDecoder implentation to decode this control from a ByteString.
   */
  private final static class Decoder
      implements ControlDecoder<ProxiedAuthV1Control>
  {
    /**
     * {@inheritDoc}
     */
    public ProxiedAuthV1Control decode(boolean isCritical, ByteString value)
        throws DecodeException
    {
      if (!isCritical)
      {
        Message message = ERR_PROXYAUTH1_CONTROL_NOT_CRITICAL.get();
        throw new DecodeException(message);
      }

      if (value == null)
      {
        Message message = ERR_PROXYAUTH1_NO_CONTROL_VALUE.get();
        throw new DecodeException(message);
      }

      ASN1Reader reader = ASN1.getReader(value);
      String authorizationDN;
      try
      {
        reader.readStartSequence();
        authorizationDN = reader.readOctetStringAsString();
        reader.readEndSequence();
      }
      catch (IOException e)
      {
        if (debugEnabled())
        {
          TRACER.debugCaught(DebugLogLevel.ERROR, e);
        }

        Message message =
            ERR_PROXYAUTH1_CANNOT_DECODE_VALUE.get(getExceptionMessage(e));
        throw new DecodeException(message, e);
      }

      return new ProxiedAuthV1Control(authorizationDN);
    }

    public String getOID()
    {
      return OID_PROXIED_AUTH_V1;
    }

  }

  /**
   * The Control Decoder that can be used to decode this control.
   */
  public static final ControlDecoder<ProxiedAuthV1Control> DECODER =
    new Decoder();

  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();




  // The raw, unprocessed authorization DN from the control value.
  private String authorizationDN;



  /**
   * Creates a new instance of the proxied authorization v1 control with the
   * provided information.
   *
   * @param  authorizationDN  The raw, unprocessed authorization DN from the
   *                             control value.  It must not be {@code null}.
   */
  public ProxiedAuthV1Control(String authorizationDN)
  {
    super(OID_PROXIED_AUTH_V1, true);

    Validator.ensureNotNull(authorizationDN);
    this.authorizationDN = authorizationDN;
  }



  /**
   * Creates a new instance of the proxied authorization v1 control with the
   * provided information.
   *
   * @param  authorizationDN  The authorization DN from the control value.  It
   *                          must not be {@code null}.
   */
  public ProxiedAuthV1Control(DN authorizationDN)
  {
    super(OID_PROXIED_AUTH_V1, true);

    Validator.ensureNotNull(authorizationDN);
    this.authorizationDN = authorizationDN.toString();
  }

  public ByteString getValue() {
    return ByteString.valueOf(authorizationDN);
  }

  public boolean hasValue() {
    return true;
  }



  /**
   * Retrieves the raw, unprocessed authorization DN from the control value.
   *
   * @return  The raw, unprocessed authorization DN from the control value.
   */
  public String getAuthorizationDN()
  {
    return authorizationDN;
  }

  public ProxiedAuthV1Control setAuthorizationDN(String authorizationDN)
  {
    Validator.ensureNotNull(authorizationDN);
    this.authorizationDN = authorizationDN;
    return this;
  }

 public ProxiedAuthV1Control setAuthorizationDN(DN authorizationDN)
  {
    Validator.ensureNotNull(authorizationDN);
    this.authorizationDN = authorizationDN.toString();
    return this;
  }

  /**
   * Appends a string representation of this proxied auth v1 control to the
   * provided buffer.
   *
   * @param  buffer  The buffer to which the information should be appended.
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("ProxiedAuthorizationV1Control(oid=");
    buffer.append(getOID());
    buffer.append(", criticality=");
    buffer.append(isCritical());
    buffer.append(", authorizationDN=\"");
    buffer.append(authorizationDN);
    buffer.append("\")");
  }
}
