package org.opends.schema.matchingrules;

import org.opends.ldap.DecodeException;
import org.opends.schema.Schema;
import org.opends.schema.syntaxes.GeneralizedTimeSyntax;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

/**
 * This class defines the generalizedTimeMatch matching rule defined in X.520
 * and referenced in RFC 2252.
 */
public class GeneralizedTimeEqualityMatchingRule
    extends AbstractEqualityMatchingRuleImplementation
{
  public ByteSequence normalizeAttributeValue(Schema schema, ByteSequence value)
  {
    try
    {
      return ByteString.valueOf(
          GeneralizedTimeSyntax.decodeGeneralizedTimeValue(value));
    }
    catch(DecodeException de)
    {
      return value;
    }
  }
}
