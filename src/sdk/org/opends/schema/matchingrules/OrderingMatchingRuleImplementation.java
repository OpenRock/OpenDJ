package org.opends.schema.matchingrules;

import org.opends.server.types.ByteSequence;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import org.opends.types.ConditionResult;
import org.opends.schema.MatchingRule;

import java.util.List;
import java.util.Map;

/**
 * This interface defines the set of methods that must be implemented
 * by a Directory Server module that implements a matching
 * rule used for determining the correct order of values when sorting
 * or processing range filters.
 */
public abstract class OrderingMatchingRuleImplementation
    extends MatchingRuleImplementation
{
  protected OrderingMatchingRuleImplementation(String oid, List<String> names,
                                               String description,
                                               boolean obsolete, String syntax,
                                               Map<String,
                                               List<String>> extraProperties)
  {
    super(oid, names, description, obsolete, syntax, extraProperties);
  }

  protected OrderingMatchingRuleImplementation(
      MatchingRule orginalMatchingRule) {
    super(orginalMatchingRule);
  }

  /**
   * Retrieves the normalized form of the provided attribute value, which is
   * best suite for efficiently performing matching operations on
   * that value.
   *
   * @param value
   *          The attribute value to be normalized.
   * @return The normalized version of the provided attribute value.
   */
  public abstract ByteSequence normalizeAttributeValue(ByteSequence value);

  /**
   * Retrieves the normalized form of the provided assertion value, which is
   * best suite for efficiently performing matching operations on that value.
   * The assertion value is guarenteed to be valid against this matching rule's
   * assertion syntax.
   *
   * @param value The syntax checked assertion value to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public ByteSequence normalizeAssertionValue(ByteSequence value)
  {
    return normalizeAttributeValue(value);
  }

  /**
   * Compares the attribute value to the assertion value and returns a value
   * that indicates their relative order.
   *
   * @param attributeValue
   *          The normalized form of the attribute value to compare.
   * @param assertionValue
   *          The normalized form of the assertion value to compare.
   *
   * @return  A negative integer if {@code attributeValue} should come before
   *          {@code assertionValue} in ascending order, a positive integer if
   *          {@code attributeValue} should come after {@code assertionValue} in
   *          ascending order, or zero if there is no difference
   *          between the values with regard to ordering.
   */
  public int compareValues(
      ByteSequence attributeValue,
      ByteSequence assertionValue)
  {
    return attributeValue.compareTo(assertionValue);
  }

  /**
   * Indicates whether the provided attribute value should appear earlier then
   * the given assertion value.
   *
   * @param attributeValue
   *          The attribute value.
   * @param assertionValue
   *          The assertion value.
   * @return {@code TRUE} if and only if the attribute value is comes before
   *         the provided assertion value, {@code FALSE} otherwise, or
   *         {@code UNDEFINED} if the result is undefined.
   */
  public ConditionResult valuesMatch(
      ByteSequence attributeValue,
      ByteSequence assertionValue)
  {
    ByteSequence normAttributeValue =
        normalizeAttributeValue(attributeValue);
    ByteSequence normAssertionValue = normalizeAssertionValue(assertionValue);
    return compareValues(normAttributeValue,
        normAssertionValue) < 0 ?
        ConditionResult.TRUE : ConditionResult.FALSE;
  }
}
