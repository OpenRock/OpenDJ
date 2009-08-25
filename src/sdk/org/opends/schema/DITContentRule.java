package org.opends.schema;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.opends.server.util.Validator;

/**
 * This class defines a DIT content rule, which defines the set of
 * allowed, required, and prohibited attributes for entries with a
 * given structural objectclass, and also indicates which auxiliary
 * classes that may be included in the entry.
 */
public abstract class DITContentRule extends AbstractSchemaElement
{
  // The structural objectclass for this DIT content rule.
  protected final String structuralClassOID;

  // The set of user defined names for this definition.
  protected final List<String> names;

  // Indicates whether this definition is declared "obsolete".
  protected final boolean isObsolete;

  // The set of auxiliary objectclasses that entries with this content
  // rule may contain, in a mapping between the objectclass and the
  // user-defined name for that class.
  protected final Set<String> auxiliaryClassOIDs;

  // The set of optional attribute types for this DIT content rule.
  protected final Set<String> optionalAttributeOIDs;

  // The set of prohibited attribute types for this DIT content rule.
  protected final Set<String> prohibitedAttributeOIDs;

  // The set of required attribute types for this DIT content rule.
  protected final Set<String> requiredAttributeOIDs;

  // The definition string used to create this objectclass.
  protected final String definition;

  protected DITContentRule(String structuralClassOID,
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
  public abstract ObjectClass getStructuralClass();

    /**
   * Retrieves the set of auxiliary objectclasses that may be used for
   * entries associated with this DIT content rule.
   *
   * @return  The set of auxiliary objectclasses that may be used for
   *          entries associated with this DIT content rule.
   */
  public abstract Iterable<ObjectClass> getAuxiliaryClasses();

    /**
   * Retrieves the set of required attributes for this DIT content
   * rule.
   *
   * @return  The set of required attributes for this DIT content
   *          rule.
   */
  public abstract Iterable<AttributeType> getRequiredAttributes();

    /**
   * Retrieves the set of optional attributes for this DIT content
   * rule.
   *
   * @return  The set of optional attributes for this DIT content
   *          rule.
   */
  public abstract Iterable<AttributeType> getOptionalAttributes();

    /**
   * Retrieves the set of prohibited attributes for this DIT content
   * rule.
   *
   * @return  The set of prohibited attributes for this DIT content
   *          rule.
   */
  public abstract Iterable<AttributeType> getProhibitedAttributes();



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

  protected final void toStringContent(StringBuilder buffer)
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

  protected abstract DITContentRule duplicate();
}
