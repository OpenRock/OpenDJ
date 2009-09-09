package org.opends.sdk.schema.matchingrules;

import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

/**
 * This class defines the octetStringSubstringsMatch matching rule defined in
 * X.520.  It will be used as the default substring matching rule for the binary
 * and octet string syntaxes.
 */
public class OctetStringSubstringMatchingRule
    extends AbstractSubstringMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value)
  {
    return value.toByteString();
  }
}
