package org.opends.schema.matchingrules;

import org.opends.schema.SchemaUtils;
import org.opends.schema.Schema;
import static org.opends.schema.StringPrepProfile.prepareUnicode;
import static org.opends.schema.StringPrepProfile.TRIM;
import static org.opends.schema.StringPrepProfile.CASE_FOLD;
import static org.opends.server.schema.SchemaConstants.EMR_WORD_NAME;
import static org.opends.server.schema.SchemaConstants.EMR_WORD_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_DIRECTORY_STRING_OID;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.util.ServerConstants;

/**
 * This class implements the wordMatch matching rule defined in X.520.  That
 * document defines "word" as implementation-specific, but in this case we will
 * consider it a match if the assertion value is contained within the attribute
 * value and is bounded by the edge of the value or any of the following
 * characters:
 * <BR>
 * <UL>
 *   <LI>A space</LI>
 *   <LI>A period</LI>
 *   <LI>A comma</LI>
 *   <LI>A slash</LI>
 *   <LI>A dollar sign</LI>
 *   <LI>A plus sign</LI>
 *   <LI>A dash</LI>
 *   <LI>An underscore</LI>
 *   <LI>An octothorpe</LI>
 *   <LI>An equal sign</LI>
 * </UL>
 */
public class WordEqualityMatchingRule
    extends AbstractEqualityMatchingRuleImplementation
{
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

    return ByteString.valueOf(buffer.toString());
  }

  @Override
  public boolean areEqual(Schema schema, ByteSequence attributeValue, ByteSequence assertionValue) {
    // See if the assertion value is contained in the attribute value.
    // If not, then it isn't a match.
    String valueStr1 = attributeValue.toString();
    String valueStr2 = assertionValue.toString();
    int pos = valueStr1.indexOf(valueStr2);
    if (pos < 0)
    {
      return false;
    }


    if (pos > 0)
    {
      char c = valueStr1.charAt(pos-1);
      switch (c)
      {
        case ' ':
        case '.':
        case ',':
        case '/':
        case '$':
        case '+':
        case '-':
        case '_':
        case '#':
        case '=':
          // These are all acceptable.
          break;

        default:
          // Anything else is not.
          return false;
      }
    }


    if (valueStr1.length() > (pos + valueStr2.length()))
    {
      char c = valueStr1.charAt(pos + valueStr2.length());
      switch (c)
      {
        case ' ':
        case '.':
        case ',':
        case '/':
        case '$':
        case '+':
        case '-':
        case '_':
        case '#':
        case '=':
          // These are all acceptable.
          break;

        default:
          // Anything else is not.
          return false;
      }
    }


    // If we've gotten here, then we can assume it is a match.
    return true;
  }
}
