package org.opends.schema.matchingrules;

import org.testng.annotations.DataProvider;
import static org.opends.server.schema.SchemaConstants.EMR_BIT_STRING_OID;
import org.opends.types.ConditionResult;
import org.opends.schema.MatchingRule;
import org.opends.schema.CoreSchema;

/**
 * Test the BitStringEqualityMatchingRule.
 */
public class BitStringEqualityMatchingRuleTest extends MatchingRuleTest
{
    /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="matchingRuleInvalidAttributeValues")
  public Object[][] createMatchingRuleInvalidAttributeValues()
  {
    return new Object[][] {
        {"\'a\'B"},
        {"0"},
        {"010101"},
        {"\'10101"},
        {"\'1010\'A"},
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
        {"\'0\'B", "\'0\'B", ConditionResult.TRUE},
        {"\'1\'B", "\'1\'B", ConditionResult.TRUE},
        {"\'0\'B", "\'1\'B", ConditionResult.FALSE},
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected MatchingRule getRule()
  {
    return CoreSchema.instance().getMatchingRule(EMR_BIT_STRING_OID);
  }
}
