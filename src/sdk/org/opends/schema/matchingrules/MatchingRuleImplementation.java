package org.opends.schema.matchingrules;

import org.opends.server.types.ByteSequence;
import org.opends.types.ConditionResult;
import org.opends.schema.MatchingRule;
import org.opends.schema.Schema;
import org.opends.schema.Syntax;
import org.opends.schema.syntaxes.SyntaxImplementation;

import java.util.List;
import java.util.Map;

/**
 * This interface defines the set of methods that must be implemented
 * to define a new matching rule.
 */
public abstract class MatchingRuleImplementation extends MatchingRule
{
  protected MatchingRuleImplementation(String oid, List<String> names,
                                       String description, boolean obsolete,
                                       String syntax, Map<String,
                                       List<String>> extraProperties)
  {
    super(oid, names, description, obsolete, syntax, extraProperties);
  }

  protected MatchingRuleImplementation(MatchingRule orginalMatchingRule) {
    super(orginalMatchingRule);
  }

  /**
   * Indicates whether the provided attribute value should be
   * considered a match for the given assertion value. The assertion value is
   * guarenteed to be valid against this matching rule's assertion syntax.
   *
   * @param schema The schema to use to lookup schema elements if needed.
   * @param attributeValue The attribute value.
   * @param assertionValue The schema checked assertion value.
   * @return {@code TRUE} if the attribute value should be considered
   *         a match for the provided assertion value, {@code FALSE}
   *         if it does not match, or {@code UNDEFINED} if the result
   *         is undefined.
   */
  public abstract ConditionResult valuesMatch(
      Schema schema, ByteSequence attributeValue,
      ByteSequence assertionValue);
}
