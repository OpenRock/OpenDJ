package org.opends.schema.syntaxes;

import static org.opends.server.util.ServerConstants.SCHEMA_PROPERTY_ORIGIN;
import static org.opends.server.util.StaticUtils.isDigit;
import static org.opends.server.schema.SchemaConstants.SYNTAX_NUMERIC_STRING_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_NUMERIC_STRING_NAME;
import static org.opends.server.schema.SchemaConstants.SYNTAX_NUMERIC_STRING_DESCRIPTION;
import org.opends.server.types.ByteSequence;
import org.opends.messages.MessageBuilder;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_NUMERIC_STRING_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_NUMERIC_STRING_ILLEGAL_CHAR;

import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * This class implements the numeric string attribute syntax, which may be hold
 * one or more numeric digits and/or spaces.  Equality, ordering, and substring
 * matching will be allowed by default.
 */
public class NumericStringSyntax extends SyntaxDescription
{
  public NumericStringSyntax(Map<String, List<String>> extraProperties)
  {
    super(SYNTAX_NUMERIC_STRING_OID, SYNTAX_NUMERIC_STRING_NAME,
        SYNTAX_NUMERIC_STRING_DESCRIPTION, extraProperties);
  }


  public boolean isHumanReadable() {
    return true;
  }
  

  /**
   * Indicates whether the provided value is acceptable for use in an attribute
   * with this syntax.  If it is not, then the reason may be appended to the
   * provided buffer.
   *
   * @param  value          The value for which to make the determination.
   * @param  invalidReason  The buffer to which the invalid reason should be
   *                        appended.
   *
   * @return  <CODE>true</CODE> if the provided value is acceptable for use with
   *          this syntax, or <CODE>false</CODE> if not.
   */
  public boolean valueIsAcceptable(ByteSequence value,
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
}
