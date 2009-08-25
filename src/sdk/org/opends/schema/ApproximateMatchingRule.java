package org.opends.schema;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.opends.server.types.ByteSequence;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 4, 2009
 * Time: 1:48:11 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ApproximateMatchingRule extends MatchingRule
{
  protected ApproximateMatchingRule(String oid, List<String> names,
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
   * Indicates whether the two provided normalized values are
   * approximately equal to each other.
   *
   * @param  attributeValue  The normalized form of the first value to
   *                 compare.
   * @param  assertionValue  The normalized form of the second value to
   *                 compare.
   *
   * @return  {@code true} if the provided values are approximately
   *          equal, or {@code false} if not.
   */
  public abstract boolean approximatelyMatch(ByteSequence attributeValue,
                                   ByteSequence assertionValue);
}
