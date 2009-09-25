package org.opends.sdk.schema.matchingrules;

import org.opends.server.types.ByteString;
import org.opends.server.types.ByteSequence;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.syntaxes.UserPasswordSyntax;
import org.opends.sdk.DecodeException;
import org.opends.sdk.util.StaticUtils;

/**
 * This class implements the userPasswordExactMatch matching rule, which will
 * simply compare encoded hashed password values to see if they are exactly
 * equal to each other.
 */
public class UserPasswordExactEqualityMatchingRule
    extends AbstractMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value)
      throws DecodeException
  {
    // The normalized form of this matching rule is exactly equal to the
    // non-normalized form, except that the scheme needs to be converted to
    // lowercase (if there is one).

    if (UserPasswordSyntax.isEncoded(value))
    {
      StringBuilder builder = new StringBuilder(value.length());
      int closingBracePos = -1;
      for (int i=1; i < value.length(); i++)
      {
        if (value.byteAt(i) == '}')
        {
          closingBracePos = i;
          break;
        }
      }
      ByteSequence seq1 = value.subSequence(0, closingBracePos + 1);
      ByteSequence seq2 =
        value.subSequence(closingBracePos + 1, value.length());
      StaticUtils.toLowerCase(seq1, builder, false);
      builder.append(seq2);
      return ByteString.valueOf(builder.toString());
    }
    else
    {
      return value.toByteString();
    }
  }
}
