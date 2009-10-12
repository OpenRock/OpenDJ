package org.opends.sdk.schema;

import java.util.*;

import org.opends.sdk.util.Validator;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_MRUSE_UNKNOWN_MATCHING_RULE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_MRUSE_UNKNOWN_ATTR;

/**
 * This class defines a data structure for storing and interacting
 * with a matching rule use definition, which may be used to restrict
 * the set of attribute types that may be used for a given matching
 * rule.
 */
public final class MatchingRuleUse extends SchemaElement
{
  // The OID of the matching rule associated with this matching rule
  // use definition.
  private final String oid;

  // The set of user defined names for this definition.
  private final List<String> names;

  // Indicates whether this definition is declared "obsolete".
  private final boolean isObsolete;

  // The set of attribute types with which this matching rule use is
  // associated.
  private final Set<String> attributeOIDs;

  // The definition string used to create this objectclass.
  private final String definition;

  private MatchingRule matchingRule;
  private Set<AttributeType> attributes = Collections.emptySet();

  MatchingRuleUse(String oid,
                            List<String> names,
                            String description,
                            boolean obsolete,
                            Set<String> attributeOIDs,
                            Map<String, List<String>> extraProperties,
                            String definition)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(oid, names, attributeOIDs);
    this.oid = oid;
    this.names = names;
    this.isObsolete = obsolete;
    this.attributeOIDs = attributeOIDs;

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
   * Retrieves the matching rule OID for this schema definition.
   *
   * @return The OID for this schema definition.
   */
  public String getMatchingRuleOID() {
    return oid;
  }


  /**
   * Retrieves the name or matching rule OID for this schema definition.
   * If it has one or more names, then the primary name will be returned. If it
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
   * or matching rule OID.
   *
   * @param value
   *          The value for which to make the determination.
   * @return <code>true</code> if the provided value matches the OID
   *         or one of the names assigned to this schema definition,
   *         or <code>false</code> if not.
   */
  public boolean hasNameOrOID(String value) {
    return hasName(value) ||
        oid.equals(value);
  }



  /**
   * Indicates whether this schema definition is declared "obsolete".
   *
   * @return <code>true</code> if this schema definition is declared
   *         "obsolete", or <code>false</code> if not.
   */
  public final boolean isObsolete()
  {
    return isObsolete;
  }


  /**
   * Retrieves the matching rule for this matching rule use.
   *
   * @return  The matching rule for this matching rule use.
   */
  public MatchingRule getMatchingRule() {
    return matchingRule;
  }




  /**
   * Retrieves the set of attributes associated with this matching
   * rule use.
   *
   * @return  The set of attributes associated with this matching
   *          rule use.
   */
  public Iterable<AttributeType> getAttributes()
  {
    return attributes;
  }




  /**
   * Indicates whether the provided attribute type is referenced by
   * this matching rule use.
   *
   * @param  attributeType  The attribute type for which to make the
   *                        determination.
   *
   * @return  {@code true} if the provided attribute type is
   *          referenced by this matching rule use, or {@code false}
   *          if it is not.
   */
  public boolean hasAttribute(AttributeType attributeType)
  {
    return attributes.contains(attributeType);
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

  MatchingRuleUse duplicate() {
    return new MatchingRuleUse(oid, names, description, isObsolete,
        attributeOIDs, extraProperties, definition);
  }

  @Override
  void validate(List<Message> warnings, Schema schema)
      throws SchemaException
  {
    try
    {
      matchingRule = schema.getMatchingRule(oid);
    }
    catch(UnknownSchemaElementException e)
    {
      // This is bad because the matching rule use is associated with a
      // matching rule that we don't know anything about.
      Message message =
          ERR_ATTR_SYNTAX_MRUSE_UNKNOWN_MATCHING_RULE.get(definition, oid);
      throw new SchemaException(message, e);
    }

    attributes = new HashSet<AttributeType>(attributeOIDs.size());
    AttributeType attributeType;
    for(String attribute : attributeOIDs)
    {
      try
      {
        attributeType = schema.getAttributeType(attribute);
      }
      catch(UnknownSchemaElementException e)
      {
        Message message = ERR_ATTR_SYNTAX_MRUSE_UNKNOWN_ATTR.get(
            oid, attribute);
        throw new SchemaException(message, e);
      }
      attributes.add(attributeType);
    }
  }

  void toStringContent(StringBuilder buffer)
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

    if (!attributeOIDs.isEmpty()) {
      Iterator<String> iterator = attributeOIDs.iterator();

      String firstName = iterator.next();
      if (iterator.hasNext()) {
        buffer.append(" APPLIES ( ");
        buffer.append(firstName);

        while (iterator.hasNext()) {
          buffer.append(" $ ");
          buffer.append(iterator.next());
        }

        buffer.append(" )");
      } else {
        buffer.append(" APPLIES ");
        buffer.append(firstName);
      }
    }
  }

  @Override
  public int hashCode() {
    return oid.hashCode();
  }
}
