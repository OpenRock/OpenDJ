package org.opends.common.api.controls;

import org.opends.server.types.*;
import static org.opends.server.util.ServerConstants.OID_PAGED_RESULTS_CONTROL;
import org.opends.server.util.Validator;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.messages.Message;
import static org.opends.messages.ProtocolMessages.*;
import static org.opends.messages.ProtocolMessages.ERR_LDAP_PAGED_RESULTS_DECODE_SEQUENCE;
import org.opends.common.api.DecodeException;
import org.opends.common.protocols.asn1.ASN1Reader;
import org.opends.common.protocols.asn1.ASN1;
import org.opends.common.protocols.asn1.ASN1Writer;

import java.io.IOException;

/**
 * This class represents a paged results control value as defined in
 * RFC 2696.
 *
 * The searchControlValue is an OCTET STRING wrapping the BER-encoded
 * version of the following SEQUENCE:
 *
 * realSearchControlValue ::= SEQUENCE {
 *         size            INTEGER (0..maxInt),
 *                                 -- requested page size from client
 *                                 -- result set size estimate from server
 *         cookie          OCTET STRING
 * }
 *
 */
public class PagedResultsControl extends Control
{
    /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  /**
   * ControlDecoder implentation to decode this control from a ByteString.
   */
  private final static class Decoder
      implements ControlDecoder<PagedResultsControl>
  {
    /**
     * {@inheritDoc}
     */
    public PagedResultsControl decode(boolean isCritical, ByteString value)
        throws DecodeException
    {
      if (value == null)
      {
        Message message = ERR_LDAP_PAGED_RESULTS_DECODE_NULL.get();
        throw new DecodeException(message);
      }

      ASN1Reader reader = ASN1.getReader(value);
      try
      {
        reader.readStartSequence();
      }
      catch (Exception e)
      {
        if (debugEnabled())
        {
          TRACER.debugCaught(DebugLogLevel.ERROR, e);
        }

        Message message =
            ERR_LDAP_PAGED_RESULTS_DECODE_SEQUENCE.get(String.valueOf(e));
        throw new DecodeException(message, e);
      }

      int size;
      try
      {
        size = (int)reader.readInteger();
      }
      catch (Exception e)
      {
        if (debugEnabled())
        {
          TRACER.debugCaught(DebugLogLevel.ERROR, e);
        }

        Message message =
            ERR_LDAP_PAGED_RESULTS_DECODE_SIZE.get(String.valueOf(e));
        throw new DecodeException(message, e);
      }

      ByteString cookie;
      try
      {
        cookie = reader.readOctetString();
      }
      catch (Exception e)
      {
        if (debugEnabled())
        {
          TRACER.debugCaught(DebugLogLevel.ERROR, e);
        }

        Message message =
            ERR_LDAP_PAGED_RESULTS_DECODE_COOKIE.get(String.valueOf(e));
        throw new DecodeException(message, e);
      }

      try
      {
        reader.readEndSequence();
      }
      catch (Exception e)
      {
        if (debugEnabled())
        {
          TRACER.debugCaught(DebugLogLevel.ERROR, e);
        }

        Message message =
            ERR_LDAP_PAGED_RESULTS_DECODE_SEQUENCE.get(String.valueOf(e));
        throw new DecodeException(message, e);
      }

      return new PagedResultsControl(isCritical, size, cookie);
    }

    public String getOID()
    {
      return OID_PAGED_RESULTS_CONTROL;
    }
  }

  /**
   * The Control Decoder that can be used to decode this control.
   */
  public static final ControlDecoder<PagedResultsControl> DECODER =
    new Decoder();


 /**
   * The control value size element, which is either the requested page size
   * from the client, or the result set size estimate from the server.
   */
  private int size;


  /**
   * The control value cookie element.
   */
  private ByteString cookie;

   /**
   * Creates a new paged results control with the specified information.
   *
   * @param  size        The size element.
   * @param  cookie      The cookie element.
   */
  public PagedResultsControl(int size, ByteString cookie)
  {
    this(false, size, cookie);
  }

  /**
   * Creates a new paged results control with the specified information.
   *
   * @param  isCritical  Indicates whether this control should be considered
   *                     critical in processing the request.
   * @param  size        The size element.
   * @param  cookie      The cookie element.
   */
  public PagedResultsControl(boolean isCritical, int size,
                             ByteString cookie)
  {
    super(OID_PAGED_RESULTS_CONTROL, isCritical);

    Validator.ensureNotNull(cookie);
    this.size   = size;
    this.cookie = cookie;
  }

  public ByteString getValue() {
    ByteStringBuilder buffer = new ByteStringBuilder();
    ASN1Writer writer = ASN1.getWriter(buffer);
    try
    {
      writer.writeStartSequence();
      writer.writeInteger(size);
      writer.writeOctetString(cookie);
      writer.writeEndSequence();
      return buffer.toByteString();
    }
    catch(IOException ioe)
    {
      // This should never happen unless there is a bug somewhere.
      throw new RuntimeException(ioe);
    }
  }

  public boolean hasValue() {
    return true;
  }

  public void toString(StringBuilder buffer) {
    buffer.append("PagedResultsControl(oid=");
    buffer.append(getOID());
    buffer.append(", criticality=");
    buffer.append(isCritical());
    buffer.append(", size=");
    buffer.append(size);
    buffer.append(", cookie=");
    buffer.append(cookie);
    buffer.append(")");
  }


  /**
   * Get the control value size element, which is either the requested page size
   * from the client, or the result set size estimate from the server.
   * @return The control value size element.
   */
  public int getSize()
  {
    return size;
  }



  /**
   * Get the control value cookie element.
   * @return The control value cookie element.
   */
  public ByteString getCookie()
  {
    return cookie;
  }
}
