package org.opends.schema.syntaxes;

import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_ILLEGAL_TOKEN;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_MR_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_MR_EXPECTED_OPEN_PARENTHESIS;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_MR_NO_SYNTAX;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import static org.opends.server.schema.SchemaConstants.SYNTAX_MATCHING_RULE_NAME;

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
 * This class implements the matching rule description syntax, which is used to
 * hold matching rule definitions in the server schema.  The format of this
 * syntax is defined in RFC 2252.
 */
public class MatchingRuleSyntax extends AbstractSyntaxImplementation
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  public String getName() {
    return SYNTAX_MATCHING_RULE_NAME;
  }

  public boolean isHumanReadable() {
    return true;
  }

  /**
   * Indicates whether the provided value is acceptable for use in an attribute
   * with this syntax.  If it is not, then the reason may be appended to the
   * provided buffer.
   *
   * @param schema
   *@param  value          The value for which to make the determination.
   * @param  invalidReason  The buffer to which the invalid reason should be
 *                        appended.
 * @return  <CODE>true</CODE> if the provided value is acceptable for use with
   *          this syntax, or <CODE>false</CODE> if not.
   */
  public boolean valueIsAcceptable(Schema schema, ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    // We'll use the decodeMatchingRule method to determine if the value is
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
        Message message = ERR_ATTR_SYNTAX_MR_EMPTY_VALUE.get();
        throw new DecodeException(message);
      }


      // The next character must be an open parenthesis.  If it is not, then
      // that is an error.
      char c = reader.read();
      if (c != '(')
      {
        Message message = ERR_ATTR_SYNTAX_MR_EXPECTED_OPEN_PARENTHESIS.
            get(definition, (reader.pos()-1), String.valueOf(c));
        throw new DecodeException(message);
      }


      // Skip over any spaces immediately following the opening parenthesis.
      reader.skipWhitespaces();

      // The next set of characters must be the OID.
      SchemaUtils.readNumericOID(reader);
      String syntax = null;

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
          // This specifies the description for the matching rule.  It is an
          // arbitrary string of characters enclosed in single quotes.
          SchemaUtils.readQuotedString(reader);
        }
        else if (tokenName.equalsIgnoreCase("obsolete"))
        {
          // This indicates whether the matching rule should be considered
          // obsolete.  We do not need to do any more parsing for this token.
        }
        else if (tokenName.equalsIgnoreCase("syntax"))
        {
          syntax = SchemaUtils.readNumericOID(reader);
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

      // Make sure that a syntax was specified.
      if (syntax == null)
      {
        Message message = ERR_ATTR_SYNTAX_MR_NO_SYNTAX.get(definition);
        throw new DecodeException(message);
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
