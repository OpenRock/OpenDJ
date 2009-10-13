package org.opends.sdk.schema.syntaxes;

import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_ILLEGAL_TOKEN;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_MRUSE_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_MRUSE_EXPECTED_OPEN_PARENTHESIS;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_MRUSE_NO_ATTR;
import static org.opends.sdk.schema.SchemaConstants.SYNTAX_MATCHING_RULE_USE_NAME;
import static org.opends.sdk.schema.SchemaConstants.EMR_OID_FIRST_COMPONENT_OID;

import java.util.Set;

import org.opends.messages.Message;
import org.opends.messages.MessageBuilder;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.SchemaUtils;
import org.opends.sdk.util.SubstringReader;
import org.opends.sdk.util.StaticUtils;
import org.opends.server.types.ByteSequence;

/**
 * This class implements the matching rule use description syntax, which is used
 * to hold matching rule use definitions in the server schema.  The format of
 * this syntax is defined in RFC 2252.
 */
public class MatchingRuleUseSyntax extends AbstractSyntaxImplementation
{

  public String getName() {
    return SYNTAX_MATCHING_RULE_USE_NAME;
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
    // We'll use the decodeAttributeType method to determine if the value is
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
        Message message = ERR_ATTR_SYNTAX_MRUSE_EMPTY_VALUE.get();
        DecodeException e = new DecodeException(message);
        StaticUtils.DEBUG_LOG.throwing(
            "MatchingRuleUseSyntax",  "valueIsAcceptable", e);
        throw e;
      }


      // The next character must be an open parenthesis.  If it is not, then
      // that is an error.
      char c = reader.read();
      if (c != '(')
      {
        Message message = ERR_ATTR_SYNTAX_MRUSE_EXPECTED_OPEN_PARENTHESIS.
            get(definition, (reader.pos()-1), String.valueOf(c));
        DecodeException e = new DecodeException(message);
        StaticUtils.DEBUG_LOG.throwing(
            "MatchingRuleUseSyntax",  "valueIsAcceptable", e);
        throw e;
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
      Set<String> attributes = null;
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
        else if (tokenName.equalsIgnoreCase("applies"))
        {
          attributes = SchemaUtils.readOIDs(reader);
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
          DecodeException e = new DecodeException(message);
          StaticUtils.DEBUG_LOG.throwing(
              "MatchingRuleUseSyntax",  "valueIsAcceptable", e);
          throw e;
        }
      }

      // Make sure that the set of attributes was defined.
      if (attributes == null || attributes.size() == 0)
      {
        Message message = ERR_ATTR_SYNTAX_MRUSE_NO_ATTR.get(definition);
        DecodeException e = new DecodeException(message);
        StaticUtils.DEBUG_LOG.throwing(
            "MatchingRuleUseSyntax",  "valueIsAcceptable", e);
        throw e;
      }
      return true;
    }
    catch (DecodeException de)
    {
      invalidReason.append(de.getMessageObject());
      return false;
    }
  }

  @Override
  public String getEqualityMatchingRule() {
    return EMR_OID_FIRST_COMPONENT_OID;
  }
}
