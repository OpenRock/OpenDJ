package org.opends.schema.matchingrules;

import org.opends.types.ConditionResult;
import org.opends.server.types.ByteSequence;
import org.opends.server.util.Validator;
import org.opends.schema.MatchingRule;
import org.opends.schema.Schema;

import java.util.List;

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
    public ByteSequence normalizeAssertionValue(Schema schema, ByteSequence value) {
      return substitute.normalizeAssertionValue(null, value);
    }

    @Override
    public boolean areEqual(Schema schema, ByteSequence attributeValue,
                            ByteSequence assertionValue) {
      return substitute.areEqual(null, attributeValue, assertionValue);
    }

    @Override
    public ConditionResult valuesMatch(Schema schema, ByteSequence attributeValue,
                                       ByteSequence assertionValue) {
      return substitute.valuesMatch(null, attributeValue, assertionValue);
    }

    public ByteSequence normalizeAttributeValue(Schema schema, ByteSequence value) {
      return substitute.normalizeAttributeValue(null, value);
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
    public ByteSequence normalizeAssertionValue(Schema schema, ByteSequence value) {
      return substitute.normalizeAssertionValue(null, value);
    }

    @Override
    public int compareValues(Schema schema, ByteSequence attributeValue,
                             ByteSequence assertionValue) {
      return substitute.compareValues(null, attributeValue, assertionValue);
    }

    @Override
    public ConditionResult valuesMatch(Schema schema, ByteSequence attributeValue,
                                       ByteSequence assertionValue) {
      return substitute.valuesMatch(null, attributeValue, assertionValue);
    }

    public ByteSequence normalizeAttributeValue(Schema schema, ByteSequence value) {
      return substitute.normalizeAttributeValue(null, value);
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
    public ByteSequence normalizeSubInitialValue(Schema schema, ByteSequence value) {
      return substitute.normalizeSubInitialValue(null, value);
    }

    @Override
    public ByteSequence normalizeSubAnyValue(Schema schema, ByteSequence value) {
      return substitute.normalizeSubAnyValue(null, value);
    }

    @Override
    public ByteSequence normalizeSubFinalValue(Schema schema, ByteSequence value) {
      return substitute.normalizeSubFinalValue(null, value);
    }

    @Override
    public boolean valueMatchesSubstring(Schema schema, ByteSequence attributeValue,
                                         ByteSequence subInitial,
                                         List<ByteSequence> subAnyElements,
                                         ByteSequence subFinal) {
      return substitute.valueMatchesSubstring(null, attributeValue, subInitial,
          subAnyElements, subFinal);
    }

    @Override
    public ConditionResult valuesMatch(Schema schema, ByteSequence attributeValue,
                                       ByteSequence assertionValue) {
      return substitute.valuesMatch(null, attributeValue, assertionValue);
    }

    public ByteSequence normalizeAttributeValue(Schema schema, ByteSequence value) {
      return substitute.normalizeAttributeValue(null, value);
    }
  }
}
