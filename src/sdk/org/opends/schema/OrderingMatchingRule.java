package org.opends.schema;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Comparator;

import org.opends.server.types.ByteSequence;
import org.opends.types.Assertion;
import org.opends.ldap.DecodeException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 4, 2009
 * Time: 1:40:42 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class OrderingMatchingRule extends MatchingRule
{
  protected OrderingMatchingRule(String oid, List<String> names,
                                 String description, boolean obsolete,
                                 String syntax,
                                 Map<String, List<String>> extraProperties,
                                 String definition)
  {
    super(oid, names, description, obsolete, syntax, extraProperties,
        definition);
  }

  /**
   * Get a comparator that can be used to compare the attribute values
   * normalized by this matching rule.
   *
   * @return  A comparator that can be used to compare the attribute values
   * normalized by this matching rule.
   */
  public abstract Comparator<ByteSequence> comparator();

  /**
   * Retrieves the normalized form of the provided assertion value, which is
   * best suite for efficiently performing greater than or equal matching
   * operations on that value.
   * The assertion value is guarenteed to be valid against this matching rule's
   * assertion syntax.
   *
   * @param value The syntax checked assertion value to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public abstract Assertion getGreaterOrEqualAssertion(ByteSequence value)
      throws DecodeException;

  /**
   * Retrieves the normalized form of the provided assertion value, which is
   * best suite for efficiently performing greater than or equal matching
   * operations on that value.
   * The assertion value is guarenteed to be valid against this matching rule's
   * assertion syntax.
   *
   * @param value The syntax checked assertion value to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public abstract Assertion getLessOrEqualAssertion(ByteSequence value)
      throws DecodeException;

  /**
   * Retrieves the normalized form of the provided assertion value, which is
   * best suite for efficiently performing matching operations on that value.
   * The assertion evaluates to true if provided attribute value
   * should appear earlier then the assertion value.
   * The assertion value is guarenteed to be valid against this matching rule's
   * assertion syntax.
   *
   * @param value The syntax checked assertion value to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public abstract Assertion getAssertion(ByteSequence value)
      throws DecodeException;
}
