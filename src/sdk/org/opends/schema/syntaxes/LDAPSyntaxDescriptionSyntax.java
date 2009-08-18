package org.opends.schema.syntaxes;

import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_ATTRSYNTAX_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_ATTRSYNTAX_EXPECTED_OPEN_PARENTHESIS;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import static org.opends.server.schema.SchemaConstants.SYNTAX_LDAP_SYNTAX_NAME;

import org.opends.ldap.DecodeException;
import org.opends.messages.Message;
import org.opends.messages.MessageBuilder;
import org.opends.schema.Schema;
import org.opends.schema.SchemaUtils;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.DebugLogLevel;
import org.opends.util.SubstringReader;

/**
 * This class defines the LDAP syntax description syntax, which is used to
 * hold attribute syntax definitions in the schema.  The format of this
 * syntax is defined in RFC 2252.
 */
public class LDAPSyntaxDescriptionSyntax extends AbstractSyntaxImplementation
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  public String getName() {
    return SYNTAX_LDAP_SYNTAX_NAME;
  }

  public boolean isHumanReadable() {
    return true;
  }

  public boolean valueIsAcceptable(Schema schema, ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    // We'll use the decodeNameForm method to determine if the value is
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
        Message message = ERR_ATTR_SYNTAX_ATTRSYNTAX_EMPTY_VALUE.get();
        throw new DecodeException(message);
      }


      // The next character must be an open parenthesis.  If it is not, then
      // that is an error.
      char c = reader.read();
      if (c != '(')
      {
        Message message = ERR_ATTR_SYNTAX_ATTRSYNTAX_EXPECTED_OPEN_PARENTHESIS.
            get(definition, (reader.pos()-1), String.valueOf(c));
        throw new DecodeException(message);
      }


      // Skip over any spaces immediately following the opening parenthesis.
      reader.skipWhitespaces();

      // The next set of characters must be the OID.
      SchemaUtils.readNumericOID(reader);

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
        else if (tokenName.equalsIgnoreCase("desc"))
        {
          // This specifies the description for the syntax.  It is an
          // arbitrary string of characters enclosed in single quotes.
          SchemaUtils.readQuotedString(reader);
        }
        else
        {
          // This must be a non-standard property and it must be followed by
          // either a single value in single quotes or an open parenthesis
          // followed by one or more values in single quotes separated by spaces
          // followed by a close parenthesis.
          SchemaUtils.readExtraParameterValues(reader);
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
}
