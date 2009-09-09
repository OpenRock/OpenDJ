package org.opends.sdk.schema.matchingrules;

import static org.opends.sdk.schema.StringPrepProfile.NO_CASE_FOLD;
import static org.opends.sdk.schema.StringPrepProfile.TRIM;
import static org.opends.sdk.schema.StringPrepProfile.prepareUnicode;

import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

/**
 * This class implements the numericStringMatch matching rule defined in X.520
 * and referenced in RFC 2252.  It allows for values with numeric digits and
 * spaces, but ignores spaces when performing matching.
 */
public class NumericStringEqualityMatchingRule
    extends AbstractMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value)
  {
    StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, TRIM, NO_CASE_FOLD);

    if(buffer.length() == 0)
    {
      return ByteString.empty();
    }
    return ByteString.valueOf(buffer.toString());
  }
}
