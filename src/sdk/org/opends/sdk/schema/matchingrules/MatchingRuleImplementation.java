package org.opends.sdk.schema.matchingrules;

import org.opends.sdk.Assertion;
import org.opends.sdk.ConditionResult;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

import java.util.Comparator;
import java.util.List;

/**
 * This interface defines the set of methods that must be implemented
 * to define a new matching rule.
 */
public interface MatchingRuleImplementation
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
   * @throws DecodeException if an syntax error occured while parsing the value.
   */
  public ByteString normalizeAttributeValue(Schema schema,
                                            ByteSequence value)
      throws DecodeException;

  /**
   * Retrieves the normalized form of the provided assertion value, which is
   * best suite for efficiently performing matching operations on that value.
   * The assertion value is guarenteed to be valid against this matching rule's
   * assertion syntax.
   *
   * @param schema The schema in which this matching rule is defined.
   * @param value The syntax checked assertion value to be normalized.
   * @return The normalized version of the provided assertion value.
   * @throws DecodeException if an syntax error occured while parsing the value.
   */
  public Assertion getAssertion(Schema schema, ByteSequence value)
      throws DecodeException;

  /**
   * Retrieves the normalized form of the provided assertion substring values,
   * which is best suite for efficiently performing matching operations on that
   * value.
   *
   * @param schema The schema in which this matching rule is defined.
   * @param  subInitial      The normalized substring value fragment
   *                         that should appear at the beginning of
   *                         the target value.
   * @param  subAnyElements  The normalized substring value fragments
   *                         that should appear in the middle of the
   *                         target value.
   * @param  subFinal        The normalized substring value fragment
   *                         that should appear at the end of the
   *                         target value.
   * @return The normalized version of the provided assertion value.
   * @throws DecodeException if an syntax error occured while parsing the value.
   */
  public Assertion getAssertion(Schema schema, ByteSequence subInitial,
                                List<ByteSequence> subAnyElements,
                                ByteSequence subFinal)
      throws DecodeException;

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
   * @throws DecodeException if an syntax error occured while parsing the value.
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
   * @throws DecodeException if an syntax error occured while parsing the value.
   */
  public Assertion getLessOrEqualAssertion(Schema schema, ByteSequence value)
      throws DecodeException;
}
