package org.opends.schema;

import org.opends.server.util.Validator;
import static org.opends.server.util.ServerConstants.SCHEMA_PROPERTY_APPROX_RULE;
import org.opends.ldap.DecodeException;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.*;
import org.opends.util.SubstringReader;
import org.opends.schema.matchingrules.MatchingRuleImplementation;
import org.opends.schema.syntaxes.SyntaxImplementation;

import java.util.*;


/**
 * This class defines a data structure for storing and interacting
 * with an attribute type, which contains information about the format
 * of an attribute and the syntax and matching rules that should be
 * used when interacting with it.
 * <p>
 * Where ordered sets of names, or extra properties are provided, the
 * ordering will be preserved when the associated fields are accessed
 * via their getters or via the {@link #toString()} methods.
 */
public final class AttributeType extends AbstractSchemaElement
{
  // The OID that may be used to reference this definition.
  private final String oid;

  // The set of user defined names for this definition.
  private final SortedSet<String> names;

  // Indicates whether this definition is declared "obsolete".
  private final boolean isObsolete;

  // The superior attribute type from which this attribute type
  // inherits.
  private final Pair<String, AttributeType> superiorType;

  // The equality matching rule for this attribute type.
  private final Pair<String, MatchingRuleImplementation> equalityMatchingRule;

  // The ordering matching rule for this attribute type.
  private final Pair<String, MatchingRuleImplementation> orderingMatchingRule;

  // The substring matching rule for this attribute type.
  private final Pair<String, MatchingRuleImplementation> substringMatchingRule;

  // The approximate matching rule for this attribute type.
  private final Pair<String, MatchingRuleImplementation>
      approximateMatchingRule;

  // The syntax for this attribute type.
  private final Pair<String, SyntaxImplementation> syntax;

  // Indicates whether this attribute type is declared "single-value".
  private final boolean isSingleValue;

  // Indicates whether this attribute type is declared "collective".
  private final boolean isCollective;

  // Indicates whether this attribute type is declared
  // "no-user-modification".
  private final boolean isNoUserModification;

  // The attribute usage for this attribute type.
  private final AttributeUsage attributeUsage;

  // The definition string used to create this objectclass.
  private final String definition;

  public AttributeType(String oid,
                       SortedSet<String> names,
                       String description,
                       boolean obsolete,
                       String superiorType,
                       String equalityMatchingRule,
                       String orderingMatchingRule,
                       String substringMatchingRule,
                       String approximateMatchingRule,
                       String syntax,
                       boolean singleValue,
                       boolean collective,
                       boolean noUserModification,
                       AttributeUsage attributeUsage,
                       Map<String, List<String>> extraProperties)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(oid, names, attributeUsage);
    this.oid = oid;
    this.names = names;
    this.isObsolete = obsolete;
    this.superiorType = Pair.createPair(superiorType);
    this.equalityMatchingRule =
        Pair.createPair(equalityMatchingRule);
    this.orderingMatchingRule =
        Pair.createPair(orderingMatchingRule);
    this.substringMatchingRule =
        Pair.createPair(substringMatchingRule);
    this.approximateMatchingRule =
        Pair.createPair(approximateMatchingRule);
    this.syntax = Pair.createPair(syntax);
    this.isSingleValue = singleValue;
    this.isCollective = collective;
    this.isNoUserModification = noUserModification;
    this.attributeUsage = attributeUsage;

