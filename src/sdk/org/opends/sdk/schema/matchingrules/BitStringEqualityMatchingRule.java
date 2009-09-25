package org.opends.sdk.schema.matchingrules;

import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_BIT_STRING_TOO_SHORT;
import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_BIT_STRING_NOT_QUOTED;
import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_BIT_STRING_INVALID_BIT;

/**
 * This class defines the bitStringMatch matching rule defined in X.520 and
 * referenced in RFC 2252.
 */
public class BitStringEqualityMatchingRule
    extends AbstractMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value)
      throws DecodeException
  {
String valueString = value.toString().toUpperCase();

    int length = valueString.length();
    if (length < 3)
    {
      Message message = WARN_ATTR_SYNTAX_BIT_STRING_TOO_SHORT.get(
          value.toString());
      throw new DecodeException(message);
    }


    if ((valueString.charAt(0) != '\'') ||
        (valueString.charAt(length-2) != '\'') ||
        (valueString.charAt(length-1) != 'B'))
    {
      Message message = WARN_ATTR_SYNTAX_BIT_STRING_NOT_QUOTED.get(
          value.toString());
      throw new DecodeException(message);
    }


    for (int i=1; i < (length-2); i++)
    {
      switch (valueString.charAt(i))
      {
        case '0':
        case '1':
          // These characters are fine.
          break;
        default:
          Message message = WARN_ATTR_SYNTAX_BIT_STRING_INVALID_BIT.get(
              value.toString(), String.valueOf(valueString.charAt(i)));
          throw new DecodeException(message);
      }
    }

    return ByteString.valueOf(valueString);
  }
}
