package org.opends.sdk.schema.matchingrules;

import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

/**
 * This class defines the octetStringOrderingMatch matching rule defined in
 * X.520.  This will be the default ordering matching rule for the binary and
 * octet string syntaxes.
 */
public class OctetStringOrderingMatchingRule
    extends AbstractOrderingMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema,
                                              ByteSequence value)
  {
    return value.toByteString();
  }
}
