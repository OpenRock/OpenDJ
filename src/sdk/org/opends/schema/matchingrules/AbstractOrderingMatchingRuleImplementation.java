package org.opends.schema.matchingrules;

import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.types.ConditionResult;

/**
 * This interface defines the set of methods that must be implemented
 * by a Directory Server module that implements a matching
 * rule used for determining the correct order of values when sorting
 * or processing range filters.
 */
public abstract class AbstractOrderingMatchingRuleImplementation
    implements OrderingMatchingRuleImplementation
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
   * Compares the attribute value to the assertion value and returns a value
   * that indicates their relative order.
   *
   * @param schema The schema in which this matching rule is defined.
   *@param attributeValue
   *          The normalized form of the attribute value to compare.
   * @param assertionValue
 *          The normalized form of the assertion value to compare.
 * @return  A negative integer if {@code attributeValue} should come before
   *          {@code assertionValue} in ascending order, a positive integer if
   *          {@code attributeValue} should come after {@code assertionValue} in
   *          ascending order, or zero if there is no difference
   *          between the values with regard to ordering.
   */
  public int compareValues(
      Schema schema, ByteSequence attributeValue,
      ByteSequence assertionValue)
  {
    return attributeValue.compareTo(assertionValue);
  }

  /**
   * Indicates whether the provided attribute value should appear earlier then
   * the given assertion value.
   *
   * @param schema The schema in which this matching rule is defined.
   *@param attributeValue
   *          The attribute value.
   * @param assertionValue
 *          The assertion value. @return {@code TRUE} if and only if the
   *        attribute value is comes before the provided assertion value,
   *        {@code FALSE} otherwise, or {@code UNDEFINED} if the result is
   *        undefined.
   */
  public ConditionResult valuesMatch(
      Schema schema, ByteSequence attributeValue,
      ByteSequence assertionValue)
  {
    ByteSequence normAttributeValue =
        normalizeAttributeValue(null, attributeValue);
    ByteSequence normAssertionValue =
        normalizeAssertionValue(null, assertionValue);
    return compareValues(null, normAttributeValue,
        normAssertionValue) < 0 ?
        ConditionResult.TRUE : ConditionResult.FALSE;
  }
}
