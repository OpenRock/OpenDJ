package org.opends.sdk.schema;

import java.util.*;

import org.opends.sdk.util.Validator;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.*;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DCR_PROHIBITED_REQUIRED_BY_STRUCTURAL;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DCR_PROHIBITED_REQUIRED_BY_AUXILIARY;

/**
 * This class defines a DIT content rule, which defines the set of
 * allowed, required, and prohibited attributes for entries with a
 * given structural objectclass, and also indicates which auxiliary
 * classes that may be included in the entry.
 */
public final class DITContentRule extends SchemaElement
{
  // The structural objectclass for this DIT content rule.
  private final String structuralClassOID;

  // The set of user defined names for this definition.
  private final List<String> names;

  // Indicates whether this definition is declared "obsolete".
  private final boolean isObsolete;

  // The set of auxiliary objectclasses that entries with this content
  // rule may contain, in a mapping between the objectclass and the
  // user-defined name for that class.
  private final Set<String> auxiliaryClassOIDs;

  // The set of optional attribute types for this DIT content rule.
  private final Set<String> optionalAttributeOIDs;

  // The set of prohibited attribute types for this DIT content rule.
  private final Set<String> prohibitedAttributeOIDs;

  // The set of required attribute types for this DIT content rule.
  private final Set<String> requiredAttributeOIDs;

  // The definition string used to create this objectclass.
  private final String definition;

  private ObjectClass structuralClass;
  private Set<ObjectClass> auxiliaryClasses = Collections.emptySet();
  private Set<AttributeType> optionalAttributes = Collections.emptySet();
  private Set<AttributeType> prohibitedAttributes = Collections.emptySet();
  private Set<AttributeType> requiredAttributes = Collections.emptySet();

  DITContentRule(String structuralClassOID,
                           List<String> names,
                           String description,
                           boolean obsolete,
                           Set<String> auxiliaryClassOIDs,
                           Set<String> optionalAttributeOIDs,
                           Set<String> prohibitedAttributeOIDs,
                           Set<String> requiredAttributeOIDs,
                           Map<String, List<String>> extraProperties,
                           String definition)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(structuralClassOID, names);
    Validator.ensureNotNull(auxiliaryClassOIDs, optionalAttributeOIDs,
        prohibitedAttributeOIDs, requiredAttributeOIDs);
    this.names = names;
    this.isObsolete = obsolete;
    this.structuralClassOID = structuralClassOID;
    this.auxiliaryClassOIDs = auxiliaryClassOIDs;
    this.optionalAttributeOIDs = optionalAttributeOIDs;
    this.prohibitedAttributeOIDs = prohibitedAttributeOIDs;
    this.requiredAttributeOIDs = requiredAttributeOIDs;

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
   * Retrieves the structural class OID for this schema definition.
   *
   * @return The structural class OID for this schema definition.
   */
  public String getStructuralClassOID() {
    return structuralClassOID;
  }


  /**
   * Retrieves the name or structural class OID for this schema definition.
   * If it has one or more names, then the primary name will be returned. If it
   * does not have any names, then the OID will be returned.
   *
   * @return The name or OID for this schema definition.
   */
  public String getNameOrOID() {
    if(names.isEmpty())
    {
      return structuralClassOID;
    }
    return names.get(0);
  }

