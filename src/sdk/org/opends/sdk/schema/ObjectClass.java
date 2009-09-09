package org.opends.sdk.schema;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.opends.server.util.Validator;

/**
 * This class defines a data structure for storing and interacting
 * with an objectclass, which contains a collection of attributes that
 * must and/or may be present in an entry with that objectclass.
 * <p>
 * Where ordered sets of names, attribute types, or extra properties
 * are provided, the ordering will be preserved when the associated
 * fields are accessed via their getters or via the
 * {@link #toString()} methods.
 */
public abstract class ObjectClass extends AbstractSchemaElement
{
  // The OID that may be used to reference this definition.
  protected final String oid;

  // The set of user defined names for this definition.
  protected final List<String> names;

  // Indicates whether this definition is declared "obsolete".
  protected final boolean isObsolete;

  // The reference to the superior objectclasses.
  protected final Set<String> superiorClassOIDs;

  // The objectclass type for this objectclass.
  protected final ObjectClassType objectClassType;

  // The set of required attribute types for this objectclass.
  protected final Set<String> requiredAttributeOIDs;

  // The set of optional attribute types for this objectclass.
  protected final Set<String> optionalAttributeOIDs;

  // The definition string used to create this objectclass.
  protected final String definition;

  protected ObjectClass(String oid,
                        List<String> names,
                        String description,
                        boolean obsolete,
                        Set<String> superiorClassOIDs,
                        Set<String> requiredAttributeOIDs,
                        Set<String> optionalAttributeOIDs,
                        ObjectClassType objectClassType,
                        Map<String, List<String>> extraProperties,
                        String definition)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(oid, names);
    Validator.ensureNotNull(superiorClassOIDs, requiredAttributeOIDs,
        optionalAttributeOIDs, objectClassType);
    this.oid = oid;
    this.names = names;
    this.isObsolete = obsolete;
    this.superiorClassOIDs = superiorClassOIDs;
    this.objectClassType = objectClassType;
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
   * Indicates whether this objectclass is a descendant of the
   * provided class.
   *
   * @param objectClass
   *          The objectClass for which to make the determination.
   * @return <code>true</code> if this objectclass is a descendant
   *         of the provided class, or <code>false</code> if not.
   */
  public abstract boolean isDescendantOf(ObjectClass objectClass);

  /**
   * Retrieves the reference to the superior classes for this
   * objectclass.
   *
   * @return The list of superior classes for this objectlass.
   */
  public abstract Iterable<ObjectClass> getSuperiorClasses();

  /**
   * Retrieves the list of required attributes
   * for this objectclass. Note that this set will not automatically
   * include any required attributes for superior objectclasses.
   *
   * @return Returns the list of required attributes for this
   * objectclass.
   */
  public abstract Iterable<AttributeType> getDeclaredRequiredAttributes();

  /**
   * Retrieves the list of all required attributes for this objectclass and
   * any superior objectclasses that it might have.
   *
   * @return Returns the list of all required
   *         attributes for this objectclass and any superior
   *         objectclasses that it might have.
   */
  public abstract Iterable<AttributeType> getRequiredAttributes();

  /**
   * Retrieves the list of all optional attributes for this objectclass and
   * any superior objectclasses that it might have.
   *
   * @return Returns the list of all optional
   *         attributes for this objectclass and any superior
   *         objectclasses that it might have.
   */
  public abstract Iterable<AttributeType> getOptionalAttributes();

  /**
   * Retrieves the list of optional attributes
   * for this objectclass. Note that this set will not automatically
   * include any optional attributes for superior objectclasses.
   *
   * @return Returns the list of optional attributes for this
   * objectclass.
   */
  public abstract Iterable<AttributeType> getDeclaredOptionalAttributes();

  /**
   * Indicates whether the provided attribute type is included in the
   * required attribute list for this or any of its superior
   * objectclasses.
   *
   * @param attributeType
   *          The attribute type for which to make the determination.
   * @return <code>true</code> if the provided attribute type is
   *         required by this objectclass or any of its superior
   *         classes, or <code>false</code> if not.
   */
  public abstract boolean isRequired(AttributeType attributeType);

  /**
   * Indicates whether the provided attribute type is included in the
   * optional attribute list for this or any of its superior
   * objectclasses.
   *
   * @param attributeType
   *          The attribute type for which to make the determination.
   * @return <code>true</code> if the provided attribute type is
   *         optional for this objectclass or any of its superior
   *         classes, or <code>false</code> if not.
   */
  public abstract boolean isOptional(AttributeType attributeType);

  /**
   * Indicates whether the provided attribute type is in the list of
   * required or optional attributes for this objectclass or any of
   * its superior classes.
   *
   * @param attributeType
   *          The attribute type for which to make the determination.
   * @return <code>true</code> if the provided attribute type is
   *         required or allowed for this objectclass or any of its
   *         superior classes, or <code>false</code> if it is not.
   */
  public abstract boolean isRequiredOrOptional(AttributeType attributeType);

  /**
   * Retrieves the objectclass type for this objectclass.
   *
   * @return The objectclass type for this objectclass.
   */
  public ObjectClassType getObjectClassType() {

    return objectClassType;
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

    if (!superiorClassOIDs.isEmpty()) {
      Iterator<String> iterator = superiorClassOIDs.iterator();

      String firstName = iterator.next();
      if (iterator.hasNext()) {
        buffer.append(" SUP ( ");
        buffer.append(firstName);

        while (iterator.hasNext()) {
          buffer.append(" $ ");
          buffer.append(iterator.next());
        }

        buffer.append(" )");
      } else {
        buffer.append(" SUP ");
        buffer.append(firstName);
      }
    }

    if (objectClassType != null) {
      buffer.append(" ");
      buffer.append(objectClassType.toString());
    }

    if (!requiredAttributeOIDs.isEmpty()) {
      Iterator<String> iterator = requiredAttributeOIDs.iterator();

      String firstName = iterator.next();
      if (iterator.hasNext()) {
        buffer.append(" MUST ( ");
        buffer.append(firstName);

        while (iterator.hasNext()) {
          buffer.append(" $ ");
          buffer.append(iterator.next());
        }

        buffer.append(" )");
      } else {
        buffer.append(" MUST ");
        buffer.append(firstName);
      }
    }

    if (!optionalAttributeOIDs.isEmpty()) {
      Iterator<String> iterator = optionalAttributeOIDs.iterator();

      String firstName = iterator.next();
      if (iterator.hasNext()) {
        buffer.append(" MAY ( ");
        buffer.append(firstName);

        while (iterator.hasNext()) {
          buffer.append(" $ ");
          buffer.append(iterator.next());
        }

        buffer.append(" )");
      } else {
        buffer.append(" MAY ");
        buffer.append(firstName);
      }
    }
  }

  @Override
  public final int hashCode() {
    return oid.hashCode();
  }

  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o instanceof ObjectClass)
    {
      ObjectClass other = (ObjectClass) o;
      return oid.equals(other.oid);
    }

    return false;
  }

  protected abstract ObjectClass duplicate();
}
