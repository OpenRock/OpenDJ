package org.opends.schema.matchingrules;

import org.opends.schema.SchemaUtils;
import org.opends.schema.Schema;
import static org.opends.server.schema.SchemaConstants.SMR_OCTET_STRING_NAME;
import static org.opends.server.schema.SchemaConstants.SMR_OCTET_STRING_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_SUBSTRING_ASSERTION_OID;
import org.opends.server.types.ByteSequence;

import java.util.Collections;

/**
 * This class defines the octetStringSubstringsMatch matching rule defined in
 * X.520.  It will be used as the default substring matching rule for the binary
 * and octet string syntaxes.
 */
public class OctetStringSubstringMatchingRule
    extends SubstringMatchingRuleImplementation
{
  public OctetStringSubstringMatchingRule()
  {
    super(SMR_OCTET_STRING_OID,
        Collections.singletonList(SMR_OCTET_STRING_NAME),
        "",
        false,
        SYNTAX_SUBSTRING_ASSERTION_OID,
        SchemaUtils.RFC4512_ORIGIN);
  }

  public ByteSequence normalizeAttributeValue(Schema schema,
                                              ByteSequence value)
  {
    return value;
  }
}
