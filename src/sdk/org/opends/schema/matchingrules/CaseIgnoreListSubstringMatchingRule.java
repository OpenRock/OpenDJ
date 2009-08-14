package org.opends.schema.matchingrules;

import static org.opends.schema.StringPrepProfile.*;

import org.opends.schema.Schema;
import org.opends.server.schema.StringPrepProfile;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.util.ServerConstants;

/**
 * This class implements the caseIgnoreListSubstringsMatch matching rule defined
 * in X.520 and referenced in RFC 2252.
 */
public class CaseIgnoreListSubstringMatchingRule
    extends AbstractSubstringMatchingRuleImplementation
{
  public ByteSequence normalizeAttributeValue(Schema schema, ByteSequence value) {
    StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, StringPrepProfile.TRIM, CASE_FOLD);

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


    // Replace any consecutive spaces with a single space.  Any spaces around a
    // dollar sign will also be removed.
    for (int pos = bufferLength-1; pos > 0; pos--)
    {
      if (buffer.charAt(pos) == ' ')
      {
        char c = buffer.charAt(pos-1);
        if (c == ' ')
        {
          buffer.delete(pos, pos+1);
        }
        else if (c == '$')
        {
          if ((pos <= 1) || (buffer.charAt(pos-2) != '\\'))
          {
            buffer.delete(pos, pos+1);
          }
        }
        else if (buffer.charAt(pos+1) == '$')
        {
          buffer.delete(pos, pos+1);
        }
      }
    }

    return ByteString.valueOf(buffer.toString());
  }

  @Override
  public ByteSequence normalizeSubInitialValue(Schema schema, ByteSequence value) {
    return normalize(value);
  }

  @Override
  public ByteSequence normalizeSubAnyValue(Schema schema, ByteSequence value) {
    return normalize(value);
  }

  @Override
  public ByteSequence normalizeSubFinalValue(Schema schema, ByteSequence value) {
    return normalize(value);
  }

  private ByteSequence normalize(ByteSequence value)
  {
    // In this case, the process for normalizing a substring is the same as
    // normalizing a full value with the exception that it may include an
    // opening or trailing space.
    StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, false, CASE_FOLD);

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
