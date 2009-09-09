package org.opends.sdk.schema.matchingrules;

import static org.opends.server.util.StaticUtils.isDigit;
import static org.opends.server.util.StaticUtils.toLowerCase;

import org.opends.sdk.Assertion;
import org.opends.sdk.ConditionResult;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.*;
import org.opends.sdk.util.StaticUtils;
import org.opends.sdk.util.SubstringReader;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

/**
 * This class defines the objectIdentifierMatch matching rule defined in X.520
 * and referenced in RFC 2252.  This expects to work on OIDs and will match
 * either an attribute/objectclass name or a numeric OID.
 *
 * NOTE: This matching rule requires a schema to lookup object identifiers in
 * the descriptor form.
 */
public class ObjectIdentifierEqualityMatchingRule
    extends AbstractMatchingRuleImplementation
{
  static class OIDAssertion implements Assertion
  {
    private String oid;

    OIDAssertion(String oid) {
      this.oid = oid;
    }

    public ConditionResult matches(ByteString attributeValue) {
      String attrStr = attributeValue.toString();

      // We should have normalized all values to OIDs. If not, we know
      // the descriptor form is not valid in the schema.
      if(attrStr.length() == 0 || !isDigit(attrStr.charAt(0)))
      {
        return ConditionResult.UNDEFINED;
      }
      if(oid.length() == 0 || !isDigit(oid.charAt(0)))
      {
        return ConditionResult.UNDEFINED;
      }

      return attrStr.equals(oid) ?
          ConditionResult.TRUE : ConditionResult.FALSE;
    }
  }

  public ByteString normalizeAttributeValue(
      Schema schema, ByteSequence value) throws DecodeException
  {
    String definition = value.toString();
    SubstringReader reader = new SubstringReader(definition);
    String normalized = resolveNames(schema, SchemaUtils.readOID(reader));
    return ByteString.valueOf(normalized);
  }

  @Override
  public Assertion getAssertion(Schema schema, ByteSequence value)
      throws DecodeException {
    String definition = value.toString();
    SubstringReader reader = new SubstringReader(definition);
    String normalized = resolveNames(schema, SchemaUtils.readOID(reader));

    return new OIDAssertion(normalized);
  }
  
  static String resolveNames(Schema schema, String oid)
  {
    if (!isDigit(oid.charAt(0)))
    {
      // Do an best effort attempt to normalize names to OIDs.

      String schemaName = null;

      if (schema.hasAttributeType(oid))
      {
        schemaName = schema.getAttributeType(oid).getOID();
      }

      if (schemaName == null)
      {
        if (schema.hasDITContentRule(oid))
        {
          schemaName =
              schema.getDITContentRule(oid).getStructuralClass().getOID();
        }
      }

      if (schemaName == null)
      {
        if (schema.hasSyntax(oid))
        {
          schemaName = schema.getSyntax(oid).getOID();
        }
      }

      if (schemaName == null)
      {
        if (schema.hasObjectClass(oid))
        {
          schemaName = schema.getObjectClass(oid).getOID();
        }
      }

      if (schemaName == null)
      {
        if (schema.hasMatchingRule(oid))
        {
          schemaName = schema.getMatchingRule(oid).getOID();
        }
      }

      if (schemaName == null)
      {
        if (schema.hasMatchingRuleUse(oid))
        {
          schemaName =
              schema.getMatchingRuleUse(oid).getMatchingRule().getOID();
        }
      }

      if (schemaName == null)
      {
        if (schema.hasNameForm(oid))
        {
          schemaName = schema.getNameForm(oid).getOID();
        }
      }

      if (schemaName != null)
      {
        return schemaName;
      }
      else
      {
        return StaticUtils.toLowerCase(oid);
      }
    }
    return oid;
  }
}
