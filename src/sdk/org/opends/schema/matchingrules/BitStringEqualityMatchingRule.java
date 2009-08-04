package org.opends.schema.matchingrules;

import static org.opends.server.schema.SchemaConstants.EMR_BIT_STRING_OID;
import static org.opends.server.schema.SchemaConstants.EMR_BIT_STRING_NAME;
import static org.opends.server.schema.SchemaConstants.SYNTAX_BIT_STRING_OID;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteSequence;
import org.opends.schema.SchemaUtils;
import org.opends.schema.Schema;

/**
 * This class defines the bitStringMatch matching rule defined in X.520 and
 * referenced in RFC 2252.
 */
public class BitStringEqualityMatchingRule
    extends AbstractEqualityMatchingRuleImplementation
{
  public ByteSequence normalizeAttributeValue(
      Schema schema, ByteSequence value)
  {
    String valueString = value.toString();
    int numLeadingBits = valueString.length();
    for(int i = numLeadingBits - 1; i >= 0; i--)
    {
      if(valueString.charAt(i) == '0')
      {
        numLeadingBits--;
      }
      else
      {
        break;
      }
    }
    return ByteString.valueOf(valueString.substring(0, numLeadingBits));
  }
}
