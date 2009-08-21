package org.opends.schema.syntaxes;

import org.opends.schema.Syntax;
import org.opends.schema.SchemaException;
import org.opends.schema.SchemaBuilder;
import org.opends.ldap.DecodeException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 21, 2009
 * Time: 3:45:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegexSyntaxTestCase extends SyntaxTestCase
{
   /**
   * {@inheritDoc}
   */
  @Override
  protected Syntax getRule() throws SchemaException, DecodeException
  {
    SchemaBuilder builder = new SchemaBuilder();
    builder.addSyntax("1.1.1", "Host and Port in the format of HOST:PORT",
        Pattern.compile("^[a-z-A-Z]+:[0-9.]+\\d$"), false);
    return builder.toSchema().getSyntax("1.1.1");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="acceptableValues")
  public Object[][] createAcceptableValues()
  {
    return new Object [][] {
        {"invalid regex", false},
        {"host:0.0.0", true},
    };
  }

  @Test(expectedExceptions=DecodeException.class)
  public void testInvalidPattern() throws SchemaException, DecodeException
  {
    // This should fail due to invalid pattern.
    SchemaBuilder builder = new SchemaBuilder();
    builder.addSyntax("( 1.1.1 DESC 'Host and Port in the format of HOST:PORT' " +
        " X-PATTERN '^[a-z-A-Z+:[0-@.]+\\d$' )", true);
  }

  @Test
  public void testDecode() throws SchemaException, DecodeException
  {
    // This should fail due to invalid pattern.
    SchemaBuilder builder = new SchemaBuilder();
    builder.addSyntax("( 1.1.1 DESC 'Host and Port in the format of HOST:PORT' " +
        " X-PATTERN '^[a-z-A-Z]+:[0-9.]+\\d$' )", true);
    builder.toSchema();
  }

}
