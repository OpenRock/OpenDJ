package org.opends.schema.syntaxes;

import org.opends.server.api.AttributeSyntax;
import org.opends.server.schema.*;
import static org.opends.server.schema.SchemaConstants.SYNTAX_GUIDE_OID;
import org.opends.schema.Syntax;
import org.opends.schema.Schema;
import org.opends.schema.CoreSchema;
import org.testng.annotations.DataProvider;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 18, 2009
 * Time: 5:02:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class GuideSyntaxTest extends SyntaxTestCase
{
    /**
   * {@inheritDoc}
   */
  @Override
  protected Syntax getRule()
  {
    return CoreSchema.instance().getSyntax(SYNTAX_GUIDE_OID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="acceptableValues")
  public Object[][] createAcceptableValues()
  {
    return new Object [][] {
        {"sn$EQ|!(sn$EQ)", true},
        {"!(sn$EQ)", true},
        {"person#sn$EQ", true},
        {"(sn$EQ)", true},
        {"sn$EQ", true},
        {"sn$SUBSTR", true},
        {"sn$GE", true},
        {"sn$LE", true},
        {"sn$ME", false},
        {"?true", true},
        {"?false", true},
        {"true|sn$GE", false},
        {"sn$APPROX", true},
        {"sn$EQ|(sn$EQ)", true},
        {"sn$EQ|(sn$EQ", false},
        {"sn$EQ|(sn$EQ)|sn$EQ", true},
        {"sn$EQ|(cn$APPROX&?false)", true},
        {"sn$EQ|(cn$APPROX&|?false)", false},
    };
  }
}
