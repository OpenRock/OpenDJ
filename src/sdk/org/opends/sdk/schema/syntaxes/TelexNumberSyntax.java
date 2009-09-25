package org.opends.sdk.schema.syntaxes;

import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_TELEX_ILLEGAL_CHAR;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_TELEX_NOT_PRINTABLE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_TELEX_TOO_SHORT;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_TELEX_TRUNCATED;

import org.opends.messages.MessageBuilder;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;
import static org.opends.sdk.schema.SchemaConstants.*;

/**
 * This class implements the telex number attribute syntax, which contains three
 * printable strings separated by dollar sign characters.  Equality, ordering,
 * and substring matching will be allowed by default.
 */
public class TelexNumberSyntax extends AbstractSyntaxImplementation
{

  public String getName() {
    return SYNTAX_TELEX_NAME;
  }

  public boolean isHumanReadable() {
    return false;
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
    // Get a string representation of the value and find its length.
    String valueString = value.toString();
    int    valueLength = valueString.length();

    if (valueLength < 5)
    {

      invalidReason.append(ERR_ATTR_SYNTAX_TELEX_TOO_SHORT.get(valueString));
      return false;
    }


    // The first character must be a printable string character.
    char c = valueString.charAt(0);
    if (! PrintableStringSyntax.isPrintableCharacter(c))
    {

      invalidReason.append(ERR_ATTR_SYNTAX_TELEX_NOT_PRINTABLE.get(
              valueString, String.valueOf(c), 0));
      return false;
    }


    // Continue reading until we find a dollar sign.  Every intermediate
    // character must be a printable string character.
    int pos = 1;
    for ( ; pos < valueLength; pos++)
    {
      c = valueString.charAt(pos);
      if (c == '$')
      {
        pos++;
        break;
      }
      else
      {
        if (! PrintableStringSyntax.isPrintableCharacter(c))
        {

          invalidReason.append(ERR_ATTR_SYNTAX_TELEX_ILLEGAL_CHAR.get(
                  valueString, String.valueOf(c), pos));
        }
      }
    }

    if (pos >= valueLength)
    {

      invalidReason.append(ERR_ATTR_SYNTAX_TELEX_TRUNCATED.get(valueString));
      return false;
    }


    // The next character must be a printable string character.
    c = valueString.charAt(pos++);
    if (! PrintableStringSyntax.isPrintableCharacter(c))
    {

      invalidReason.append(ERR_ATTR_SYNTAX_TELEX_NOT_PRINTABLE.get(
              valueString, String.valueOf(c), (pos-1)));
      return false;
    }


    // Continue reading until we find another dollar sign.  Every intermediate
    // character must be a printable string character.
    for ( ; pos < valueLength; pos++)
    {
      c = valueString.charAt(pos);
      if (c == '$')
      {
        pos++;
        break;
      }
      else
      {
        if (! PrintableStringSyntax.isPrintableCharacter(c))
        {

          invalidReason.append(ERR_ATTR_SYNTAX_TELEX_ILLEGAL_CHAR.get(
                  valueString, String.valueOf(c), pos));
          return false;
        }
      }
    }

    if (pos >= valueLength)
    {

      invalidReason.append(ERR_ATTR_SYNTAX_TELEX_TRUNCATED.get(valueString));
      return false;
    }


    // The next character must be a printable string character.
    c = valueString.charAt(pos++);
    if (! PrintableStringSyntax.isPrintableCharacter(c))
    {

      invalidReason.append(ERR_ATTR_SYNTAX_TELEX_NOT_PRINTABLE.get(
              valueString, String.valueOf(c), (pos-1)));
      return false;
    }


    // Continue reading until the end of the value.  Every intermediate
    // character must be a printable string character.
    for ( ; pos < valueLength; pos++)
    {
      c = valueString.charAt(pos);
      if (! PrintableStringSyntax.isPrintableCharacter(c))
      {

        invalidReason.append(ERR_ATTR_SYNTAX_TELEX_ILLEGAL_CHAR.get(
                valueString, String.valueOf(c), pos));
        return false;
      }
    }


    // If we've gotten here, then we're at the end of the value and it is
    // acceptable.
    return true;
  }


  @Override
  public String getEqualityMatchingRule() {
    return EMR_CASE_IGNORE_OID;
  }

  @Override
  public String getOrderingMatchingRule() {
    return OMR_CASE_IGNORE_OID;
  }

  @Override
  public String getSubstringMatchingRule() {
    return SMR_CASE_IGNORE_OID;
  }
}
