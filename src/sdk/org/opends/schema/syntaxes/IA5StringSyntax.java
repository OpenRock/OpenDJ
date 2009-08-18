package org.opends.schema.syntaxes;

import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_IA5_ILLEGAL_CHARACTER;
import static org.opends.server.schema.SchemaConstants.SYNTAX_IA5_STRING_NAME;

import org.opends.messages.Message;
import org.opends.messages.MessageBuilder;
import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;

/**
 * This class implements the IA5 string attribute syntax, which is simply a
 * set of ASCII characters.  By default, they will be treated in a
 * case-insensitive manner, and equality, ordering, substring, and approximate
 * matching will be allowed.
 */
public class IA5StringSyntax extends AbstractSyntaxImplementation
{
  public String getName() {
    return SYNTAX_IA5_STRING_NAME;
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
    // We will allow any value that does not contain any non-ASCII characters.
    // Empty values are acceptable as well.
    byte b;
    for (int i = 0; i < value.length(); i++)
    {
      b = value.byteAt(i);
      if ((b & 0x7F) != b)
      {

        Message message = WARN_ATTR_SYNTAX_IA5_ILLEGAL_CHARACTER.get(
                value.toString(), String.valueOf(b));
        invalidReason.append(message);
        return false;
      }
    }

    return true;
  }
}
