package org.opends.schema.matchingrules;

import static org.opends.server.util.StaticUtils.toLowerCase;

import java.util.List;

import org.opends.ldap.DecodeException;
import org.opends.schema.AttributeType;
import org.opends.schema.MatchingRule;
import org.opends.schema.Schema;
import org.opends.schema.SchemaUtils;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.types.ConditionResult;
import org.opends.util.SubstringReader;

/**
 * This class defines the distinguishedNameMatch matching rule defined in X.520
 * and referenced in RFC 2252.
 */
public class DistinguishedNameEqualityMatchingRule
    extends AbstractEqualityMatchingRuleImplementation
{
  public ByteSequence normalizeAttributeValue(Schema schema, ByteSequence value)
  {
    String lowerString = toLowerCase(value.toString());
    int           length = lowerString.length();
    StringBuilder buffer = new StringBuilder(length);

    for (int i=0; i < length; i++)
    {
      char c = lowerString.charAt(i);
      if (c == ' ')
      {
        if (i == 0)
        {
          // A space at the beginning of the value will be ignored.
          continue;
        }
        else
        {
          // Look at the previous character.  If it was a backslash, then keep
          // the space.  If it was a comma, then skip the space. Otherwise, keep
          // processing.
          char previous = lowerString.charAt(i-1);
          if (previous == '\\')
          {
            buffer.append(' ');
            continue;
          }
          else if (previous == ',')
          {
            continue;
          }
        }


        if (i == (length-1))
        {
          // A space at the end of the value will be ignored.
          break;
        }
        else
        {
          // Look at the next character.  If it is a space or a comma, then skip
          // the space.  Otherwise, include it.
          char next = lowerString.charAt(i+1);
          if ((next == ' ') || (next == ','))
          {
            continue;
          }
          else
          {
            buffer.append(' ');
          }
        }
      }
      else
      {
        // It's not a space, so we'll include it.
        buffer.append(c);
      }
    }

    return ByteString.valueOf(buffer.toString());
  }

  @Override
  public boolean areEqual(Schema schema, ByteSequence attributeValue,
                          ByteSequence assertionValue) {

    List<List<SchemaUtils.AttributeTypeAndValue>> attrDN;
    List<List<SchemaUtils.AttributeTypeAndValue>> assrDN;
    try
    {
      attrDN =
        SchemaUtils.readDN(new SubstringReader(attributeValue.toString()));
      assrDN =
        SchemaUtils.readDN(new SubstringReader(assertionValue.toString()));
    }
    catch(DecodeException de)
    {
      return attributeValue.equals(assertionValue);
    }

    if(attrDN.size() != assrDN.size())
    {
      return false;
    }

    for(int i = 0; i < attrDN.size(); i++)
    {
      List<SchemaUtils.AttributeTypeAndValue> attrAVAs = attrDN.get(i);
      List<SchemaUtils.AttributeTypeAndValue> assrAVAs = assrDN.get(i);

      if(attrAVAs.size() != assrAVAs.size())
      {
        return false;
      }

      for(SchemaUtils.AttributeTypeAndValue attrAVA : attrAVAs)
      {
        AttributeType attrType =
            schema.getAttributeType(toLowerCase(attrAVA.getAttributeType()));
        if(attrType.getEqualityMatchingRule() == null)
        {
          return false;
        }
        MatchingRule matchingRule =
            attrType.getEqualityMatchingRule();
        if(matchingRule == null)
        {
          return false;
        }
        SchemaUtils.AttributeTypeAndValue foundAssrAVA = null;
        for(SchemaUtils.AttributeTypeAndValue assrAVA : assrAVAs)
        {
          AttributeType assrType =
            schema.getAttributeType(toLowerCase(assrAVA.getAttributeType()));
          if(attrType.getOID().equals(assrType.getOID()))
          {
            foundAssrAVA = assrAVA;
            break;
          }
        }
        if(foundAssrAVA == null)
        {
          return false;
        }

        if(matchingRule.valuesMatch(attrAVA.getAttributeValue(),
            foundAssrAVA.getAttributeValue()) != ConditionResult.TRUE)
        {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public ConditionResult valuesMatch(Schema schema, ByteSequence attributeValue,
                                     ByteSequence assertionValue) {
    List<List<SchemaUtils.AttributeTypeAndValue>> attrDN;
    List<List<SchemaUtils.AttributeTypeAndValue>> assrDN;
    try
    {
      attrDN =
        SchemaUtils.readDN(new SubstringReader(attributeValue.toString()));
      assrDN =
        SchemaUtils.readDN(new SubstringReader(assertionValue.toString()));
    }
    catch(DecodeException de)
    {
      return attributeValue.equals(assertionValue) ?
          ConditionResult.TRUE : ConditionResult.FALSE;
    }

    if(attrDN.size() != assrDN.size())
    {
      return ConditionResult.FALSE;
    }

    for(int i = 0; i < attrDN.size(); i++)
    {
      List<SchemaUtils.AttributeTypeAndValue> attrAVAs = attrDN.get(i);
      List<SchemaUtils.AttributeTypeAndValue> assrAVAs = assrDN.get(i);

      if(attrAVAs.size() != assrAVAs.size())
      {
        return ConditionResult.FALSE;
      }

      for(SchemaUtils.AttributeTypeAndValue attrAVA : attrAVAs)
      {
        AttributeType attrType =
            schema.getAttributeType(toLowerCase(attrAVA.getAttributeType()));
        if(attrType.getEqualityMatchingRule() == null)
        {
          return ConditionResult.UNDEFINED;
        }
        MatchingRule matchingRule = attrType.getEqualityMatchingRule();
        if(matchingRule == null)
        {
          return ConditionResult.UNDEFINED;
        }
        SchemaUtils.AttributeTypeAndValue foundAssrAVA = null;
        for(SchemaUtils.AttributeTypeAndValue assrAVA : assrAVAs)
        {
          AttributeType assrType =
            schema.getAttributeType(toLowerCase(assrAVA.getAttributeType()));
          if(attrType.getOID().equals(assrType.getOID()))
          {
            foundAssrAVA = assrAVA;
            break;
          }
        }
        if(foundAssrAVA == null)
        {
          return ConditionResult.FALSE;
        }

        ConditionResult result =
            matchingRule.valuesMatch(attrAVA.getAttributeValue(),
                foundAssrAVA.getAttributeValue());
        if(result == ConditionResult.FALSE ||
            result == ConditionResult.UNDEFINED)
        {
          return result;
        }
      }
    }
    return ConditionResult.TRUE;
  }
}
