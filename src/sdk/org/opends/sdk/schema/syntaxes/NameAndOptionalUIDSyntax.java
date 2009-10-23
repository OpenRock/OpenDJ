package org.opends.sdk.schema.syntaxes;

import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_NAMEANDUID_ILLEGAL_BINARY_DIGIT;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_NAMEANDUID_INVALID_DN;
import static org.opends.sdk.schema.SchemaConstants.SYNTAX_NAME_AND_OPTIONAL_UID_NAME;
import static org.opends.sdk.schema.SchemaConstants.EMR_UNIQUE_MEMBER_OID;
import static org.opends.sdk.schema.SchemaConstants.SMR_CASE_IGNORE_OID;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.util.LocalizedIllegalArgumentException;

import org.opends.messages.MessageBuilder;
import org.opends.sdk.DN;
import org.opends.server.types.ByteSequence;

/**
 * This class implements the name and optional UID attribute syntax, which holds
 * values consisting of a DN, optionally followed by an octothorpe (#) and a bit
 * string value.
 */
public class NameAndOptionalUIDSyntax extends AbstractSyntaxImplementation
{

  public String getName() {
    return SYNTAX_NAME_AND_OPTIONAL_UID_NAME;
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
    String valueString = value.toString().trim();
    int    valueLength = valueString.length();


    // See if the value contains the "optional uid" portion.  If we think it
    // does, then mark its location.
    int dnEndPos = valueLength;
    int sharpPos = -1;
    if (valueString.endsWith("'B") || valueString.endsWith("'b"))
    {
      sharpPos = valueString.lastIndexOf("#'");
      if (sharpPos > 0)
      {
        dnEndPos = sharpPos;
      }
    }


    // Take the DN portion of the string and try to normalize it.
    try
    {
      DN.valueOf(valueString.substring(0, dnEndPos), schema);
    }
    catch (LocalizedIllegalArgumentException e)
    {
      // We couldn't normalize the DN for some reason.  The value cannot be
      // acceptable.

      invalidReason.append(ERR_ATTR_SYNTAX_NAMEANDUID_INVALID_DN.get(
              valueString, e.getMessageObject()));
      return false;
    }



    // If there is an "optional uid", then normalize it and make sure it only
    // contains valid binary digits.
    if (sharpPos > 0)
    {
      int     endPos = valueLength - 2;
      for (int i=sharpPos+2; i < endPos; i++)
      {
        char c = valueString.charAt(i);
        if (! ((c == '0') || (c == '1')))
        {

          invalidReason.append(
                  ERR_ATTR_SYNTAX_NAMEANDUID_ILLEGAL_BINARY_DIGIT.get(
                          valueString, String.valueOf(c), i));
          return false;
        }
      }
    }


    // If we've gotten here, then the value is acceptable.
    return true;
  }

  @Override
  public String getEqualityMatchingRule() {
    return EMR_UNIQUE_MEMBER_OID;
  }

  @Override
  public String getSubstringMatchingRule() {
    return SMR_CASE_IGNORE_OID;
  }
}
