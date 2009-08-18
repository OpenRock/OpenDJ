package org.opends.schema.syntaxes;

import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_TELETEXID_EMPTY;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_TELETEXID_END_WITH_DOLLAR;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_TELETEXID_ILLEGAL_PARAMETER;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_TELETEXID_NOT_PRINTABLE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_TELETEXID_PARAM_NO_COLON;
import static org.opends.server.schema.SchemaConstants.SYNTAX_TELETEX_TERM_ID_NAME;

import java.util.HashSet;

import org.opends.messages.MessageBuilder;
import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;

/**
 * This class implements the teletex terminal identifier attribute syntax, which
 * contains a printable string (the terminal identifier) followed by zero or
 * more parameters, which start with a dollar sign and are followed by a
 * parameter name, a colon, and a value.  The parameter value should consist of
 * any string of bytes (the dollar sign and backslash must be escaped with a
 * preceding backslash), and the parameter name must be one of the following
 * strings:
 * <UL>
 *   <LI>graphic</LI>
 *   <LI>control</LI>
 *   <LI>misc</LI>
 *   <LI>page</LI>
 *   <LI>private</LI>
 * </UL>
 */
public class TeletexTerminalIdentifierSyntax
    extends AbstractSyntaxImplementation
{
  /**
   * The set of allowed fax parameter values, formatted entirely in lowercase
   * characters.
   */
  public static final HashSet<String> ALLOWED_TTX_PARAMETERS =
       new HashSet<String>(5);

  static
  {
    ALLOWED_TTX_PARAMETERS.add("graphic");
    ALLOWED_TTX_PARAMETERS.add("control");
    ALLOWED_TTX_PARAMETERS.add("misc");
    ALLOWED_TTX_PARAMETERS.add("page");
    ALLOWED_TTX_PARAMETERS.add("private");
  }
  
  public String getName() {
    return SYNTAX_TELETEX_TERM_ID_NAME;
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
    // Get a lowercase string representation of the value and find its length.
    String valueString = value.toString();
    int    valueLength = valueString.length();


    // The value must contain at least one character.
    if (valueLength == 0)
    {

      invalidReason.append(ERR_ATTR_SYNTAX_TELETEXID_EMPTY.get());
      return false;
    }


    // The first character must be a printable string character.
    char c = valueString.charAt(0);
    if (! PrintableStringSyntax.isPrintableCharacter(c))
    {

      invalidReason.append(ERR_ATTR_SYNTAX_TELETEXID_NOT_PRINTABLE.get(
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

          invalidReason.append(ERR_ATTR_SYNTAX_TELETEXID_NOT_PRINTABLE.get(
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

        invalidReason.append(ERR_ATTR_SYNTAX_TELETEXID_END_WITH_DOLLAR.get(
                valueString));
        return false;
      }
      else
      {
        return true;
      }
    }


    // Continue reading until we find the end of the string.  Each substring
    // must be a valid teletex terminal identifier parameter followed by a colon
    // and the value.  Dollar signs must be escaped
    int paramStartPos = pos;
    boolean escaped = false;
    while (pos < valueLength)
    {
      if (escaped)
      {
        pos++;
        continue;
      }

      c = valueString.charAt(pos++);
      if (c == '\\')
      {
        escaped = true;
        continue;
      }
      else if (c == '$')
      {
        String paramStr = valueString.substring(paramStartPos, pos);

        int colonPos = paramStr.indexOf(':');
        if (colonPos < 0)
        {

          invalidReason.append(ERR_ATTR_SYNTAX_TELETEXID_PARAM_NO_COLON.get(
                  valueString));
          return false;
        }

        String paramName = paramStr.substring(0, colonPos);
        if (! ALLOWED_TTX_PARAMETERS.contains(paramName))
        {

          invalidReason.append(ERR_ATTR_SYNTAX_TELETEXID_ILLEGAL_PARAMETER.get(
                  valueString, paramName));
          return false;
        }

        paramStartPos = pos;
      }
    }


    // We must be at the end of the value.  Read the last parameter and make
    // sure it is valid.
    String paramStr = valueString.substring(paramStartPos);
    int colonPos = paramStr.indexOf(':');
    if (colonPos < 0)
    {

      invalidReason.append(ERR_ATTR_SYNTAX_TELETEXID_PARAM_NO_COLON.get(
              valueString));
      return false;
    }

    String paramName = paramStr.substring(0, colonPos);
    if (! ALLOWED_TTX_PARAMETERS.contains(paramName))
    {

      invalidReason.append(ERR_ATTR_SYNTAX_TELETEXID_ILLEGAL_PARAMETER.get(
              valueString, paramName));
      return false;
    }


    // If we've gotten here, then the value must be valid.
    return true;
  }


}
