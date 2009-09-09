package org.opends.sdk.schema.syntaxes;

import static org.testng.Assert.*;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.SchemaException;
import org.opends.sdk.schema.SchemaTestCase;
import org.opends.sdk.schema.Syntax;
import org.opends.messages.MessageBuilder;
import org.opends.server.types.ByteString;

public abstract class SyntaxTestCase extends SchemaTestCase
{
    /**
   * Create data for the testAcceptableValues test.
   * This should be a table of tables with 2 elements.
   * The first one should be the value to test, the second the expected
   * result of the test.
   *
   * @return a table containing data for the testAcceptableValues Test.
   */
  @DataProvider(name="acceptableValues")
  public abstract Object[][] createAcceptableValues();

  /**
   * Get an instance of the attribute syntax that muste be tested.
   *
   * @return An instance of the attribute syntax that muste be tested.
   */
  protected abstract Syntax getRule() throws SchemaException, DecodeException;

  /**
   * Test the normalization and the approximate comparison.
   */
  @Test(dataProvider= "acceptableValues")
  public void testAcceptableValues(String value, Boolean result)
         throws Exception
  {
    // Make sure that the specified class can be instantiated as a task.
    Syntax syntax = getRule();

    MessageBuilder reason = new MessageBuilder();
    // test the valueIsAcceptable method
    Boolean liveResult =
      syntax.valueIsAcceptable(ByteString.valueOf(value), reason);

    if (liveResult != result)
      fail(syntax + ".valueIsAcceptable gave bad result for " + value +
          "reason : " + reason);

    // call the getters
    syntax.getApproximateMatchingRule();
    syntax.getDescription();
    syntax.getEqualityMatchingRule();
    syntax.getOID();
    syntax.getOrderingMatchingRule();
    syntax.getSubstringMatchingRule();
    syntax.hashCode();
    syntax.isHumanReadable();
    syntax.toString();
  }
}
