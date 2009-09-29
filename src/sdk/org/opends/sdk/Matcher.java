/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2009 Sun Microsystems, Inc.
 */

package org.opends.sdk;

import org.opends.sdk.schema.MatchingRule;
import org.opends.sdk.schema.MatchingRuleUse;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteSequence;

import java.util.List;
import java.util.ArrayList;


/**
 * An interface for determining whether entries match a {@code Filter}.
 */
public abstract class Matcher
{
  /**
   * Indicates whether this filter {@code Matcher} matches the provided {@code
   * Entry}.
   *
   * @param entry The entry to be matched.
   *
   * @return {@code true} if this filter {@code Matcher} matches the provided
   *         {@code Entry}.
   */
  public abstract ConditionResult matches(Entry entry);

  private static class AndMatcher extends Matcher
  {
    private List<Matcher> subMatchers;

    private AndMatcher(List<Matcher> subMatchers)
    {
      this.subMatchers = subMatchers;
    }

    public ConditionResult matches(Entry entry)
    {
      ConditionResult r = ConditionResult.TRUE;
      for (Matcher m : subMatchers)
      {
        switch (m.matches(entry))
        {
          case FALSE:
            return ConditionResult.FALSE;
          case UNDEFINED:
            r = ConditionResult.UNDEFINED;
        }
      }
      return r;
    }
  }

  private static class AssertionMatcher extends Matcher
  {
    private AttributeDescription attribute;
    private MatchingRule rule;
    private MatchingRuleUse ruleUse;
    private Assertion assertion;
    private boolean dnAttributes;

    private AssertionMatcher(AttributeDescription attribute,
                             MatchingRule rule, MatchingRuleUse ruleUse,
                             Assertion assertion, boolean dnAttributes)
    {
      this.attribute = attribute;
      this.rule = rule;
      this.ruleUse = ruleUse;
      this.assertion = assertion;
      this.dnAttributes = dnAttributes;
    }

    public ConditionResult matches(Entry entry)
    {
      ConditionResult r = ConditionResult.FALSE;
      if (attribute != null)
      {
        // If the matchingRule field is absent, the type field will be
        // present and the default equality matching rule is used,
        // and an equality match is performed for that type.

        // If the type field is present and the matchingRule is present, the
        // matchValue is compared against the specified attribute type and its
        // subtypes.
        switch (Matcher.matches(entry.getAttribute(attribute), rule, assertion))
        {
          case TRUE:
            return ConditionResult.TRUE;
          case UNDEFINED:
            r = ConditionResult.UNDEFINED;
        }
      }
      else
      {
        // If the type field is absent and the matchingRule is present, the
        // matchValue is compared against all attributes in an entry that
        // support that matchingRule.
        for (Attribute a : entry.getAttributes())
        {
          if (ruleUse.hasAttribute(
              a.getAttributeDescription().getAttributeType()))
          {
            switch (Matcher.matches(a, rule, assertion))
            {
              case TRUE:
                return ConditionResult.TRUE;
              case UNDEFINED:
                r = ConditionResult.UNDEFINED;
            }
          }
        }
      }

      if (dnAttributes)
      {
        // If the dnAttributes field is set to TRUE, the match is additionally
        // applied against all the AttributeValueAssertions in an entry's
        // distinguished name, and it evaluates to TRUE if there is at least
        // one attribute or subtype in the distinguished name for which the
        // filter item evaluates to TRUE. 
        DN dn = entry.getNameDN();
        for (RDN rdn : dn)
        {
          for (RDN.AttributeTypeAndValue ava : rdn)
          {
            if (ruleUse.hasAttribute(ava.attributeType()))
            {
              switch (Matcher.matches(ava.attributeValue(), rule, assertion))
              {
                case TRUE:
                  return ConditionResult.TRUE;
                case UNDEFINED:
                  r = ConditionResult.UNDEFINED;
              }
            }
          }
        }
      }
      return r;
    }
  }

  private static class GreaterOrEqualMatcher extends Matcher
  {
    private AttributeDescription attribute;
    private MatchingRule rule;
    private Assertion assertion;

