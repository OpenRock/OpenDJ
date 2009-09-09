package org.opends.sdk.schema.syntaxes;

import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_ATTRTYPE_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_ATTRTYPE_EXPECTED_OPEN_PARENTHESIS;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_ILLEGAL_TOKEN;
import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_ATTRTYPE_INVALID_ATTRIBUTE_USAGE;
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
import static org.opends.server.schema.SchemaConstants.*;

/**
 * This class defines the attribute type description syntax, which is used to
 * hold attribute type definitions in the server schema.  The format of this
 * syntax is defined in RFC 2252.
 */
public class AttributeTypeSyntax extends AbstractSyntaxImplementation
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  public String getName() {
    return SYNTAX_ATTRIBUTE_TYPE_NAME;
  }

  public boolean isHumanReadable() {
    return true;
  }

  public boolean valueIsAcceptable(Schema schema, ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    try
    {
      String definition = value.toString();
      SubstringReader reader = new SubstringReader(definition);

      // We'll do this a character at a time.  First, skip over any leading
      // whitespace.
      reader.skipWhitespaces();

      if (reader.remaining() <= 0)
      {
        // This means that the definition was empty or contained only
        // whitespace.  That is illegal.
        Message message = ERR_ATTR_SYNTAX_ATTRTYPE_EMPTY_VALUE.get();
        throw new DecodeException(message);
      }


      // The next character must be an open parenthesis.  If it is not,
      // then that is an error.
      char c = reader.read();
      if (c != '(')
      {
        Message message =
            ERR_ATTR_SYNTAX_ATTRTYPE_EXPECTED_OPEN_PARENTHESIS.get(definition,
                (reader.pos()-1), String.valueOf(c));
        throw new DecodeException( message);
      }


      // Skip over any spaces immediately following the opening parenthesis.
      reader.skipWhitespaces();

      // The next set of characters must be the OID.
      String oid = SchemaUtils.readNumericOID(reader);

      // At this point, we should have a pretty specific syntax that describes
      // what may come next, but some of the components are optional and it
      // would be pretty easy to put something in the wrong order, so we will
      // be very flexible about what we can accept.  Just look at the next
      // token, figure out what it is and how to treat what comes after it,
      // then repeat until we get to the end of the definition.  But before we
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
          // This specifies the name or OID of the superior attribute type from
          // which this attribute type should inherit its properties.
          SchemaUtils.readOID(reader);
        }
        else if (tokenName.equalsIgnoreCase("equality"))
        {
          // This specifies the name or OID of the equality matching rule to use
          // for this attribute type.
          SchemaUtils.readOID(reader);
        }
        else if (tokenName.equalsIgnoreCase("ordering"))
        {
          // This specifies the name or OID of the ordering matching rule to use
          // for this attribute type.
          SchemaUtils.readOID(reader);
        }
        else if (tokenName.equalsIgnoreCase("substr"))
        {
          // This specifies the name or OID of the substring matching rule to
          // use for this attribute type.
          SchemaUtils.readOID(reader);
        }
        else if (tokenName.equalsIgnoreCase("syntax"))
        {
          // This specifies the numeric OID of the syntax for this matching
          // rule. It may optionally be immediately followed by an open curly
          // brace, an integer definition, and a close curly brace to suggest
          // the minimum number of characters that should be allowed in values
          // of that type. This implementation will ignore any such length
          // because it does not impose any practical limit on the length of
          // attribute values.
          SchemaUtils.readNumericOIDLen(reader);
        }
        else if (tokenName.equalsIgnoreCase("single-definition"))
        {
          // This indicates that attributes of this type are allowed to have at
          // most one definition.  We do not need any more parsing for this
          // token.
        }
        else if (tokenName.equalsIgnoreCase("single-value"))
        {
          // This indicates that attributes of this type are allowed to have at
          // most one value.  We do not need any more parsing for this token.
        }
        else if (tokenName.equalsIgnoreCase("collective"))
        {
          // This indicates that attributes of this type are collective (i.e.,
          // have their values generated dynamically in some way).  We do not
          // need any more parsing for this token.
        }
        else if (tokenName.equalsIgnoreCase("no-user-modification"))
        {
          // This indicates that the values of attributes of this type are not
          // to be modified by end users.  We do not need any more parsing for
          // this token.
        }
        else if (tokenName.equalsIgnoreCase("usage"))
        {
          // This specifies the usage string for this attribute type.  It should
          // be followed by one of the strings "userApplications",
          // "directoryOperation", "distributedOperation", or "dSAOperation".
          int length = 0;

          reader.skipWhitespaces();
          reader.mark();

          while(reader.read() != ' ')
          {
            length++;
          }

          reader.reset();
          String usageStr = reader.read(length);
          if (!usageStr.equalsIgnoreCase("userapplications") &&
              !usageStr.equalsIgnoreCase("directoryoperation") &&
              !usageStr.equalsIgnoreCase("distributedoperation") &&
              !usageStr.equalsIgnoreCase("dsaoperation"))
          {
            Message message = WARN_ATTR_SYNTAX_ATTRTYPE_INVALID_ATTRIBUTE_USAGE.
                get(String.valueOf(oid), usageStr);
            throw new DecodeException(message);
          }
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
