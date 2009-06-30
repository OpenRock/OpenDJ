package org.opends.common.api.controls;

import org.opends.common.api.filter.*;
import org.opends.common.api.DecodeException;
import org.opends.common.protocols.asn1.ASN1Writer;
import org.opends.common.protocols.asn1.ASN1;
import org.opends.common.protocols.asn1.ASN1Reader;
import org.opends.common.protocols.ldap.LDAPDecoder;
import org.opends.server.util.Validator;
import static org.opends.server.util.StaticUtils.getExceptionMessage;
import static org.opends.server.util.StaticUtils.byteToHex;
import static org.opends.server.util.ServerConstants.OID_MATCHED_VALUES;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;
import org.opends.server.types.LDAPException;
import org.opends.server.types.DebugLogLevel;
import static org.opends.server.protocols.ldap.LDAPConstants.*;
import org.opends.server.protocols.ldap.LDAPResultCode;
import org.opends.messages.Message;
import static org.opends.messages.ProtocolMessages.*;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * This class implements the matched values control as defined in RFC 3876.  It
 * may be included in a search request to indicate that only attribute values
 * matching one or more filters contained in the matched values control should
 * be returned to the client.
 */
public class MatchedValuesControl extends Control
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  private static final String EMPTY_STRING = "".intern();

    /**
   * ControlDecoder implentation to decode this control from a ByteString.
   */
  private final static class Decoder
      implements ControlDecoder<MatchedValuesControl>
  {
    /**
     * {@inheritDoc}
     */
    public MatchedValuesControl decode(boolean isCritical,
                                       ByteString value)
        throws DecodeException
    {
      ArrayList<Filter> filters;
      if (value == null)
      {
        Message message = ERR_MATCHEDVALUES_NO_CONTROL_VALUE.get();
        throw new DecodeException(message);
      }

      ASN1Reader reader = ASN1.getReader(value);
      try
      {
        reader.readStartSequence();
        if (!reader.hasNextElement())
        {
          Message message = ERR_MATCHEDVALUES_NO_FILTERS.get();
          throw new DecodeException(message);
        }

        MatchedValuesControl control =
            new MatchedValuesControl(isCritical);
        while(reader.hasNextElement())
        {
          decodeFilter(reader, control);
        }
        reader.readEndSequence();

        return control;
      }
      catch (IOException e)
      {
        if (debugEnabled())
        {
          TRACER.debugCaught(DebugLogLevel.ERROR, e);
        }

        Message message =
            ERR_MATCHEDVALUES_CANNOT_DECODE_VALUE_AS_SEQUENCE.get(
            getExceptionMessage(e));
        throw new DecodeException(message);
      }
    }


    public String getOID()
    {
      return OID_MATCHED_VALUES;
    }

  }

  /**
   * The Control Decoder that can be used to decode this control.
   */
  public static final ControlDecoder<MatchedValuesControl> DECODER =
    new Decoder();
  private List<Filter> filters;

    /**
   * Creates a new matched values control using the default OID and the provided
   * criticality and set of filters.
   *
   * @param  isCritical  Indicates whether this control should be considered
   *                     critical to the operation processing.
   */
  public MatchedValuesControl(boolean isCritical)
  {
    super(OID_MATCHED_VALUES, isCritical);

    this.filters = new ArrayList<Filter>();
  }

  /**
   * Retrieves the set of filters associated with this matched values control.
   *
   * @return  The set of filters associated with this matched values control.
   */
  public Iterable<Filter> getFilters()
  {
    return filters;
  }

  public MatchedValuesControl addFilter(AssertionFilter filter)
  {
    Validator.ensureNotNull(filter);
    filters.add(filter);
    return this;
  }

  public MatchedValuesControl addFilter(SubstringFilter filter)
  {
    Validator.ensureNotNull(filter);
    filters.add(filter);
    return this;
  }

  public MatchedValuesControl addFilter(PresenceFilter filter)
  {
    Validator.ensureNotNull(filter);
    filters.add(filter);
    return this;
  }

  public MatchedValuesControl addFilter(SimpleMatchingFilter filter)
  {
    Validator.ensureNotNull(filter);
    filters.add(filter);
    return this;
  }

  public ByteString getValue() {
    ByteStringBuilder buffer = new ByteStringBuilder();
    ASN1Writer writer = ASN1.getWriter(buffer);
    try
    {
      writer.writeStartSequence();
      for (Filter f : filters)
      {
        f.encodeLDAP(writer);
      }
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
    buffer.append("MatchingValuesControl(oid=");
    buffer.append(getOID());
    buffer.append(", criticality=");
    buffer.append(isCritical());
    buffer.append(")");
  }

  public static Filter decodeFilter(ASN1Reader reader,
                                    MatchedValuesControl control)
      throws IOException, DecodeException
  {
    byte type = reader.peekType();

    switch (type)
    {
      case TYPE_FILTER_EQUALITY:
        control.addFilter(LDAPDecoder.decodeEqualFilter(reader));

      case TYPE_FILTER_GREATER_OR_EQUAL:
        control.addFilter(
            LDAPDecoder.decodeGreaterOrEqualFilter(reader));

      case TYPE_FILTER_LESS_OR_EQUAL:
        control.addFilter(
            LDAPDecoder.decodeLessOrEqualFilter(reader));

      case TYPE_FILTER_APPROXIMATE:
        control.addFilter(
            LDAPDecoder.decodeApproximateFilter(reader));

      case TYPE_FILTER_SUBSTRING:
        control.addFilter(LDAPDecoder.decodeSubstringFilter(reader));

      case TYPE_FILTER_PRESENCE:
        control.addFilter(
            new PresenceFilter(reader.readOctetStringAsString(type)));

      case TYPE_FILTER_EXTENSIBLE_MATCH:
        control.addFilter(decodeSimpleMatchingFilter(reader));

      default:
        Message message =
            ERR_MVFILTER_INVALID_ELEMENT_TYPE.get(byteToHex(type));
        throw new DecodeException(message);
    }
  }

  private static SimpleMatchingFilter decodeSimpleMatchingFilter(
      ASN1Reader reader)
      throws IOException, DecodeException
  {
    String extensibleType = EMPTY_STRING;
    String matchingRuleID = EMPTY_STRING;

    reader.readStartSequence(TYPE_FILTER_EXTENSIBLE_MATCH);
    if(reader.peekType() == TYPE_MATCHING_RULE_ID)
    {
      matchingRuleID =
          reader.readOctetStringAsString(TYPE_MATCHING_RULE_ID);
    }
    if(reader.peekType() == TYPE_MATCHING_RULE_TYPE)
    {
      extensibleType =
          reader.readOctetStringAsString(TYPE_MATCHING_RULE_TYPE);
    }

    if(extensibleType.equals(EMPTY_STRING) &&
        matchingRuleID.equals(EMPTY_STRING))
    {
      throw new DecodeException(
          ERR_MVFILTER_INVALID_EXTENSIBLE_SEQUENCE_SIZE.get(1));
    }
    SimpleMatchingFilter extensibleFilter =
        new SimpleMatchingFilter(matchingRuleID, extensibleType,
            reader.readOctetString(TYPE_MATCHING_RULE_VALUE));

    reader.readEndSequence();
    return extensibleFilter;
  }
}
