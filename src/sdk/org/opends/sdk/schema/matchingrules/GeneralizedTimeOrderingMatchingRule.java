package org.opends.sdk.schema.matchingrules;

import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.syntaxes.GeneralizedTimeSyntax;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

/**
 * This class defines the generalizedTimeOrderingMatch matching rule defined in
 * X.520 and referenced in RFC 2252.
 */
public class GeneralizedTimeOrderingMatchingRule
    extends AbstractOrderingMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value)
      throws DecodeException
  {
    return ByteString.valueOf(
        GeneralizedTimeSyntax.decodeGeneralizedTimeValue(value));
  }
}
