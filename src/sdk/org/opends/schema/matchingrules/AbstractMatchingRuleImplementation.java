package org.opends.schema.matchingrules;

import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.types.ConditionResult;
import org.opends.types.Assertion;
import org.opends.ldap.DecodeException;

import java.util.Comparator;

/**
 * This class defines the set of methods and structures that must be
 * implemented by a Directory Server module that implements a matching
 * rule used for equality matching.
 */
public abstract class AbstractMatchingRuleImplementation
    implements MatchingRuleImplementation
{
  protected class ByteOrderAssertion implements Assertion
  {
    ByteSequence normalizedAssertionValue;

    protected ByteOrderAssertion(ByteSequence normalizedAssertionValue) {
      this.normalizedAssertionValue = normalizedAssertionValue;
    }

    public ConditionResult matches(ByteString attributeValue) {
      return normalizedAssertionValue.equals(attributeValue) ?
        ConditionResult.TRUE : ConditionResult.FALSE;
    }
  }

  public Assertion getAssertion(Schema schema, ByteSequence value)
      throws DecodeException
  {
    return new ByteOrderAssertion(normalizeAttributeValue(schema, value));
  }
}
