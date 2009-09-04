package org.opends.schema.matchingrules;

import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.schema.Schema;
import org.opends.types.Assertion;
import org.opends.types.ConditionResult;
import org.opends.ldap.DecodeException;

/**
 * This class implements an extremely simple approximate matching rule that will
 * consider two values approximately equal only if they have the same length.
 * It is intended purely for testing purposes.
 */
public class EqualLengthApproximateMatchingRule
    extends AbstractMatchingRuleImplementation
{
  /**
   * {@inheritDoc}
   */
  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value)
  {
    return value.toByteString();
  }

  @Override
  public Assertion getAssertion(Schema schema, final ByteSequence value)
      throws DecodeException {
    return new Assertion()
    {
      public ConditionResult matches(ByteString attributeValue) {
        return attributeValue.length() == value.length() ?
        ConditionResult.TRUE : ConditionResult.FALSE;
      }
    };
  }
}
