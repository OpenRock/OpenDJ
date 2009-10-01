package org.opends.sdk.schema.matchingrules;

import org.opends.sdk.Assertion;
import org.opends.sdk.ConditionResult;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

/**
 * This class implements a default ordering matching rule that
 * matches normalized values in byte order.
 */
public abstract class AbstractOrderingMatchingRuleImplementation
    extends AbstractMatchingRuleImplementation
{
  public Assertion getGreaterOrEqualAssertion(Schema schema, ByteSequence value)
      throws DecodeException
  {
    final ByteString normAssertion = normalizeAttributeValue(schema, value);
    return new Assertion()
    {
      public ConditionResult matches(ByteString attributeValue) {
        return attributeValue.compareTo(normAssertion) >= 0 ?
            ConditionResult.TRUE : ConditionResult.FALSE;
      }
    };
  }

  public Assertion getLessOrEqualAssertion(Schema schema, ByteSequence value)
      throws DecodeException
  {
    final ByteString normAssertion = normalizeAttributeValue(schema, value);
    return new Assertion()
    {
      public ConditionResult matches(ByteString attributeValue) {
        return attributeValue.compareTo(normAssertion) <= 0 ?
            ConditionResult.TRUE : ConditionResult.FALSE;
      }
    };
  }

  public Assertion getAssertion(Schema schema, ByteSequence value)
      throws DecodeException {
  final ByteString normAssertion = normalizeAttributeValue(schema, value);
    return new Assertion()
    {
      public ConditionResult matches(ByteString attributeValue) {
        return attributeValue.compareTo(normAssertion) < 0 ?
            ConditionResult.TRUE : ConditionResult.FALSE;
      }
    };
  }
}
