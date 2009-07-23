package org.opends.schema.matchingrules;

import org.opends.schema.SchemaUtils;
import org.opends.schema.Schema;
import org.opends.schema.syntaxes.GeneralizedTimeSyntax;
import static org.opends.server.schema.SchemaConstants.OMR_GENERALIZED_TIME_NAME;
import static org.opends.server.schema.SchemaConstants.OMR_GENERALIZED_TIME_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_GENERALIZED_TIME_OID;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.ldap.DecodeException;

import java.util.Collections;

/**
 * This class defines the generalizedTimeOrderingMatch matching rule defined in
 * X.520 and referenced in RFC 2252.
 */
public class GeneralizedTimeOrderingMatchingRule
    extends OrderingMatchingRuleImplementation
{
  public GeneralizedTimeOrderingMatchingRule()
  {
    super(OMR_GENERALIZED_TIME_OID,
        Collections.singletonList(OMR_GENERALIZED_TIME_NAME),
        "",
        false,
        SYNTAX_GENERALIZED_TIME_OID,
        SchemaUtils.RFC4512_ORIGIN);
  }

  public ByteSequence normalizeAttributeValue(Schema schema, ByteSequence value) {
    try
    {
      return ByteString.valueOf(
          GeneralizedTimeSyntax.decodeGeneralizedTimeValue(value));
    }
    catch(DecodeException de)
    {
      return value;
    }
  }
}
