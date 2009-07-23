package org.opends.schema.matchingrules;

import org.opends.server.types.ByteSequence;
import org.opends.schema.MatchingRule;
import org.opends.schema.Schema;
import org.opends.types.ConditionResult;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;

/**
 * This class defines the set of methods and structures that must be
 * implemented by a Directory Server module that implements a matching
 * rule used for substring matching.
 */
public abstract class SubstringMatchingRuleImplementation
    extends MatchingRuleImplementation
{
  protected SubstringMatchingRuleImplementation(String oid, List<String> names,
                                               String description,
                                               boolean obsolete, String syntax,
                                               Map<String,
                                               List<String>> extraProperties)
  {
    super(oid, names, description, obsolete, syntax, extraProperties);
  }

  protected SubstringMatchingRuleImplementation(
      MatchingRule orginalMatchingRule) {
    super(orginalMatchingRule);
  }

  /**
   * Retrieves the normalized form of the provided attribute value, which is
   * best suite for efficiently performing matching operations on
   * that value.
   *
   * @param schema The schema to use to lookup schema elements if needed.
   * @param value
   *          The attribute value to be normalized.
   * @return The normalized version of the provided attribute value.
   */
  public abstract ByteSequence normalizeAttributeValue(Schema schema,
                                                       ByteSequence value);

  /**
   * Retrieves the normalized form of the provided initial assertion value
   * substring, which is best suite for efficiently performing matching
   * operations on that value.
   *
   * @param schema The schema to use to lookup schema elements if needed.
   * @param value The initial assertion value substring to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public ByteSequence normalizeSubInitialValue(Schema schema,
                                               ByteSequence value)
  {
    return normalizeAttributeValue(null, value);
  }

  /**
   * Retrieves the normalized form of the provided middle assertion value
   * substring, which is best suite for efficiently performing matching
   * operations on that value.
   *
   * @param schema The schema to use to lookup schema elements if needed.
   * @param value The middle assertion value substring to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public ByteSequence normalizeSubAnyValue(Schema schema, ByteSequence value)
  {
    return normalizeAttributeValue(null, value);
  }

  /**
   * Retrieves the normalized form of the provided final assertion value
   * substring, which is best suite for efficiently performing matching
   * operations on that value.
   *
   * @param schema The schema to use to lookup schema elements if needed.
   * @param value The final assertion value substring to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public ByteSequence normalizeSubFinalValue(Schema schema, ByteSequence value)
  {
    return normalizeAttributeValue(null, value);
  }

  /**
   * Determines whether the provided value matches the given substring
   * filter components.  Note that any of the substring filter
   * components may be {@code null} but at least one of them must be
   * non-{@code null}.
   *
   * @param schema The schema to use to lookup schema elements if needed.
   * @param attributeValue The normalized attribute value against which to
   *                       compare the substring components.
   * @param  subInitial      The normalized substring value fragment
   *                         that should appear at the beginning of
   *                         the target value.
   * @param  subAnyElements  The normalized substring value fragments
 *                         that should appear in the middle of the
 *                         target value.
   * @param  subFinal        The normalized substring value fragment
*                         that should appear at the end of the
*                         target value.
* @return  {@code true} if the provided value does match the given
   *          substring components, or {@code false} if not.
   */
  public boolean valueMatchesSubstring(Schema schema,
                                       ByteSequence attributeValue,
                                       ByteSequence subInitial,
                                       List<ByteSequence> subAnyElements,
                                       ByteSequence subFinal)
  {
    int valueLength = attributeValue.length();

    int pos = 0;
    if (subInitial != null)
    {
      int initialLength = subInitial.length();
      if (initialLength > valueLength)
      {
        return false;
      }

      for (; pos < initialLength; pos++)
      {
        if (subInitial.byteAt(pos) != attributeValue.byteAt(pos))
        {
          return false;
        }
      }
    }


    if ((subAnyElements != null) && (! subAnyElements.isEmpty()))
    {
      for (ByteSequence element : subAnyElements)
      {
        int anyLength = element.length();
        if(anyLength == 0)
            continue;
        int end = valueLength - anyLength;
        boolean match = false;
        for (; pos <= end; pos++)
        {
          if (element.byteAt(0) == attributeValue.byteAt(pos))
          {
            boolean subMatch = true;
            for (int i=1; i < anyLength; i++)
            {
              if (element.byteAt(i) != attributeValue.byteAt(pos+i))
              {
                subMatch = false;
                break;
              }
            }

            if (subMatch)
            {
              match = subMatch;
              break;
            }
          }
        }

        if (match)
        {
          pos += anyLength;
        }
        else
        {
          return false;
        }
      }
    }


    if (subFinal != null)
    {
      int finalLength = subFinal.length();

      if ((valueLength - finalLength) < pos)
      {
        return false;
      }

      pos = valueLength - finalLength;
      for (int i=0; i < finalLength; i++,pos++)
      {
        if (subFinal.byteAt(i) != attributeValue.byteAt(pos))
        {
          return false;
        }
      }
    }


    return true;
  }

  public ConditionResult valuesMatch(Schema schema, ByteSequence attributeValue,
                                     ByteSequence assertionValue)
  {
    ByteSequence initialString = null;
    ByteSequence finalString = null;
    LinkedList<ByteSequence> anyStrings = null;

    int lastAsteriskIndex = -1;
    int length = assertionValue.length();
    for (int i = 0; i < length; i++)
    {
      if (assertionValue.byteAt(i) == '*')
      {
        if (lastAsteriskIndex == -1)
        {
          if (i > 0)
          {
            // Got an initial substring.
            initialString = assertionValue.subSequence(0, i);
          }
          lastAsteriskIndex = i;
        }
        else
        {
          // Got an any substring.
          if (anyStrings == null)
          {
            anyStrings = new LinkedList<ByteSequence>();
          }

          int s = lastAsteriskIndex + 1;
          if (s != i)
          {
            anyStrings.add(assertionValue.subSequence(s, i));
          }

          lastAsteriskIndex = i;
        }
      }
    }

    if (lastAsteriskIndex == length - 1)
    {
      // Got a final substring.
      finalString =
          assertionValue.subSequence(lastAsteriskIndex, length);
    }

    ByteSequence normAttributeValue = normalizeAttributeValue(null, attributeValue);
    ByteSequence normInitialString = initialString == null ?  null :
        normalizeSubInitialValue(null, initialString);

    List<ByteSequence> normAnyStrings;
    if(anyStrings != null && !anyStrings.isEmpty())
    {
      normAnyStrings = new ArrayList<ByteSequence>(anyStrings.size());
      for(ByteSequence anyString : anyStrings)
      {
        normAnyStrings.add(normalizeSubAnyValue(null, anyString));
      }
    }
    else
    {
      normAnyStrings = null;
    }

    ByteSequence normFinalString = finalString == null ?  null :
        normalizeSubFinalValue(null, initialString);

    return valueMatchesSubstring(null, normAttributeValue, normInitialString,
        normAnyStrings, normFinalString) ?
        ConditionResult.TRUE : ConditionResult.FALSE;
  }
}
