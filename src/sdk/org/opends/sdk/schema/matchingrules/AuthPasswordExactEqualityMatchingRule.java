package org.opends.sdk.schema.matchingrules;

import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.syntaxes.AuthPasswordSyntax;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

/**
 * This class implements the authPasswordMatch matching rule defined in RFC
 * 3112.
 */
public class AuthPasswordExactEqualityMatchingRule
    extends AbstractMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value)
      throws DecodeException
  {
    StringBuilder[] authPWComponents =
        AuthPasswordSyntax.decodeAuthPassword(value.toString());

    StringBuilder normalizedValue =
        new StringBuilder(2 + authPWComponents[0].length() +
                          authPWComponents[1].length() +
                          authPWComponents[2].length());
    normalizedValue.append(authPWComponents[0]);
    normalizedValue.append('$');
    normalizedValue.append(authPWComponents[1]);
    normalizedValue.append('$');
    normalizedValue.append(authPWComponents[2]);

    return ByteString.valueOf(normalizedValue.toString());
  }
}
