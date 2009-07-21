package org.opends.schema.matchingrules;

import org.opends.server.types.ByteSequence;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import org.opends.types.ConditionResult;
import org.opends.schema.MatchingRule;

import java.util.List;
import java.util.Map;

/**
 * This class defines the set of methods and structures that must be
 * implemented by a Directory Server module that implements a matching
 * rule used for equality matching.
 */
public abstract class EqualityMatchingRuleImplementation
    extends MatchingRuleImplementation
{
  protected EqualityMatchingRuleImplementation(String oid, List<String> names,
                                               String description,
                                               boolean obsolete, String syntax,
                                               Map<String,
                                               List<String>> extraProperties)
  {
    super(oid, names, description, obsolete, syntax, extraProperties);
  }

  protected EqualityMatchingRuleImplementation(
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
   * Indicates whether the provided normalized attribute values should be
   * considered a match for the given normalized assertion value. The assertion
   * value is guarenteed to be valid against this matching rule's assertion
   * syntax.
   *
   * @param attributeValue
   *          The syntax checked normalized form of the attribute value to
   *          compare.
   * @param assertionValue
   *          The normalized form of the assertion value to compare.
   *
   * @return  {@code true} if the provided values are equal, or
   *          {@code false} if not.
   */
  public boolean areEqual(
      ByteSequence attributeValue,
      ByteSequence assertionValue)
  {
    return attributeValue.equals(assertionValue);
  }

  public ConditionResult valuesMatch(
      ByteSequence attributeValue,
      ByteSequence assertionValue)
  {
    ByteSequence normAttributeValue = normalizeAttributeValue(
        attributeValue);
    ByteSequence normAssertionValue = normalizeAssertionValue(assertionValue);
    return areEqual(normAttributeValue, normAssertionValue) ?
        ConditionResult.TRUE : ConditionResult.FALSE;
  }
}
