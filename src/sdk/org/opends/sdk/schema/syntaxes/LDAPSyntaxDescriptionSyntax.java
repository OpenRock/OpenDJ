package org.opends.sdk.schema.syntaxes;

import static org.opends.messages.SchemaMessages.*;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import static org.opends.server.schema.SchemaConstants.SYNTAX_LDAP_SYNTAX_NAME;
import static org.opends.server.schema.SchemaConstants.EMR_OID_FIRST_COMPONENT_OID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
      String oid = SchemaUtils.readNumericOID(reader);

      Map<String, List<String>> extraProperties = Collections.emptyMap();
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
        else if(tokenName.matches("^X-[A-Za-z_-]+$"))
        {
          // This must be a non-standard property and it must be followed by
          // either a single definition in single quotes or an open parenthesis
          // followed by one or more values in single quotes separated by spaces
          // followed by a close parenthesis.
          if(extraProperties.isEmpty())
          {
            extraProperties = new HashMap<String, List<String>>();
          }
          extraProperties.put(tokenName,
              SchemaUtils.readExtensions(reader));
        }
        else
        {
          Message message = ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
          throw new DecodeException(message);
        }
      }

      for(Map.Entry<String, List<String>> property : extraProperties.entrySet())
      {
        if(property.getKey().equalsIgnoreCase("x-pattern"))
        {
          Iterator<String> values = property.getValue().iterator();
          if(values.hasNext())
          {
            String pattern = values.next();
            try
            {
              Pattern.compile(values.next());
            }
            catch(Exception e)
            {
              Message message =
                  WARN_ATTR_SYNTAX_LDAPSYNTAX_REGEX_INVALID_PATTERN.get
                      (oid, pattern);
              throw new DecodeException(message);
            }
            break;
          }
        }
        else if(property.getKey().equalsIgnoreCase("x-enum"))
        {
          List<String> values = property.getValue();
          for(int i = 0; i < values.size() - 1; i++)
          {
            String entry = values.get(i);
            for(int j = i + 1; j < values.size(); j++)
            {
              if(entry.equals(values.get(j)))
              {
                Message message =
                    WARN_ATTR_SYNTAX_LDAPSYNTAX_ENUM_DUPLICATE_VALUE.get(
                        oid, entry, j);
                throw new DecodeException(message);
              }
            }
          }
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
