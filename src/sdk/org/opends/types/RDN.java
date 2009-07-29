package org.opends.types;

import java.util.Iterator;
import java.util.Map;

import org.opends.schema.AttributeType;
import org.opends.schema.Schema;
import org.opends.schema.matchingrules.MatchingRuleImplementation;
import org.opends.schema.syntaxes.SyntaxImplementation;
import org.opends.server.types.ByteString;


/**
 * This class defines a data structure for storing and interacting
 * with the relative distinguished names associated with entries in
 * the Directory Server.
 */
public class RDN
{
  private Schema schema;
  transient volatile Iterator<AttributeType> attributeTypes = null;
  transient volatile Iterator<ByteString> attributeValues = null;

  private Map<AttributeType, ByteString> attributeTypeAndValues;

  public ByteString getAttributeValue(AttributeType attributeType)
  {
    return attributeTypeAndValues.get(attributeType);
  }


  public int numAttributeTypeAndValues()
  {
    return attributeTypeAndValues.size();
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj)
    {
      return true;
    }

    if(obj instanceof RDN)
    {
      RDN that = (RDN)obj;
      return matches(that) == ConditionResult.TRUE;
    }

    return false;
  }

  public ConditionResult matches(RDN rdn)
  {
    if(attributeTypeAndValues.size() ==
        rdn.attributeTypeAndValues.size())
    {
      ByteString thatValue;
      MatchingRuleImplementation matchingRule;
      ConditionResult result;
      for(Map.Entry<AttributeType,ByteString> ava :
          attributeTypeAndValues.entrySet())
      {
        thatValue = rdn.getAttributeValue(ava.getKey());
        if(thatValue != null)
        {
          matchingRule =
              schema.getEqualityMatchingRule(ava.getKey());
          if(matchingRule != null)
          {
            result = matchingRule.valuesMatch(schema, ava.getValue(),
                thatValue);
            if(result != ConditionResult.TRUE)
            {
              return result;
            }
          }
          return ConditionResult.UNDEFINED;
        }
        else
        {
          return ConditionResult.FALSE;
        }
      }
      return ConditionResult.TRUE;
    }
    return ConditionResult.FALSE;
  }

  public void toString(StringBuilder buffer)
  {
    Iterator<Map.Entry<AttributeType, ByteString>> i =
        attributeTypeAndValues.entrySet().iterator();
    if(i.hasNext())
    {
      Map.Entry<AttributeType, ByteString> ava = i.next();
      SyntaxImplementation syntax;
      while(true)
      {
        if(!ava.getKey().getNames().iterator().hasNext())
        {
          buffer.append(ava.getKey().getOID());
          buffer.append("=#");
          buffer.append(ava.getValue().toHex());
        }
        else
        {
          buffer.append(ava.getKey().getNameOrOID());
          buffer.append("=");
          syntax = schema.getSyntax(ava.getKey());
          if(!syntax.isHumanReadable())
          {
            buffer.append("#");
            buffer.append(ava.getValue().toHex());
          }
          else
          {
            String str = ava.getValue().toString();
            char c;
            for(int si = 0; si < str.length(); si++)
            {
              c = str.charAt(si);
              if(c == ' ' || c == '#' || c == '"' || c == '+' ||
                  c == ',' || c == ';' || c == '<' || c == '=' ||
                  c == '>' || c == '\\' || c == '\u0000')
              {
                buffer.append('\\');
              }
              buffer.append(c);
            }
          }
        }
        if(i.hasNext())
        {
          buffer.append("+");
          ava = i.next();
        }
        else
        {
          break;
        }
      }
    }
  }
}
