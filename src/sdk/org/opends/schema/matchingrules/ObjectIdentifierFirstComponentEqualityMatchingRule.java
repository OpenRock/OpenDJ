package org.opends.schema.matchingrules;

import org.opends.schema.*;
import static org.opends.server.schema.SchemaConstants.EMR_OID_FIRST_COMPONENT_NAME;
import static org.opends.server.schema.SchemaConstants.EMR_OID_FIRST_COMPONENT_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_OID_OID;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import static org.opends.server.util.StaticUtils.isDigit;
import static org.opends.server.util.StaticUtils.toLowerCase;
import org.opends.server.util.ServerConstants;
import org.opends.types.ConditionResult;

/**
 * This class implements the objectIdentifierFirstComponentMatch matching rule
 * defined in X.520 and referenced in RFC 2252.  This rule is intended for use
 * with attributes whose values contain a set of parentheses enclosing a
 * space-delimited set of names and/or name-value pairs (like attribute type or
 * objectclass descriptions) in which the "first component" is the first item
 * after the opening parenthesis.
 */
public class ObjectIdentifierFirstComponentEqualityMatchingRule
    extends AbstractEqualityMatchingRuleImplementation
{
  public ByteSequence normalizeAttributeValue(Schema schema, ByteSequence value)
  {
    StringBuilder buffer = new StringBuilder();
    toLowerCase(value, buffer, true);

    int bufferLength = buffer.length();
    if (bufferLength == 0)
    {
      if (value.length() > 0)
      {
        // This should only happen if the value is composed entirely of spaces.
        // In that case, the normalized value is a single space.
        return ServerConstants.SINGLE_SPACE_VALUE;
      }
      else
      {
        // The value is empty, so it is already normalized.
        return ByteString.empty();
      }
    }

    String valueString = buffer.toString();
    int valueLength = valueString.length();

    if ((valueLength == 0) || (valueString.charAt(0) != '('))
    {
      // They cannot be equal if the attribute value is empty or doesn't start
      // with an open parenthesis.
      return value;
    }

    int  pos = 1;
    while ((pos < valueLength) && ((valueString.charAt(pos)) == ' '))
    {
      pos++;
    }

    if (pos >= valueLength)
    {
      return value;
    }


    // The current position must be the start position for the value.  Keep
    // reading until we find the next space.
    int startPos = pos++;
    while ((pos < valueLength) && ((valueString.charAt(pos)) != ' '))
    {
      pos++;
    }

    if (pos >= valueLength)
    {
      return value;
    }


    // We should now have the position of the object identifier.
    return resolveNames(schema, valueString.substring(startPos, pos));
  }

  @Override
  public ByteSequence normalizeAssertionValue(Schema schema,
                                              ByteSequence value)
  {
    StringBuilder buffer = new StringBuilder();
    toLowerCase(value, buffer, true);

    int bufferLength = buffer.length();
    if (bufferLength == 0)
    {
      if (value.length() > 0)
      {
        // This should only happen if the value is composed entirely of spaces.
        // In that case, the normalized value is a single space.
        return ServerConstants.SINGLE_SPACE_VALUE;
      }
      else
      {
        // The value is empty, so it is already normalized.
        return ByteString.empty();
      }
    }

    return resolveNames(schema, buffer.toString());
  }

  @Override
  public ConditionResult valuesMatch(Schema schema, ByteSequence attributeValue,
                                     ByteSequence assertionValue)
  {
    ByteSequence normAttributeValue =
        normalizeAttributeValue(schema, attributeValue);
    ByteSequence normAssertionValue =
        normalizeAssertionValue(schema, assertionValue);

    String attrStr = normAttributeValue.toString();
    String assrStr = normAssertionValue.toString();

    // We should have normalized all values to OIDs. If not, we know
    // the descriptor form is not valid in the schema.
    if(attrStr.length() == 0 || !isDigit(attrStr.charAt(0)))
    {
      return ConditionResult.UNDEFINED;
    }
    if(assrStr.length() == 0 || !isDigit(assrStr.charAt(0)))
    {
      return ConditionResult.UNDEFINED;
    }

    return areEqual(schema, normAttributeValue, normAssertionValue) ?
        ConditionResult.TRUE : ConditionResult.FALSE;
  }

  private ByteSequence resolveNames(Schema schema, String lowerString)
  {
    if (!isDigit(lowerString.charAt(0)))
    {
      // Do an best effort attempt to normalize names to OIDs.

      String schemaName = null;

      AttributeType attributeType = schema.getAttributeType(lowerString);
      if (attributeType != null)
      {
        schemaName = attributeType.getOID();
      }

      if (schemaName == null)
      {
        DITContentRule contentRule = schema.getDITContentRule(lowerString);
        if (contentRule != null)
        {
          schemaName = contentRule.getStructuralClass();
        }
      }

      if (schemaName == null)
      {
        Syntax syntax = schema.getSyntax(lowerString);
        if (syntax != null)
        {
          schemaName = syntax.getOID();
        }
      }

      if (schemaName == null)
      {
        ObjectClass objectClass = schema.getObjectClass(lowerString);
        if (objectClass != null)
        {
          schemaName = objectClass.getOID();
        }
      }

      if (schemaName == null)
      {
        MatchingRule matchingRule = schema.getMatchingRule(lowerString);
        if (matchingRule != null)
        {
          schemaName = matchingRule.getOID();
        }
      }

      if (schemaName == null)
      {
        MatchingRuleUse matchingRuleUse =
            schema.getMatchingRuleUse(lowerString);
        if (matchingRuleUse != null)
        {
          schemaName = matchingRuleUse.getOID();
        }
      }

      if (schemaName == null)
      {
        NameForm nameForm = schema.getNameForm(lowerString);
        if (nameForm != null)
        {
          schemaName = nameForm.getOID();
        }
      }

      if (schemaName != null)
      {
        return ByteString.valueOf(toLowerCase(schemaName));
      }
    }
    return ByteString.valueOf(lowerString);
  }
}
