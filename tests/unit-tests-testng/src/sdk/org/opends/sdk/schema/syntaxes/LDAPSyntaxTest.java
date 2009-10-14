package org.opends.sdk.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_LDAP_SYNTAX_OID;

import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.Syntax;
import org.testng.annotations.DataProvider;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 19, 2009
 * Time: 1:35:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class LDAPSyntaxTest extends SyntaxTestCase
{
   /**
   * {@inheritDoc}
   */
  @Override
  protected Syntax getRule()
  {
    return Schema.getCoreSchema().getSyntax(SYNTAX_LDAP_SYNTAX_OID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="acceptableValues")
  public Object[][] createAcceptableValues()
  {
      return new Object [][] {
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "X-9EN ('this' 'is' 'a' 'test'))",
                    false},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "(X-name 'this",
                    false},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "(X-name 'this'",
                    false},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "Y-name 'this')",
                    false},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "X-name 'this' 'is')",
                    false},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "X-name )",
                    false},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "X- ('this' 'is' 'a' 'test'))",
                    false},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "X-name ('this' 'is' 'a' 'test') X-name-a 'this' X-name-b ('this')",
                    false},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "X-name ('this' 'is' 'a' 'test') X-name-a 'this' X-name-b ('this'",
                    false},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "X-name ('this' 'is' 'a' 'test') X-name-a 'this' X-name-b ('this'))))",
                    false},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "X-name ('this' 'is' 'a' 'test') X-name-a  X-name-b ('this'))))",
                    false},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "X-name ('this' 'is' 'a' 'test') X-name-a  'X-name-b' ('this'))))",
                    false},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "X-name ('this' 'is' 'a' 'test') X-name-a 'this' X-name-b ('this'))",
                    true},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "X-a-_eN_- ('this' 'is' 'a' 'test'))",
                    true},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "X-name ('this'))",
                    true},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "X-name 'this')",
                    true},
              {"( 2.5.4.3 DESC 'full syntax description' " +
                    "X-name 'this' X-name-a 'test')",
                    true},
              {"( 2.5.4.3 DESC 'full syntax description' )", true},
              {"   (    2.5.4.3    DESC  ' syntax description'    )", true},
              {"( 2.5.4.3 DESC syntax description )", false},
              {"($%^*&!@ DESC 'syntax description' )", false},
              {"(temp-oid DESC 'syntax description' )", true},
              {"2.5.4.3 DESC 'syntax description' )", false},
              {"(2.5.4.3 DESC 'syntax description' ", false},
      };
  }
}
