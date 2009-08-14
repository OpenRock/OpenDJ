package org.opends.schema.matchingrules;

import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.util.ServerConstants;

/**
 * This class defines the booleanMatch matching rule defined in X.520 and
 * referenced in RFC 4519.
 */
public class BooleanEqualityMatchingRule
    extends AbstractEqualityMatchingRuleImplementation
{
  public ByteSequence normalizeAttributeValue(
      Schema schema, ByteSequence value)
  {
    String valueString = value.toString().toUpperCase();
    if (valueString.equals("TRUE") || valueString.equals("YES") ||
        valueString.equals("ON") || valueString.equals("1"))
    {
      return ServerConstants.TRUE_VALUE;
    }
    else if (valueString.equals("FALSE") || valueString.equals("NO") ||
        valueString.equals("OFF") || valueString.equals("0"))
    {
      return ServerConstants.FALSE_VALUE;
    }
    else
    {
      return value;
    }
  }
}
