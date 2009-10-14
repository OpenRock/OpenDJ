package org.opends.sdk.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_DIT_CONTENT_RULE_OID;

import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.Syntax;
import org.testng.annotations.DataProvider;

public class DITContentRuleSyntaxTest extends SyntaxTestCase
{
   /**
   * {@inheritDoc}
   */
  @Override
  protected Syntax getRule()
  {
    return Schema.getCoreSchema().getSyntax(SYNTAX_DIT_CONTENT_RULE_OID);
  }

  @Override
  @DataProvider(name="acceptableValues")
  public Object[][] createAcceptableValues()
  {
    return new Object [][] {
        {"( 2.5.6.4 DESC 'content rule for organization' NOT "
             + "( x121Address $ telexNumber ) )", true},
        {"( 2.5.6.4 NAME 'full rule' DESC 'rule with all possible fields' "
              + " OBSOLETE"
              + " AUX ( posixAccount )"
              + " MUST ( cn $ sn )"
              + " MAY ( dc )"
              + " NOT ( x121Address $ telexNumber ) )"
                , true},
        {"( 2.5.6.4 NAME 'full rule' DESC 'ommit parenthesis' "
                  + " OBSOLETE"
                  + " AUX posixAccount "
                  + " MUST cn "
                  + " MAY dc "
                  + " NOT x121Address )"
              , true},
         {"( 2.5.6.4 NAME 'full rule' DESC 'use numeric OIDs' "
                + " OBSOLETE"
                + " AUX 1.3.6.1.1.1.2.0"
                + " MUST cn "
                + " MAY dc "
                + " NOT x121Address )"
                   , true},
         {"( 2.5.6.4 NAME 'full rule' DESC 'illegal OIDs' "
               + " OBSOLETE"
               + " AUX 2.5.6.."
               + " MUST cn "
               + " MAY dc "
               + " NOT x121Address )"
               , false},
         {"( 2.5.6.4 NAME 'full rule' DESC 'illegal OIDs' "
                 + " OBSOLETE"
                 + " AUX 2.5.6.x"
                 + " MUST cn "
                 + " MAY dc "
                 + " NOT x121Address )"
                 , false},
         {"( 2.5.6.4 NAME 'full rule' DESC 'missing closing parenthesis' "
                 + " OBSOLETE"
                 + " AUX posixAccount"
                 + " MUST cn "
                 + " MAY dc "
                 + " NOT x121Address"
             , false},
         {"( 2.5.6.4 NAME 'full rule' DESC 'extra parameterss' "
                 + " MUST cn "
                 + " X-name ( 'this is an extra parameter' ) )"
             , true},

    };
  }
}
