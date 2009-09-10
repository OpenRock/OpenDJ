package org.opends.sdk.schema.matchingrules;

import org.opends.sdk.Assertion;
import org.opends.sdk.ConditionResult;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

import java.util.List;
import java.util.LinkedList;
import java.util.Comparator;

/**
 * This class implements a default equality or approximate matching rule that
 * matches normalized values in byte order.
 */
public abstract class AbstractMatchingRuleImplementation
    implements MatchingRuleImplementation
{
  public static final Assertion UNDEFINED_ASSERTION = new Assertion()
  {
    public ConditionResult matches(ByteString attributeValue) {
      return ConditionResult.UNDEFINED;
    }
  };

  private static final Comparator<ByteSequence> DEFAULT_COMPARATOR =
      new Comparator<ByteSequence>()
      {
        public int compare(ByteSequence o1, ByteSequence o2) {
          return o1.compareTo(o2);
        }
      };

  protected static class DefaultEqualityAssertion implements Assertion
  {
    ByteSequence normalizedAssertionValue;

    protected DefaultEqualityAssertion(ByteSequence normalizedAssertionValue) {
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
    return new DefaultEqualityAssertion(normalizeAttributeValue(schema, value));
  }

  public Assertion getAssertion(Schema schema, ByteSequence subInitial,
                                List<ByteSequence> subAnyElements,
                                ByteSequence subFinal)
      throws DecodeException
  {
    return UNDEFINED_ASSERTION;
  }

  public Assertion getGreaterOrEqualAssertion(Schema schema,
                                                ByteSequence value)
      throws DecodeException
  {
    return UNDEFINED_ASSERTION;
  }

  public Assertion getLessOrEqualAssertion(Schema schema, ByteSequence value)
      throws DecodeException
  {
    return UNDEFINED_ASSERTION;
  }

  public Comparator<ByteSequence> comparator(Schema schema) {
    return DEFAULT_COMPARATOR;
  }
}
