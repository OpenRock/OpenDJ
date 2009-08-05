package org.opends.schema;

import org.opends.server.util.Validator;
import static org.opends.server.util.ServerConstants.SCHEMA_PROPERTY_APPROX_RULE;

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
public abstract class AttributeType extends AbstractSchemaElement
{
  // The OID that may be used to reference this definition.
  protected final String oid;

  // The set of user defined names for this definition.
  protected final SortedSet<String> names;

  // Indicates whether this definition is declared "obsolete".
  protected final boolean isObsolete;

  // The superior attribute type from which this attribute type
  // inherits.
  protected final String superiorTypeOID;

  // The equality matching rule for this attribute type.
  protected final String equalityMatchingRuleOID;

  // The ordering matching rule for this attribute type.
  protected final String orderingMatchingRuleOID;

  // The substring matching rule for this attribute type.
  protected final String substringMatchingRuleOID;

  // The approximate matching rule for this attribute type.
  protected final String approximateMatchingRuleOID;

  // The syntax for this attribute type.
  protected final String syntaxOID;

  // Indicates whether this attribute type is declared "single-value".
  protected final boolean isSingleValue;

  // Indicates whether this attribute type is declared "collective".
  protected final boolean isCollective;

  // Indicates whether this attribute type is declared
  // "no-user-modification".
  protected final boolean isNoUserModification;

  // The attribute usage for this attribute type.
  protected final AttributeUsage attributeUsage;

  // The definition string used to create this objectclass.
  protected final String definition;

  protected AttributeType(String oid,
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

    Validator.ensureNotNull(oid, names, description, attributeUsage);
    Validator.ensureTrue(superiorType != null || syntax != null,
        "superiorType and/or syntax must not be null");
    Validator.ensureNotNull(extraProperties);

    this.oid = oid;
    this.names = names;
    this.isObsolete = obsolete;
    this.superiorTypeOID = superiorType;
    this.equalityMatchingRuleOID = equalityMatchingRule;
    this.orderingMatchingRuleOID = orderingMatchingRule;
    this.substringMatchingRuleOID = substringMatchingRule;
    this.approximateMatchingRuleOID = approximateMatchingRule;
    this.syntaxOID = syntax;
    this.isSingleValue = singleValue;
    this.isCollective = collective;
    this.isNoUserModification = noUserModification;
    this.attributeUsage = attributeUsage;

    if(definition != null)
    {
      this.definition = definition;
    }
    else
    {
      this.definition = buildDefinition();
    }
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
  public abstract AttributeType getSuperiorType();

  /**
   * Retrieves the syntax for this attribute type.
   *
   * @return  The syntax for this attribute type.
   */
  public abstract Syntax getSyntax();



  /**
   * Retrieves the matching rule that should be used for approximate
   * matching with this attribute type.
   *
   * @return  The matching rule that should be used for approximate
   *          matching with this attribute type.
   */
  public abstract MatchingRule getApproximateMatchingRule();



  /**
   * Retrieves the matching rule that should be used for equality
   * matching with this attribute type.
   *
   * @return  The matching rule that should be used for equality
   *          matching with this attribute type.
   */
  public abstract MatchingRule getEqualityMatchingRule();



  /**
   * Retrieves the matching rule that should be used for ordering with
   * this attribute type.
   *
   * @return  The matching rule that should be used for ordering with
   *          this attribute type.
   */
  public abstract MatchingRule getOrderingMatchingRule();



  /**
   * Retrieves the matching rule that should be used for substring
   * matching with this attribute type.
   *
   * @return  The matching rule that should be used for substring
   *          matching with this attribute type.
   */
  public abstract MatchingRule getSubstringMatchingRule();



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

  @Override
  public final int hashCode() {
    return oid.hashCode();
  }

  protected final void toStringContent(StringBuilder buffer)
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

    if (superiorTypeOID != null)
    {
      buffer.append(" SUP ");
      buffer.append(superiorTypeOID);
    }

    if (equalityMatchingRuleOID != null)
    {
      buffer.append(" EQUALITY ");
      buffer.append(equalityMatchingRuleOID);
    }

    if (orderingMatchingRuleOID != null)
    {
      buffer.append(" ORDERING ");
      buffer.append(orderingMatchingRuleOID);
    }

    if (substringMatchingRuleOID != null)
    {
      buffer.append(" SUBSTR ");
      buffer.append(substringMatchingRuleOID);
    }

    if (syntaxOID != null)
    {
      buffer.append(" SYNTAX ");
      buffer.append(syntaxOID);
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

    if(approximateMatchingRuleOID != null)
    {
      buffer.append(" ");
      buffer.append(SCHEMA_PROPERTY_APPROX_RULE);
      buffer.append(" '");
      buffer.append(approximateMatchingRuleOID);
      buffer.append("'");
    }
  }

  /**
   * Retrieves the string representation of this schema definition in
   * the form specified in RFC 2252.
   *
   * @return The string representation of this schema definition in
   *         the form specified in RFC 2252.
   */
  public final String toString() {
    return definition;
  }
}
