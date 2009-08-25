package org.opends.schema;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.opends.server.types.ByteSequence;

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
  public abstract ByteSequence normalizeAssertionValue(ByteSequence value);

  /**
   * Compares the attribute value to the assertion value and returns a value
   * that indicates their relative order.
   *
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
  public abstract int compareValues(ByteSequence attributeValue,
                                    ByteSequence assertionValue);
}
