package org.opends.ldap.extensions;



import static org.opends.messages.ProtocolMessages.*;
import static org.opends.server.loggers.debug.DebugLogger.*;
import static org.opends.server.protocols.ldap.LDAPConstants.*;
import static org.opends.server.util.ServerConstants.*;
import static org.opends.server.util.StaticUtils.*;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.opends.asn1.ASN1;
import org.opends.asn1.ASN1Reader;
import org.opends.asn1.ASN1Writer;
import org.opends.ldap.Control;
import org.opends.ldap.ControlDecoder;
import org.opends.ldap.DecodeException;
import org.opends.messages.Message;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;
import org.opends.server.types.DebugLogLevel;
import org.opends.server.util.Validator;
import org.opends.types.filter.AbstractFilterVisitor;
import org.opends.types.filter.Filter;
import org.opends.types.filter.FilterVisitor;
import org.opends.types.filter.IllegalFilterException;



/**
 * This class implements the matched values control as defined in RFC
 * 3876. It may be included in a search request to indicate that only
 * attribute values matching one or more filters contained in the
 * matched values control should be returned to the client.
 */
public class MatchedValuesControl extends Control
{
  /**
   * ControlDecoder implentation to decode this control from a
   * ByteString.
   */
  private final static class Decoder implements
      ControlDecoder<MatchedValuesControl>
  {
    /**
     * {@inheritDoc}
     */
    public MatchedValuesControl decode(boolean isCritical,
        ByteString value) throws DecodeException
    {
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

        LinkedList<Filter> filters = new LinkedList<Filter>();
        do
        {
          filters.add(decodeFilter(reader));
        }
        while (reader.hasNextElement());

        reader.readEndSequence();

        return new MatchedValuesControl(isCritical, Collections
            .unmodifiableList(filters));
      }
      catch (IOException e)
      {
        if (debugEnabled())
        {
          TRACER.debugCaught(DebugLogLevel.ERROR, e);
        }

        Message message =
            ERR_MATCHEDVALUES_CANNOT_DECODE_VALUE_AS_SEQUENCE
                .get(getExceptionMessage(e));
        throw new DecodeException(message);
      }
    }



