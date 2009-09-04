package org.opends.schema.matchingrules;

import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.types.Assertion;
import org.opends.ldap.DecodeException;

import java.util.Comparator;

/**
 * This interface defines the set of methods that must be implemented
 * by a Directory Server module that implements a matching
 * rule used for determining the correct order of values when sorting
 * or processing range filters.
 */
public interface OrderingMatchingRuleImplementation
    extends MatchingRuleImplementation
{
  /**
   * Get a comparator that can be used to compare the attribute values
   * normalized by this matching rule.
   *
   * @param schema The schema in which this matching rule is defined.
   * @return  A comparator that can be used to compare the attribute values
   * normalized by this matching rule.
   */
  public Comparator<ByteSequence> comparator(Schema schema);

  /**
   * Retrieves the normalized form of the provided assertion value, which is
   * best suite for efficiently performing greater than or equal matching
   * operations on that value.
   * The assertion value is guarenteed to be valid against this matching rule's
   * assertion syntax.
   *
   * @param schema The schema in which this matching rule is defined.
   * @param value The syntax checked assertion value to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public Assertion getGreaterOrEqualAssertion(Schema schema, ByteSequence value)
      throws DecodeException;

  /**
   * Retrieves the normalized form of the provided assertion value, which is
   * best suite for efficiently performing greater than or equal matching
   * operations on that value.
   * The assertion value is guarenteed to be valid against this matching rule's
   * assertion syntax.
   *
   * @param schema The schema in which this matching rule is defined.
   * @param value The syntax checked assertion value to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public Assertion getLessOrEqualAssertion(Schema schema, ByteSequence value)
      throws DecodeException;

  /**
   * Retrieves the normalized form of the provided assertion value, which is
   * best suite for efficiently performing matching operations on that value.
   * The assertion evaluates to true if provided attribute value
   * should appear earlier then the assertion value.
   * The assertion value is guarenteed to be valid against this matching rule's
   * assertion syntax.
   *
   * @param schema The schema in which this matching rule is defined.
   * @param value The syntax checked assertion value to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public Assertion getAssertion(Schema schema, ByteSequence value)
      throws DecodeException;
}
