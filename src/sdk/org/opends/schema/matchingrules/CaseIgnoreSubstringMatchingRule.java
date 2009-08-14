package org.opends.schema.matchingrules;

import static org.opends.schema.StringPrepProfile.*;

import org.opends.schema.Schema;
import org.opends.schema.StringPrepProfile;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.util.ServerConstants;

/**
 * This class defines the caseIgnoreSubstringsMatch matching rule defined in
 * X.520 and referenced in RFC 2252.
 */
public class CaseIgnoreSubstringMatchingRule
    extends AbstractSubstringMatchingRuleImplementation
{
  public ByteSequence normalizeAttributeValue(Schema schema, ByteSequence value) {
    return normalize(TRIM, value);
  }

  @Override
  public ByteSequence normalizeSubInitialValue(Schema schema, ByteSequence value) {
    // In this case, the process for normalizing a substring is the same as
    // normalizing a full value with the exception that it may include an
    // opening or trailing space.
    return normalize(false, value);
  }

  @Override
  public ByteSequence normalizeSubAnyValue(Schema schema, ByteSequence value) {
    // In this case, the process for normalizing a substring is the same as
    // normalizing a full value with the exception that it may include an
    // opening or trailing space.
    return normalize(false, value);
  }

  @Override
  public ByteSequence normalizeSubFinalValue(Schema schema, ByteSequence value) {
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
