package org.opends.sdk.schema.matchingrules;

import static org.opends.sdk.schema.StringPrepProfile.CASE_FOLD;
import static org.opends.sdk.schema.StringPrepProfile.prepareUnicode;

import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.StringPrepProfile;
import org.opends.sdk.schema.SchemaConstants;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

import java.util.List;

/**
 * This class implements the caseIgnoreListSubstringsMatch matching rule defined
 * in X.520 and referenced in RFC 2252.
 */
public class CaseIgnoreListSubstringMatchingRule
    extends AbstractSubstringMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value) {
    StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, StringPrepProfile.TRIM, CASE_FOLD);

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
  protected ByteString normalizeSubString(Schema schema, ByteSequence value)
      throws DecodeException {
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
        return SchemaConstants.SINGLE_SPACE_VALUE;
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