    private GreaterOrEqualMatcher(AttributeDescription attribute,
                                  MatchingRule rule, Assertion assertion)
    {
      this.attribute = attribute;
      this.rule = rule;
      this.assertion = assertion;
    }

    public ConditionResult matches(Entry entry)
    {
      switch (Matcher.matches(entry.getAttribute(attribute), rule, assertion))
      {
        case TRUE:
          return ConditionResult.FALSE;
        case FALSE:
          return ConditionResult.TRUE;
        default:
          return ConditionResult.UNDEFINED;
      }
    }
  }

  private static class LessOrEqualMatcher extends Matcher
  {
    private AttributeDescription attribute;
    private MatchingRule equalityRule;
    private MatchingRule orderingRule;
    private Assertion equalityAssertion;
    private Assertion orderingAssertion;

    private LessOrEqualMatcher(AttributeDescription attribute,
                               MatchingRule equalityRule,
                               MatchingRule orderingRule,
                               Assertion equalityAssertion,
                               Assertion orderingAssertion)
    {
      this.attribute = attribute;
      this.equalityRule = equalityRule;
      this.orderingRule = orderingRule;
      this.equalityAssertion = equalityAssertion;
      this.orderingAssertion = orderingAssertion;
    }

    public ConditionResult matches(Entry entry)
    {
      ConditionResult or = ConditionResult.UNDEFINED;
      if (orderingRule != null)
      {
        or = Matcher.matches(entry.getAttribute(attribute),
                             orderingRule, orderingAssertion);
        if (or == ConditionResult.TRUE)
        {
          return ConditionResult.TRUE;
        }
      }

      if (equalityRule != null)
      {
        if (Matcher.matches(entry.getAttribute(attribute), equalityRule,
                            equalityAssertion) == ConditionResult.TRUE)
        {
          return ConditionResult.TRUE;
        }
      }

      return or;
    }
  }

  private static class NotMatcher extends Matcher
  {
    private Matcher subFilter;

    private NotMatcher(Matcher subFilter)
    {
      this.subFilter = subFilter;
    }

    public ConditionResult matches(Entry entry)
    {
      switch (subFilter.matches(entry))
      {
        case TRUE:
          return ConditionResult.FALSE;
        case FALSE:
          return ConditionResult.TRUE;
        default:
          return ConditionResult.UNDEFINED;
      }
    }
  }

  private static class OrMatcher extends Matcher
  {
    private List<Matcher> subMatchers;

    private OrMatcher(List<Matcher> subMatchers)
    {
      this.subMatchers = subMatchers;
    }

    public ConditionResult matches(Entry entry)
    {
      ConditionResult r = ConditionResult.FALSE;
      for (Matcher m : subMatchers)
      {
        switch (m.matches(entry))
        {
          case TRUE:
            return ConditionResult.TRUE;
          case UNDEFINED:
            r = ConditionResult.UNDEFINED;
        }
      }
      return r;
    }
  }

  private static class PresentMatcher extends Matcher
  {
    private AttributeDescription attribute;

    private PresentMatcher(AttributeDescription attribute)
    {
      this.attribute = attribute;
    }

    public ConditionResult matches(Entry entry)
    {
      return entry.getAttribute(attribute) == null ?
             ConditionResult.FALSE : ConditionResult.TRUE;
    }
  }

  private static final UndefinedMatcher UNDEFINED = new UndefinedMatcher();

  private static class UndefinedMatcher extends Matcher
  {
    public ConditionResult matches(Entry entry)
    {
      return ConditionResult.UNDEFINED;
    }
  }

  private static ConditionResult matches(Attribute a, MatchingRule rule,
                                         Assertion assertion)
  {

    ConditionResult r = ConditionResult.FALSE;
    if (a != null)
    {
      for (ByteString v : a)
      {
        switch (matches(v, rule, assertion))
        {
          case TRUE:
            return ConditionResult.TRUE;
          case UNDEFINED:
            r = ConditionResult.UNDEFINED;
        }
      }
    }
    return r;
  }

