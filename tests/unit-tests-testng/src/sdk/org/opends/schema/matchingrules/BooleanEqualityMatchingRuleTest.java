package org.opends.schema.matchingrules;

import org.testng.annotations.DataProvider;
import org.opends.types.ConditionResult;
import org.opends.schema.MatchingRule;
import org.opends.schema.CoreSchema;
import static org.opends.server.schema.SchemaConstants.EMR_BOOLEAN_OID;

/**
 * Test the BooleanEqualityMatchingRule.
 */
public class BooleanEqualityMatchingRuleTest extends MatchingRuleTest
{

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="matchingrules")
  public Object[][] createMatchingRuleTest()
  { 
    return new Object[][] {
        {"TRUE",  "true",  ConditionResult.TRUE},
        {"YES",   "true",  ConditionResult.TRUE},
        {"ON",    "true",  ConditionResult.TRUE},
        {"1",     "true",  ConditionResult.TRUE},
        {"FALSE", "false", ConditionResult.TRUE},
        {"NO",    "false", ConditionResult.TRUE},
        {"OFF",   "false", ConditionResult.TRUE},
        {"0",     "false", ConditionResult.TRUE},
        {"TRUE",  "false", ConditionResult.FALSE},
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="matchingRuleInvalidAttributeValues")
  public Object[][] createMatchingRuleInvalidAttributeValues()
  {
    return new Object[][] {
        {"garbage"},
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected MatchingRule getRule()
  {
    return CoreSchema.instance().getMatchingRule(EMR_BOOLEAN_OID);
  }

}

