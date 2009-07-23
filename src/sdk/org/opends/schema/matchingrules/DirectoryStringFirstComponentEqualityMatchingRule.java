package org.opends.schema.matchingrules;

import static org.opends.server.schema.SchemaConstants.EMR_DIRECTORY_STRING_FIRST_COMPONENT_NAME;
import static org.opends.server.schema.SchemaConstants.EMR_DIRECTORY_STRING_FIRST_COMPONENT_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_DIRECTORY_STRING_OID;
import static org.opends.schema.StringPrepProfile.prepareUnicode;
import static org.opends.schema.StringPrepProfile.TRIM;
import static org.opends.schema.StringPrepProfile.CASE_FOLD;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.util.ServerConstants;
import org.opends.schema.SchemaUtils;
import org.opends.schema.Schema;

import java.util.Collections;

/**
 * This class implements the directoryStringFirstComponentMatch matching rule
 * defined in X.520 and referenced in RFC 2252.  This rule is intended for use
 * with attributes whose values contain a set of parentheses enclosing a
 * space-delimited set of names and/or name-value pairs (like attribute type or
 * objectclass descriptions) in which the "first component" is the first item
 * after the opening parenthesis.
 */
public class DirectoryStringFirstComponentEqualityMatchingRule
    extends EqualityMatchingRuleImplementation
{
  public DirectoryStringFirstComponentEqualityMatchingRule()
  {
    super(EMR_DIRECTORY_STRING_FIRST_COMPONENT_OID,
        Collections.singletonList(EMR_DIRECTORY_STRING_FIRST_COMPONENT_NAME),
        "",
        false,
        SYNTAX_DIRECTORY_STRING_OID,
        SchemaUtils.RFC4512_ORIGIN);
  }

  public ByteSequence normalizeAttributeValue(Schema schema, ByteSequence value) {
    StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, TRIM, CASE_FOLD);

    int bufferLength = buffer.length();
    if (bufferLength == 0)
    {
      if (value.length() > 0)
      {
        // This should only happen if the value is composed entirely of spaces.
        // In that case, the normalized value is a single space.
        return ServerConstants.SINGLE_SPACE_VALUE;
      }
      else
      {
        // The value is empty, so it is already normalized.
        return ByteString.empty();
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

    // The attribute value must start with an open parenthesis,
    // followed by one or more spaces.
    String value1String = buffer.toString();
    int    value1Length = value1String.length();

    if ((value1Length == 0) || (value1String.charAt(0) != '('))
    {
      // They cannot be equal if the attribute value is empty or doesn't start
      // with an open parenthesis.
      return value;
    }

    int  pos = 1;
    while ((pos < value1Length) && ((value1String.charAt(pos)) == ' '))
    {
      pos++;
    }

    if (pos >= value1Length)
    {
      // We hit the end of the value before finding a non-space character.
      return value;
    }


    // The current position must be the start position for the value.  Keep
    // reading until we find the next space.
    int startPos = pos++;
    while ((pos < value1Length) && ((value1String.charAt(pos)) != ' '))
    {
      pos++;
    }

    if (pos >= value1Length)
    {
      // We hit the end of the value before finding the next space.
      return value;
    }


    // Grab the substring between the start pos and the current pos
    return ByteString.valueOf(value1String.substring(startPos, pos));
  }

  @Override
  public ByteSequence normalizeAssertionValue(Schema schema, ByteSequence value) {
    StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, TRIM, CASE_FOLD);

    int bufferLength = buffer.length();
    if (bufferLength == 0)
    {
      if (value.length() > 0)
      {
        // This should only happen if the value is composed entirely of spaces.
        // In that case, the normalized value is a single space.
        return ServerConstants.SINGLE_SPACE_VALUE;
      }
      else
      {
        // The value is empty, so it is already normalized.
        return ByteString.empty();
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

    return ByteString.valueOf(buffer.toString());
  }
}
