package org.opends.sdk.schema.matchingrules;

import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_UUID_EXPECTED_DASH;
import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_UUID_EXPECTED_HEX;
import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_UUID_INVALID_LENGTH;

import org.opends.messages.Message;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

/**
 * This class defines the uuidOrderingMatch matching rule defined in RFC 4530.
 * This will be the default ordering matching rule for the UUID syntax.
 */
public class UUIDOrderingMatchingRule
    extends AbstractOrderingMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value)
      throws DecodeException
  {
    if (value.length() != 36)
    {
      Message message = WARN_ATTR_SYNTAX_UUID_INVALID_LENGTH.get(
          value.toString(), value.length());
      throw new DecodeException(message);
    }

    StringBuilder builder = new StringBuilder(36);
    char c;
    for (int i=0; i < 36; i++)
    {
      // The 9th, 14th, 19th, and 24th characters must be dashes.  All others
      // must be hex.  Convert all uppercase hex characters to lowercase.
      c = (char)value.byteAt(i);
      switch (i)
      {
        case 8:
        case 13:
        case 18:
        case 23:
          if (c != '-')
          {
            Message message = WARN_ATTR_SYNTAX_UUID_EXPECTED_DASH.get(
                value.toString(), i, String.valueOf(c));
            throw new DecodeException(message);
          }
          builder.append(c);
          break;
        default:
          switch (c)
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
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
              // These are all fine.
              builder.append(c);
              break;
            case 'A':
              builder.append('a');
              break;
            case 'B':
              builder.append('b');
              break;
            case 'C':
              builder.append('c');
              break;
            case 'D':
              builder.append('d');
              break;
            case 'E':
              builder.append('e');
              break;
            case 'F':
              builder.append('f');
              break;
            default:
              Message message = WARN_ATTR_SYNTAX_UUID_EXPECTED_HEX.get(
                  value.toString(), i, String.valueOf(value.byteAt(i)));
              throw new DecodeException(message);
          }
      }
    }

    return ByteString.valueOf(builder.toString());
  }
}
