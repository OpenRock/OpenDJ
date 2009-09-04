package org.opends.schema.matchingrules;

import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.types.ConditionResult;
import org.opends.types.Assertion;
import org.opends.ldap.DecodeException;

import java.util.Comparator;

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
   */
  public Assertion getAssertion(Schema schema, ByteSequence value)
      throws DecodeException;
}
