package org.opends.sdk.schema.matchingrules;

import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_IA5_ILLEGAL_CHARACTER;
import static org.opends.sdk.schema.StringPrepProfile.NO_CASE_FOLD;
import static org.opends.sdk.schema.StringPrepProfile.TRIM;
import static org.opends.sdk.schema.StringPrepProfile.prepareUnicode;

import org.opends.messages.Message;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.SchemaConstants;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

/**
 * This class implements the caseExactIA5SubstringsMatch matching rule.  This
 * matching rule actually isn't defined in any official specification, but some
 * directory vendors do provide an implementation using an OID from their own
 * private namespace.
 */
public class CaseExactIA5SubstringMatchingRule
    extends AbstractSubstringMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value)
      throws DecodeException {
    return normalize(TRIM, value);
  }

  @Override
  protected ByteString normalizeSubString(Schema schema, ByteSequence value)
      throws DecodeException {
    return normalize(false, value);
  }

  private ByteString normalize(boolean trim, ByteSequence value)
      throws DecodeException
  {
    StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, trim, NO_CASE_FOLD);

    int bufferLength = buffer.length();
    if (bufferLength == 0)
    {
      if (value.length() > 0)
      {
        // This should only happen if the value is composed entirely of spaces.
        // In that case, the normalized value is a single space.
        return SchemaConstants.SINGLE_SPACE_VALUE;
      }
      else
      {
        // The value is empty, so it is already normalized.
        return ByteString.empty();
      }
    }


    // Replace any consecutive spaces with a single space and watch out for
    // non-ASCII characters.
    for (int pos = bufferLength-1; pos > 0; pos--)
    {
      char c = buffer.charAt(pos);
      if (c == ' ')
      {
        if (buffer.charAt(pos-1) == ' ')
        {
          buffer.delete(pos, pos+1);
        }
      }
      else if ((c & 0x7F) != c)
      {
        // This is not a valid character for an IA5 string.  If strict syntax
        // enforcement is enabled, then we'll throw an exception.  Otherwise,
        // we'll get rid of the character.
        Message message = WARN_ATTR_SYNTAX_IA5_ILLEGAL_CHARACTER.get(
                value.toString(), String.valueOf(c));
            throw new DecodeException(message);
      }
    }

    return ByteString.valueOf(buffer.toString());
  }
}