    this.definition = buildDefinition();
  }

  private AttributeType(String oid,
                       SortedSet<String> names,
                       String description,
                       boolean obsolete,
                       String superiorType,
                       String equalityMatchingRule,
                       String orderingMatchingRule,
                       String substringMatchingRule,
                       String approximateMatchingRule,
                       String syntax,
                       boolean singleValue,
                       boolean collective,
                       boolean noUserModification,
                       AttributeUsage attributeUsage,
                       Map<String, List<String>> extraProperties,
                       String definition)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(oid, names, attributeUsage, definition);
    this.oid = oid;
    this.names = names;
    this.isObsolete = obsolete;
    this.superiorType = Pair.createPair(superiorType);
    this.equalityMatchingRule =
        Pair.createPair(equalityMatchingRule);
    this.orderingMatchingRule =
        Pair.createPair(orderingMatchingRule);
    this.substringMatchingRule =
        Pair.createPair(substringMatchingRule);
    this.approximateMatchingRule =
        Pair.createPair(approximateMatchingRule);
    this.syntax = Pair.createPair(syntax);
    this.isSingleValue = singleValue;
    this.isCollective = collective;
    this.isNoUserModification = noUserModification;
    this.attributeUsage = attributeUsage;
    this.definition = definition;
  }


  /**
   * Retrieves the OID for this schema definition.
   *
   * @return The OID for this schema definition.
   */
  public final String getOID() {

    return oid;
  }


  /**
   * Retrieves an iterable over the set of user-defined names that may
   * be used to reference this schema definition.
   *
   * @return Returns an iterable over the set of user-defined names
   *         that may be used to reference this schema definition.
   */
  public Iterable<String> getNames() {
    return names;
  }

  /**
   * Indicates whether this schema definition has the specified name.
   *
   * @param name
   *          The name for which to make the determination.
   * @return <code>true</code> if the specified name is assigned to
   *         this schema definition, or <code>false</code> if not.
   */
  public boolean hasName(String name) {
    for(String n : names)
    {
      if(n.equalsIgnoreCase(name))
      {
        return true;
      }
    }
    return false;
  }


  /**
   * Retrieves the name or OID for this schema definition. If it has
   * one or more names, then the primary name will be returned. If it
   * does not have any names, then the OID will be returned.
   *
   * @return The name or OID for this schema definition.
   */
  public String getNameOrOID() {
    if(names.isEmpty())
    {
      return oid;
    }
    return names.first();
  }

  /**
   * Indicates whether this schema definition has the specified name
   * or OID.
   *
   * @param value
   *          The value for which to make the determination.
   * @return <code>true</code> if the provided value matches the OID
   *         or one of the names assigned to this schema definition,
   *         or <code>false</code> if not.
   */
  public boolean hasNameOrOID(String value) {
    return hasName(value) ||
        getOID().equals(value);
  }



  /**
   * Indicates whether this schema definition is declared "obsolete".
   *
   * @return <code>true</code> if this schema definition is declared
   *         "obsolete", or <code>false</code> if not.
   */
  public boolean isObsolete()
  {
    return isObsolete;
  }

  /**
   * Retrieves the superior type for this attribute type.
   *
   * @return  The superior type for this attribute type, or
   *          <CODE>null</CODE> if it does not have one.
   */
  public AttributeType getSuperiorType()
  {
    return superiorType.getValue();
  }

  /**
   * Retrieves the syntax for this attribute type.
   *
   * @return  The syntax for this attribute type.
   */
  public SyntaxImplementation getSyntax()
  {
    return syntax.getValue();
  }



  /**
   * Retrieves the matching rule that should be used for approximate
   * matching with this attribute type.
   *
   * @return  The matching rule that should be used for approximate
   *          matching with this attribute type.
   */
  public MatchingRuleImplementation getApproximateMatchingRule()
  {
    return approximateMatchingRule.getValue();
  }



  /**
   * Retrieves the matching rule that should be used for equality
   * matching with this attribute type.
   *
   * @return  The matching rule that should be used for equality
   *          matching with this attribute type.
   */
  public MatchingRuleImplementation getEqualityMatchingRule()
  {
    return equalityMatchingRule.getValue();
  }



  /**
   * Retrieves the matching rule that should be used for ordering with
   * this attribute type.
   *
   * @return  The matching rule that should be used for ordering with
   *          this attribute type.
   */
  public MatchingRuleImplementation getOrderingMatchingRule()
  {
    return orderingMatchingRule.getValue();
  }



  /**
   * Retrieves the matching rule that should be used for substring
   * matching with this attribute type.
   *
   * @return  The matching rule that should be used for substring
   *          matching with this attribute type.
   */
  public MatchingRuleImplementation getSubstringMatchingRule()
  {
    return substringMatchingRule.getValue();
  }



  /**
   * Retrieves the usage indicator for this attribute type.
   *
   * @return  The usage indicator for this attribute type.
   */
  public AttributeUsage getUsage()
  {
    return attributeUsage;
  }

  /**
   * Indicates whether this attribute type is declared "collective".
   *
   * @return  <CODE>true</CODE> if this attribute type is declared
   * "collective", or <CODE>false</CODE> if not.
   */
  public boolean isCollective()
  {
    return isCollective;
  }



  /**
   * Indicates whether this attribute type is declared
   * "no-user-modification".
   *
   * @return  <CODE>true</CODE> if this attribute type is declared
   *          "no-user-modification", or <CODE>false</CODE> if not.
   */
  public boolean isNoUserModification()
  {
    return isNoUserModification;
  }


  /**
   * Indicates whether this attribute type is declared "single-value".
   *
   * @return  <CODE>true</CODE> if this attribute type is declared
   *          "single-value", or <CODE>false</CODE> if not.
   */
  public boolean isSingleValue()
  {
    return isSingleValue;
  }



  /**
   * Retrieves the string representation of this schema definition in
   * the form specified in RFC 2252.
   *
   * @return The string representation of this schema definition in
   *         the form specified in RFC 2252.
   */
  public String toString() {
    return definition;
  }

  protected void toStringContent(StringBuilder buffer)
  {
    buffer.append(oid);

    if (!names.isEmpty()) {
      Iterator<String> iterator = names.iterator();

      String firstName = iterator.next();
      if (iterator.hasNext()) {
        buffer.append(" NAME ( '");
        buffer.append(firstName);

        while (iterator.hasNext()) {
          buffer.append("' '");
          buffer.append(iterator.next());
        }

        buffer.append("' )");
      } else {
        buffer.append(" NAME '");
        buffer.append(firstName);
        buffer.append("'");
      }
    }

    if ((description != null) && (description.length() > 0)) {
      buffer.append(" DESC '");
      buffer.append(description);
      buffer.append("'");
    }

    if (isObsolete) {
      buffer.append(" OBSOLETE");
    }

    if (superiorType.getKey() != null)
    {
      buffer.append(" SUP ");
      buffer.append(superiorType);
    }

    if (equalityMatchingRule.getKey() != null)
    {
      buffer.append(" EQUALITY ");
      buffer.append(equalityMatchingRule);
    }

    if (orderingMatchingRule.getKey() != null)
    {
      buffer.append(" ORDERING ");
      buffer.append(orderingMatchingRule);
    }

    if (substringMatchingRule.getKey() != null)
    {
      buffer.append(" SUBSTR ");
      buffer.append(substringMatchingRule);
    }

    if (syntax.getKey() != null)
    {
      buffer.append(" SYNTAX ");
      buffer.append(syntax);
    }

    if (isSingleValue())
    {
      buffer.append(" SINGLE-VALUE");
    }

    if (isCollective())
    {
      buffer.append(" COLLECTIVE");
    }

    if (isNoUserModification())
    {
      buffer.append(" NO-USER-MODIFICATION");
    }

    if (attributeUsage != null)
    {
      buffer.append(" USAGE ");
      buffer.append(attributeUsage.toString());
    }

    if(getApproximateMatchingRule() != null)
    {
      buffer.append(" ");
      buffer.append(SCHEMA_PROPERTY_APPROX_RULE);
      buffer.append(" '");
      buffer.append(getApproximateMatchingRule().getNameOrOID());
      buffer.append("'");
    }
  }

  @Override
  public int hashCode() {
    return oid.hashCode();
  }

  protected void resolveReferences(Schema schema) throws SchemaException
  {
    if(superiorType.getKey() != null)
    {
      superiorType.setValue(schema.getAttributeType(superiorType.getKey()));
      if(superiorType.getValue() == null)
      {
        // This is bad because we don't know what the superior attribute
        // type is so we can't base this attribute type on it.
        Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_SUPERIOR_TYPE.
            get(getNameOrOID(), superiorType.getKey());
        throw new SchemaException(message);
      }

      // If there is a superior type, then it must have the same usage as the
      // subordinate type.  Also, if the superior type is collective, then so
      // must the subordinate type be collective.
      if (superiorType.getValue().getUsage() != getUsage())
      {
        Message message = WARN_ATTR_SYNTAX_ATTRTYPE_INVALID_SUPERIOR_USAGE.get(
            getNameOrOID(), getUsage().toString(),
            superiorType.getValue().getNameOrOID());
        throw new SchemaException(message);
      }

      if (superiorType.getValue().isCollective() != isCollective())
      {
        Message message;
        if (isCollective())
        {
          message =
              WARN_ATTR_SYNTAX_ATTRTYPE_COLLECTIVE_FROM_NONCOLLECTIVE.get(
                  getNameOrOID(), superiorType.getValue().getNameOrOID());
        }
        else
        {
          message =
              WARN_ATTR_SYNTAX_ATTRTYPE_NONCOLLECTIVE_FROM_COLLECTIVE.get(
                  getNameOrOID(), superiorType.getValue().getNameOrOID());
        }
        throw new SchemaException(message);
      }
    }

    if(syntax.getKey() != null)
    {
      syntax.setValue(schema.getSyntax(syntax.getKey()));
      if(syntax.getValue() == null)
      {
      Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_SYNTAX.get(
              getNameOrOID(), syntax.getKey());
      throw new SchemaException(message);
      }
    }
    else if(getSuperiorType() != null && getSuperiorType().getSyntax() != null)
    {
      // Try to inherit the syntax from the superior type if possible
      syntax.setValue(getSuperiorType().getSyntax());
    }

    if(equalityMatchingRule.getKey() != null)
    {
      // Use explicitly defined matching rule first.
      equalityMatchingRule.setValue(
          schema.getMatchingRule(equalityMatchingRule.getKey()));
      if(equalityMatchingRule.getValue() == null)
      {
      // This is bad because we have no idea what the equality matching
      // rule should be.
      Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_EQUALITY_MR.
          get(getNameOrOID(), equalityMatchingRule.getKey());
      throw new SchemaException(message);
      }
    }
    else if(getSuperiorType() != null &&
        getSuperiorType().getEqualityMatchingRule() != null)
    {
      // Inherit matching rule from superior type if possible
      equalityMatchingRule.setValue(
          getSuperiorType().getEqualityMatchingRule());
    }
    else if(getSyntax() != null &&
        getSyntax().getDefaultEqualityMatchingRule() != null)
    {
      // Use default for syntax
      equalityMatchingRule.setValue(
          getSyntax().getDefaultEqualityMatchingRule());
    }

    if(orderingMatchingRule.getKey() != null)
    {
      orderingMatchingRule.setValue(
          schema.getMatchingRule(orderingMatchingRule.getKey()));
      if(orderingMatchingRule.getValue() == null)
      {
      // This is bad because we have no idea what the ordering matching
      // rule should be.
      Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_ORDERING_MR.
          get(getNameOrOID(), orderingMatchingRule.getKey());
      throw new SchemaException(message);
      }
    }

    if(substringMatchingRule.getKey() != null)
    {
      substringMatchingRule.setValue(
          schema.getMatchingRule(substringMatchingRule.getKey()));
      if(substringMatchingRule.getValue() == null)
      {
      // This is bad because we have no idea what the substring matching
      // rule should be.
      Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_SUBSTRING_MR.
          get(getNameOrOID(), substringMatchingRule.getKey());
      throw new SchemaException(message);
      }
    }

    if(approximateMatchingRule.getKey() != null)
    {
      approximateMatchingRule.setValue(
          schema.getMatchingRule(approximateMatchingRule.getKey()));
      if(approximateMatchingRule.getValue() == null)
      {
      // This is bad because we have no idea what the approximate matching
      // rule should be.
      Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_APPROXIMATE_MR.
          get(getNameOrOID(), approximateMatchingRule.getKey());
      throw new SchemaException(message);
      }
    }

    // If the attribute type is COLLECTIVE, then it must have a usage of
    // userApplications.
    if (isCollective() && getUsage() != AttributeUsage.USER_APPLICATIONS)
    {
      Message message =
          WARN_ATTR_SYNTAX_ATTRTYPE_COLLECTIVE_IS_OPERATIONAL.get(
              getNameOrOID());
      throw new SchemaException(message);
    }

    // If the attribute type is NO-USER-MODIFICATION, then it must not have a
    // usage of userApplications.
    if (isNoUserModification() &&
        getUsage() == AttributeUsage.USER_APPLICATIONS)
    {
      Message message =
          WARN_ATTR_SYNTAX_ATTRTYPE_NO_USER_MOD_NOT_OPERATIONAL.get(
              getNameOrOID());
      throw new SchemaException(message);
    }

  }

  public static AttributeType decode(String definition)
      throws DecodeException
  {
    SubstringReader reader = new SubstringReader(definition);

    // We'll do this a character at a time.  First, skip over any leading
    // whitespace.
    reader.skipWhitespaces();

    if (reader.remaining() <= 0)
    {
      // This means that the definition was empty or contained only whitespace.  That
      // is illegal.
      Message message = ERR_ATTR_SYNTAX_ATTRTYPE_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }


    // The next character must be an open parenthesis.  If it is not, then that
    // is an error.
    char c = reader.read();
    if (c != '(')
    {
      Message message = ERR_ATTR_SYNTAX_ATTRTYPE_EXPECTED_OPEN_PARENTHESIS.get(
          definition, (reader.pos()-1), String.valueOf(c));
      throw new DecodeException( message);
    }


    // Skip over any spaces immediately following the opening parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    String oid = SchemaUtils.readNumericOID(reader);

    SortedSet<String> names = SchemaUtils.emptySortedSet();
    String description = "".intern();
    boolean isObsolete = false;
    String superiorType = null;
    String equalityMatchingRule = null;
    String orderingMatchingRule = null;
    String substringMatchingRule = null;
    String approximateMatchingRule = null;
    String syntax = null;
    boolean isSingleValue = false;
    boolean isCollective = false;
    boolean isNoUserModification = false;
    AttributeUsage attributeUsage = AttributeUsage.USER_APPLICATIONS;
    Map<String, List<String>> extraProperties = Collections.emptyMap();

    // At this point, we should have a pretty specific syntax that describes
    // what may come next, but some of the components are optional and it would
    // be pretty easy to put something in the wrong order, so we will be very
    // flexible about what we can accept.  Just look at the next token, figure
    // out what it is and how to treat what comes after it, then repeat until
    // we get to the end of the definition.  But before we start, set default
    // values for everything else we might need to know.
    while (true)
    {
      String tokenName = SchemaUtils.readTokenName(reader);

      if (tokenName == null)
      {
        // No more tokens.
        break;
      }
      else if (tokenName.equalsIgnoreCase("name"))
      {
        names = SchemaUtils.readNameDescriptors(reader);
      }
      else if (tokenName.equalsIgnoreCase("desc"))
      {
        // This specifies the description for the attribute type.  It is an
        // arbitrary string of characters enclosed in single quotes.
        description = SchemaUtils.readQuotedString(reader);
      }
      else if (tokenName.equalsIgnoreCase("obsolete"))
      {
        // This indicates whether the attribute type should be considered
        // obsolete.  We do not need to do any more parsing for this token.
        isObsolete = true;
      }
      else if (tokenName.equalsIgnoreCase("sup"))
      {
        // This specifies the name or OID of the superior attribute type from
        // which this attribute type should inherit its properties.
        superiorType = SchemaUtils.readOID(reader);
      }
      else if (tokenName.equalsIgnoreCase("equality"))
      {
        // This specifies the name or OID of the equality matching rule to use
        // for this attribute type.
        equalityMatchingRule = SchemaUtils.readOID(reader);
      }
      else if (tokenName.equalsIgnoreCase("ordering"))
      {
        // This specifies the name or OID of the ordering matching rule to use
        // for this attribute type.
        orderingMatchingRule = SchemaUtils.readOID(reader);
      }
      else if (tokenName.equalsIgnoreCase("substr"))
      {
        // This specifies the name or OID of the substring matching rule to use
        // for this attribute type.
        substringMatchingRule = SchemaUtils.readOID(reader);
      }
      else if (tokenName.equalsIgnoreCase("syntax"))
      {
        // This specifies the numeric OID of the syntax for this matching rule.
        // It may optionally be immediately followed by an open curly brace, an
        // integer definition, and a close curly brace to suggest the minimum
        // number of characters that should be allowed in values of that type.
        // This implementation will ignore any such length because it does not
        // impose any practical limit on the length of attribute values.
        syntax = substringMatchingRule = SchemaUtils.readNumericOIDLen(reader);
      }
      else if (tokenName.equalsIgnoreCase("single-definition"))
      {
        // This indicates that attributes of this type are allowed to have at
        // most one definition.  We do not need any more parsing for this token.
        isSingleValue = true;
      }
      else if (tokenName.equalsIgnoreCase("collective"))
      {
        // This indicates that attributes of this type are collective (i.e.,
        // have their values generated dynamically in some way).  We do not need
        // any more parsing for this token.
        isCollective = true;
      }
      else if (tokenName.equalsIgnoreCase("no-user-modification"))
      {
        // This indicates that the values of attributes of this type are not to
        // be modified by end users.  We do not need any more parsing for this
        // token.
        isNoUserModification = true;
      }
      else if (tokenName.equalsIgnoreCase("usage"))
      {
        // This specifies the usage string for this attribute type.  It should
        // be followed by one of the strings "userApplications",
        // "directoryOperation", "distributedOperation", or "dSAOperation".
        int length = 0;

        reader.skipWhitespaces();
        reader.mark();

        while(reader.read() != ' ')
        {
          length++;
        }

        reader.reset();
        String usageStr = reader.read(length);
        if (usageStr.equalsIgnoreCase("userapplications"))
        {
          attributeUsage = AttributeUsage.USER_APPLICATIONS;
        }
        else if (usageStr.equalsIgnoreCase("directoryoperation"))
        {
          attributeUsage = AttributeUsage.DIRECTORY_OPERATION;
        }
        else if (usageStr.equalsIgnoreCase("distributedoperation"))
        {
          attributeUsage = AttributeUsage.DISTRIBUTED_OPERATION;
        }
        else if (usageStr.equalsIgnoreCase("dsaoperation"))
        {
          attributeUsage = AttributeUsage.DSA_OPERATION;
        }
        else
        {
          Message message = WARN_ATTR_SYNTAX_ATTRTYPE_INVALID_ATTRIBUTE_USAGE.
              get(String.valueOf(oid), usageStr);
          throw new DecodeException(message);
        }
      }
      else
      {
        // This must be a non-standard property and it must be followed by
        // either a single definition in single quotes or an open parenthesis
        // followed by one or more values in single quotes separated by spaces
        // followed by a close parenthesis.
        if(extraProperties == Collections.emptyList())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName,
            SchemaUtils.readExtraParameterValues(reader));
      }
    }

    List<String> approxRules =
        extraProperties.get(SCHEMA_PROPERTY_APPROX_RULE);
    if ((approxRules != null) && (! approxRules.isEmpty()))
    {
      approximateMatchingRule = approxRules.get(0);
    }

    return new AttributeType(
        oid, names, description, isObsolete, superiorType, 
        equalityMatchingRule, orderingMatchingRule, substringMatchingRule,
        approximateMatchingRule, syntax, isSingleValue, isCollective,
        isNoUserModification, attributeUsage, extraProperties, definition);
  }
}
