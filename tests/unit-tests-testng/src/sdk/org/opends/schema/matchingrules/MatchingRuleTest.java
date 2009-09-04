package org.opends.schema.matchingrules;

import org.opends.schema.SchemaTestCase;
import org.opends.schema.MatchingRule;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.DirectoryException;
import org.opends.server.core.DirectoryServer;
import org.opends.types.ConditionResult;
import org.opends.types.Assertion;
import org.opends.ldap.DecodeException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Test The equality matching rules and the equality matching rule api.
 */
public abstract class MatchingRuleTest extends SchemaTestCase
{
   /**
   * Generate data for the Matching Rule test.
   *
   * @return the data for the equality matching rule test.
   */
  @DataProvider(name="matchingrules")
  public abstract Object[][] createMatchingRuleTest();

  /**
   * Generate invalid attribute values for the Matching Rule test.
   *
   * @return the data for the EqualityMatchingRulesInvalidValuestest.
   */
  @DataProvider(name="matchingRuleInvalidAttributeValues")
  public abstract Object[][] createMatchingRuleInvalidAttributeValues();

  /**
   * Generate invalid assertion values for the Matching Rule test.
   *
   * @return the data for the EqualityMatchingRulesInvalidValuestest.
   */
  @DataProvider(name="matchingRuleInvalidAssertionValues")
  public Object[][] createMatchingRuleInvalidAssertionValues()
  {
    return createMatchingRuleInvalidAttributeValues();
  }


  /**
   * Get an instance of the matching rule.
   *
   * @return An instance of the matching rule to test.
   */
  protected abstract MatchingRule getRule();

  /**
   * Test the normalization and the comparison of valid values.
   */
  @Test(dataProvider= "matchingrules")
  public void matchingRules(String value1, String value2,
                                    ConditionResult result) throws Exception
  {
    MatchingRule rule = getRule();

    // normalize the 2 provided values and check that they are equals
    ByteString normalizedValue1 =
      rule.normalizeAttributeValue(ByteString.valueOf(value1));
    Assertion assertion =
      rule.getAssertion(ByteString.valueOf(value2));

    ConditionResult liveResult = assertion.matches(normalizedValue1);
    assertEquals(result, liveResult);
  }

  /**
   * Test that invalid values are rejected.
   */
  @Test(expectedExceptions = DecodeException.class,
      dataProvider= "matchingRuleInvalidAttributeValues")
  public void matchingRulesInvalidAttributeValues(String value) throws Exception
    {
    // Get the instance of the rule to be tested.
    MatchingRule rule = getRule();

    rule.normalizeAttributeValue(ByteString.valueOf(value));
  }

  /**
   * Test that invalid values are rejected.
   */
  @Test(expectedExceptions = DecodeException.class,
      dataProvider= "matchingRuleInvalidAssertionValues")
  public void matchingRulesInvalidAssertionValues(String value) throws Exception
    {
    // Get the instance of the rule to be tested.
    MatchingRule rule = getRule();

    rule.getAssertion(ByteString.valueOf(value));
  }
}
