package org.opends.sdk.schema.matchingrules;

import org.opends.sdk.Assertion;
import org.opends.sdk.ConditionResult;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.OrderingMatchingRule;
import org.opends.sdk.schema.SchemaTestCase;
import org.opends.server.types.ByteString;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Sep 8, 2009
 * Time: 2:30:58 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class OrderingMatchingRuleTest extends SchemaTestCase
{
   /**
   * Create data for the OrderingMatchingRules test.
   *
   * @return The data for the OrderingMatchingRules test.
   */
  @DataProvider(name="Orderingmatchingrules")
  public abstract Object[][] createOrderingMatchingRuleTestData();


  /**
   * Test the comparison of valid values.
   */
  @Test(dataProvider= "Orderingmatchingrules")
  public void OrderingMatchingRules(String value1,String value2, int result)
         throws Exception
  {
    // Make sure that the specified class can be instantiated as a task.
    OrderingMatchingRule ruleInstance = getRule();

    ByteString normalizedValue1 =
      ruleInstance.normalizeAttributeValue(ByteString.valueOf(value1));
    ByteString normalizedValue2 =
      ruleInstance.normalizeAttributeValue(ByteString.valueOf(value2));

    // Test the comparator
    int comp =
        ruleInstance.comparator().compare(normalizedValue1,normalizedValue2);
    if(comp == 0)
      Assert.assertEquals(comp, result);
    else if(comp > 0)
      Assert.assertTrue(result > 0);
    else if(comp < 0)
      Assert.assertTrue(result < 0);


    Assertion a =
        ruleInstance.getGreaterOrEqualAssertion(ByteString.valueOf(value2));
    Assert.assertEquals(a.matches(normalizedValue1),
        result >= 0 ? ConditionResult.TRUE : ConditionResult.FALSE);

    a = ruleInstance.getLessOrEqualAssertion(ByteString.valueOf(value2));
    Assert.assertEquals(a.matches(normalizedValue1),
        result <= 0 ? ConditionResult.TRUE : ConditionResult.FALSE);
    
    a = ruleInstance.getAssertion(ByteString.valueOf(value2));
    Assert.assertEquals(a.matches(normalizedValue1),
        result < 0 ? ConditionResult.TRUE : ConditionResult.FALSE);
  }

  /**
   * Get the Ordering matching Rules that is to be tested.
   *
   * @return The Ordering matching Rules that is to be tested.
   */
  protected abstract OrderingMatchingRule getRule();


  /**
   * Create data for the OrderingMatchingRulesInvalidValues test.
   *
   * @return The data for the OrderingMatchingRulesInvalidValues test.
   */
  @DataProvider(name="OrderingMatchingRuleInvalidValues")
  public abstract Object[][] createOrderingMatchingRuleInvalidValues();


  /**
   * Test that invalid values are rejected.
   */
  @Test(expectedExceptions=DecodeException.class,
      dataProvider= "OrderingMatchingRuleInvalidValues")
  public void OrderingMatchingRulesInvalidValues(String value) throws Exception
  {
    // Make sure that the specified class can be instantiated as a task.
    OrderingMatchingRule ruleInstance = getRule();

    // normalize the 2 provided values
      ruleInstance.normalizeAttributeValue(ByteString.valueOf(value));
  }
}
