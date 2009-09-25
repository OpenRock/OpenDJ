package org.opends.sdk.schema.syntaxes;

import org.opends.messages.MessageBuilder;
import org.opends.messages.SchemaMessages;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;
import static org.opends.sdk.schema.SchemaConstants.*;

/**
 * This class defines the substring assertion attribute syntax, which contains
 * one or more substring components, as used in a substring search filter.  For
 * the purposes of matching, it will be treated like a Directory String syntax
 * except that approximate matching will not be allowed.
 */
public class SubstringAssertionSyntax extends AbstractSyntaxImplementation
{

  public String getName() {
    return SYNTAX_SUBSTRING_ASSERTION_NAME;
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
    // Get the string representation of the value and check its length.  A
    // zero-length value is acceptable.  A one-length value is acceptable as
    // long as it is not an asterisk.  For all other lengths, just ensure that
    // there are no consecutive wildcards.
    String valueString = value.toString();
    int    valueLength = valueString.length();
    if (valueLength == 0)
    {
      return true;
    }
    else if (valueLength == 1)
    {
      if (valueString.charAt(0) == '*')
      {
        invalidReason.append(
            SchemaMessages.WARN_ATTR_SYNTAX_SUBSTRING_ONLY_WILDCARD.get());

        return false;
      }
      else
      {
        return true;
      }
    }
    else
    {
      for (int i=1; i < valueLength; i++)
      {
        if ((valueString.charAt(i) == '*') && (valueString.charAt(i-1) == '*'))
        {
          invalidReason.append(
              SchemaMessages.WARN_ATTR_SYNTAX_SUBSTRING_CONSECUTIVE_WILDCARDS.
                  get(valueString, i));
          return false;
        }
      }

      return true;
    }
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
}
