package org.opends.schema.matchingrules;

import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

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
