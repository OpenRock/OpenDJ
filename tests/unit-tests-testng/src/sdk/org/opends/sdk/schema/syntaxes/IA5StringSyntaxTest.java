package org.opends.sdk.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_IA5_STRING_OID;

import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.Syntax;
import org.testng.annotations.DataProvider;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 18, 2009
 * Time: 5:04:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class IA5StringSyntaxTest extends SyntaxTestCase
{
  /**
   * {@inheritDoc}
   */
  @Override
  protected Syntax getRule()
  {
    return Schema.getCoreSchema().getSyntax(SYNTAX_IA5_STRING_OID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="acceptableValues")
  public Object[][] createAcceptableValues()
  {
    return new Object [][] {
        {"12345678", true},
        {"12345678\u2163", false},
    };
  }

}
