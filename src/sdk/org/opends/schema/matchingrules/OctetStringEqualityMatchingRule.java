package org.opends.schema.matchingrules;

import org.opends.schema.SchemaUtils;
import org.opends.schema.Schema;
import static org.opends.server.schema.SchemaConstants.EMR_OCTET_STRING_NAME;
import static org.opends.server.schema.SchemaConstants.EMR_OCTET_STRING_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_OCTET_STRING_OID;
import org.opends.server.types.ByteSequence;

/**
 * This class defines the octetStringMatch matching rule defined in X.520.  It
 * will be used as the default equality matching rule for the binary and octet
 * string syntaxes.
 */
public class OctetStringEqualityMatchingRule
    extends AbstractEqualityMatchingRuleImplementation
{
  public ByteSequence normalizeAttributeValue(Schema schema,
                                              ByteSequence value)
  {
    return value;
  }
}
