package org.opends.sdk.controls;



import static org.opends.messages.ProtocolMessages.ERR_POSTREADREQ_CANNOT_DECODE_VALUE;
import static org.opends.messages.ProtocolMessages.ERR_POSTREADREQ_NO_CONTROL_VALUE;
import static org.opends.messages.ProtocolMessages.ERR_POSTREADRESP_CANNOT_DECODE_VALUE;
import static org.opends.messages.ProtocolMessages.ERR_POSTREADRESP_NO_CONTROL_VALUE;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import static org.opends.server.util.ServerConstants.OID_LDAP_READENTRY_POSTREAD;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opends.messages.Message;
import org.opends.sdk.DecodeException;
import org.opends.sdk.SearchResultEntry;
import org.opends.sdk.asn1.ASN1;
import org.opends.sdk.asn1.ASN1Reader;
import org.opends.sdk.asn1.ASN1Writer;
import org.opends.sdk.ldap.LDAPDecoder;
import org.opends.sdk.ldap.LDAPEncoder;
import org.opends.sdk.spi.ControlDecoder;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;
import org.opends.server.types.DebugLogLevel;



/**
 * This class implements the post-read control as defined in RFC 4527.
 * This control makes it possible to retrieve an entry in the state that
 * it held immediately after an add, modify, or modify DN operation. It
 * may specify a specific set of attributes that should be included in
 * that entry.
 */
public class PostReadControl
{
  /**
   * This class implements the post-read request control as defined in
   * RFC 4527. This control makes it possible to retrieve an entry in
   * the state that it held immediately after an add, modify, or modify
   * DN operation. It may specify a specific set of attributes that
   * should be included in that entry. The entry will be encoded in a
   * corresponding response control.
   */
  public static class Request extends Control
  {
    // The set of raw attributes to return in the entry.
    private final Set<String> attributes;



    /**
     * Creates a new instance of this LDAP post-read request control
     * with the provided information.
     *
     * @param isCritical
     *          Indicates whether support for this control should be
     *          considered a critical part of the server processing.
     * @param attributes
     *          The set of attributes to return in the entry. A null or
     *          empty set will indicates that all user attributes should
     *          be returned.
     */
    public Request(boolean isCritical, String... attributes)
    {
      super(OID_LDAP_READENTRY_POSTREAD, isCritical);

      this.attributes = new LinkedHashSet<String>();
      if (attributes != null)
      {
        this.attributes.addAll(Arrays.asList(attributes));
      }
    }



    private Request(boolean isCritical, Set<String> attributes)
    {
      super(OID_LDAP_READENTRY_POSTREAD, isCritical);

      this.attributes = attributes;
    }



    public Request addAttribute(String attribute)
    {
      attributes.add(attribute);
      return this;
    }



    /**
     * Retrieves the raw, unprocessed set of requested attributes.
     *
     * @return The raw, unprocessed set of attributes.
     */
    public Iterable<String> getAttributes()
    {
      return attributes;
    }



    @Override
    public ByteString getValue()
    {
      ByteStringBuilder buffer = new ByteStringBuilder();
      ASN1Writer writer = ASN1.getWriter(buffer);
      try
      {
        writer.writeStartSequence();
        if (attributes != null)
        {
          for (String attr : attributes)
          {
            writer.writeOctetString(attr);
          }
        }
        writer.writeEndSequence();
        return buffer.toByteString();
      }
      catch (IOException ioe)
      {
        // This should never happen unless there is a bug somewhere.
        throw new RuntimeException(ioe);
      }
    }



    @Override
    public boolean hasValue()
    {
      return true;
    }



    /**
     * Appends a string representation of this LDAP post-read request
     * control to the provided buffer.
     *
     * @param buffer
     *          The buffer to which the information should be appended.
     */
    @Override
    public void toString(StringBuilder buffer)
    {
      buffer.append("PostReadRequestControl(oid=");
      buffer.append(getOID());
      buffer.append(", criticality=");
      buffer.append(isCritical());
      buffer.append(", attributes=");
      buffer.append(attributes);
      buffer.append(")");
    }
  }

  /**
   * This class implements the post-read response control as defined in
   * RFC 4527. This control holds the search result entry representing
   * the state of the entry immediately before an add, modify, or modify
   * DN operation.
   */
  public static class Response extends Control
  {
    private final SearchResultEntry entry;



