package org.opends.schema.matchingrules;

import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.util.ServerConstants;
import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_ILLEGAL_BOOLEAN;
import org.opends.ldap.DecodeException;

/**
 * This class defines the booleanMatch matching rule defined in X.520 and
 * referenced in RFC 4519.
 */
public class BooleanEqualityMatchingRule
    extends AbstractMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value) 
      throws DecodeException
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

    throw new DecodeException(WARN_ATTR_SYNTAX_ILLEGAL_BOOLEAN.get(
              value.toString()));
  }
}
