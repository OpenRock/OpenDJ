package org.opends.sdk.schema.matchingrules;

import org.opends.server.types.ByteString;
import org.opends.server.types.ByteSequence;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.SchemaConstants;
import static org.opends.sdk.schema.StringPrepProfile.prepareUnicode;
import static org.opends.sdk.schema.StringPrepProfile.TRIM;
import static org.opends.sdk.schema.StringPrepProfile.CASE_FOLD;

/**
 * This class implements the protocolInformationMatch matching rule defined in
 * X.520 and referenced in RFC 2252.  However, since this matching rule and the
 * associated syntax have been deprecated, this matching rule behaves exactly
 * like the caseIgnoreMatch rule.
 */
public class ProtocolInformationEqualityMatchingRule
    extends AbstractMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value)
  {
    StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, TRIM, CASE_FOLD);

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
