package org.opends.sdk.schema.syntaxes;

import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_ILLEGAL_TOKEN;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_OBJECTCLASS_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_OBJECTCLASS_EXPECTED_OPEN_PARENTHESIS;
import static org.opends.sdk.schema.SchemaConstants.EMR_OID_FIRST_COMPONENT_OID;
import static org.opends.sdk.schema.SchemaConstants.SYNTAX_OBJECTCLASS_NAME;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;

import org.opends.messages.Message;
import org.opends.messages.MessageBuilder;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.SchemaUtils;
import org.opends.sdk.util.SubstringReader;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.DebugLogLevel;

/**
 * This class implements the object class description syntax, which is used to
 * hold objectclass definitions in the server schema.  The format of this
 * syntax is defined in RFC 2252.
 */
public class ObjectClassSyntax extends AbstractSyntaxImplementation
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  public String getName() {
    return SYNTAX_OBJECTCLASS_NAME;
  }

  public boolean isHumanReadable() {
    return true;
  }

  public boolean valueIsAcceptable(Schema schema, ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    // We'll use the decodeObjectClass method to determine if the value is
    // acceptable.
    try
    {
      String definition = value.toString();
      SubstringReader reader = new SubstringReader(definition);

      // We'll do this a character at a time.  First, skip over any leading
      // whitespace.
      reader.skipWhitespaces();

      if (reader.remaining() <= 0)
      {
        // This means that the value was empty or contained only whitespace.
        // That is illegal.
        Message message = ERR_ATTR_SYNTAX_OBJECTCLASS_EMPTY_VALUE.get();
        throw new DecodeException(message);
      }


      // The next character must be an open parenthesis.  If it is not, then
      // that is an error.
      char c = reader.read();
      if (c != '(')
      {
        Message message = ERR_ATTR_SYNTAX_OBJECTCLASS_EXPECTED_OPEN_PARENTHESIS.
            get(definition, (reader.pos()-1), String.valueOf(c));
        throw new DecodeException(message);
      }


      // Skip over any spaces immediately following the opening parenthesis.
      reader.skipWhitespaces();

      // The next set of characters must be the OID.
      SchemaUtils.readOID(reader);

      // At this point, we should have a pretty specific syntax that describes
      // what may come next, but some of the components are optional and it
      // would be pretty easy to put something in the wrong order, so we will
      // be very flexible about what we can accept.  Just look at the next
      // token, figure out what it is and how to treat what comes after it,
      // then repeat until we get to the end of the value.  But before we
      // start, set default values for everything else we might need to know.
      while (true)
      {
        String tokenName = SchemaUtils.readTokenName(reader);

        if (tokenName == null)
        {
          // No more tokens.
          break;
        }
        else if (tokenName.equalsIgnoreCase("name"))
        {
          SchemaUtils.readNameDescriptors(reader);
        }
        else if (tokenName.equalsIgnoreCase("desc"))
        {
          // This specifies the description for the attribute type.  It is an
          // arbitrary string of characters enclosed in single quotes.
          SchemaUtils.readQuotedString(reader);
        }
        else if (tokenName.equalsIgnoreCase("obsolete"))
        {
          // This indicates whether the attribute type should be considered
          // obsolete.  We do not need to do any more parsing for this token.
        }
        else if (tokenName.equalsIgnoreCase("sup"))
        {
          SchemaUtils.readOIDs(reader);
        }
        else if (tokenName.equalsIgnoreCase("abstract"))
        {
          // This indicates that entries must not include this objectclass
          // unless they also include a non-abstract objectclass that
          // inherits from this class.  We do not need any more parsing
          // for this token.
        }
        else if (tokenName.equalsIgnoreCase("structural"))
        {
          // This indicates that this is a structural objectclass.
          // We do not need any more parsing for this token.
        }
        else if (tokenName.equalsIgnoreCase("auxiliary"))
        {
          // This indicates that this is an auxiliary objectclass.
          // We do not need any more parsing for this token.
        }
        else if (tokenName.equalsIgnoreCase("must"))
        {
          SchemaUtils.readOIDs(reader);
        }
        else if (tokenName.equalsIgnoreCase("may"))
        {
          SchemaUtils.readOIDs(reader);
        }
        else if(tokenName.matches("^X-[A-Za-z_-]+$"))
        {
          // This must be a non-standard property and it must be followed by
          // either a single definition in single quotes or an open parenthesis
          // followed by one or more values in single quotes separated by spaces
          // followed by a close parenthesis.
          SchemaUtils.readExtensions(reader);
        }
        else
        {
          Message message = ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
          throw new DecodeException(message);
        }
      }
      return true;
    }
    catch (DecodeException de)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, de);
      }

      invalidReason.append(de.getMessageObject());
      return false;
    }
  }

  @Override
  public String getEqualityMatchingRule() {
    return EMR_OID_FIRST_COMPONENT_OID;
  }
}
