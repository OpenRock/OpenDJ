package org.opends.schema.matchingrules;

import org.opends.schema.SchemaUtils;
import static org.opends.server.schema.SchemaConstants.EMR_BOOLEAN_NAME;
import static org.opends.server.schema.SchemaConstants.EMR_BOOLEAN_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_BOOLEAN_OID;
import org.opends.server.types.ByteSequence;
import org.opends.server.util.ServerConstants;

import java.util.Collections;

/**
 * This class defines the booleanMatch matching rule defined in X.520 and
 * referenced in RFC 4519.
 */
public class BooleanEqualityMatchingRule
    extends EqualityMatchingRuleImplementation
{
  public BooleanEqualityMatchingRule()
  {
    super(EMR_BOOLEAN_OID,
        Collections.singletonList(EMR_BOOLEAN_NAME),
        "",
        false,
        SYNTAX_BOOLEAN_OID,
        SchemaUtils.RFC4512_ORIGIN);
  }

  public ByteSequence normalizeAttributeValue(
      ByteSequence value)
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