  private static ConditionResult matches(ByteString v, MatchingRule rule,
                                         Assertion assertion)
  {
    try
    {
      ByteString normalizedValue = rule.normalizeAttributeValue(v);
      return assertion.matches(normalizedValue);
    }
    catch (DecodeException de)
    {
      // TODO: Debug logging?
      return ConditionResult.UNDEFINED;
    }
  }

  static FilterVisitor<Matcher, Schema> getVisitor()
  {
    return VISITOR;
  }

  private static final FilterVisitor<Matcher, Schema> VISITOR =
      new FilterVisitor<Matcher, Schema>()
      {
        public Matcher visitAndFilter(Schema schema, List<Filter> subFilters)
        {
          List<Matcher> subMatchers = new ArrayList<Matcher>(subFilters.size());
          for (Filter f : subFilters)
          {
            subMatchers.add(f.accept(this, schema));
          }
          return new AndMatcher(subMatchers);
        }

        public Matcher visitApproxMatchFilter(Schema schema,
                                              String attributeDescription,
                                              ByteSequence assertionValue)
        {
          AttributeDescription ad;
          MatchingRule rule;
          Assertion assertion;

          try
          {
            ad = AttributeDescription.valueOf(attributeDescription, schema);
          }
          catch (LocalizedIllegalArgumentException e)
          {
            return UNDEFINED;
          }

          if ((rule = ad.getAttributeType().getApproximateMatchingRule()) == null)
          {
            return UNDEFINED;
          }

          try
          {
            assertion = rule.getAssertion(assertionValue);
          }
          catch (DecodeException de)
          {
            return UNDEFINED;
          }
          return new AssertionMatcher(ad, rule, null, assertion, false);
        }

        public Matcher visitEqualityMatchFilter(Schema schema,
                                                String attributeDescription,
                                                ByteSequence assertionValue)
        {
          AttributeDescription ad;
          MatchingRule rule;
          Assertion assertion;

          try
          {
            ad = AttributeDescription.valueOf(attributeDescription, schema);
          }
          catch (LocalizedIllegalArgumentException e)
          {
            return UNDEFINED;
          }

          if ((rule = ad.getAttributeType().getEqualityMatchingRule()) == null)
          {
            return UNDEFINED;
          }

          try
          {
            assertion = rule.getAssertion(assertionValue);
          }
          catch (DecodeException de)
          {
            return UNDEFINED;
          }
          return new AssertionMatcher(ad, rule, null, assertion, false);
        }

        public Matcher visitExtensibleMatchFilter(Schema schema,
                                                  String matchingRule,
                                                  String attributeDescription,
                                                  ByteSequence assertionValue,
                                                  boolean dnAttributes)
        {
          AttributeDescription ad = null;
          MatchingRule rule = null;
          MatchingRuleUse ruleUse = null;
          Assertion assertion;

          if (matchingRule != null)
          {
            if ((rule = schema.getMatchingRule(matchingRule)) == null)
            {
              return UNDEFINED;
            }
          }

          if (attributeDescription != null)
          {
            try
            {
              ad = AttributeDescription.valueOf(attributeDescription, schema);
            }
            catch (LocalizedIllegalArgumentException e)
            {
              return UNDEFINED;
            }

            if (rule == null)
            {
              if ((rule = ad.getAttributeType().getEqualityMatchingRule()) == null)
              {
                return UNDEFINED;
              }
            }
            else
            {
              ruleUse = schema.getMatchingRuleUse(rule);
              if (ruleUse != null && !ruleUse.hasAttribute(ad.getAttributeType()))
              {
                // TODO: What if ruleUse is null?
                return UNDEFINED;
              }
            }
          }
          else
          {
            ruleUse = schema.getMatchingRuleUse(rule);
            // TODO: What if ruleUse is null?
          }

          try
          {
            assertion = rule.getAssertion(assertionValue);
          }
          catch (DecodeException de)
          {
            return UNDEFINED;
          }
          return new AssertionMatcher(ad, rule, ruleUse, assertion, dnAttributes);
        }

        public Matcher visitGreaterOrEqualFilter(Schema schema,
                                                 String attributeDescription,
                                                 ByteSequence assertionValue)
        {
          AttributeDescription ad;
          MatchingRule rule;
          Assertion assertion;

          try
          {
            ad = AttributeDescription.valueOf(attributeDescription, schema);
          }
          catch (LocalizedIllegalArgumentException e)
          {
            return UNDEFINED;
          }

          if ((rule = ad.getAttributeType().getOrderingMatchingRule()) == null)
          {
            return UNDEFINED;
          }

          try
          {
            assertion = rule.getAssertion(assertionValue);
          }
          catch (DecodeException de)
          {
            return UNDEFINED;
          }
          return new GreaterOrEqualMatcher(ad, rule, assertion);
        }

        public Matcher visitLessOrEqualFilter(Schema schema,
                                              String attributeDescription,
                                              ByteSequence assertionValue)
        {
          AttributeDescription ad;
          MatchingRule orderingRule;
          Assertion orderingAssertion = null;
          MatchingRule equalityRule = null;
          Assertion equalityAssertion = null;

          try
          {
            ad = AttributeDescription.valueOf(attributeDescription, schema);
          }
          catch (LocalizedIllegalArgumentException e)
          {
            return UNDEFINED;
          }

          if ((orderingRule =
              ad.getAttributeType().getOrderingMatchingRule()) == null &&
              (equalityRule =
                  ad.getAttributeType().getEqualityMatchingRule()) == null)
          {
            return UNDEFINED;
          }

          try
          {
            if (orderingRule != null)
            {
              orderingAssertion = orderingRule.getAssertion(assertionValue);
            }
            if (equalityRule != null)
            {
              equalityAssertion = equalityRule.getAssertion(assertionValue);
            }
          }
          catch (DecodeException de)
          {
            return UNDEFINED;
          }
          return new LessOrEqualMatcher(ad, orderingRule, equalityRule,
                                        orderingAssertion, equalityAssertion);
        }

        public Matcher visitNotFilter(Schema schema, Filter subFilter)
        {
          Matcher subMatcher = subFilter.accept(this, schema);
          return new NotMatcher(subMatcher);
        }

        public Matcher visitOrFilter(Schema schema, List<Filter> subFilters)
        {
          List<Matcher> subMatchers = new ArrayList<Matcher>(subFilters.size());
          for (Filter f : subFilters)
          {
            subMatchers.add(f.accept(this, schema));
          }
          return new OrMatcher(subMatchers);
        }

        public Matcher visitPresentFilter(Schema schema,
                                          String attributeDescription)
        {
          AttributeDescription ad;
          try
          {
            ad = AttributeDescription.valueOf(attributeDescription, schema);
          }
          catch (LocalizedIllegalArgumentException e)
          {
            return UNDEFINED;
          }

          return new PresentMatcher(ad);
        }

        public Matcher visitSubstringsFilter(Schema schema,
                                             String attributeDescription,
                                             ByteSequence initialSubstring,
                                             List<ByteSequence> anySubstrings,
                                             ByteSequence finalSubstring)
        {
          AttributeDescription ad;
          MatchingRule rule;
          Assertion assertion;

          try
          {
            ad = AttributeDescription.valueOf(attributeDescription, schema);
          }
          catch (LocalizedIllegalArgumentException e)
          {
            return UNDEFINED;
          }

          if ((rule = ad.getAttributeType().getSubstringMatchingRule()) == null)
          {
            return UNDEFINED;
          }

          try
          {
            assertion = rule.getAssertion(initialSubstring, anySubstrings,
                                          finalSubstring);
          }
          catch (DecodeException de)
          {
            return UNDEFINED;
          }
          return new AssertionMatcher(ad, rule, null, assertion, false);
        }

        public Matcher visitUnrecognizedFilter(Schema schema, byte filterTag,
                                               ByteSequence filterBytes)
        {
          return UNDEFINED;
        }
      };
}
