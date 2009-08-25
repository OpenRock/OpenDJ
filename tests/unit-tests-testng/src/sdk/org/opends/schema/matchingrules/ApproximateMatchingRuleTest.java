package org.opends.schema.matchingrules;

import org.opends.schema.SchemaTestCase;
import org.opends.schema.ApproximateMatchingRule;
import org.opends.schema.CoreSchema;
import org.opends.schema.MatchingRule;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteSequence;
import static org.opends.server.schema.SchemaConstants.AMR_DOUBLE_METAPHONE_NAME;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 24, 2009
 * Time: 11:37:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class ApproximateMatchingRuleTest extends SchemaTestCase
{
  MatchingRule metaphone =
      CoreSchema.instance().getMatchingRule(AMR_DOUBLE_METAPHONE_NAME);
  /**
   * Build the data for the approximateMatchingRules test.
   */
  @DataProvider(name="approximatematchingrules")
  public Object[][] createapproximateMatchingRuleTest()
  {
    // fill this table with tables containing :
    // - the name of the approxiamtematchingrule to test
    // - 2 values that must be tested for matching
    // - a boolean indicating if the values match or not
    return new Object[][] {
        {metaphone, "celebre", "selebre", true},
        {metaphone, "cygale", "sigale", true},
        {metaphone, "cigale", "sigale", true},
        {metaphone, "accacia", "akacia", true},
        {metaphone, "cigale", "sigale", true},
        {metaphone, "bertucci", "bertuchi", true},
        {metaphone, "manger", "manjer", true},
        {metaphone, "gyei", "kei", true},
        {metaphone, "agnostique", "aknostic", true},
        {metaphone, "ghang", "kang", true},
        {metaphone, "affiche", "afiche", true},
        {metaphone, "succeed", "sukid", true},
        {metaphone, "McCarthur", "macarthur", true},
        {metaphone, "czet", "set", true},
        {metaphone, "re\u00C7u", "ressu", true},
        {metaphone, "ni\u00D1o", "nino", true},
        {metaphone, "bateaux", "bateau", true},
        {metaphone, "witz", "wits", true},
        {metaphone, "barre", "bare", true},
        {metaphone, "write", "rite", true},
        {metaphone, "the", "ze", false},
        {metaphone, "motion", "mochion", true},
        {metaphone, "bois", "boi", true},
        {metaphone, "schi", "chi", true},
        {metaphone, "escalier", "eskalier",true},
        {metaphone, "science", "sience", true},
        {metaphone, "school", "skool", true},
        {metaphone, "swap", "sap", true},
        {metaphone, "szize", "size", true},
        {metaphone, "shoek", "choek", false},
        {metaphone, "sugar", "chugar", true},
        {metaphone, "isle", "ile", true},
        {metaphone, "yle", "ysle", true},
        {metaphone, "focaccia", "focashia", true},
        {metaphone, "machine", "mashine", true},
        {metaphone, "michael", "mikael", true},
        {metaphone, "abba", "aba", true},
        {metaphone, "caesar", "saesar", true},
        {metaphone, "femme", "fame", true},
        {metaphone, "panne", "pane", true},
        {metaphone, "josa", "josa", true},
        {metaphone, "jose", "hose", true},
        {metaphone, "hello", "hello", true},
        {metaphone, "hello", "ello", false},
        {metaphone, "bag", "bak", true},
        {metaphone, "bagg", "bag", true},
        {metaphone, "tagliaro", "takliaro", true},
        {metaphone, "biaggi", "biaji", true},
        {metaphone, "bioggi", "bioji", true},
        {metaphone, "rough", "rouf", true},
        {metaphone, "ghislane", "jislane", true},
        {metaphone, "ghaslane", "kaslane", true},
        {metaphone, "odd", "ot", true},
        {metaphone, "edgar", "etkar", true},
        {metaphone, "edge", "eje", true},
        {metaphone, "accord", "akord", true},
        {metaphone, "noize", "noise", true},
        {metaphone, "orchid", "orkid", true},
        {metaphone, "chemistry", "kemistry", true},
        {metaphone, "chianti", "kianti", true},
        {metaphone, "bacher", "baker", true},
        {metaphone, "achtung", "aktung", true},
        {metaphone, "Writing", "riting", true},
        {metaphone, "xeon", "zeon", true},
        {metaphone, "lonely", "loneli", true},
        {metaphone, "bellaton", "belatton", true},
        {metaphone, "pate", "patte", true},
        {metaphone, "voiture", "vouatur", true},
        {metaphone, "garbage", "garbedge", true},
        {metaphone, "algorithme", "algorizm", true},
        {metaphone, "testing", "testng", true},
        {metaphone, "announce", "annonce", true},
        {metaphone, "automaticly", "automatically", true},
        {metaphone, "modifyd", "modified", true},
        {metaphone, "bouteille", "butaille", true},
        {metaphone, "xeon", "zeon", true},
        {metaphone, "achtung", "aktung", true},
        {metaphone, "throttle", "throddle", true},
        {metaphone, "thimble", "thimblle", true},
        {metaphone, "", "", true},
    };
  }

  /**
   * Test the normalization and the approximate comparison.
   */
  @Test(dataProvider= "approximatematchingrules")
  public void approximateMatchingRules(ApproximateMatchingRule rule,
                                       String value1, String value2,
                                       Boolean result)
      throws Exception
  {
    // normalize the 2 provided values
    ByteSequence normalizedValue1 =
      rule.normalizeAttributeValue(ByteString.valueOf(value1));
    ByteSequence normalizedValue2 =
      rule.normalizeAssertionValue(ByteString.valueOf(value2));

    // check that the approximatelyMatch return the expected result.
    Boolean liveResult = rule.approximatelyMatch(normalizedValue1,
        normalizedValue2);
    assertEquals(result, liveResult);
  } 
}
