package org.opends.schema.matchingrules;

import static org.opends.schema.StringPrepProfile.CASE_FOLD;
import static org.opends.schema.StringPrepProfile.TRIM;
import static org.opends.schema.StringPrepProfile.prepareUnicode;

import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.types.Assertion;
import org.opends.types.ConditionResult;
import org.opends.ldap.DecodeException;

/**
 * This class implements the keywordMatch matching rule defined in X.520.  That
 * document defines "keyword" as implementation-specific, but in this case we
 * will consider it a match if the assertion value is contained within the
 * attribute value and is bounded by the edge of the value or any of the
 * following characters:
 * <BR>
 * <UL>
 *   <LI>A space</LI>
 *   <LI>A period</LI>
 *   <LI>A comma</LI>
 *   <LI>A slash</LI>
 *   <LI>A dollar sign</LI>
 *   <LI>A plus sign</LI>
 *   <LI>A dash</LI>
 *   <LI>An underscore</LI>
 *   <LI>An octothorpe</LI>
 *   <LI>An equal sign</LI>
 * </UL>
 */
public class KeywordEqualityMatchingRule
    extends AbstractMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema,
                                            ByteSequence value)
  {
    return ByteString.valueOf(normalize(value));
  }

  @Override
  public Assertion getAssertion(Schema schema, ByteSequence value)
      throws DecodeException
  {
    final String normalStr = normalize(value);

    return new Assertion()
    {
      public ConditionResult matches(ByteString attributeValue) {
        // See if the assertion value is contained in the attribute value.
        // If not, then it isn't a match.
        String valueStr1 = attributeValue.toString();

        int pos = valueStr1.indexOf(normalStr);
        if (pos < 0)
        {
          return ConditionResult.FALSE;
        }


        if (pos > 0)
        {
          char c = valueStr1.charAt(pos-1);
          switch (c)
          {
            case ' ':
            case '.':
            case ',':
            case '/':
            case '$':
            case '+':
            case '-':
            case '_':
            case '#':
            case '=':
              // These are all acceptable.
              break;

            default:
              // Anything else is not.
              return ConditionResult.FALSE;
          }
        }


        if (valueStr1.length() > (pos + normalStr.length()))
        {
          char c = valueStr1.charAt(pos + normalStr.length());
          switch (c)
          {
            case ' ':
            case '.':
            case ',':
            case '/':
            case '$':
            case '+':
            case '-':
            case '_':
            case '#':
            case '=':
              // These are all acceptable.
              break;

            default:
              // Anything else is not.
              return ConditionResult.FALSE;
          }
        }


        // If we've gotten here, then we can assume it is a match.
        return ConditionResult.TRUE;
      }
    };
  }

  private String normalize(ByteSequence value)
  {
    StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, TRIM, CASE_FOLD);

    int bufferLength = buffer.length();
    if (bufferLength == 0)
    {
      if (value.length() > 0)
      {
        // This should only happen if the value is composed entirely of spaces.
        // In that case, the normalized value is a single space.
        return " ".intern();
      }
      else
      {
        // The value is empty, so it is already normalized.
        return "".intern();
      }
    }


    // Replace any consecutive spaces with a single space.
    for (int pos = bufferLength-1; pos > 0; pos--)
    {
      if (buffer.charAt(pos) == ' ')
      {
        if (buffer.charAt(pos-1) == ' ')
        {
          buffer.delete(pos, pos+1);
        }
      }
    }

    return buffer.toString();
  }
}
