package org.opends.sdk.schema.matchingrules;

import java.util.List;

import org.opends.sdk.Assertion;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;

/**
 * This interface defines the set of methods that must be
 * implemented by a Directory Server module that implements a matching
 * rule used for substring matching.
 */
public interface SubstringMatchingRuleImplementation
    extends MatchingRuleImplementation
{
  /**
   * Retrieves the normalized form of the provided assertion substring values,
   * which is best suite for efficiently performing matching operations on that
   * value.
   *
   * @param schema The schema in which this matching rule is defined.
   * @param  subInitial      The normalized substring value fragment
   *                         that should appear at the beginning of
   *                         the target value.
   * @param  subAnyElements  The normalized substring value fragments
   *                         that should appear in the middle of the
   *                         target value.
   * @param  subFinal        The normalized substring value fragment
   *                         that should appear at the end of the
   *                         target value.
   * @return The normalized version of the provided assertion value.
   */
  public Assertion getAssertion(Schema schema, ByteSequence subInitial,
                                       List<ByteSequence> subAnyElements,
                                       ByteSequence subFinal)
      throws DecodeException;
}
