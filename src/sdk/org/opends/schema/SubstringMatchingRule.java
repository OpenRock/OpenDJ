package org.opends.schema;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.opends.server.types.ByteSequence;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 4, 2009
 * Time: 1:41:56 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class SubstringMatchingRule extends MatchingRule
{
  protected SubstringMatchingRule(String oid, List<String> names,
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
   * Retrieves the normalized form of the provided initial assertion value
   * substring, which is best suite for efficiently performing matching
   * operations on that value.
   *
   * @param value The initial assertion value substring to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public abstract ByteSequence normalizeSubInitialValue(ByteSequence value);

  /**
   * Retrieves the normalized form of the provided middle assertion value
   * substring, which is best suite for efficiently performing matching
   * operations on that value.
   *
   * @param value The middle assertion value substring to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public abstract ByteSequence normalizeSubAnyValue(ByteSequence value);

  /**
   * Retrieves the normalized form of the provided final assertion value
   * substring, which is best suite for efficiently performing matching
   * operations on that value.
   *
   * @param value The final assertion value substring to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public abstract ByteSequence normalizeSubFinalValue(ByteSequence value);

  /**
   * Determines whether the provided value matches the given substring
   * filter components.  Note that any of the substring filter
   * components may be {@code null} but at least one of them must be
   * non-{@code null}.
   *
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
  public abstract boolean valueMatchesSubstring(ByteSequence attributeValue,
                                                ByteSequence subInitial,
                                              List<ByteSequence> subAnyElements,
                                              ByteSequence subFinal);
}
