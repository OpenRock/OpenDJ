package org.opends.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_FAXNUMBER_NAME;
import static org.opends.server.util.StaticUtils.toLowerCase;
import org.opends.server.types.ByteSequence;
import org.opends.messages.MessageBuilder;
import static org.opends.messages.SchemaMessages.*;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_FAXNUMBER_ILLEGAL_PARAMETER;
import org.opends.schema.Schema;

import java.util.HashSet;

/**
 * This class implements the facsimile telephone number attribute syntax, which
 * contains a printable string (the number) followed by zero or more parameters.
 * Those parameters should start with a dollar sign may be any of the following
 * strings:
 * <UL>
 *   <LI>twoDimensional</LI>
 *   <LI>fineResolution</LI>
 *   <LI>unlimitedLength</LI>
 *   <LI>b4Length</LI>
 *   <LI>a3Width</LI>
 *   <LI>b4Width</LI>
 *   <LI>uncompressed</LI>
 * </UL>
 */
public class FacsimileNumberSyntax extends AbstractSyntaxImplementation
{
  /**
   * The set of allowed fax parameter values, formatted entirely in lowercase
   * characters.
   */
  public static final HashSet<String> ALLOWED_FAX_PARAMETERS =
       new HashSet<String>(7);

  static
  {
    ALLOWED_FAX_PARAMETERS.add("twodimensional");
    ALLOWED_FAX_PARAMETERS.add("fineresolution");
    ALLOWED_FAX_PARAMETERS.add("unlimitedlength");
    ALLOWED_FAX_PARAMETERS.add("b4length");
    ALLOWED_FAX_PARAMETERS.add("a3width");
    ALLOWED_FAX_PARAMETERS.add("b4width");
    ALLOWED_FAX_PARAMETERS.add("uncompressed");
  }

  public String getName() {
    return SYNTAX_FAXNUMBER_NAME;
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
    // Get a lowercase string representation of the value and find its length.
    String valueString = toLowerCase(value.toString());
    int    valueLength = valueString.length();


    // The value must contain at least one character.
    if (valueLength == 0)
    {

      invalidReason.append(ERR_ATTR_SYNTAX_FAXNUMBER_EMPTY.get());
      return false;
    }


    // The first character must be a printable string character.
    char c = valueString.charAt(0);
    if (! PrintableStringSyntax.isPrintableCharacter(c))
    {

      invalidReason.append(ERR_ATTR_SYNTAX_FAXNUMBER_NOT_PRINTABLE.get(
              valueString, String.valueOf(c), 0));
      return false;
    }


    // Continue reading until we find a dollar sign or the end of the string.
    // Every intermediate character must be a printable string character.
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

          invalidReason.append(ERR_ATTR_SYNTAX_FAXNUMBER_NOT_PRINTABLE.get(
                  valueString, String.valueOf(c), pos));
        }
      }
    }

    if (pos >= valueLength)
    {
      // We're at the end of the value, so it must be valid unless the last
      // character was a dollar sign.
      if (c == '$')
      {

        invalidReason.append(ERR_ATTR_SYNTAX_FAXNUMBER_END_WITH_DOLLAR.get(
                valueString));
        return false;
      }
      else
      {
        return true;
      }
    }


    // Continue reading until we find the end of the string.  Each substring
    // must be a valid fax parameter.
    int paramStartPos = pos;
    while (pos < valueLength)
    {
      c = valueString.charAt(pos++);
      if (c == '$')
      {
        String paramStr = valueString.substring(paramStartPos, pos);
        if (! ALLOWED_FAX_PARAMETERS.contains(paramStr))
        {

          invalidReason.append(ERR_ATTR_SYNTAX_FAXNUMBER_ILLEGAL_PARAMETER.get(
                  valueString, paramStr, paramStartPos, (pos-1)));
          return false;
        }

        paramStartPos = pos;
      }
    }


    // We must be at the end of the value.  Read the last parameter and make
    // sure it is valid.
    String paramStr = valueString.substring(paramStartPos);
    if (! ALLOWED_FAX_PARAMETERS.contains(paramStr))
    {
      invalidReason.append(ERR_ATTR_SYNTAX_FAXNUMBER_ILLEGAL_PARAMETER.get(
              valueString, paramStr, paramStartPos, (pos-1)));
      return false;
    }


    // If we've gotten here, then the value must be valid.
    return true;
  }
}
