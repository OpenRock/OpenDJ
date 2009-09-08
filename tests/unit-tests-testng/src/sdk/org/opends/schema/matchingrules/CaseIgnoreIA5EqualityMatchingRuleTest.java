package org.opends.schema.matchingrules;

import org.testng.annotations.DataProvider;
import org.opends.schema.CoreSchema;
import org.opends.schema.MatchingRule;
import static org.opends.server.schema.SchemaConstants.EMR_CASE_IGNORE_IA5_OID;
import org.opends.types.ConditionResult;

/**
 * Test the CaseExactIA5EqualityMatchingRule.
 */
public class CaseIgnoreIA5EqualityMatchingRuleTest extends
    MatchingRuleTest
{

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="matchingRuleInvalidAttributeValues")
  public Object[][] createMatchingRuleInvalidAttributeValues()
  {
    return new Object[][] {
        {"12345678\uFFFD"},
    };
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
        {"ABC45678", "ABC45678", ConditionResult.TRUE},
        {"ABC45678", "abc45678", ConditionResult.TRUE},
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected MatchingRule getRule()
  {
    return CoreSchema.instance().getMatchingRule(EMR_CASE_IGNORE_IA5_OID);
  }

}
