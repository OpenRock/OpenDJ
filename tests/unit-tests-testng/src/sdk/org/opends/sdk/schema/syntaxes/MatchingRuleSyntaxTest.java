package org.opends.sdk.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_MATCHING_RULE_OID;

import org.opends.sdk.schema.CoreSchema;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.Syntax;
import org.testng.annotations.DataProvider;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 19, 2009
 * Time: 1:40:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class MatchingRuleSyntaxTest extends SyntaxTestCase
{
  /**
   * {@inheritDoc}
   */
  @Override
  protected Syntax getRule()
  {
    return CoreSchema.instance().getSyntax(SYNTAX_MATCHING_RULE_OID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="acceptableValues")
  public Object[][] createAcceptableValues()
  {
    return new Object [][] {
          {"( 1.2.3.4 NAME 'full matching rule' "
              + " DESC 'description of matching rule' OBSOLETE "
              + " SYNTAX 1.3.6.1.4.1.1466.115.121.1.17 "
              + " X-name ( 'this is an extension' ) )", true},
          {"( 1.2.3.4 NAME 'missing closing parenthesis' "
                + " DESC 'description of matching rule' "
                + " SYNTAX 1.3.6.1.4.1.1466.115.121.1.17 "
                + " X-name ( 'this is an extension' ) ", false},
    };
  }

}
