package org.opends.sdk.schema.matchingrules;

import org.testng.annotations.DataProvider;
import org.opends.sdk.ConditionResult;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.MatchingRule;

import static org.opends.server.schema.SchemaConstants.EMR_CASE_IGNORE_OID;

/**
 * Test the CaseIgnoreEqualityMatchingRule.
 */
public class CaseIgnoreEqualityMatchingRuleTest extends
    MatchingRuleTest
{
  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="matchingRuleInvalidAttributeValues")
  public Object[][] createMatchingRuleInvalidAttributeValues()
  {
    return new Object[][] { };
  }


  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="matchingrules")
  public Object[][] createMatchingRuleTest()
  {
    return new Object[][] {
        {" string ", "string", ConditionResult.TRUE},
        {"string ",  "string", ConditionResult.TRUE},
        {" string",  "string", ConditionResult.TRUE},
        {"    ",     " ",      ConditionResult.TRUE},
        {"Z",        "z",      ConditionResult.TRUE},
        {"ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890",
         "abcdefghijklmnopqrstuvwxyz1234567890", ConditionResult.TRUE},
         {"foo\u0020bar\u0020\u0020","foo bar",ConditionResult.TRUE},
         {"test\u00AD\u200D","test",ConditionResult.TRUE},
         {"foo\u070Fbar" ,"foobar",ConditionResult.TRUE},
          //Case-folding data below.
          {"foo\u0149bar","foo\u02BC\u006Ebar",ConditionResult.TRUE},
          {"foo\u017Bbar", "foo\u017Cbar", ConditionResult.TRUE},

    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected MatchingRule getRule()
  {
    return Schema.getCoreSchema().getMatchingRule(EMR_CASE_IGNORE_OID);
  }
}

