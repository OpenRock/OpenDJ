package org.opends.schema.matchingrules;

import org.opends.schema.SchemaTestCase;
import org.opends.schema.EqualityMatchingRule;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.DirectoryException;
import org.opends.server.core.DirectoryServer;
import org.opends.types.ConditionResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Test The equality matching rules and the equality matching rule api.
 */
public abstract class EqualityMatchingRuleTest extends SchemaTestCase
{
   /**
   * Generate data for the EqualityMatching Rule test.
   *
   * @return the data for the equality matching rule test.
   */
  @DataProvider(name="equalitymatchingrules")
  public abstract Object[][] createEqualityMatchingRuleTest();

  /**
   * Generate invalid data for the EqualityMatching Rule test.
   *
   * @return the data for the EqualityMatchingRulesInvalidValuestest.
   */
  @DataProvider(name="equalityMatchingRuleInvalidValues")
  public abstract Object[][] createEqualityMatchingRuleInvalidValues();


  /**
   * Get an instance of the matching rule.
   *
   * @return An instance of the matching rule to test.
   */
  protected abstract EqualityMatchingRule getRule();

  /**
   * Test the normalization and the comparison of valid values.
   */
  @Test(dataProvider= "equalitymatchingrules")
  public void equalityMatchingRules(String value1,
                             String value2, Boolean result) throws Exception
  {
    EqualityMatchingRule rule = getRule();

    // normalize the 2 provided values and check that they are equals
    ByteSequence normalizedValue1 =
      rule.normalizeAttributeValue(ByteString.valueOf(value1));
    ByteSequence normalizedValue2 =
      rule.normalizeAssertionValue(ByteString.valueOf(value2));

    Boolean liveResult = rule.areEqual(normalizedValue1, normalizedValue2);
    assertEquals(result, liveResult);
  }




  /**
   * Generate data for the EqualityMatching Rule test.
   *
   * @return the data for the equality matching rule test.
   */
  @DataProvider(name="valuesMatch")
  public Object[][] createValuesMatch()
  {
    return new Object[][] {};
  }

  /**
   * Test the valuesMatch method used for extensible filters.
   */
  @Test(dataProvider= "valuesMatch")
  public void testValuesMatch(String value1,
                             String value2, Boolean result) throws Exception
  {
    EqualityMatchingRule rule = getRule();

    ConditionResult liveResult =
      rule.valuesMatch(ByteString.valueOf(value1), ByteString.valueOf(value2));
    if (result)
      assertEquals(ConditionResult.TRUE, liveResult);
    else
      assertEquals(ConditionResult.FALSE, liveResult);

  }
}
