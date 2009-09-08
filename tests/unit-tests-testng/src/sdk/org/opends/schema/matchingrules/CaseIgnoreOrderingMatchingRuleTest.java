package org.opends.schema.matchingrules;

import org.opends.schema.OrderingMatchingRule;
import org.opends.schema.CoreSchema;
import static org.opends.server.schema.SchemaConstants.OMR_CASE_IGNORE_OID;
import org.testng.annotations.DataProvider;

/**
 * Test the CaseIgnoreOrderingMatchingRule.
 */
public class CaseIgnoreOrderingMatchingRuleTest extends
    OrderingMatchingRuleTest
{

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="OrderingMatchingRuleInvalidValues")
  public Object[][] createOrderingMatchingRuleInvalidValues()
  {
    return new Object[][] {
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="Orderingmatchingrules")
  public Object[][] createOrderingMatchingRuleTestData()
  {
    return new Object[][] {
        {"12345678", "02345678", 1},
        {"abcdef", "bcdefa", -1},
        {"abcdef", "abcdef", 0},
        {"abcdef", "ABCDEF", 0},
        {"abcdef", "aCcdef", -1},
        {"aCcdef", "abcdef", 1},
        {"foo\u0020bar\u0020\u0020","foo bar",0},
        {"test\u00AD\u200D","test",0},
        {"foo\u070Fbar" ,"foobar",0},
        //Case-folding data below.
        {"foo\u0149bar","foo\u02BC\u006Ebar",0},
        {"foo\u017Bbar", "foo\u017Cbar",0},
        {"foo\u017Bbar", "goo\u017Cbar",-1},
        //issue# 3583
        {"a","\u00f8",-1},
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected OrderingMatchingRule getRule()
  {
    return (OrderingMatchingRule)
        CoreSchema.instance().getMatchingRule(OMR_CASE_IGNORE_OID);
  }
}
