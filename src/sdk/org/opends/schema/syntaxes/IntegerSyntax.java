package org.opends.schema.syntaxes;

import static org.opends.messages.SchemaMessages.*;
import static org.opends.server.schema.SchemaConstants.*;

import org.opends.messages.MessageBuilder;
import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;

/**
 * This class defines the integer attribute syntax, which holds an
 * arbitrarily-long integer value.  Equality, ordering, and substring matching
 * will be allowed by default.
 */
public class IntegerSyntax extends AbstractSyntaxImplementation
{
  public String getName() {
    return SYNTAX_INTEGER_NAME;
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
    String valueString = value.toString();
    int    length      = valueString.length();

    if (length == 0)
    {
      invalidReason.append(
              WARN_ATTR_SYNTAX_INTEGER_EMPTY_VALUE.get(valueString));
      return false;
    }
    else if (length == 1)
    {
      switch (valueString.charAt(0))
      {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          return true;
        case '-':
          invalidReason.append(WARN_ATTR_SYNTAX_INTEGER_DASH_NEEDS_VALUE.get(
                  valueString));
          return false;
        default:
          invalidReason.append(WARN_ATTR_SYNTAX_INTEGER_INVALID_CHARACTER.get(
                  valueString,
                  valueString.charAt(0), 0));
          return false;
      }
    }
    else
    {
      boolean negative = false;

      switch (valueString.charAt(0))
      {
        case '0':
          invalidReason.append(WARN_ATTR_SYNTAX_INTEGER_INITIAL_ZERO.get(
                  valueString));
          return false;
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          // These are all fine.
          break;
        case '-':
          // This is fine too.
          negative = true;
          break;
        default:
          invalidReason.append(WARN_ATTR_SYNTAX_INTEGER_INVALID_CHARACTER.get(
                  valueString,
                  valueString.charAt(0), 0));
          return false;
      }

      switch (valueString.charAt(1))
      {
        case '0':
          // This is fine as long as the value isn't negative.
          if (negative)
          {
            invalidReason.append(WARN_ATTR_SYNTAX_INTEGER_INITIAL_ZERO.get(
                    valueString));
            return false;
          }
          break;
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          // These are all fine.
          break;
        default:
          invalidReason.append(WARN_ATTR_SYNTAX_INTEGER_INVALID_CHARACTER.get(
                  valueString,
                  valueString.charAt(0), 0));
          return false;
      }

      for (int i=2; i < length; i++)
      {
        switch (valueString.charAt(i))
        {
          case '0':
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            // These are all fine.
            break;
          default:
            invalidReason.append(WARN_ATTR_SYNTAX_INTEGER_INVALID_CHARACTER.get(
                    valueString,
                    valueString.charAt(0), 0));
            return false;
        }
      }

      return true;
    }
  }
}
