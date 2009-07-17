package org.opends.schema;

import org.opends.server.util.Validator;
import static org.opends.server.util.ServerConstants.SCHEMA_PROPERTY_APPROX_RULE;
import org.opends.ldap.DecodeException;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.*;
import org.opends.util.SubstringReader;

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

  // The superior attribute type from which this attribute type
  // inherits.
  private final String superiorType;

  // The equality matching rule for this attribute type.
  private final String equalityMatchingRule;

  // The ordering matching rule for this attribute type.
  private final String orderingMatchingRule;

  // The substring matching rule for this attribute type.
  private final String substringMatchingRule;

  // The approximate matching rule for this attribute type.
  private final String approximateMatchingRule;

  // The syntax for this attribute type.
  private final String syntax;

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
                       List<String> names,
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
    super(names, description, obsolete, extraProperties);

    Validator.ensureNotNull(oid, attributeUsage);
    this.oid = oid;
    this.superiorType = superiorType;
    this.equalityMatchingRule = equalityMatchingRule;
    this.orderingMatchingRule = orderingMatchingRule;
    this.substringMatchingRule = substringMatchingRule;
    this.approximateMatchingRule = approximateMatchingRule;
    this.syntax = syntax;
    this.isSingleValue = singleValue;
    this.isCollective = collective;
    this.isNoUserModification = noUserModification;
    this.attributeUsage = attributeUsage;

    this.definition = buildDefinition();
  }

  private AttributeType(String oid,
                       List<String> names,
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
    super(names, description, obsolete, extraProperties);

    Validator.ensureNotNull(oid, attributeUsage, definition);
    this.oid = oid;
    this.superiorType = superiorType;
    this.equalityMatchingRule = equalityMatchingRule;
    this.orderingMatchingRule = orderingMatchingRule;
    this.substringMatchingRule = substringMatchingRule;
    this.approximateMatchingRule = approximateMatchingRule;
    this.syntax = syntax;
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
    return names.get(0);
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
   * Retrieves the superior type for this attribute type.
   *
   * @return  The superior type for this attribute type, or
   *          <CODE>null</CODE> if it does not have one.
   */
  public String getSuperiorType()
  {
    return superiorType;
  }

  /**
   * Retrieves the syntax for this attribute type.
   *
   * @return  The syntax for this attribute type.
   */
  public String getSyntax()
  {
    return syntax;
  }



  /**
   * Retrieves the matching rule that should be used for approximate
   * matching with this attribute type.
   *
   * @return  The matching rule that should be used for approximate
   *          matching with this attribute type.
   */
  public String getApproximateMatchingRule()
  {
    return approximateMatchingRule;
  }



  /**
   * Retrieves the matching rule that should be used for equality
   * matching with this attribute type.
   *
   * @return  The matching rule that should be used for equality
   *          matching with this attribute type.
   */
  public String getEqualityMatchingRule()
  {
    return equalityMatchingRule;
  }



  /**
   * Retrieves the matching rule that should be used for ordering with
   * this attribute type.
   *
   * @return  The matching rule that should be used for ordering with
   *          this attribute type.
   */
  public String getOrderingMatchingRule()
  {
    return orderingMatchingRule;
  }



  /**
   * Retrieves the matching rule that should be used for substring
   * matching with this attribute type.
   *
   * @return  The matching rule that should be used for substring
   *          matching with this attribute type.
   */
  public String getSubstringMatchingRule()
  {
    return substringMatchingRule;
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

  protected String getIdentifier() {
    return oid;
  }

  protected String getDefinition() {
    return definition;
  }

  protected void toStringContent(StringBuilder buffer)
  {
    if (superiorType != null)
    {
      buffer.append(" SUP ");
      buffer.append(superiorType);
    }

    if (equalityMatchingRule != null)
    {
      buffer.append(" EQUALITY ");
      buffer.append(equalityMatchingRule);
    }

    if (orderingMatchingRule != null)
    {
      buffer.append(" ORDERING ");
      buffer.append(orderingMatchingRule);
    }

    if (substringMatchingRule != null)
    {
      buffer.append(" SUBSTR ");
      buffer.append(substringMatchingRule);
    }

    // NOTE -- We will not include any approximate matching rule
    // information here because it would break the standard and
    // anything that depends on it.
    // FIXME -- Should we encode this into one of the "extra"
    // properties?

    if (syntax != null)
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

    if(approximateMatchingRule != null)
    {
      buffer.append(" ");
      buffer.append(SCHEMA_PROPERTY_APPROX_RULE);
      buffer.append(" '");
      buffer.append(approximateMatchingRule);
      buffer.append("'");
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

    List<String> names = Collections.emptyList();
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
    // we get to the end of the definition.  But before we start, set default values
    // for everything else we might need to know.
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
        // integer definition, and a close curly brace to suggest the minimum number
        // of characters that should be allowed in values of that type.  This
        // implementation will ignore any such length because it does not
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
