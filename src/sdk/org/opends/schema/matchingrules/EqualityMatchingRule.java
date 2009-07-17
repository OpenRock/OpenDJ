package org.opends.schema.matchingrules;

import org.opends.server.types.ByteSequence;
import org.opends.server.types.ConditionResult;
import org.opends.schema.MatchingRule;

import java.util.List;
import java.util.Map;

/**
 * This class defines the set of methods and structures that must be
 * implemented by a Directory Server module that implements a matching
 * rule used for equality matching.
 */
public abstract class EqualityMatchingRule extends MatchingRule
{
  protected EqualityMatchingRule(String oid, List<String> names,
                                 String description, boolean obsolete,
                                 String syntax,
                                 Map<String, List<String>> extraProperties)
  {
    super(oid, names, description, obsolete, syntax, extraProperties);
  }

  protected EqualityMatchingRule(String oid, List<String> names,
                                 String description, boolean obsolete,
                                 String syntax,
                                 Map<String, List<String>> extraProperties,
                                 String definition)
  {
    super(oid, names, description, obsolete, syntax, extraProperties,
        definition);
  }

  /**
   * Indicates whether the two provided normalized values are equal to
   * each other.
   *
   * @param  value1  The normalized form of the first value to
   *                 compare.
   * @param  value2  The normalized form of the second value to
   *                 compare.
   *
   * @return  {@code true} if the provided values are equal, or
   *          {@code false} if not.
   */
  public boolean areEqual(ByteSequence value1, ByteSequence value2)
  {
    return value1.equals(value2);
  }

  /**
   * Indicates whether the provided attribute value should be
   * considered a match for the given assertion value.  This will only
   * be used for the purpose of extensible matching.  Other forms of
   * matching against equality matching rules should use the
   * {@code areEqual} method.
   *
   * @param  attributeValue  The attribute value in a form that has
   *                         been normalized according to this
   *                         matching rule.
   * @param  assertionValue  The assertion value in a form that has
   *                         been normalized according to this
   *                         matching rule.
   *
   * @return  {@code true} if the attribute value should be considered
   *          a match for the provided assertion value, or
   *          {@code false} if not.
   */
  public ConditionResult valuesMatch(ByteSequence attributeValue,
                                     ByteSequence assertionValue)
  {
    if (areEqual(attributeValue, assertionValue))
    {
      return ConditionResult.TRUE;
    }
    else
    {
      return ConditionResult.FALSE;
    }
  }



  /**
   * Generates a hash code for the provided attribute value.  This
   * version of the method will simply create a hash code from the
   * normalized form of the attribute value.  For matching rules
   * explicitly designed to work in cases where byte-for-byte
   * comparisons of normalized values is not sufficient for
   * determining equality (e.g., if the associated attribute syntax is
   * based on hashed or encrypted values), then this method must be
   * overridden to provide an appropriate implementation for that
   * case.
   *
   * @param  attributeValue  The attribute value for which to generate
   *                         the hash code.
   *
   * @return  The hash code generated for the provided attribute
   *          value.
   */
  public int generateHashCode(ByteSequence attributeValue)
  {
    return attributeValue.hashCode();
  }
}
