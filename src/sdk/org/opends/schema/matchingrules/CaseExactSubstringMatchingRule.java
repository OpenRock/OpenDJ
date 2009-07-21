package org.opends.schema.matchingrules;

import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import static org.opends.schema.StringPrepProfile.prepareUnicode;
import static org.opends.schema.StringPrepProfile.TRIM;
import static org.opends.schema.StringPrepProfile.NO_CASE_FOLD;
import static org.opends.server.schema.SchemaConstants.SYNTAX_SUBSTRING_ASSERTION_OID;
import static org.opends.server.schema.SchemaConstants.SMR_CASE_EXACT_NAME;
import static org.opends.server.schema.SchemaConstants.SMR_CASE_EXACT_OID;
import org.opends.server.util.ServerConstants;
import org.opends.schema.SchemaUtils;

import java.util.Collections;

/**
 * This class defines the caseExactSubstringsMatch matching rule defined in
 * X.520 and referenced in RFC 2252.
 */
public class CaseExactSubstringMatchingRule
    extends SubstringMatchingRuleImplementation
{
  public CaseExactSubstringMatchingRule()
  {
    super(SMR_CASE_EXACT_OID,
        Collections.singletonList(SMR_CASE_EXACT_NAME),
        "",
        false,
        SYNTAX_SUBSTRING_ASSERTION_OID,
        SchemaUtils.RFC4512_ORIGIN);
  }

  public ByteSequence normalizeAttributeValue(ByteSequence value) {
    return normalize(TRIM, value);
  }

  @Override
  public ByteSequence normalizeSubInitialValue(ByteSequence value) {
    return normalize(false, value);
  }

  @Override
  public ByteSequence normalizeSubAnyValue(ByteSequence value) {
    return normalize(false, value);
  }

  @Override
  public ByteSequence normalizeSubFinalValue(ByteSequence value) {
    return normalize(false, value);
  }

  private ByteSequence normalize(boolean trim, ByteSequence value)
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
