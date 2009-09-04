package org.opends.schema.matchingrules;

import org.testng.annotations.DataProvider;
import org.opends.types.ConditionResult;
import org.opends.schema.CoreSchema;
import org.opends.schema.MatchingRule;
import static org.opends.server.schema.SchemaConstants.EMR_CASE_EXACT_IA5_OID;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Sep 3, 2009
 * Time: 4:08:30 PM
 * To change this template use File | Settings | File Templates.
 */
/**
 * Test the CaseExactIA5EqualityMatchingRule.
 */
public class CaseExactIA5EqualityMatchingRuleTest extends MatchingRuleTest
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
        {"ABC45678", "abc45678", ConditionResult.FALSE},
        {"\u0020foo\u0020bar\u0020\u0020","foo bar",ConditionResult.TRUE},
        {"test\u00AD\u200D","test",ConditionResult.TRUE},
       {"foo\u000Bbar","foo\u0020bar",ConditionResult.TRUE},
       {"foo\u070Fbar" ,"foobar", ConditionResult.TRUE},

    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected MatchingRule getRule()
  {
    return CoreSchema.instance().getMatchingRule(EMR_CASE_EXACT_IA5_OID);
  }

}
