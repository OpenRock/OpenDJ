package org.opends.sdk.schema;

import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.opends.messages.MessageBuilder;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.*;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_UNKNOWN_APPROXIMATE_MATCHING_RULE;
import org.opends.sdk.util.Validator;
import org.opends.sdk.schema.syntaxes.SyntaxImplementation;
import org.opends.sdk.schema.syntaxes.RegexSyntax;
import org.opends.server.types.ByteSequence;

/**
 * This class defines a data structure for storing and interacting
 * with an LDAP syntaxes, which constrain the structure of attribute values
 * stored in an LDAP directory, and determine the representation of attribute
 * and assertion values transferred in the LDAP protocol.
 * <p>
 * Syntax implementations must extend the <code>SyntaxImplementation</code>
 * class so they can be used by OpenDS to validate attribute values.
 * <p>
 * Where ordered sets of names, or extra properties are provided, the
 * ordering will be preserved when the associated fields are accessed
 * via their getters or via the {@link #toString()} methods.
 */
public final class Syntax extends SchemaElement
{
  private final String oid;
  private final String definition;

  private MatchingRule equalityMatchingRule;
  private MatchingRule orderingMatchingRule;
  private MatchingRule substringMatchingRule;
  private MatchingRule approximateMatchingRule;

  private Schema schema;
  private SyntaxImplementation implementation;

  Syntax(String oid, String description,
         Map<String, List<String>> extraProperties,
         String definition, SyntaxImplementation implementation)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(oid);
    this.oid = oid;

