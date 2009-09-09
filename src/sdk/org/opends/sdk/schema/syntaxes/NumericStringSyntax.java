package org.opends.sdk.schema.syntaxes;

import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_NUMERIC_STRING_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_NUMERIC_STRING_ILLEGAL_CHAR;
import static org.opends.server.util.StaticUtils.isDigit;

import org.opends.messages.MessageBuilder;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;
import static org.opends.server.schema.SchemaConstants.*;

/**
 * This class implements the numeric string attribute syntax, which may be hold
 * one or more numeric digits and/or spaces.  Equality, ordering, and substring
 * matching will be allowed by default.
 */
public class NumericStringSyntax extends AbstractSyntaxImplementation
{

  public String getName() {
    return SYNTAX_NUMERIC_STRING_NAME;
  }

  public boolean isHumanReadable() {
    return true;
  }
  

  /**
   * Indicates whether the provided value is acceptable for use in an attribute
   * with this syntax.  If it is not, then the reason may be appended to the
   * provided buffer.
   *
   * @param schema
   *@param  value          The value for which to make the determination.
   * @param  invalidReason  The buffer to which the invalid reason should be
 *                        appended.
 * @return  <CODE>true</CODE> if the provided value is acceptable for use with
   *          this syntax, or <CODE>false</CODE> if not.
   */
  public boolean valueIsAcceptable(Schema schema, ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    String valueString = value.toString();
    int    length      = valueString.length();


    // It must have at least one digit or space.
    if (length == 0)
    {

      invalidReason.append(ERR_ATTR_SYNTAX_NUMERIC_STRING_EMPTY_VALUE.get());
      return false;
    }


    // Iterate through the characters and make sure they are all digits or
    // spaces.
    for (int i=0; i < length; i++)
    {
      char c = valueString.charAt(i);
      if (! (isDigit(c) || (c == ' ')))
      {

        invalidReason.append(WARN_ATTR_SYNTAX_NUMERIC_STRING_ILLEGAL_CHAR.get(
                valueString, String.valueOf(c), i));
        return false;
      }
    }

    return true;
  }

  @Override
  public String getEqualityMatchingRule() {
    return EMR_NUMERIC_STRING_OID;
  }

  @Override
  public String getOrderingMatchingRule() {
    return OMR_NUMERIC_STRING_OID;
  }

  @Override
  public String getSubstringMatchingRule() {
    return SMR_CASE_EXACT_OID;
  }
}
