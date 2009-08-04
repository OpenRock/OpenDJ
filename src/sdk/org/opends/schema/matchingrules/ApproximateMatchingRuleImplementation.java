package org.opends.schema.matchingrules;

import org.opends.server.types.ByteSequence;
import org.opends.schema.Schema;

/**
 * This interface defines the set of methods that must be
 * implemented by a Directory Server module that implements a matching
 * rule used for approximate matching.
 */
public interface ApproximateMatchingRuleImplementation
    extends MatchingRuleImplementation
{
  /**
   * Retrieves the normalized form of the provided attribute value, which is
   * best suite for efficiently performing matching operations on
   * that value.
   *
   * @param schema The schema in which this matching rule is defined.
   * @param value
   *          The attribute value to be normalized.
   * @return The normalized version of the provided attribute value.
   */
  public ByteSequence normalizeAttributeValue(Schema schema,
                                              ByteSequence value);

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
  public ByteSequence normalizeAssertionValue(Schema schema,
                                              ByteSequence value);

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
  public boolean approximatelyMatch(Schema schema, ByteSequence attributeValue,
                                    ByteSequence assertionValue);
}
