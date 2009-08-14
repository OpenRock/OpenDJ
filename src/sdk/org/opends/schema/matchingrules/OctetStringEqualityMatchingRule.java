package org.opends.schema.matchingrules;

import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;

/**
 * This class defines the octetStringMatch matching rule defined in X.520.  It
 * will be used as the default equality matching rule for the binary and octet
 * string syntaxes.
 */
public class OctetStringEqualityMatchingRule
    extends AbstractEqualityMatchingRuleImplementation
{
  public ByteSequence normalizeAttributeValue(Schema schema,
                                              ByteSequence value)
  {
    return value;
  }
}
