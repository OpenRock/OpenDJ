package org.opends.schema.matchingrules;

import java.util.List;

import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;

/**
 * This interface defines the set of methods that must be
 * implemented by a Directory Server module that implements a matching
 * rule used for substring matching.
 */
public interface SubstringMatchingRuleImplementation
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
   * Retrieves the normalized form of the provided initial assertion value
   * substring, which is best suite for efficiently performing matching
   * operations on that value.
   *
   * @param schema The schema in which this matching rule is defined.
   * @param value The initial assertion value substring to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public ByteSequence normalizeSubInitialValue(Schema schema,
                                               ByteSequence value);

  /**
   * Retrieves the normalized form of the provided middle assertion value
   * substring, which is best suite for efficiently performing matching
   * operations on that value.
   *
   * @param schema The schema in which this matching rule is defined.
   * @param value The middle assertion value substring to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public ByteSequence normalizeSubAnyValue(Schema schema, ByteSequence value);

  /**
   * Retrieves the normalized form of the provided final assertion value
   * substring, which is best suite for efficiently performing matching
   * operations on that value.
   *
   * @param schema The schema in which this matching rule is defined.
   * @param value The final assertion value substring to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public ByteSequence normalizeSubFinalValue(Schema schema, ByteSequence value);

  /**
   * Determines whether the provided value matches the given substring
   * filter components.  Note that any of the substring filter
   * components may be {@code null} but at least one of them must be
   * non-{@code null}.
   *
   * @param schema The schema in which this matching rule is defined.
   * @param attributeValue The normalized attribute value against which to
   *                       compare the substring components.
   * @param  subInitial      The normalized substring value fragment
   *                         that should appear at the beginning of
   *                         the target value.
   * @param  subAnyElements  The normalized substring value fragments
   *                         that should appear in the middle of the
   *                         target value.
   * @param  subFinal        The normalized substring value fragment
   *                         that should appear at the end of the
   *                         target value.
   * @return  {@code true} if the provided value does match the given
   *          substring components, or {@code false} if not.
   */
  public boolean valueMatchesSubstring(Schema schema,
                                       ByteSequence attributeValue,
                                       ByteSequence subInitial,
                                       List<ByteSequence> subAnyElements,
                                       ByteSequence subFinal);
}
