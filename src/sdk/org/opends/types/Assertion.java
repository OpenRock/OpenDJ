package org.opends.types;

import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 26, 2009
 * Time: 5:22:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Assertion
{
  /**
   * Indicates whether the provided attribute value should be
   * considered a match for this assertion value according to the matching rule.
   *
   * @param attributeValue The attribute value.
   * @return {@code TRUE} if the attribute value should be considered
   *         a match for the provided assertion value, {@code FALSE}
   *         if it does not match, or {@code UNDEFINED} if the result
   *         is undefined.
   */
  public abstract ConditionResult matches(ByteString attributeValue);
}
