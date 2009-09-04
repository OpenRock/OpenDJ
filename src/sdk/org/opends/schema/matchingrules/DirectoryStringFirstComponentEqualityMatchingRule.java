package org.opends.schema.matchingrules;

import static org.opends.schema.StringPrepProfile.CASE_FOLD;
import static org.opends.schema.StringPrepProfile.TRIM;
import static org.opends.schema.StringPrepProfile.prepareUnicode;

import org.opends.schema.Schema;
import org.opends.schema.SchemaUtils;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.util.ServerConstants;
import org.opends.types.Assertion;
import org.opends.util.SubstringReader;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_EXPECTED_OPEN_PARENTHESIS;
import org.opends.ldap.DecodeException;

/**
 * This class implements the directoryStringFirstComponentMatch matching rule
 * defined in X.520 and referenced in RFC 2252.  This rule is intended for use
 * with attributes whose values contain a set of parentheses enclosing a
 * space-delimited set of names and/or name-value pairs (like attribute type or
 * objectclass descriptions) in which the "first component" is the first item
 * after the opening parenthesis.
 */
public class DirectoryStringFirstComponentEqualityMatchingRule
    extends AbstractMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value)
      throws DecodeException
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
      Message message = ERR_ATTR_SYNTAX_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }


    // The next character must be an open parenthesis.  If it is not, then
    // that is an error.
    char c = reader.read();
    if (c != '(')
    {
      Message message = ERR_ATTR_SYNTAX_EXPECTED_OPEN_PARENTHESIS.
          get(definition, (reader.pos()-1), String.valueOf(c));
      throw new DecodeException(message);
    }


    // Skip over any spaces immediately following the opening parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    String string = SchemaUtils.readQuotedString(reader);


    // Grab the substring between the start pos and the current pos
    return ByteString.valueOf(string);
  }

  @Override
  public Assertion getAssertion(Schema schema, ByteSequence value) {
    StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, TRIM, CASE_FOLD);

    int bufferLength = buffer.length();
    if (bufferLength == 0)
    {
      if (value.length() > 0)
      {
        // This should only happen if the value is composed entirely of spaces.
        // In that case, the normalized value is a single space.
        return new ByteOrderAssertion(ServerConstants.SINGLE_SPACE_VALUE);
      }
      else
      {
        // The value is empty, so it is already normalized.
        return new ByteOrderAssertion(ByteString.empty());
      }
    }


    // Replace any consecutive spaces with a single space.
    for (int pos = bufferLength-1; pos > 0; pos--)
    {
      if (buffer.charAt(pos) == ' ')
      {
        if (buffer.charAt(pos-1) == ' ')
        {
          buffer.delete(pos, pos+1);
        }
      }
    }

    return new ByteOrderAssertion(ByteString.valueOf(buffer.toString()));
  }
}
