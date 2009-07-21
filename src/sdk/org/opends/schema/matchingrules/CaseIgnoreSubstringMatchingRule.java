package org.opends.schema.matchingrules;

import org.opends.schema.SchemaUtils;
import static org.opends.schema.StringPrepProfile.TRIM;
import static org.opends.server.schema.SchemaConstants.SMR_CASE_IGNORE_NAME;
import static org.opends.server.schema.SchemaConstants.SMR_CASE_IGNORE_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_SUBSTRING_ASSERTION_OID;
import org.opends.schema.StringPrepProfile;
import static org.opends.server.schema.StringPrepProfile.CASE_FOLD;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.util.ServerConstants;

import java.util.Collections;

/**
 * This class defines the caseIgnoreSubstringsMatch matching rule defined in
 * X.520 and referenced in RFC 2252.
 */
public class CaseIgnoreSubstringMatchingRule
    extends SubstringMatchingRuleImplementation
{
  public CaseIgnoreSubstringMatchingRule()
  {
    super(SMR_CASE_IGNORE_OID,
        Collections.singletonList(SMR_CASE_IGNORE_NAME),
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
    // In this case, the process for normalizing a substring is the same as
    // normalizing a full value with the exception that it may include an
    // opening or trailing space.
    return normalize(false, value);
  }

  @Override
  public ByteSequence normalizeSubAnyValue(ByteSequence value) {
    // In this case, the process for normalizing a substring is the same as
    // normalizing a full value with the exception that it may include an
    // opening or trailing space.
    return normalize(false, value);
  }

  @Override
  public ByteSequence normalizeSubFinalValue(ByteSequence value) {
    // In this case, the process for normalizing a substring is the same as
    // normalizing a full value with the exception that it may include an
    // opening or trailing space.
    return normalize(false, value);
  }

  private ByteSequence normalize(boolean trim, ByteSequence value)
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
