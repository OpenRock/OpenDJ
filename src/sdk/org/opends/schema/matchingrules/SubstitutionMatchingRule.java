package org.opends.schema.matchingrules;

import org.opends.types.ConditionResult;
import org.opends.server.types.ByteSequence;
import org.opends.server.util.Validator;
import org.opends.schema.MatchingRule;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 21, 2009
 * Time: 5:41:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class SubstitutionMatchingRule
{
  public static class Equality extends EqualityMatchingRuleImplementation
  {
    EqualityMatchingRuleImplementation substitute;

    public Equality(MatchingRule matchingRule,
                       EqualityMatchingRuleImplementation substitute)
    {
      super(matchingRule);
      Validator.ensureNotNull(substitute);
      this.substitute = substitute;
    }

    @Override
    public ByteSequence normalizeAssertionValue(ByteSequence value) {
      return substitute.normalizeAssertionValue(value);
    }

    @Override
    public boolean areEqual(ByteSequence attributeValue,
                            ByteSequence assertionValue) {
      return substitute.areEqual(attributeValue, assertionValue);
    }

    @Override
    public ConditionResult valuesMatch(ByteSequence attributeValue,
                                       ByteSequence assertionValue) {
      return substitute.valuesMatch(attributeValue, assertionValue);
    }

    public ByteSequence normalizeAttributeValue(ByteSequence value) {
      return substitute.normalizeAttributeValue(value);
    }
  }

  public static class Ordering extends OrderingMatchingRuleImplementation
  {
    OrderingMatchingRuleImplementation substitute;

    public Ordering(MatchingRule matchingRule,
                       OrderingMatchingRuleImplementation substitute)
    {
      super(matchingRule);
      Validator.ensureNotNull(substitute);
      this.substitute = substitute;
    }

    @Override
    public ByteSequence normalizeAssertionValue(ByteSequence value) {
      return substitute.normalizeAssertionValue(value);
    }

    @Override
    public int compareValues(ByteSequence attributeValue,
                             ByteSequence assertionValue) {
      return substitute.compareValues(attributeValue, assertionValue);
    }

    @Override
    public ConditionResult valuesMatch(ByteSequence attributeValue,
                                       ByteSequence assertionValue) {
      return substitute.valuesMatch(attributeValue, assertionValue);
    }

    public ByteSequence normalizeAttributeValue(ByteSequence value) {
      return substitute.normalizeAttributeValue(value);
    }
  }

  public static class Substring extends SubstringMatchingRuleImplementation
  {
    SubstringMatchingRuleImplementation substitute;

    public Substring(MatchingRule matchingRule, 
                       SubstringMatchingRuleImplementation substitute)
    {
      super(matchingRule);
      Validator.ensureNotNull(substitute);
      this.substitute = substitute;
    }

    @Override
    public ByteSequence normalizeSubInitialValue(ByteSequence value) {
      return substitute.normalizeSubInitialValue(value);
    }

    @Override
    public ByteSequence normalizeSubAnyValue(ByteSequence value) {
      return substitute.normalizeSubAnyValue(value);
    }

    @Override
    public ByteSequence normalizeSubFinalValue(ByteSequence value) {
      return substitute.normalizeSubFinalValue(value);
    }

    @Override
    public boolean valueMatchesSubstring(ByteSequence value,
                                         ByteSequence subInitial,
                                         List<ByteSequence> subAnyElements,
                                         ByteSequence subFinal) {
      return substitute.valueMatchesSubstring(value, subInitial,
          subAnyElements, subFinal);
    }

    @Override
    public ConditionResult valuesMatch(ByteSequence attributeValue,
                                       ByteSequence assertionValue) {
      return substitute.valuesMatch(attributeValue, assertionValue);
    }

    public ByteSequence normalizeAttributeValue(ByteSequence value) {
      return substitute.normalizeAttributeValue(value);
    }
  }
}
