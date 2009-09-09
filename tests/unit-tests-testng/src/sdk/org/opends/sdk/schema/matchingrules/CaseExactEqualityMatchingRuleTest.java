package org.opends.sdk.schema.matchingrules;

import org.testng.annotations.DataProvider;
import org.opends.sdk.ConditionResult;
import org.opends.sdk.schema.CoreSchema;
import org.opends.sdk.schema.MatchingRule;

import static org.opends.server.schema.SchemaConstants.EMR_CASE_EXACT_OID;

/**
 * Test the CaseExactEqualityMatchingRule.
 */
public class CaseExactEqualityMatchingRuleTest extends MatchingRuleTest
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
        {"12345678", "12345678", ConditionResult.TRUE},
        {"12345678\u2163", "12345678\u2163", ConditionResult.TRUE},
        {"ABC45678", "ABC45678", ConditionResult.TRUE},
        {"  ABC45678  ", "ABC45678", ConditionResult.TRUE},
        {"ABC   45678", "ABC 45678", ConditionResult.TRUE},
        {"   ", " ", ConditionResult.TRUE},
        {"", "", ConditionResult.TRUE},
        {"ABC45678", "abc45678", ConditionResult.FALSE},
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected MatchingRule getRule()
  {
    return CoreSchema.instance().getMatchingRule(EMR_CASE_EXACT_OID);
  }

}
