package org.opends.schema.syntaxes;

import org.testng.annotations.DataProvider;
import org.opends.schema.*;
import static org.opends.server.schema.SchemaConstants.SYNTAX_ATTRIBUTE_TYPE_OID;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 18, 2009
 * Time: 3:28:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class AttributeTypeSyntaxTest extends SyntaxTestCase
{
   /**
   * {@inheritDoc}
   */
  @Override
  protected Syntax getRule()
  {
    return CoreSchema.instance().getSyntax(SYNTAX_ATTRIBUTE_TYPE_OID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @DataProvider(name="acceptableValues")
  public Object[][] createAcceptableValues()
  {
    return new Object [][] {
        {"(1.2.8.5 NAME 'testtype' DESC 'full type' OBSOLETE SUP cn " +
          " EQUALITY caseIgnoreMatch ORDERING caseIgnoreOrderingMatch" +
          " SUBSTR caseIgnoreSubstringsMatch" +
          " SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 SINGLE-VALUE" +
          " USAGE userApplications )",
          true},
        {"(1.2.8.5 NAME 'testtype' DESC 'full type' OBSOLETE " +
          " EQUALITY caseIgnoreMatch ORDERING caseIgnoreOrderingMatch" +
          " SUBSTR caseIgnoreSubstringsMatch" +
          " X-APPROX 'equalLengthApproximateMatch'" +
          " SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 SINGLE-VALUE" +
          " COLLECTIVE USAGE userApplications )",
          true},
        {"(1.2.8.5 NAME 'testtype' DESC 'full type')",
              true},
        {"(1.2.8.5 USAGE directoryOperation )",
              true},
        {"(1.2.8.5 NAME 'testtype' DESC 'full type' OBSOLETE SUP cn " +
          " EQUALITY caseIgnoreMatch ORDERING caseIgnoreOrderingMatch" +
          " SUBSTR caseIgnoreSubstringsMatch" +
          " SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 SINGLE-VALUE" +
          " COLLECTIVE USAGE userApplications )",
          false}, // Collective can't inherit from non-collective
        {"(1.2.8.5 NAME 'testtype' DESC 'full type' OBSOLETE " +
          " EQUALITY caseIgnoreMatch ORDERING caseIgnoreOrderingMatch" +
          " SUBSTR caseIgnoreSubstringsMatch" +
          " SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 SINGLE-VALUE" +
          " COLLECTIVE USAGE directoryOperation )",
          false}, // Collective can't be operational
        {"(1.2.8.5 NAME 'testtype' DESC 'full type' OBSOLETE SUP cn " +
          " EQUALITY caseIgnoreMatch ORDERING caseIgnoreOrderingMatch" +
          " SUBSTR caseIgnoreSubstringsMatch" +
          " SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 SINGLE-VALUE" +
          " NO-USER-MODIFICATION USAGE directoryOperation )",
          false}, // directoryOperation can't inherit from userApplications
        {"(1.2.8.5 NAME 'testtype' DESC 'full type' OBSOLETE " +
          " EQUALITY caseIgnoreMatch ORDERING caseIgnoreOrderingMatch" +
          " SUBSTR caseIgnoreSubstringsMatch" +
          " SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 SINGLE-VALUE" +
          " NO-USER-MODIFICATION USAGE userApplications )",
          false}, // NO-USER-MODIFICATION can't have non-operational usage
    };
  }
}
