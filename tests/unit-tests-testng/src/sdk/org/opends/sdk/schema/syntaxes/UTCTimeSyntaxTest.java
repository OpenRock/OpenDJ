package org.opends.sdk.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_UTC_TIME_OID;

import org.opends.sdk.schema.CoreSchema;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.Syntax;
import org.opends.sdk.schema.syntaxes.UTCTimeSyntax;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 19, 2009
 * Time: 1:45:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class UTCTimeSyntaxTest extends SyntaxTestCase
{
  /**
   * {@inheritDoc}
   */
  @Override
  protected Syntax getRule()
  {
    return CoreSchema.instance().getSyntax(SYNTAX_UTC_TIME_OID);
  }

  @Override
  @DataProvider(name="acceptableValues")
  public Object[][] createAcceptableValues()
  {
    return new Object [][] {
        // tests for the UTC time syntax.
        {"060906135030+01",   true},
        {"0609061350Z",       true},
        {"060906135030Z",     true},
        {"061116135030Z",     true},
        {"061126135030Z",     true},
        {"061231235959Z",     true},
        {"060906135030+0101", true},
        {"060906135030+2359", true},
        {"060906135060+0101", true},
        {"060906135061+0101", false},
        {"060906135030+3359", false},
        {"060906135030+2389", false},
        {"062231235959Z",     false},
        {"061232235959Z",     false},
        {"06123123595aZ",     false},
        {"0a1231235959Z",     false},
        {"06j231235959Z",     false},
        {"0612-1235959Z",     false},
        {"061231#35959Z",     false},
        {"2006",              false},
        {"062106135030+0101", false},
        {"060A06135030+0101", false},
        {"061A06135030+0101", false},
        {"060936135030+0101", false},
        {"06090A135030+0101", false},
        {"06091A135030+0101", false},
        {"060900135030+0101", false},
        {"060906335030+0101", false},
        {"0609061A5030+0101", false},
        {"0609062A5030+0101", false},
        {"060906137030+0101", false},
        {"060906135A30+0101", false},
        {"060906135", false},
        {"0609061350", false},
        {"060906135070+0101", false},
        {"06090613503A+0101", false},
        {"06090613503", false},
        {"0609061350Z0", false},
        {"0609061350+0", false},
        {"0609061350+000A", false},
        {"0609061350+A00A", false},
        {"060906135030Z0", false},
        {"060906135030+010", false},
        {"060906135030+010A", false},
        {"060906135030+0A01", false},
        {"060906135030+2501", false},
        {"060906135030+0170", false},
        {"060906135030+010A", false},
        {"060906135030+A00A", false},
        {"060906135030Q", false},
        {"060906135030+", false},
    };
  }

    /**
   * Tests the {@code createUTCTimeValue} and {@code decodeUTCTimeValue}
   * methods.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testCreateAndDecodeUTCTimeValue()
         throws Exception
  {
    Date d = new Date();
    String timeValue = UTCTimeSyntax.createUTCTimeValue(d);
    Date decodedDate = UTCTimeSyntax.decodeUTCTimeValue(timeValue);

    // UTCTime does not have support for sub-second values, so we need to make
    // sure that the decoded value is within 1000 milliseconds.
    assertTrue(Math.abs(d.getTime() - decodedDate.getTime()) < 1000);
  }
}