    public String getOID()
    {
      return OID_MATCHED_VALUES;
    }

  }



  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  /**
   * The Control Decoder that can be used to decode this control.
   */
  public static final ControlDecoder<MatchedValuesControl> DECODER =
      new Decoder();



  private static Filter decodeFilter(ASN1Reader reader)
      throws IOException, DecodeException
  {
    byte type = reader.peekType();

    switch (type)
    {
    case TYPE_FILTER_EQUALITY:
    case TYPE_FILTER_GREATER_OR_EQUAL:
    case TYPE_FILTER_LESS_OR_EQUAL:
    case TYPE_FILTER_APPROXIMATE:
    case TYPE_FILTER_SUBSTRING:
    case TYPE_FILTER_PRESENCE:
      return Filter.decode(reader);
    case TYPE_FILTER_EXTENSIBLE_MATCH:
      return decodeSimpleMatchingFilter(reader);
    default:
      Message message =
          ERR_MVFILTER_INVALID_ELEMENT_TYPE.get(byteToHex(type));
      throw new DecodeException(message);
    }
  }



  private static Filter decodeSimpleMatchingFilter(ASN1Reader reader)
      throws IOException, DecodeException
  {
    reader.readStartSequence(TYPE_FILTER_EXTENSIBLE_MATCH);

    String matchingRule = null;
    if (reader.peekType() == TYPE_MATCHING_RULE_ID)
    {
      matchingRule =
          reader.readOctetStringAsString(TYPE_MATCHING_RULE_ID);
    }

    String attributeDescription = null;
    if (reader.peekType() == TYPE_MATCHING_RULE_TYPE)
    {
      attributeDescription =
          reader.readOctetStringAsString(TYPE_MATCHING_RULE_TYPE);
    }

    if ((matchingRule == null) && (attributeDescription == null))
    {
      throw new DecodeException(
          ERR_MVFILTER_INVALID_EXTENSIBLE_SEQUENCE_SIZE.get(1));
    }

    ByteString assertionValue =
        reader.readOctetString(TYPE_MATCHING_RULE_VALUE);

    reader.readEndSequence();

    return Filter.newExtensibleMatchFilter(matchingRule,
        attributeDescription, assertionValue, false);
  }



  private static void validateFilter(final Filter filter)
      throws IllegalFilterException
  {
    FilterVisitor<IllegalFilterException, Void> visitor =
        new AbstractFilterVisitor<IllegalFilterException, Void>()
        {

          @Override
          public IllegalFilterException visitAndFilter(Void p,
              List<Filter> subFilters)
          {
            Message message =
                ERR_MVFILTER_BAD_FILTER_AND.get(filter.toString());
            return new IllegalFilterException(message);
          }



          @Override
          public IllegalFilterException visitExtensibleMatchFilter(
              Void p, String matchingRule, String attributeDescription,
              ByteString assertionValue, boolean dnAttributes)
          {
            if (dnAttributes)
            {
              Message message =
                  ERR_MVFILTER_BAD_FILTER_EXT.get(filter.toString());
              return new IllegalFilterException(message);
            }
            else
            {
              return null;
            }
          }



          @Override
          public IllegalFilterException visitNotFilter(Void p,
              Filter subFilter)
          {
            Message message =
                ERR_MVFILTER_BAD_FILTER_NOT.get(filter.toString());
            return new IllegalFilterException(message);
          }



          @Override
          public IllegalFilterException visitOrFilter(Void p,
              List<Filter> subFilters)
          {
            Message message =
                ERR_MVFILTER_BAD_FILTER_OR.get(filter.toString());
            return new IllegalFilterException(message);
          }



          @Override
          public IllegalFilterException visitUnrecognizedFilter(Void p,
              byte filterTag, ByteString filterBytes)
          {
            Message message =
                ERR_MVFILTER_BAD_FILTER_UNRECOGNIZED.get(filter
                    .toString(), filterTag);
            return new IllegalFilterException(message);
          }

        };

    IllegalFilterException e = filter.accept(visitor, null);
    if (e != null)
    {
      throw e;
    }
  }



  private List<Filter> filters;



  /**
   * Creates a new matched values control using the default OID and the
   * provided criticality and set of filters.
   * 
   * @param isCritical
   *          Indicates whether this control should be considered
   *          critical to the operation processing.
   * @param filters
   *          The set of matched value filters.
   * @throws IllegalFilterException
   *           If one of the filters is not permitted by the matched
   *           values control.
   */
  public MatchedValuesControl(boolean isCritical, Filter... filters)
      throws IllegalFilterException
  {
    super(OID_MATCHED_VALUES, isCritical);

    Validator.ensureNotNull(filters);
    Validator.ensureTrue(filters.length > 0);

    if (filters.length == 1)
    {
      validateFilter(filters[0]);
      this.filters = Collections.singletonList(filters[0]);
    }
    else
    {
      LinkedList<Filter> list = new LinkedList<Filter>();
      for (Filter filter : filters)
      {
        validateFilter(filter);
        list.add(filter);
      }
      this.filters = Collections.unmodifiableList(list);
    }
  }



  private MatchedValuesControl(boolean isCritical, List<Filter> filters)
  {
    super(OID_MATCHED_VALUES, isCritical);
    this.filters = filters;
  }



  /**
   * Retrieves the set of filters associated with this matched values
   * control.
   * 
   * @return The set of filters associated with this matched values
   *         control.
   */
  public Iterable<Filter> getFilters()
  {
    return filters;
  }



  @Override
  public ByteString getValue()
  {
    ByteStringBuilder buffer = new ByteStringBuilder();
    ASN1Writer writer = ASN1.getWriter(buffer);
    try
    {
      writer.writeStartSequence();
      for (Filter f : filters)
      {
        f.encode(writer);
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



  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("MatchingValuesControl(oid=");
    buffer.append(getOID());
    buffer.append(", criticality=");
    buffer.append(isCritical());
    buffer.append(")");
  }
}
