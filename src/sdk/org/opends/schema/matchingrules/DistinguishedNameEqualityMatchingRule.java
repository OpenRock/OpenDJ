package org.opends.schema.matchingrules;

import static org.opends.server.util.StaticUtils.toLowerCase;

import org.opends.ldap.DecodeException;
import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.types.ConditionResult;
import org.opends.types.DN;
import org.opends.types.Assertion;

/**
 * This class defines the distinguishedNameMatch matching rule defined in X.520
 * and referenced in RFC 2252.
 */
public class DistinguishedNameEqualityMatchingRule
    extends AbstractMatchingRuleImplementation
{
  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value)
  {
    return ByteString.valueOf(DN.valueOf(value.toString(), schema).toString());
  }

  @Override
  public Assertion getAssertion(final Schema schema, ByteSequence value)
      throws DecodeException
  {
    final DN assertion = DN.valueOf(value.toString(), schema);
    return new Assertion()
    {
      public ConditionResult matches(ByteString attributeValue) {
        DN attribute = DN.valueOf(attributeValue.toString(), schema);
        return attribute.matches(assertion);
      }
    };
  }
}
