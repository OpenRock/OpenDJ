package org.opends.sdk.schema.matchingrules;

import java.util.LinkedList;
import java.util.List;

import org.opends.messages.SchemaMessages;
import org.opends.sdk.Assertion;
import org.opends.sdk.ConditionResult;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.util.StaticUtils;
import org.opends.sdk.util.SubstringReader;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

/**
 * This class implements a default substring matching rule that
 * matches normalized substring assertion values in byte order.
 */
public abstract class AbstractSubstringMatchingRuleImplementation
    extends AbstractMatchingRuleImplementation
{

  protected static class DefaultSubstringAssertion implements Assertion
  {
    private ByteString normInitial;
    private ByteString[] normAnys;
    private ByteString normFinal;

    protected DefaultSubstringAssertion(ByteString normInitial, ByteString[] normAnys,
                               ByteString normFinal)
    {
      this.normInitial = normInitial;
      this.normAnys = normAnys;
      this.normFinal = normFinal;
    }

    public ConditionResult matches(ByteString attributeValue) {
    int valueLength = attributeValue.length();

    int pos = 0;
    if (normInitial != null)
    {
      int initialLength = normInitial.length();
      if (initialLength > valueLength)
      {
        return ConditionResult.FALSE;
      }

      for (; pos < initialLength; pos++)
      {
        if (normInitial.byteAt(pos) != attributeValue.byteAt(pos))
        {
          return ConditionResult.FALSE;
        }
      }
    }


    if ((normAnys != null) && (normAnys.length != 0))
    {
      for (ByteSequence element : normAnys)
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
          return ConditionResult.FALSE;
        }
      }
    }


    if (normFinal != null)
    {
      int finalLength = normFinal.length();

      if ((valueLength - finalLength) < pos)
      {
        return ConditionResult.FALSE;
      }

      pos = valueLength - finalLength;
      for (int i=0; i < finalLength; i++,pos++)
      {
        if (normFinal.byteAt(i) != attributeValue.byteAt(pos))
        {
          return ConditionResult.FALSE;
        }
      }
    }


    return ConditionResult.TRUE;
    }
  }

  protected ByteString normalizeSubString(Schema schema, ByteSequence value)
      throws DecodeException
  {
    return normalizeAttributeValue(schema, value);
  }

  public Assertion getAssertion(Schema schema, ByteSequence subInitial,
                                List<ByteSequence> subAnyElements,
                                ByteSequence subFinal)
      throws DecodeException
  {
    ByteString normInitial = subInitial == null ?  null :
        normalizeSubString(schema, subInitial);

    ByteString[] normAnys = null;
    if(subAnyElements != null && !subAnyElements.isEmpty())
    {
      normAnys = new ByteString[subAnyElements.size()];
      for(int i = 0; i < subAnyElements.size(); i++)
      {
        normAnys[i] = normalizeSubString(schema, subAnyElements.get(i));
      }
    }
    ByteString normFinal = subFinal == null ?  null :
        normalizeSubString(schema, subFinal);

    return new DefaultSubstringAssertion(normInitial, normAnys, normFinal);
  }

  public Assertion getAssertion(Schema schema, ByteSequence value)
      throws DecodeException
  {
    if(value.length() == 0)
    {
      throw new DecodeException(
          SchemaMessages.WARN_ATTR_SYNTAX_SUBSTRING_EMPTY.get());
    }

    ByteSequence initialString = null;
    ByteSequence finalString = null;
    List<ByteSequence> anyStrings = null;

    String valueString = value.toString();

    if(valueString.length() == 1 && valueString.charAt(0) == '*')
    {
      return getAssertion(schema, initialString, anyStrings, finalString);
    }

    char[] escapeChars = new char[]{'*'};
    SubstringReader reader = new SubstringReader(valueString);

    ByteString bytes = StaticUtils.evaluateEscapes(reader, escapeChars, false);
    if(bytes.length() > 0)
    {
      initialString = normalizeSubString(schema, bytes);
    }
    if(reader.remaining() == 0)
    {
      throw new DecodeException(
          SchemaMessages.WARN_ATTR_SYNTAX_SUBSTRING_NO_WILDCARDS.
              get(value.toString()));
    }
    while(true)
    {
      reader.read();
      bytes = StaticUtils.evaluateEscapes(reader, escapeChars, false);
      if(reader.remaining() > 0)
      {
        if(bytes.length() == 0)
        {
          throw new DecodeException(
              SchemaMessages.WARN_ATTR_SYNTAX_SUBSTRING_CONSECUTIVE_WILDCARDS.
                  get(value.toString(), reader.pos()));
        }
        if(anyStrings == null)
        {
          anyStrings = new LinkedList<ByteSequence>();
        }
        anyStrings.add(normalizeSubString(schema, bytes));
      }
      else
      {
        if(bytes.length() > 0)
        {
          finalString = normalizeSubString(schema, bytes);
        }
        break;
      }
    }

    return getAssertion(schema, initialString, anyStrings, finalString);
  }
}
