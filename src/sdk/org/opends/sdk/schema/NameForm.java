package org.opends.sdk.schema;

import java.util.*;

import org.opends.sdk.util.Validator;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.*;

/**
 * This class defines a data structure for storing and interacting
 * with a name form, which defines the attribute type(s) that must
 * and/or may be used in the RDN of an entry with a given structural
 * objectclass.
 */
public final class NameForm extends SchemaElement
{
  // The OID that may be used to reference this definition.
  private final String oid;

  // The set of user defined names for this definition.
  private final List<String> names;

  // Indicates whether this definition is declared "obsolete".
  private final boolean isObsolete;

  // The reference to the structural objectclass for this name form.
  private final String structuralClassOID;

  // The set of optional attribute types for this name form.
  private final Set<String> optionalAttributeOIDs;

  // The set of required attribute types for this name form.
  private final Set<String> requiredAttributeOIDs;

  // The definition string used to create this objectclass.
  private final String definition;

  private ObjectClass structuralClass;
  private Set<AttributeType> optionalAttributes = Collections.emptySet();
  private Set<AttributeType> requiredAttributes = Collections.emptySet();

  NameForm(String oid,
           List<String> names,
           String description,
           boolean obsolete,
           String structuralClassOID,
           Set<String> requiredAttributeOIDs,
           Set<String> optionalAttributeOIDs,
           Map<String, List<String>> extraProperties,
           String definition)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(oid, names);
    Validator.ensureNotNull(structuralClassOID, requiredAttributeOIDs,
        optionalAttributeOIDs);
    Validator.ensureTrue(requiredAttributeOIDs.size() > 0,
        "required attribute is empty");
    this.oid = oid;
    this.names = names;
    this.isObsolete = obsolete;
    this.structuralClassOID = structuralClassOID;
    this.requiredAttributeOIDs = requiredAttributeOIDs;
    this.optionalAttributeOIDs = optionalAttributeOIDs;

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
   * Retrieves the reference to the structural objectclass for this
   * name form.
   *
   * @return  The reference to the structural objectclass for this
   *          name form.
   */
  public ObjectClass getStructuralClass()
  {
    return structuralClass;
  }



  /**
   * Retrieves the set of required attributes for this name form.
   *
   * @return  The set of required attributes for this name form.
   */
  public Iterable<AttributeType> getRequiredAttributes()
  {
    return requiredAttributes;
  }



  /**
   * Retrieves the set of optional attributes for this name form.
   *
   * @return  The set of optional attributes for this name form.
   */
  public Iterable<AttributeType> getOptionalAttributes()
  {
    return optionalAttributes;
  }

  NameForm duplicate() {
    return new NameForm(oid, names, description, isObsolete,
        structuralClassOID, requiredAttributeOIDs, optionalAttributeOIDs,
        extraProperties, definition);
  }

  void validate(List<Message> warnings, Schema schema)
      throws SchemaException
  {
    try
    {
      structuralClass = schema.getObjectClass(structuralClassOID);
    }
    catch(UnknownSchemaElementException e)
    {
      Message message =
          ERR_ATTR_SYNTAX_NAME_FORM_UNKNOWN_STRUCTURAL_CLASS.
              get(oid, structuralClassOID);
      throw new SchemaException(message, e);
    }
    if(structuralClass.getObjectClassType() != ObjectClassType.STRUCTURAL)
    {
      // This is bad because the associated structural class type is not
      // structural.
      Message message =
          ERR_ATTR_SYNTAX_NAME_FORM_STRUCTURAL_CLASS_NOT_STRUCTURAL.
              get(oid, structuralClass.getOID(),
                  structuralClass.getNameOrOID(),
                  String.valueOf(structuralClass.getObjectClassType()));
      throw new SchemaException(message);
    }

    requiredAttributes =
        new HashSet<AttributeType>(requiredAttributeOIDs.size());
    AttributeType attributeType;
    for(String oid : requiredAttributeOIDs)
    {
      try
      {
        attributeType = schema.getAttributeType(oid);
      }
      catch(UnknownSchemaElementException e)
      {
        // This isn't good because it means that the name form requires
        // an attribute type that we don't know anything about.
        Message message =
            ERR_ATTR_SYNTAX_NAME_FORM_UNKNOWN_REQUIRED_ATTR.
                get(this.oid, oid);
        throw new SchemaException(message, e);
      }
      requiredAttributes.add(attributeType);
    }

    if(!optionalAttributeOIDs.isEmpty())
    {
      optionalAttributes =
          new HashSet<AttributeType>(optionalAttributeOIDs.size());
      for(String oid : optionalAttributeOIDs)
      {
        try
        {
          attributeType = schema.getAttributeType(oid);
        }
        catch(UnknownSchemaElementException e)
        {
          // This isn't good because it means that the name form requires
          // an attribute type that we don't know anything about.
          Message message =
              ERR_ATTR_SYNTAX_NAME_FORM_UNKNOWN_OPTIONAL_ATTR.
                  get(this.oid, oid);
          throw new SchemaException(message, e);
        }
        optionalAttributes.add(attributeType);
      }
    }
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

    buffer.append(" OC ");
    buffer.append(structuralClassOID);

    if (! requiredAttributeOIDs.isEmpty())
    {
      Iterator<String> iterator = requiredAttributeOIDs.iterator();

      String firstName = iterator.next();
      if (iterator.hasNext())
      {
        buffer.append(" MUST ( ");
        buffer.append(firstName);

        while (iterator.hasNext())
        {
          buffer.append(" $ ");
          buffer.append(iterator.next());
        }

        buffer.append(" )");
      }
      else
      {
        buffer.append(" MUST ");
        buffer.append(firstName);
      }
    }

    if (! optionalAttributeOIDs.isEmpty())
    {
      Iterator<String> iterator = optionalAttributeOIDs.iterator();

      String firstName = iterator.next();
      if (iterator.hasNext())
      {
        buffer.append(" MAY ( ");
        buffer.append(firstName);

        while (iterator.hasNext())
        {
          buffer.append(" $ ");
          buffer.append(iterator.next());
        }

        buffer.append(" )");
      }
      else
      {
        buffer.append(" MAY ");
        buffer.append(firstName);
      }
    }
  }

  @Override
  public int hashCode() {
    return oid.hashCode();
  }
}
