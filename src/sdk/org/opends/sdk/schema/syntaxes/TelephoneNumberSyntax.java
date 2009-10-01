package org.opends.sdk.schema.syntaxes;

import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_TELEPHONE_EMPTY;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_TELEPHONE_ILLEGAL_CHAR;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_TELEPHONE_NO_DIGITS;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_TELEPHONE_NO_PLUS;
import static org.opends.sdk.schema.SchemaConstants.EMR_TELEPHONE_OID;
import static org.opends.sdk.schema.SchemaConstants.SMR_TELEPHONE_OID;
import static org.opends.sdk.schema.SchemaConstants.SYNTAX_TELEPHONE_NAME;
import static org.opends.sdk.util.StaticUtils.isDigit;

import org.opends.messages.Message;
import org.opends.messages.MessageBuilder;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;

/**
 * This class implements the telephone number attribute syntax, which is defined
 * in RFC 2252.  Note that this can have two modes of operation, depending on
 * its configuration.  Most of the time, it will be very lenient when deciding
 * what to accept, and will allow anything but only pay attention to the digits.
 * However, it can also be configured in a "strict" mode, in which case it will
 * only accept values in the E.123 international telephone number format.
 */
public class TelephoneNumberSyntax extends AbstractSyntaxImplementation
{
  // Indicates whether this matching rule should operate in strict mode.
  private boolean strictMode;

  public TelephoneNumberSyntax(boolean strict)
  {
    this.strictMode = strict;
  }

  public String getName() {
    return SYNTAX_TELEPHONE_NAME;
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
    // No matter what, the value can't be empty or null.
    String valueStr;
    if ((value == null) ||
        ((valueStr = value.toString().trim()).length() == 0))
    {
      invalidReason.append(ERR_ATTR_SYNTAX_TELEPHONE_EMPTY.get());
      return false;
    }

    int length = valueStr.length();


    if (strictMode)
    {
      // If the value does not start with a plus sign, then that's not
      // acceptable.
      if (valueStr.charAt(0) != '+')
      {
        Message message = ERR_ATTR_SYNTAX_TELEPHONE_NO_PLUS.get(valueStr);
        invalidReason.append(message);
        return false;
      }


      // Iterate through the remaining characters in the value.  There must be
      // at least one digit, and it must contain only valid digits and separator
      // characters.
      boolean digitSeen = false;
      for (int i=1; i < length; i++)
      {
        char c = valueStr.charAt(i);
        if (isDigit(c))
        {
          digitSeen = true;
        }
        else if (! isSeparator(c))
        {
          Message message = ERR_ATTR_SYNTAX_TELEPHONE_ILLEGAL_CHAR.get(
                  valueStr, String.valueOf(c), i);
          invalidReason.append(message);
          return false;
        }
      }

      if (! digitSeen)
      {
        Message message = ERR_ATTR_SYNTAX_TELEPHONE_NO_DIGITS.get(valueStr);
        invalidReason.append(message);
        return false;
      }


      // If we've gotten here, then we'll consider it acceptable.
      return true;
    }
    else
    {
      // If we are not in strict mode, then all non-empty values containing at
      // least one digit will be acceptable.
      for (int i=0; i < length; i++)
      {
        if (isDigit(valueStr.charAt(i)))
        {
          return true;
        }
      }

      // If we made it here, then we didn't find any digits.
      Message message = ERR_ATTR_SYNTAX_TELEPHONE_NO_DIGITS.get(valueStr);
      invalidReason.append(message);
      return false;
    }
  }



  /**
   * Indicates whether the provided character is a valid separator for telephone
   * number components when operating in strict mode.
   *
   * @param  c  The character for which to make the determination.
   *
   * @return  <CODE>true</CODE> if the provided character is a valid separator,
   *          or <CODE>false</CODE> if it is not.
   */
  private boolean isSeparator(char c)
  {
    switch (c)
    {
      case ' ':
      case '-':
        return true;
      default:
        return false;
    }
  }

  @Override
   public String getEqualityMatchingRule() {
  return EMR_TELEPHONE_OID;
}

  @Override
  public String getSubstringMatchingRule() {
    return SMR_TELEPHONE_OID;
  }
}
