package org.opends.sdk.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_BIT_STRING_OID;

import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.Syntax;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 18, 2009
 * Time: 4:24:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class BitStringSyntaxTest extends SyntaxTestCase
{
    /**
   * {@inheritDoc}
   */
  @Override
  protected Syntax getRule()
  {
    return Schema.getCoreSchema().getSyntax(SYNTAX_BIT_STRING_OID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="acceptableValues")
  public Object[][] createAcceptableValues()
  {
    return new Object [][] {
        {"'0101'B",  true},
        {"'1'B",     true},
        { "'0'B",    true},
        { "invalid", false},
        { "1",       false},
        {"'010100000111111010101000'B",  true},
    };
  }
}
