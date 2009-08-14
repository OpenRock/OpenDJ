package org.opends.schema.matchingrules;

import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;

/**
 * This class defines the octetStringOrderingMatch matching rule defined in
 * X.520.  This will be the default ordering matching rule for the binary and
 * octet string syntaxes.
 */
public class OctetStringOrderingMatchingRule
    extends AbstractOrderingMatchingRuleImplementation
{
  public ByteSequence normalizeAttributeValue(Schema schema,
                                              ByteSequence value)
  {
    return value;
  }
}
