package org.opends.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_OTHER_MAILBOX_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_OTHER_MAILBOX_NAME;
import static org.opends.server.schema.SchemaConstants.SYNTAX_OTHER_MAILBOX_DESCRIPTION;
import org.opends.server.types.ByteSequence;
import org.opends.messages.MessageBuilder;
import static org.opends.messages.SchemaMessages.*;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_OTHER_MAILBOX_ILLEGAL_MB_CHAR;
import org.opends.schema.SchemaUtils;

/**
 * This class implements the other mailbox attribute syntax, which consists of a
 * printable string component (the mailbox type) followed by a dollar sign and
 * an IA5 string component (the mailbox).  Equality and substring matching will
 * be allowed by default.
 */
public class OtherMailboxSyntax extends SyntaxDescription
{
  public OtherMailboxSyntax()
  {
    super(SYNTAX_OTHER_MAILBOX_OID, SYNTAX_OTHER_MAILBOX_NAME,
        SYNTAX_OTHER_MAILBOX_DESCRIPTION, SchemaUtils.RFC4512_ORIGIN);
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
    // Check to see if the provided value was null.  If so, then that's not
    // acceptable.
    if (value == null)
    {

      invalidReason.append(ERR_ATTR_SYNTAX_OTHER_MAILBOX_EMPTY_VALUE.get());
      return false;
    }


    // Get the value as a string and determine its length.  If it is empty, then
    // that's not acceptable.
    String valueString = value.toString();
    int    valueLength = valueString.length();
    if (valueLength == 0)
    {

      invalidReason.append(ERR_ATTR_SYNTAX_OTHER_MAILBOX_EMPTY_VALUE.get());
      return false;
    }


    // Iterate through the characters in the vale until we find a dollar sign.
    // Every character up to that point must be a printable string character.
    int pos = 0;
    for ( ; pos < valueLength; pos++)
    {
      char c = valueString.charAt(pos);
      if (c == '$')
      {
        if (pos == 0)
        {

          invalidReason.append(ERR_ATTR_SYNTAX_OTHER_MAILBOX_NO_MBTYPE.get(
                  valueString));
          return false;
        }

        pos++;
        break;
      }
      else if (! PrintableStringSyntax.isPrintableCharacter(c))
      {

        invalidReason.append(
                ERR_ATTR_SYNTAX_OTHER_MAILBOX_ILLEGAL_MBTYPE_CHAR.get(
                        valueString, String.valueOf(c), pos));
        return false;
      }
    }


    // Make sure there is at least one character left for the mailbox.
    if (pos >= valueLength)
    {

      invalidReason.append(ERR_ATTR_SYNTAX_OTHER_MAILBOX_NO_MAILBOX.get(
              valueString));
      return false;
    }


    // The remaining characters in the value must be IA5 (ASCII) characters.
    for ( ; pos < valueLength; pos++)
    {
      char c = valueString.charAt(pos);
      if (c != (c & 0x7F))
      {

        invalidReason.append(ERR_ATTR_SYNTAX_OTHER_MAILBOX_ILLEGAL_MB_CHAR.get(
                valueString, String.valueOf(c), pos));
        return false;
      }
    }


    // If we've gotten here, then the value is OK.
    return true;
  }
}
