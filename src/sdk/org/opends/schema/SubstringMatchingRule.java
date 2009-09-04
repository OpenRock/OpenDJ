package org.opends.schema;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.opends.server.types.ByteSequence;
import org.opends.types.Assertion;
import org.opends.ldap.DecodeException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 4, 2009
 * Time: 1:41:56 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class SubstringMatchingRule extends MatchingRule
{
  protected SubstringMatchingRule(String oid, List<String> names,
                                 String description, boolean obsolete,
                                 String syntax,
                                 Map<String, List<String>> extraProperties,
                                 String definition)
  {
    super(oid, names, description, obsolete, syntax, extraProperties,
        definition);
  }

  /**
   * Retrieves the normalized form of the provided assertion substring values,
   * which is best suite for efficiently performing matching operations on that
   * value.
   *
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
  public abstract Assertion getAssertion(ByteSequence subInitial,
                                       List<ByteSequence> subAnyElements,
                                       ByteSequence subFinal)
      throws DecodeException;
}
