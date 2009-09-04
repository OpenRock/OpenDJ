package org.opends.schema.matchingrules;

import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.types.ConditionResult;
import org.opends.types.Assertion;
import org.opends.ldap.DecodeException;

import java.util.Comparator;

/**
 * This interface defines the set of methods that must be implemented
 * by a Directory Server module that implements a matching
 * rule used for determining the correct order of values when sorting
 * or processing range filters.
 */
public abstract class AbstractOrderingMatchingRuleImplementation
    implements OrderingMatchingRuleImplementation
{
  private static final Comparator<ByteSequence> BYTE_ORDER_COMPARATOR =
      new Comparator<ByteSequence>()
      {
        public int compare(ByteSequence o1, ByteSequence o2) {
          return o1.compareTo(o2);
        }
      };

  public Comparator<ByteSequence> comparator(Schema schema) {
    return BYTE_ORDER_COMPARATOR;
  }

  public Assertion getGreaterOrEqualAssertion(Schema schema, ByteSequence value)
      throws DecodeException
  {
    final ByteString normAssertion = normalizeAttributeValue(schema, value);
    return new Assertion()
    {
      public ConditionResult matches(ByteString attributeValue) {
        return attributeValue.compareTo(normAssertion) >= 0 ?
            ConditionResult.TRUE : ConditionResult.FALSE;
      }
    };
  }

  public Assertion getLessOrEqualAssertion(Schema schema, ByteSequence value)
      throws DecodeException
  {
    final ByteString normAssertion = normalizeAttributeValue(schema, value);
    return new Assertion()
    {
      public ConditionResult matches(ByteString attributeValue) {
        return attributeValue.compareTo(normAssertion) <= 0 ?
            ConditionResult.TRUE : ConditionResult.FALSE;
      }
    };
  }

  public Assertion getAssertion(Schema schema, ByteSequence value)
      throws DecodeException {
  final ByteString normAssertion = normalizeAttributeValue(schema, value);
    return new Assertion()
    {
      public ConditionResult matches(ByteString attributeValue) {
        return attributeValue.compareTo(normAssertion) < 0 ?
            ConditionResult.TRUE : ConditionResult.FALSE;
      }
    };
  }
}
