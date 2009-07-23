package org.opends.schema.matchingrules;

import org.opends.schema.SchemaUtils;
import org.opends.schema.Schema;
import static org.opends.server.schema.SchemaConstants.EMR_CASE_IGNORE_IA5_NAME;
import static org.opends.server.schema.SchemaConstants.EMR_CASE_IGNORE_IA5_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_IA5_STRING_OID;
import static org.opends.schema.StringPrepProfile.prepareUnicode;
import static org.opends.schema.StringPrepProfile.TRIM;
import static org.opends.schema.StringPrepProfile.CASE_FOLD;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.util.ServerConstants;

import java.util.Collections;

/**
 * This class implements the caseIgnoreIA5Match matching rule defined in RFC
 * 2252.
 */
public class CaseIgnoreIA5EqualityMatchingRule
    extends EqualityMatchingRuleImplementation
{
  public CaseIgnoreIA5EqualityMatchingRule()
  {
    super(EMR_CASE_IGNORE_IA5_OID,
        Collections.singletonList(EMR_CASE_IGNORE_IA5_NAME),
        "",
        false,
        SYNTAX_IA5_STRING_OID,
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
      char c = buffer.charAt(pos);
      if (c == ' ')
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
