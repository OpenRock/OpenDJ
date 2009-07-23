package org.opends.schema.matchingrules;

import static org.opends.server.schema.SchemaConstants.OMR_CASE_EXACT_OID;
import static org.opends.server.schema.SchemaConstants.OMR_CASE_EXACT_NAME;
import static org.opends.server.schema.SchemaConstants.SYNTAX_DIRECTORY_STRING_OID;
import static org.opends.schema.StringPrepProfile.prepareUnicode;
import static org.opends.schema.StringPrepProfile.TRIM;
import static org.opends.schema.StringPrepProfile.NO_CASE_FOLD;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.util.ServerConstants;
import org.opends.schema.SchemaUtils;
import org.opends.schema.Schema;

import java.util.Collections;

/**
 * This class defines the caseExactOrderingMatch matching rule defined in X.520
 * and referenced in RFC 4519.
 */
public class CaseExactOrderingMatchingRule
    extends OrderingMatchingRuleImplementation
{
  public CaseExactOrderingMatchingRule()
  {
    super(OMR_CASE_EXACT_OID,
        Collections.singletonList(OMR_CASE_EXACT_NAME), 
        "",
        false,
        SYNTAX_DIRECTORY_STRING_OID,
        SchemaUtils.RFC4512_ORIGIN);
  }

  public ByteSequence normalizeAttributeValue(
      Schema schema, ByteSequence value)
  {
    StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, TRIM, NO_CASE_FOLD);

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