    /**
     * Creates a new instance of this LDAP post-read response control
     * with the provided information.
     *
     * @param isCritical
     *          Indicates whether support for this control should be
     *          considered a critical part of the server processing.
     * @param searchEntry
     *          The search result entry to include in the response
     *          control.
     */
    public Response(boolean isCritical, SearchResultEntry searchEntry)
    {
      super(OID_LDAP_READENTRY_POSTREAD, isCritical);

      this.entry = searchEntry;
    }



    /**
     * Creates a new instance of this LDAP post-read response control
     * with the provided information.
     *
     * @param searchEntry
     *          The search result entry to include in the response
     *          control.
     */
    public Response(SearchResultEntry searchEntry)
    {
      this(false, searchEntry);
    }



    /**
     * Retrieves the search result entry associated with this post-read
     * response control.
     *
     * @return The search result entry associated with this post-read
     *         response control.
     */
    public SearchResultEntry getSearchEntry()
    {
      return entry;
    }



    @Override
    public ByteString getValue()
    {
      ByteStringBuilder buffer = new ByteStringBuilder();
      ASN1Writer writer = ASN1.getWriter(buffer);
      try
      {
        LDAPEncoder.encodeEntry(writer, entry);
        return buffer.toByteString();
      }
      catch (IOException ioe)
      {
        // This should never happen unless there is a bug somewhere.
        throw new RuntimeException(ioe);
      }
    }



    @Override
    public boolean hasValue()
    {
      return true;
    }



    /**
     * Appends a string representation of this LDAP post-read response
     * control to the provided buffer.
     *
     * @param buffer
     *          The buffer to which the information should be appended.
     */
    @Override
    public void toString(StringBuilder buffer)
    {
      buffer.append("PostReadResponseControl(oid=");
      buffer.append(getOID());
      buffer.append(", criticality=");
      buffer.append(isCritical());
      buffer.append(", entry=");
      buffer.append(entry);
      buffer.append(")");
    }
  }

  /**
   * ControlDecoder implentation to decode this control from a
   * ByteString.
   */
  private final static class RequestDecoder implements
      ControlDecoder<Request>
  {
    /**
     * {@inheritDoc}
     */
    public Request decode(boolean isCritical, ByteString value)
        throws DecodeException
    {
      if (value == null)
      {
        Message message = ERR_POSTREADREQ_NO_CONTROL_VALUE.get();
        throw new DecodeException(message);
      }

      ASN1Reader reader = ASN1.getReader(value);
      LinkedHashSet<String> attributes = new LinkedHashSet<String>();
      try
      {
        reader.readStartSequence();
        while (reader.hasNextElement())
        {
          attributes.add(reader.readOctetStringAsString());
        }
        reader.readEndSequence();
      }
      catch (Exception ae)
      {
        if (debugEnabled())
        {
          TRACER.debugCaught(DebugLogLevel.ERROR, ae);
        }

        Message message =
            ERR_POSTREADREQ_CANNOT_DECODE_VALUE.get(ae.getMessage());
        throw new DecodeException(message, ae);
      }

      return new Request(isCritical, attributes);
    }



    public String getOID()
    {
      return OID_LDAP_READENTRY_POSTREAD;
    }
  }

  /**
   * ControlDecoder implentation to decode this control from a
   * ByteString.
   */
  private final static class ResponseDecoder implements
      ControlDecoder<Response>
  {
    /**
     * {@inheritDoc}
     */
    public Response decode(boolean isCritical, ByteString value)
        throws DecodeException
    {
      if (value == null)
      {
        Message message = ERR_POSTREADRESP_NO_CONTROL_VALUE.get();
        throw new DecodeException(message);
      }

      ASN1Reader reader = ASN1.getReader(value);
      SearchResultEntry searchEntry;
      try
      {
        searchEntry = LDAPDecoder.decodeEntry(reader);
      }
      catch (IOException le)
      {
        if (debugEnabled())
        {
          TRACER.debugCaught(DebugLogLevel.ERROR, le);
        }

        Message message =
            ERR_POSTREADRESP_CANNOT_DECODE_VALUE.get(le.getMessage());
        throw new DecodeException(message, le);
      }

      return new Response(isCritical, searchEntry);
    }



    public String getOID()
    {
      return OID_LDAP_READENTRY_POSTREAD;
    }

  }



  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  /**
   * The Control Decoder that can be used to decode the request control.
   */
  public static final ControlDecoder<Request> REQUEST_DECODER =
      new RequestDecoder();

  /**
   * The Control Decoder that can be used to decode the response
   * control.
   */
  public static final ControlDecoder<Response> RESPONSE_DECODER =
      new ResponseDecoder();
}