    if(definition != null)
    {
      this.definition = definition;
    }
    else
    {
      this.definition = buildDefinition();
    }
    this.implementation = implementation;
  }

  /**
   * Retrieves the OID for this attribute syntax.
   *
   * @return  The OID for this attribute syntax.
   */
  public final String getOID()
  {
    return oid;
  }

  /**
   * Retrieves the default equality matching rule that will be used
   * for attributes with this syntax.
   *
   * @return  The default equality matching rule that will be used for
   *          attributes with this syntax, or {@code null} if equality
   *          matches will not be allowed for this type by default.
   */
  public MatchingRule getEqualityMatchingRule() {
    return equalityMatchingRule;
  }



  /**
   * Retrieves the default ordering matching rule that will be used
   * for attributes with this syntax.
   *
   * @return  The default ordering matching rule that will be used for
   *          attributes with this syntax, or {@code null} if ordering
   *          matches will not be allowed for this type by default.
   */
  public MatchingRule getOrderingMatchingRule() {
    return orderingMatchingRule;
  }



  /**
   * Retrieves the default substring matching rule that will be used
   * for attributes with this syntax.
   *
   * @return  The default substring matching rule that will be used
   *          for attributes with this syntax, or {@code null} if
   *          substring matches will not be allowed for this type by
   *          default.
   */
  public MatchingRule getSubstringMatchingRule() {
    return substringMatchingRule;
  }


  /**
   * Retrieves the default approximate matching rule that will be used
   * for attributes with this syntax.
   *
   * @return  The default approximate matching rule that will be used
   *          for attributes with this syntax, or {@code null} if
   *          approximate matches will not be allowed for this type by
   *          default.
   */
  public MatchingRule getApproximateMatchingRule() {
    return approximateMatchingRule;
  }



  /**
   * Indicates whether this attribute syntax would likely be a
   * human readable string.
   * @return {@code true} if this attribute syntax would likely be a
   * human readable string or {@code false} if not.
   */
  public boolean isHumanReadable() {
    return implementation.isHumanReadable();
  }



  /**
   * Indicates whether this attribute syntax requires that values must
   * be encoded using the Basic Encoding Rules (BER) used by X.500
   * directories and always include the {@code binary} attribute
   * description option.
   *
   * @return {@code true} this attribute syntax requires that values
   *         must be BER encoded and always include the {@code binary}
   *         attribute description option, or {@code false} if not.
   * @see <a href="http://tools.ietf.org/html/rfc4522">RFC 4522 -
   *      Lightweight Directory Access Protocol (LDAP): The Binary
   *      Encoding Option </a>
   */
  public boolean isBEREncodingRequired()
  {
    return implementation.isBEREncodingRequired();
  }



  /**
   * Indicates whether the provided value is acceptable for use in an
   * attribute with this syntax.  If it is not, then the reason may be
   * appended to the provided buffer.
   *
   * @param  value          The value for which to make the
   *                        determination.
   * @param  invalidReason  The buffer to which the invalid reason
   *                        should be appended.
   *
   * @return  {@code true} if the provided value is acceptable for use
   *          with this syntax, or {@code false} if not.
   */
  public boolean valueIsAcceptable(ByteSequence value,
                                   MessageBuilder invalidReason) {
    return implementation.valueIsAcceptable(schema, value,
        invalidReason);
  }


  /**
   * Retrieves the hash code for this schema element.  It will be
   * calculated as the sum of the characters in the OID.
   *
   * @return  The hash code for this attribute syntax.
   */
  public final int hashCode()
  {
    return getOID().hashCode();
  }



  /**
   * Retrieves a string representation of this attribute syntax in the
   * format defined in RFC 2252.
   *
   * @return  A string representation of this attribute syntax in the
   *          format defined in RFC 2252.
   */
  public final String toString()
  {
    return definition;
  }

  void validate(List<Message> warnings, Schema schema) throws SchemaException
  {
    this.schema = schema;
    if(implementation == null)
    {
      // See if we need to override the implementation of the syntax
      for(Map.Entry<String, List<String>> property : extraProperties.entrySet())
      {
        // Enums are handled in the schema builder.
        if(property.getKey().equalsIgnoreCase("x-subst"))
        {
          /**
           * One unimplemented syntax can be substituted by another defined
           * syntax. A substitution syntax is an LDAPSyntaxDescriptionSyntax
           * with X-SUBST extension.
           */
          Iterator<String> values = property.getValue().iterator();
          if(values.hasNext())
          {
            String value = values.next();
            if(value.equals(oid))
            {
              Message message = ERR_ATTR_SYNTAX_CYCLIC_SUB_SYNTAX.get(oid);
              throw new SchemaException(message);
            }
            if(!schema.hasSyntax(value))
            {
              Message message =
                  ERR_ATTR_SYNTAX_UNKNOWN_SUB_SYNTAX.get(oid, value);
              throw new SchemaException(message);
            }
            Syntax subSyntax = schema.getSyntax(value);
            if(subSyntax.implementation == null)
            {
              // The substituion syntax was never validated.
              subSyntax.validate(warnings, schema);
            }
            implementation = subSyntax.implementation;
          }
        }
        else if(property.getKey().equalsIgnoreCase("x-pattern"))
        {
          Iterator<String> values = property.getValue().iterator();
          if(values.hasNext())
          {
            String value = values.next();
            try
            {
              Pattern pattern = Pattern.compile(value);
              implementation = new RegexSyntax(pattern);
            }
            catch(Exception e)
            {
              Message message =
                  WARN_ATTR_SYNTAX_LDAPSYNTAX_REGEX_INVALID_PATTERN.get
                      (oid, value);
              throw new SchemaException(message);
            }
          }
        }
      }

      // Try to find an implementation in the core schema
      if(implementation == null && Schema.getDefaultSchema().hasSyntax(oid))
      {
        implementation = Schema.getDefaultSchema().getSyntax(oid).implementation;
      }
      if(implementation == null && Schema.getCoreSchema().hasSyntax(oid))
      {
        implementation = Schema.getCoreSchema().getSyntax(oid).implementation;
      }

      if(implementation == null)
      {
        implementation = Schema.getCoreSchema().getSyntax(
            SchemaBuilder.getDefaultSyntax()).implementation;
        Message message = WARN_ATTR_SYNTAX_NOT_IMPLEMENTED.get(oid,
            SchemaBuilder.getDefaultSyntax());
        warnings.add(message);
      }
    }

    // Get references to the default matching rules. It will be ok
    // if we can't find some. Just warn.
    if(implementation.getEqualityMatchingRule() != null)
    {
      if(schema.hasMatchingRule(
          implementation.getEqualityMatchingRule()))
      {
        equalityMatchingRule = schema.getMatchingRule(
            implementation.getEqualityMatchingRule());
      }
      else
      {
        Message message =
            ERR_ATTR_SYNTAX_UNKNOWN_EQUALITY_MATCHING_RULE.get(
                implementation.getEqualityMatchingRule(),
                implementation.getName());
        warnings.add(message);
      }
    }

    if(implementation.getOrderingMatchingRule() != null)
    {
      if(schema.hasMatchingRule(
          implementation.getOrderingMatchingRule()))
      {
        orderingMatchingRule = schema.getMatchingRule(
            implementation.getOrderingMatchingRule());
      }
      else
      {
        Message message =
            ERR_ATTR_SYNTAX_UNKNOWN_ORDERING_MATCHING_RULE.get(
                implementation.getOrderingMatchingRule(),
                implementation.getName());
        warnings.add(message);
      }
    }

    if(implementation.getSubstringMatchingRule() != null)
    {
      if(schema.hasMatchingRule(
          implementation.getSubstringMatchingRule()))
      {
        substringMatchingRule = schema.getMatchingRule(
            implementation.getSubstringMatchingRule());
      }
      else
      {
        Message message =
            ERR_ATTR_SYNTAX_UNKNOWN_SUBSTRING_MATCHING_RULE.get(
                implementation.getSubstringMatchingRule(),
                implementation.getName());
        warnings.add(message);
      }
    }

    if(implementation.getApproximateMatchingRule() != null)
    {
      if(schema.hasMatchingRule(
          implementation.getApproximateMatchingRule()))
      {
        approximateMatchingRule = schema.getMatchingRule(
            implementation.getApproximateMatchingRule());
      }
      else
      {
        Message message =
            ERR_ATTR_SYNTAX_UNKNOWN_APPROXIMATE_MATCHING_RULE.get(
                implementation.getApproximateMatchingRule(),
                implementation.getName());
        warnings.add(message);
      }
    }
  }

  Syntax duplicate() {
    return new Syntax(oid, description, extraProperties,
        definition, implementation);
  }

  void toStringContent(StringBuilder buffer)
  {
    buffer.append(oid);

    if ((description != null) && (description.length() > 0)) {
      buffer.append(" DESC '");
      buffer.append(description);
      buffer.append("'");
    }
  }
}
