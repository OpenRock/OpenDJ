package org.opends.sdk.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_UUID_OID;

import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.Syntax;
import org.testng.annotations.DataProvider;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 19, 2009
 * Time: 1:51:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class UUIDSyntaxTest extends SyntaxTestCase
{
    /**
   * {@inheritDoc}
   */
  @Override
  protected Syntax getRule()
  {
    return Schema.getCoreSchema().getSyntax(SYNTAX_UUID_OID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="acceptableValues")
  public Object[][] createAcceptableValues()
  {
    return new Object [][] {
        {"12345678-9ABC-DEF0-1234-1234567890ab", true},
        {"12345678-9abc-def0-1234-1234567890ab", true},
        {"12345678-9abc-def0-1234-1234567890ab", true},
        {"12345678-9abc-def0-1234-1234567890ab", true},
        {"02345678-9abc-def0-1234-1234567890ab", true},
        {"12345678-9abc-def0-1234-1234567890ab", true},
        {"12345678-9abc-def0-1234-1234567890ab", true},
        {"02345678-9abc-def0-1234-1234567890ab", true},
        {"G2345678-9abc-def0-1234-1234567890ab", false},
        {"g2345678-9abc-def0-1234-1234567890ab", false},
        {"12345678/9abc/def0/1234/1234567890ab", false},
        {"12345678-9abc-def0-1234-1234567890a", false},
    };
  }
}
