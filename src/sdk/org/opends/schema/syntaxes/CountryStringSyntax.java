package org.opends.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.*;
import org.opends.server.types.ByteSequence;
import static org.opends.server.util.StaticUtils.toLowerCase;
import org.opends.messages.MessageBuilder;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_COUNTRY_STRING_INVALID_LENGTH;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_COUNTRY_STRING_NOT_PRINTABLE;
import org.opends.schema.SchemaUtils;

/**
 * This class defines the country string attribute syntax, which should be a
 * two-character ISO 3166 country code.  However, for maintainability, it will
 * accept any value consisting entirely of two printable characters.  In most
 * ways, it will behave like the directory string attribute syntax.
 */
public class CountryStringSyntax extends SyntaxImplementation
{

  /**
   * Creates a new instance of this syntax.
   */
  public CountryStringSyntax()
  {
    super(SYNTAX_COUNTRY_STRING_OID, SYNTAX_COUNTRY_STRING_NAME,
        SYNTAX_COUNTRY_STRING_DESCRIPTION, SchemaUtils.RFC4512_ORIGIN);
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
    String stringValue = toLowerCase(value.toString());
    if (stringValue.length() != 2)
    {
      invalidReason.append(
              ERR_ATTR_SYNTAX_COUNTRY_STRING_INVALID_LENGTH.get(stringValue));
      return false;
    }


    if ((! PrintableStringSyntax.isPrintableCharacter(stringValue.charAt(0))) ||
        (! PrintableStringSyntax.isPrintableCharacter(stringValue.charAt(1))))
    {
      invalidReason.append(
              ERR_ATTR_SYNTAX_COUNTRY_STRING_NOT_PRINTABLE.get(stringValue));
      return false;
    }


    return true;
  }

  public boolean isHumanReadable() {
    return true;
  }
}
