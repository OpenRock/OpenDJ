package org.opends.schema.matchingrules;

import org.opends.schema.SchemaUtils;
import org.opends.schema.Schema;
import static org.opends.server.schema.SchemaConstants.EMR_UNIQUE_MEMBER_NAME;
import static org.opends.server.schema.SchemaConstants.EMR_UNIQUE_MEMBER_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_NAME_AND_OPTIONAL_UID_OID;
import org.opends.server.types.ByteSequence;

import java.util.Collections;

/**
 * This class implements the uniqueMemberMatch matching rule defined in X.520
 * and referenced in RFC 2252.  It is based on the name and optional UID syntax,
 * and will compare values with a distinguished name and optional bit string
 * suffix.
 */
public class UniqueMemberEqualityMatchingRule
    extends EqualityMatchingRuleImplementation
{
  public UniqueMemberEqualityMatchingRule()
  {
    super(EMR_UNIQUE_MEMBER_OID,
        Collections.singletonList(EMR_UNIQUE_MEMBER_NAME),
        "",
        false,
        SYNTAX_NAME_AND_OPTIONAL_UID_OID,
        SchemaUtils.RFC4512_ORIGIN);
  }

  public ByteSequence normalizeAttributeValue(Schema schema,
                                              ByteSequence value)
  {
    return value;
  }
}
