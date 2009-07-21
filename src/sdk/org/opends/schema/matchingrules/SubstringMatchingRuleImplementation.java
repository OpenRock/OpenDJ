package org.opends.schema.matchingrules;

import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.schema.Syntax;
import org.opends.schema.MatchingRule;
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
   * @param value
   *          The attribute value to be normalized.
   * @return The normalized version of the provided attribute value.
   */
  public abstract ByteSequence normalizeAttributeValue(ByteSequence value);

  /**
   * Retrieves the normalized form of the provided initial assertion value
   * substring, which is best suite for efficiently performing matching
   * operations on that value.
   *
   * @param value The initial assertion value substring to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public ByteSequence normalizeSubInitialValue(ByteSequence value)
  {
    return normalizeAttributeValue(value);
  }

  /**
   * Retrieves the normalized form of the provided middle assertion value
   * substring, which is best suite for efficiently performing matching
   * operations on that value.
   *
   * @param value The middle assertion value substring to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public ByteSequence normalizeSubAnyValue(ByteSequence value)
  {
    return normalizeAttributeValue(value);
  }

  /**
   * Retrieves the normalized form of the provided final assertion value
   * substring, which is best suite for efficiently performing matching
   * operations on that value.
   *
   * @param value The final assertion value substring to be normalized.
   * @return The normalized version of the provided assertion value.
   */
  public ByteSequence normalizeSubFinalValue(ByteSequence value)
  {
    return normalizeAttributeValue(value);
  }

  /**
   * Determines whether the provided value matches the given substring
   * filter components.  Note that any of the substring filter
   * components may be {@code null} but at least one of them must be
   * non-{@code null}.
   *
   * @param  value           The normalized value against which to
   *                         compare the substring components.
   * @param  subInitial      The normalized substring value fragment
   *                         that should appear at the beginning of
   *                         the target value.
   * @param  subAnyElements  The normalized substring value fragments
   *                         that should appear in the middle of the
   *                         target value.
   * @param  subFinal        The normalized substring value fragment
   *                         that should appear at the end of the
   *                         target value.
   *
   * @return  {@code true} if the provided value does match the given
   *          substring components, or {@code false} if not.
   */
  public boolean valueMatchesSubstring(ByteSequence value,
                                    ByteSequence subInitial,
                                    List<ByteSequence> subAnyElements,
                                    ByteSequence subFinal)
  {
    int valueLength = value.length();

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
        if (subInitial.byteAt(pos) != value.byteAt(pos))
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
          if (element.byteAt(0) == value.byteAt(pos))
          {
            boolean subMatch = true;
            for (int i=1; i < anyLength; i++)
            {
              if (element.byteAt(i) != value.byteAt(pos+i))
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
        if (subFinal.byteAt(i) != value.byteAt(pos))
        {
          return false;
        }
      }
    }


    return true;
  }

  public ConditionResult valuesMatch(ByteSequence attributeValue,
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

    ByteSequence normAttributeValue = normalizeAttributeValue(attributeValue);
    ByteSequence normInitialString = initialString == null ?  null :
        normalizeSubInitialValue(initialString);

    List<ByteSequence> normAnyStrings;
    if(anyStrings != null && !anyStrings.isEmpty())
    {
      normAnyStrings = new ArrayList<ByteSequence>(anyStrings.size());
      for(ByteSequence anyString : anyStrings)
      {
        normAnyStrings.add(normalizeSubAnyValue(anyString));
      }
    }
    else
    {
      normAnyStrings = null;
    }

    ByteSequence normFinalString = finalString == null ?  null :
        normalizeSubFinalValue(initialString);

    return valueMatchesSubstring(normAttributeValue, normInitialString,
        normAnyStrings, normFinalString) ?
        ConditionResult.TRUE : ConditionResult.FALSE;
  }
}
