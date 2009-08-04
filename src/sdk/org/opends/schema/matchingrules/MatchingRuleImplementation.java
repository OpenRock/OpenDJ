package org.opends.schema.matchingrules;

import org.opends.server.types.ByteSequence;
import org.opends.server.util.Validator;
import org.opends.schema.Schema;
import org.opends.types.ConditionResult;

import java.util.SortedSet;
import java.util.Iterator;

/**
 * This interface defines the set of methods that must be implemented
 * to define a new matching rule.
 */
public interface MatchingRuleImplementation
{
  /**
   * Indicates whether the provided attribute value should be
   * considered a match for the given assertion value. The assertion value is
   * guarenteed to be valid against this matching rule's assertion syntax.
   *
   * @param schema The schema in which this matching rule is defined.
   * @param attributeValue The attribute value.
   * @param assertionValue The schema checked assertion value.
   * @return {@code TRUE} if the attribute value should be considered
   *         a match for the provided assertion value, {@code FALSE}
   *         if it does not match, or {@code UNDEFINED} if the result
   *         is undefined.
   */
  public ConditionResult valuesMatch(Schema schema, ByteSequence attributeValue,
                                     ByteSequence assertionValue);
}