  /**
   * Indicates whether this schema definition has the specified name
   * or structural class OID.
   *
   * @param value
   *          The value for which to make the determination.
   * @return <code>true</code> if the provided value matches the OID
   *         or one of the names assigned to this schema definition,
   *         or <code>false</code> if not.
   */
  public boolean hasNameOrOID(String value) {
    return hasName(value) ||
        structuralClassOID.equals(value);
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
   * Retrieves the structural objectclass for this DIT content rule.
   *
   * @return  The structural objectclass for this DIT content rule.
   */
  public ObjectClass getStructuralClass()
  {
    return structuralClass;
  }

  /**
   * Retrieves the set of auxiliary objectclasses that may be used for
   * entries associated with this DIT content rule.
   *
   * @return  The set of auxiliary objectclasses that may be used for
   *          entries associated with this DIT content rule.
   */
  public Iterable<ObjectClass> getAuxiliaryClasses()
  {
    return auxiliaryClasses;
  }

  /**
   * Retrieves the set of required attributes for this DIT content
   * rule.
   *
   * @return  The set of required attributes for this DIT content
   *          rule.
   */
  public Iterable<AttributeType> getRequiredAttributes()
  {
    return requiredAttributes;
  }

  /**
   * Retrieves the set of optional attributes for this DIT content
   * rule.
   *
   * @return  The set of optional attributes for this DIT content
   *          rule.
   */
  public Iterable<AttributeType> getOptionalAttributes()
  {
    return optionalAttributes;
  }

  /**
   * Retrieves the set of prohibited attributes for this DIT content
   * rule.
   *
   * @return  The set of prohibited attributes for this DIT content
   *          rule.
   */
  public Iterable<AttributeType> getProhibitedAttributes()
  {
    return prohibitedAttributes;
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

  DITContentRule duplicate() {
    return new DITContentRule(structuralClassOID, names, description,
        isObsolete, auxiliaryClassOIDs, optionalAttributeOIDs,
        prohibitedAttributeOIDs, requiredAttributeOIDs, extraProperties,
        definition);
  }

  @Override
  void validate(List<Message> warnings, Schema schema)
      throws SchemaException
  {
    // Get the objectclass with the specified OID.  If it does not exist or is
    // not structural, then fail.
    if(structuralClassOID != null)
    {
      try
      {
        structuralClass = schema.getObjectClass(structuralClassOID);
      }
      catch(UnknownSchemaElementException e)
      {
        Message message = ERR_ATTR_SYNTAX_DCR_UNKNOWN_STRUCTURAL_CLASS.get(
            definition, structuralClassOID);
        throw new SchemaException(message, e);
      }
      if(structuralClass.getObjectClassType() != ObjectClassType.STRUCTURAL)
      {
        Message message = ERR_ATTR_SYNTAX_DCR_STRUCTURAL_CLASS_NOT_STRUCTURAL.
            get(definition, structuralClass.getOID(),
                structuralClass.getNameOrOID(),
                structuralClass.getObjectClassType().toString());
        throw new SchemaException(message);
      }
    }

    if(!auxiliaryClassOIDs.isEmpty())
    {
      auxiliaryClasses =
          new HashSet<ObjectClass>(auxiliaryClassOIDs.size());
      ObjectClass objectClass;
      for(String oid : auxiliaryClassOIDs)
      {
        try
        {
          objectClass = schema.getObjectClass(oid);
        }
        catch(UnknownSchemaElementException e)
        {
          // This isn't good because it is an unknown auxiliary class.
          Message message = ERR_ATTR_SYNTAX_DCR_UNKNOWN_AUXILIARY_CLASS.get(
              definition, oid);
          throw new SchemaException(message, e);
        }
        if(objectClass.getObjectClassType() != ObjectClassType.AUXILIARY)
        {
          // This isn't good because it isn't an auxiliary class.
          Message message = ERR_ATTR_SYNTAX_DCR_AUXILIARY_CLASS_NOT_AUXILIARY.
              get(definition, structuralClass.getOID(),
                  structuralClass.getObjectClassType().toString());
          throw new SchemaException(message);
        }
        auxiliaryClasses.add(objectClass);
      }
    }

    if(!requiredAttributeOIDs.isEmpty())
    {
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
          // This isn't good because it means that the DIT content rule
          // requires an attribute type that we don't know anything about.
          Message message = ERR_ATTR_SYNTAX_DCR_UNKNOWN_REQUIRED_ATTR.get(
              definition, oid);
          throw new SchemaException(message, e);
        }
        requiredAttributes.add(attributeType);
      }
    }

    if(!optionalAttributeOIDs.isEmpty())
    {
      optionalAttributes =
          new HashSet<AttributeType>(optionalAttributeOIDs.size());
      AttributeType attributeType;
      for(String oid : optionalAttributeOIDs)
      {
        try
        {
          attributeType = schema.getAttributeType(oid);
        }
        catch(UnknownSchemaElementException e)
        {
          // This isn't good because it means that the DIT content rule
          // requires an attribute type that we don't know anything about.
          Message message = ERR_ATTR_SYNTAX_DCR_UNKNOWN_OPTIONAL_ATTR.get(
              definition, oid);
          throw new SchemaException(message, e);
        }
        optionalAttributes.add(attributeType);
      }
    }

    if(!prohibitedAttributeOIDs.isEmpty())
    {
      prohibitedAttributes =
          new HashSet<AttributeType>(prohibitedAttributeOIDs.size());
      AttributeType attributeType;
      for(String oid : prohibitedAttributeOIDs)
      {
        try
        {
          attributeType = schema.getAttributeType(oid);
        }
        catch(UnknownSchemaElementException e)
        {
          // This isn't good because it means that the DIT content rule
          // requires an attribute type that we don't know anything about.
          Message message = ERR_ATTR_SYNTAX_DCR_UNKNOWN_PROHIBITED_ATTR.get(
              definition, oid);
          throw new SchemaException(message, e);
        }
        prohibitedAttributes.add(attributeType);
      }
    }

    // Make sure that none of the prohibited attributes is required by the
    // structural or any of the auxiliary classes.
    for (AttributeType t : prohibitedAttributes)
    {
      if (structuralClass.isRequired(t))
      {
        Message message = ERR_ATTR_SYNTAX_DCR_PROHIBITED_REQUIRED_BY_STRUCTURAL.
            get(definition, t.getNameOrOID(), structuralClass.getNameOrOID());
        throw new SchemaException(message);
      }

      for (ObjectClass oc : auxiliaryClasses)
      {
        if (oc.isRequired(t))
        {
          Message message =
              ERR_ATTR_SYNTAX_DCR_PROHIBITED_REQUIRED_BY_AUXILIARY.
                  get(definition, t.getNameOrOID(), oc.getNameOrOID());
          throw new SchemaException(message);
        }
      }
    }
  }


  final void toStringContent(StringBuilder buffer)
  {
    buffer.append(structuralClassOID);

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

    if (! auxiliaryClassOIDs.isEmpty())
    {
      Iterator<String> iterator = auxiliaryClassOIDs.iterator();

      String firstClass = iterator.next();
      if (iterator.hasNext())
      {
        buffer.append(" AUX (");
        buffer.append(firstClass);

        while (iterator.hasNext())
        {
          buffer.append(" $ ");
          buffer.append(iterator.next());
        }

        buffer.append(" )");
      }
      else
      {
        buffer.append(" AUX ");
        buffer.append(firstClass);
      }
    }

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

    if (! prohibitedAttributeOIDs.isEmpty())
    {
      Iterator<String> iterator = prohibitedAttributeOIDs.iterator();

      String firstName = iterator.next();
      if (iterator.hasNext())
      {
        buffer.append(" NOT ( ");
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
        buffer.append(" NOT ");
        buffer.append(firstName);
      }
    }
  }

  @Override
  public final int hashCode() {
    return structuralClassOID.hashCode();
  }
}
