package org.opends.sdk.schema.syntaxes;

import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.SchemaBuilder;
import org.opends.sdk.schema.SchemaException;
import org.opends.sdk.schema.Syntax;

import static org.opends.server.schema.SchemaConstants.SYNTAX_IA5_STRING_OID;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.testng.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 20, 2009
 * Time: 5:10:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class SubstitutionSyntaxTestCase extends SyntaxTestCase
{
  /**
   * {@inheritDoc}
   */
  @Override
  protected Syntax getRule() throws SchemaException, DecodeException
  {
    // Use IA5String syntax as our substitute.
    SchemaBuilder builder = SchemaBuilder.buildFromCore();
    builder.addSyntax("9.9.9", "Unimplemented Syntax", SYNTAX_IA5_STRING_OID,
        false);
    return builder.toSchema().getSyntax("9.9.9");
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

  public void testSelfSubstitute1() throws SchemaException, DecodeException
  {
    SchemaBuilder builder = SchemaBuilder.buildFromCore();
    builder.addSyntax("( 1.3.6.1.4.1.1466.115.121.1.15 " +
        " DESC 'Replacing DirectorySyntax'  " +
        " X-SUBST '1.3.6.1.4.1.1466.115.121.1.15' )", true);
    Assert.assertFalse(builder.toSchema().getWarnings().isEmpty());
  }

  public void testSelfSubstitute2() throws SchemaException
  {
    SchemaBuilder builder = SchemaBuilder.buildFromCore();
    builder.addSyntax("1.3.6.1.4.1.1466.115.121.1.15",
        "Replacing DirectorySyntax", "1.3.6.1.4.1.1466.115.121.1.15", true);
    Assert.assertFalse(builder.toSchema().getWarnings().isEmpty());
  }

  public void testUndefinedSubstitute1() throws SchemaException, DecodeException
  {
    SchemaBuilder builder = SchemaBuilder.buildFromCore();
    builder.addSyntax("( 1.3.6.1.4.1.1466.115.121.1.15 " +
        " DESC 'Replacing DirectorySyntax'  " +
        " X-SUBST '1.1.1' )", true);
    Assert.assertFalse(builder.toSchema().getWarnings().isEmpty());
  }

  public void testUndefinedSubstitute2() throws SchemaException
  {
    SchemaBuilder builder = SchemaBuilder.buildFromCore();
    builder.addSyntax("1.3.6.1.4.1.1466.115.121.1.15",
        "Replacing DirectorySyntax", "1.1.1", true);
    Assert.assertFalse(builder.toSchema().getWarnings().isEmpty());
  }

  @Test(expectedExceptions=SchemaException.class)
  public void testSubstituteCore1() throws SchemaException, DecodeException
  {
    SchemaBuilder builder = SchemaBuilder.buildFromCore();
    builder.addSyntax("( 1.3.6.1.4.1.1466.115.121.1.26 " +
        " DESC 'Replacing DirectorySyntax'  " +
        " X-SUBST '9.9.9' )", true);
  }

  @Test(expectedExceptions=SchemaException.class)
  public void testSubstituteCore2() throws SchemaException
  {
    SchemaBuilder builder = SchemaBuilder.buildFromCore();
    builder.addSyntax("1.3.6.1.4.1.1466.115.121.1.26",
        "Replacing DirectorySyntax", "9.9.9", true);
  }
}
