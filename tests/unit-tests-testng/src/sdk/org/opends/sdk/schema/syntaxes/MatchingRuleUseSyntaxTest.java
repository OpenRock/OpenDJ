package org.opends.sdk.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_MATCHING_RULE_USE_OID;

import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.Syntax;
import org.testng.annotations.DataProvider;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 19, 2009
 * Time: 1:41:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class MatchingRuleUseSyntaxTest extends SyntaxTestCase
{
    /**
   * {@inheritDoc}
   */
  @Override
  protected Syntax getRule()
  {
    return Schema.getCoreSchema().getSyntax(SYNTAX_MATCHING_RULE_USE_OID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="acceptableValues")
  public Object[][] createAcceptableValues()
  {
    return new Object [][] {
        {"( 2.5.13.10 NAME 'full matching rule' "
            + " DESC 'description of matching rule' OBSOLETE "
            + " APPLIES 2.5.4.3 "
            + " X-name 'this is an extension' )", true},
        {"( 2.5.13.10 NAME 'missing closing parenthesis' "
            + " DESC 'description of matching rule' "
            + " SYNTAX 2.5.4.3 "
            + " X-name ( 'this is an extension' ) ", false},
    };
  }
}
