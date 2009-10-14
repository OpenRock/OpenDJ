package org.opends.sdk.schema.matchingrules;

import org.testng.annotations.DataProvider;
import org.opends.sdk.ConditionResult;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.MatchingRule;

import static org.opends.server.schema.SchemaConstants.SMR_CASE_IGNORE_OID;

/**
 * Test the CaseIgnoreSubstringMatchingRule.
 */
public class CaseIgnoreSubstringMatchingRuleTest extends
    SubstringMatchingRuleTest
{

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="substringMiddleMatchData")
  public Object[][] createSubstringMiddleMatchData()
  {
    return new Object[][] {
        {"this is a value", new String[] {"this"}, ConditionResult.TRUE },
        {"this is a value", new String[] {"is"}, ConditionResult.TRUE },
        {"this is a value", new String[] {"a"}, ConditionResult.TRUE },
        {"this is a value", new String[] {"value"}, ConditionResult.TRUE },
        {"this is a value", new String[] {"THIS"}, ConditionResult.TRUE },
        {"this is a value", new String[] {"IS"}, ConditionResult.TRUE },
        {"this is a value", new String[] {"A"}, ConditionResult.TRUE },
        {"this is a value", new String[] {"VALUE"}, ConditionResult.TRUE },
        {"this is a value", new String[] {" "}, ConditionResult.TRUE },
        {"this is a value", new String[] {"this", "is", "a", "value"}, ConditionResult.TRUE },
         // The matching rule requires ordered non overlapping substrings.
         // Issue #730 was not valid.
        {"this is a value", new String[] {"value", "this"}, ConditionResult.FALSE },
        {"this is a value", new String[] {"this", "this is"}, ConditionResult.FALSE },
        {"this is a value", new String[] {"this", "IS", "a", "VALue"}, ConditionResult.TRUE },
        {"this is a value", new String[] {"his IS", "A val",}, ConditionResult.TRUE },
        {"this is a value", new String[] {"not",}, ConditionResult.FALSE },
        {"this is a value", new String[] {"this", "not"}, ConditionResult.FALSE },
        {"this is a value", new String[] {"    "}, ConditionResult.TRUE },
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected MatchingRule getRule()
  {
    return Schema.getCoreSchema().getMatchingRule(SMR_CASE_IGNORE_OID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="substringInitialMatchData")
  public Object[][] createSubstringInitialMatchData()
  {
    return new Object[][] {
        {"this is a value",  "this",  ConditionResult.TRUE },
        {"this is a value",  "th",    ConditionResult.TRUE },
        {"this is a value",  "t",     ConditionResult.TRUE },
        {"this is a value",  "is",    ConditionResult.FALSE },
        {"this is a value",  "a",     ConditionResult.FALSE },
        {"this is a value",  "TH",    ConditionResult.TRUE },
        {"this is a value",  "T",     ConditionResult.TRUE },
        {"this is a value",  "IS",    ConditionResult.FALSE },
        {"this is a value",  "A",     ConditionResult.FALSE },
        {"this is a value",  "VALUE", ConditionResult.FALSE },
        {"this is a value",  " ",     ConditionResult.FALSE },
        {"this is a value",  "NOT",   ConditionResult.FALSE },
        {"this is a value",  "THIS",  ConditionResult.TRUE },
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="substringFinalMatchData")
  public Object[][] createSubstringFinalMatchData()
  {
    return new Object[][] {
        {"this is a value", "value", ConditionResult.TRUE },
        {"this is a value", "alue", ConditionResult.TRUE },
        {"this is a value", "ue", ConditionResult.TRUE },
        {"this is a value", "e", ConditionResult.TRUE },
        {"this is a value", "valu", ConditionResult.FALSE },
        {"this is a value",  "this", ConditionResult.FALSE },
        {"this is a value", "VALUE", ConditionResult.TRUE },
        {"this is a value", "AlUe", ConditionResult.TRUE },
        {"this is a value", "UE", ConditionResult.TRUE },
        {"this is a value", "E", ConditionResult.TRUE },
        {"this is a value", "valu", ConditionResult.FALSE },
        {"this is a value",  "THIS", ConditionResult.FALSE },
        {"this is a value", " ", ConditionResult.FALSE },
        {"this is a VALUE", "value", ConditionResult.TRUE },
        {"end with space    ", " ", ConditionResult.FALSE },
        {"end with space    ", "space", ConditionResult.TRUE },
        {"end with space    ", "SPACE", ConditionResult.TRUE },
    };
  }

  @DataProvider(name="substringInvalidAttributeValues")
  public Object[][] createMatchingRuleInvalidAttributeValues() {
    return new Object[][] {};
  }

  @DataProvider(name="substringInvalidAssertionValues")
  public Object[][] createMatchingRuleInvalidAssertionValues() {
    return new Object[][] {};
  }
}