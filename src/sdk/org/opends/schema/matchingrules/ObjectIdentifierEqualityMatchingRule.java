package org.opends.schema.matchingrules;

import org.opends.schema.*;
import static org.opends.server.schema.SchemaConstants.EMR_OID_NAME;
import static org.opends.server.schema.SchemaConstants.EMR_OID_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_OID_OID;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import static org.opends.server.util.StaticUtils.toLowerCase;
import static org.opends.server.util.StaticUtils.isDigit;
import org.opends.server.util.ServerConstants;
import org.opends.types.ConditionResult;

/**
 * This class defines the objectIdentifierMatch matching rule defined in X.520
 * and referenced in RFC 2252.  This expects to work on OIDs and will match
 * either an attribute/objectclass name or a numeric OID.
 *
 * NOTE: This matching rule requires a schema to lookup object identifiers in
 * the descriptor form.
 */
public class ObjectIdentifierEqualityMatchingRule
    extends AbstractEqualityMatchingRuleImplementation
{
  public ByteSequence normalizeAttributeValue(
      Schema schema, ByteSequence value)
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

    String lowerString = buffer.toString();
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

  @Override
  public ConditionResult valuesMatch(Schema schema,
                                     ByteSequence attributeValue,
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
}
