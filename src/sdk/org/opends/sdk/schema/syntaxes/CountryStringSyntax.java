package org.opends.sdk.schema.syntaxes;

import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_COUNTRY_STRING_INVALID_LENGTH;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_COUNTRY_STRING_NOT_PRINTABLE;
import static org.opends.sdk.util.StaticUtils.toLowerCase;

import org.opends.messages.MessageBuilder;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;
import static org.opends.sdk.schema.SchemaConstants.*;

/**
 * This class defines the country string attribute syntax, which should be a
 * two-character ISO 3166 country code.  However, for maintainability, it will
 * accept any value consisting entirely of two printable characters.  In most
 * ways, it will behave like the directory string attribute syntax.
 */
public class CountryStringSyntax extends AbstractSyntaxImplementation
{

  public String getName() {
    return SYNTAX_COUNTRY_STRING_NAME;
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

  @Override
  public String getApproximateMatchingRule() {
    return AMR_DOUBLE_METAPHONE_OID;
  }
}
