package org.opends.schema.matchingrules;

import static org.opends.schema.StringPrepProfile.CASE_FOLD;
import static org.opends.schema.StringPrepProfile.TRIM;

import org.opends.schema.Schema;
import org.opends.schema.StringPrepProfile;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.util.ServerConstants;
import org.opends.ldap.DecodeException;

/**
 * This class defines the caseIgnoreSubstringsMatch matching rule defined in
 * X.520 and referenced in RFC 2252.
 */
public class CaseIgnoreSubstringMatchingRule
    extends AbstractSubstringMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value) {
    return normalize(TRIM, value);
  }

  @Override
  protected ByteString normalizeSubString(Schema schema, ByteSequence value)
      throws DecodeException {
    return normalize(false, value);
  }

  private ByteString normalize(boolean trim, ByteSequence value)
  {

    StringBuilder buffer = new StringBuilder();
    StringPrepProfile.prepareUnicode(buffer, value, trim, CASE_FOLD);

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
        return value.toByteString();
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
