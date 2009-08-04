package org.opends.schema.matchingrules;

import org.opends.server.types.ByteSequence;
import org.opends.schema.Schema;
import org.opends.types.ConditionResult;

/**
 * This class defines the set of methods and structures that must be
 * implemented by a Directory Server module that implements a matching
 * rule used for approximate matching.
 */
public abstract class AbstractApproximateMatchingRuleImplementation
    implements ApproximateMatchingRuleImplementation
{
  /**
   * Retrieves the normalized form of the provided assertion value, which is
   * best suite for efficiently performing matching operations on that value.
   * The assertion value is guarenteed to be valid against this matching rule's
   * assertion syntax.
   *
   * @param schema The schema in which this matching rule is defined.
   * @param value The syntax checked assertion value to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public ByteSequence normalizeAssertionValue(Schema schema, ByteSequence value)
  {
    return normalizeAttributeValue(schema, value);
  }

  /**
   * Indicates whether the two provided normalized values are
   * approximately equal to each other.
   *
   * @param schema The schema in which this matching rule is defined.
   * @param attributeValue  The normalized form of the first value to
   *                 compare.
   * @param assertionValue  The normalized form of the second value to
   *                 compare.
   *
   * @return  {@code true} if the provided values are approximately
   *          equal, or {@code false} if not.
   */
  public boolean approximatelyMatch(Schema schema,
                                    ByteSequence attributeValue,
                                    ByteSequence assertionValue)
  {
    return attributeValue.equals(assertionValue);
  }

  public ConditionResult valuesMatch(
      Schema schema, ByteSequence attributeValue,
      ByteSequence assertionValue)
  {
    ByteSequence normAttributeValue =
        normalizeAttributeValue(schema, attributeValue);
    ByteSequence normAssertionValue =
        normalizeAssertionValue(schema, assertionValue);
    return approximatelyMatch(schema, normAttributeValue, normAssertionValue) ?
        ConditionResult.TRUE : ConditionResult.FALSE;
  }
}
