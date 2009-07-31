package org.opends.schema;

import org.opends.server.util.Validator;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.*;
import org.opends.util.SubstringReader;
import org.opends.ldap.DecodeException;

import java.util.*;

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
public class ObjectClass extends AbstractSchemaElement
{
  // The OID that may be used to reference this definition.
  private final String oid;

  // The set of user defined names for this definition.
  private final SortedSet<String> names;

  // Indicates whether this definition is declared "obsolete".
  private final boolean isObsolete;

  // The reference to the superior objectclasses.
  private final Set<Pair<String, ObjectClass>> superiorClasses;

  // The objectclass type for this objectclass.
  private final ObjectClassType objectClassType;

  // The set of required attribute types for this objectclass.
  private final Set<Pair<String, AttributeType>> requiredAttributes;

  // The set of optional attribute types for this objectclass.
  private final Set<Pair<String, AttributeType>> optionalAttributes;

  // The definition string used to create this objectclass.
  private final String definition;

  public ObjectClass(String oid,
                     SortedSet<String> names,
                     String description,
                     boolean obsolete,
                     Set<String> superiorClasses,
                     Set<String> requiredAttributes,
                     Set<String> optionalAttributes,
                     ObjectClassType objectClassType,
                     Map<String, List<String>> extraProperties)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(oid, names);
    Validator.ensureNotNull(superiorClasses, requiredAttributes,
        optionalAttributes, objectClassType);
    this.oid = oid;
    this.names = names;
    this.isObsolete = obsolete;
    this.superiorClasses = Pair.createPairs(superiorClasses);
    this.objectClassType = objectClassType;
    this.requiredAttributes = Pair.createPairs(requiredAttributes);
    this.optionalAttributes = Pair.createPairs(optionalAttributes);
    this.definition = buildDefinition();
  }

  private ObjectClass(String oid,
                     SortedSet<String> names,
                     String description,
                     boolean obsolete,
                     Set<String> superiorClasses,
                     Set<String> requiredAttributes,
                     Set<String> optionalAttributes,
                     ObjectClassType objectClassType,
                     Map<String, List<String>> extraProperties,
                     String definition)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(oid, names);
    Validator.ensureNotNull(superiorClasses, requiredAttributes,
        optionalAttributes, objectClassType);
    this.oid = oid;
    this.names = names;
    this.isObsolete = obsolete;
    this.superiorClasses = Pair.createPairs(superiorClasses);
    this.objectClassType = objectClassType;
    this.requiredAttributes = Pair.createPairs(requiredAttributes);
    this.optionalAttributes = Pair.createPairs(optionalAttributes);
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
  public final boolean isObsolete()
  {
    return isObsolete;
  }

    /**
   * Retrieves the reference to the superior classes for this
   * objectclass.
   *
   * @return The list of superior classes for this objectlass.
   */
  public Iterator<ObjectClass> getSuperiorClasses()
  {
    return Pair.valueIterator(superiorClasses);
  }

  public boolean hasSuperiorClasses()
  {
    return !superiorClasses.isEmpty();
  }

  /**
   * Retrieves the list of required attributes
   * for this objectclass. Note that this set will not automatically
   * include any required attributes for superior objectclasses.
   *
   * @return Returns the list of required attributes for this
   * objectclass.
   */
  public Iterator<AttributeType> getRequiredAttributes()
  {
    return Pair.valueIterator(requiredAttributes);
  }

  public boolean hasRequiredAttributes()
  {
    return !requiredAttributes.isEmpty();
  }

    /**
   * Retrieves the list of optional attributes
   * for this objectclass. Note that this set will not automatically
   * include any optional attributes for superior objectclasses.
   *
   * @return Returns the list of optional attributes for this 
   * objectclass.
   */
  public Iterator<AttributeType> getOptionalAttributes()
  {
    return Pair.valueIterator(optionalAttributes);
  }

  public boolean hasOptionalAttributes()
  {
    return !optionalAttributes.isEmpty();
  }

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

    if (!superiorClasses.isEmpty()) {
      Iterator<String> iterator = Pair.keyIterator(superiorClasses);

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

    if (!requiredAttributes.isEmpty()) {
      Iterator<String> iterator = Pair.keyIterator(requiredAttributes);

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

    if (!optionalAttributes.isEmpty()) {
      Iterator<String> iterator = Pair.keyIterator(optionalAttributes);

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
  public int hashCode() {
    return oid.hashCode();
  }

  public static ObjectClass decode(String definition)
      throws DecodeException
  {
    SubstringReader reader = new SubstringReader(definition);

    // We'll do this a character at a time.  First, skip over any leading
    // whitespace.
    reader.skipWhitespaces();

    if (reader.remaining() <= 0)
    {
      // This means that the value was empty or contained only whitespace.  That
      // is illegal.
      Message message = ERR_ATTR_SYNTAX_OBJECTCLASS_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }


    // The next character must be an open parenthesis.  If it is not, then that
    // is an error.
    char c = reader.read();
    if (c != '(')
    {
      Message message = ERR_ATTR_SYNTAX_OBJECTCLASS_EXPECTED_OPEN_PARENTHESIS.
          get(definition, (reader.pos()-1), String.valueOf(c));
      throw new DecodeException(message);
    }


    // Skip over any spaces immediately following the opening parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    String oid = SchemaUtils.readNumericOID(reader);

    SortedSet<String> names = SchemaUtils.emptySortedSet();
    String description = "".intern();
    boolean isObsolete = false;
    Set<String> superiorClasses = Collections.emptySet();
    Set<String> requiredAttributes = Collections.emptySet();
    Set<String> optionalAttributes = Collections.emptySet();
    ObjectClassType objectClassType = ObjectClassType.STRUCTURAL;
    Map<String, List<String>> extraProperties = Collections.emptyMap();

    // At this point, we should have a pretty specific syntax that describes
    // what may come next, but some of the components are optional and it would
    // be pretty easy to put something in the wrong order, so we will be very
    // flexible about what we can accept.  Just look at the next token, figure
    // out what it is and how to treat what comes after it, then repeat until
    // we get to the end of the value.  But before we start, set default values
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
        superiorClasses = SchemaUtils.readOIDs(reader);
      }
      else if (tokenName.equalsIgnoreCase("abstract"))
      {
        // This indicates that entries must not include this objectclass unless
        // they also include a non-abstract objectclass that inherits from this
        // class.  We do not need any more parsing for this token.
        objectClassType = ObjectClassType.ABSTRACT;
      }
      else if (tokenName.equalsIgnoreCase("structural"))
      {
        // This indicates that this is a structural objectclass.  We do not need
        // any more parsing for this token.
        objectClassType = ObjectClassType.STRUCTURAL;
      }
      else if (tokenName.equalsIgnoreCase("auxiliary"))
      {
        // This indicates that this is an auxiliary objectclass.  We do not need
        // any more parsing for this token.
        objectClassType = ObjectClassType.AUXILIARY;
      }
      else if (tokenName.equalsIgnoreCase("must"))
      {
        requiredAttributes = SchemaUtils.readOIDs(reader);
      }
      else if (tokenName.equalsIgnoreCase("may"))
      {
        optionalAttributes = SchemaUtils.readOIDs(reader);
      }
      else
      {
        // This must be a non-standard property and it must be followed by
        // either a single value in single quotes or an open parenthesis
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

    return new ObjectClass(oid, names, description, isObsolete, superiorClasses,
        requiredAttributes, optionalAttributes, objectClassType, extraProperties,
        definition);
  }
}
