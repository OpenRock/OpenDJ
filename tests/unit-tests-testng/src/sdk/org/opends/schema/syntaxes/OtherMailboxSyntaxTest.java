package org.opends.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_OTHER_MAILBOX_OID;
import org.opends.schema.Schema;
import org.opends.schema.Syntax;
import org.opends.schema.CoreSchema;
import org.testng.annotations.DataProvider;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 19, 2009
 * Time: 1:42:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class OtherMailboxSyntaxTest extends SyntaxTestCase
{
  /**
   * {@inheritDoc}
   */
  @Override
  protected Syntax getRule()
  {
    return CoreSchema.instance().getSyntax(SYNTAX_OTHER_MAILBOX_OID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="acceptableValues")
  public Object[][] createAcceptableValues()
  {
    return new Object [][] {
        {"MyMail$Mymailbox", true},
        {"MyMailMymailbox", false},
    };
  }
}
