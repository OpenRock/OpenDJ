package org.opends.schema.matchingrules;

import org.opends.schema.SchemaUtils;
import org.opends.schema.Schema;
import static org.opends.server.schema.SchemaConstants.EMR_NUMERIC_STRING_NAME;
import static org.opends.server.schema.SchemaConstants.EMR_NUMERIC_STRING_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_NUMERIC_STRING_OID;
import static org.opends.schema.StringPrepProfile.prepareUnicode;
import static org.opends.schema.StringPrepProfile.TRIM;
import static org.opends.schema.StringPrepProfile.NO_CASE_FOLD;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

import java.util.Collections;

/**
 * This class implements the numericStringMatch matching rule defined in X.520
 * and referenced in RFC 2252.  It allows for values with numeric digits and
 * spaces, but ignores spaces when performing matching.
 */
public class NumericStringEqualityMatchingRule
    extends EqualityMatchingRuleImplementation
{
  public NumericStringEqualityMatchingRule()
  {
    super(EMR_NUMERIC_STRING_OID,
        Collections.singletonList(EMR_NUMERIC_STRING_NAME),
        "",
        false,
        SYNTAX_NUMERIC_STRING_OID,
        SchemaUtils.RFC4512_ORIGIN);
  }

  public ByteSequence normalizeAttributeValue(Schema schema, ByteSequence value) {
    StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, TRIM, NO_CASE_FOLD);

    if(buffer.length() == 0)
    {
      return ByteString.empty();
    }
    return ByteString.valueOf(buffer.toString());
  }
}
