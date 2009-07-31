package org.opends.types;

import org.opends.server.types.ByteString;
import org.opends.schema.AttributeType;
import org.opends.schema.syntaxes.SyntaxImplementation;

import java.util.List;
import java.util.Map;
import java.util.Iterator;


/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 18, 2009 Time:
 * 9:40:42 AM To change this template use File | Settings | File
 * Templates.
 */
public abstract class DN implements Iterable<DN.AttributeTypeAndValue>
{
  private DN parentDN;
  private String definition;
  private String normalizedDefintion;

  public class AttributeTypeAndValue
  {
    private AttributeType attributeType;
    private ByteString value;


  }

  public String toNormalizedString()
  {
    return null;
  }

  public abstract ByteString getAttributeValue(AttributeType attributeType);

  public abstract int numAttributeTypeAndValues();


  private void toString(StringBuilder builder)
  {
    Iterator<AttributeTypeAndValue> i = iterator();
    if(i.hasNext())
    {
      AttributeTypeAndValue ava = i.next();
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

  public boolean isRootDN()
  {
    return parentDN == null;
  }

  public DN getParentDN()
  {
    return parentDN;
  }
}
