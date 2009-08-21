package org.opends.schema.syntaxes;

import org.opends.server.api.AttributeSyntax;
import org.opends.server.schema.*;
import static org.opends.server.schema.SchemaConstants.SYNTAX_TELEX_OID;
import org.opends.schema.Schema;
import org.opends.schema.Syntax;
import org.opends.schema.CoreSchema;
import org.testng.annotations.DataProvider;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 19, 2009
 * Time: 1:44:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class TelexSyntaxTest extends SyntaxTestCase
{
  /**
   * {@inheritDoc}
   */
  @Override
  protected Syntax getRule()
  {
    return CoreSchema.instance().getSyntax(SYNTAX_TELEX_OID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="acceptableValues")
  public Object[][] createAcceptableValues()
  {
    return new Object [][] {
        {"123$france$456", true},
        {"abcdefghijk$lmnopqr$stuvwxyz", true},
        {"12345$67890$()+,-./:? ", true},
    };
  }

}
