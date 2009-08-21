package org.opends.schema.matchingrules;

import org.opends.server.types.ByteSequence;
import org.opends.schema.Schema;

/**
 * This class implements an extremely simple approximate matching rule that will
 * consider two values approximately equal only if they have the same length.
 * It is intended purely for testing purposes.
 */
public class EqualLengthApproximateMatchingRule
    extends AbstractApproximateMatchingRuleImplementation
{
  /**
   * {@inheritDoc}
   */
  public ByteSequence normalizeAttributeValue(Schema schema, ByteSequence value)
  {
    return value;
  }

  @Override
  public boolean approximatelyMatch(Schema schema, ByteSequence attributeValue,
                                    ByteSequence assertionValue)
  {
    return attributeValue.length() == assertionValue.length();
  }
}
