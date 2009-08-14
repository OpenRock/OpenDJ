package org.opends.schema.matchingrules;

import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.types.ConditionResult;

/**
 * This class defines the set of methods and structures that must be
 * implemented by a Directory Server module that implements a matching
 * rule used for equality matching.
 */
public abstract class AbstractEqualityMatchingRuleImplementation
    implements EqualityMatchingRuleImplementation
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
   * Indicates whether the provided normalized attribute values should be
   * considered a match for the given normalized assertion value. The assertion
   * value is guarenteed to be valid against this matching rule's assertion
   * syntax.
   *
   * @param schema The schema in which this matching rule is defined.
   * @param attributeValue
   *          The syntax checked normalized form of the attribute value to
   *          compare.
   * @param assertionValue
 *          The normalized form of the assertion value to compare.
 * @return  {@code true} if the provided values are equal, or
   *          {@code false} if not.
   */
  public boolean areEqual(Schema schema, ByteSequence attributeValue,
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
    return areEqual(schema, normAttributeValue, normAssertionValue) ?
        ConditionResult.TRUE : ConditionResult.FALSE;
  }
}
