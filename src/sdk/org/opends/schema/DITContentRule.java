package org.opends.schema;

import org.opends.server.util.Validator;
import org.opends.ldap.DecodeException;
import org.opends.util.SubstringReader;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DCR_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DCR_EXPECTED_OPEN_PARENTHESIS;

import java.util.*;

/**
 * This class defines a DIT content rule, which defines the set of
 * allowed, required, and prohibited attributes for entries with a
 * given structural objectclass, and also indicates which auxiliary
 * classes that may be included in the entry.
 */
public final class DITContentRule extends AbstractSchemaElement
{
  // The structural objectclass for this DIT content rule.
  private final Pair<String, ObjectClass> structuralClass;

  // The set of user defined names for this definition.
  private final SortedSet<String> names;

  // Indicates whether this definition is declared "obsolete".
  private final boolean isObsolete;

  // The set of auxiliary objectclasses that entries with this content
  // rule may contain, in a mapping between the objectclass and the
  // user-defined name for that class.
  private final Set<Pair<String, ObjectClass>> auxiliaryClasses;

  // The set of optional attribute types for this DIT content rule.
  private final Set<Pair<String, AttributeType>> optionalAttributes;

  // The set of prohibited attribute types for this DIT content rule.
  private final Set<Pair<String, AttributeType>> prohibitedAttributes;

  // The set of required attribute types for this DIT content rule.
  private final Set<Pair<String, AttributeType>> requiredAttributes;

  // The definition string used to create this objectclass.
  private final String definition;

  public DITContentRule(String structuralClass,
                        SortedSet<String> names,
                        String description,
                        boolean obsolete,
                        Set<String> auxiliaryClasses,
                        Set<String> optionalAttributes,
                        Set<String> prohibitedAttributes,
                        Set<String> requiredAttributes,
                        Map<String, List<String>> extraProperties)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(names, structuralClass);
    Validator.ensureNotNull(auxiliaryClasses, optionalAttributes,
        prohibitedAttributes, requiredAttributes);
    this.names = names;
    this.isObsolete = obsolete;
    this.structuralClass = Pair.createPair(structuralClass);
    this.auxiliaryClasses = Pair.createPairs(auxiliaryClasses);
    this.optionalAttributes = Pair.createPairs(optionalAttributes);
    this.prohibitedAttributes = Pair.createPairs(prohibitedAttributes);
    this.requiredAttributes = Pair.createPairs(requiredAttributes);
    this.definition = buildDefinition();
  }

  private DITContentRule(String structuralClass,
                        SortedSet<String> names,
                        String description,
                        boolean obsolete,
                        Set<String> auxiliaryClasses,
                        Set<String> optionalAttributes,
                        Set<String> prohibitedAttributes,
                        Set<String> requiredAttributes,
                        Map<String, List<String>> extraProperties,
                     String definition)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(names, structuralClass);
    Validator.ensureNotNull(auxiliaryClasses, optionalAttributes,
        prohibitedAttributes, requiredAttributes);
    this.names = names;
    this.isObsolete = obsolete;
    this.structuralClass = Pair.createPair(structuralClass);
    this.auxiliaryClasses = Pair.createPairs(auxiliaryClasses);
    this.optionalAttributes = Pair.createPairs(optionalAttributes);
    this.prohibitedAttributes = Pair.createPairs(prohibitedAttributes);
    this.requiredAttributes = Pair.createPairs(requiredAttributes);
    this.definition = definition;
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
    return structuralClass.getValue();
  }

    /**
   * Retrieves the set of auxiliary objectclasses that may be used for
   * entries associated with this DIT content rule.
   *
   * @return  The set of auxiliary objectclasses that may be used for
   *          entries associated with this DIT content rule.
   */
  public Iterator<ObjectClass> getAuxiliaryClasses()
  {
    return Pair.valueIterator(auxiliaryClasses);
  }

    /**
   * Retrieves the set of required attributes for this DIT content
   * rule.
   *
   * @return  The set of required attributes for this DIT content
   *          rule.
   */
  public Iterator<AttributeType> getRequiredAttributes()
  {
    return Pair.valueIterator(requiredAttributes);
  }

    /**
   * Retrieves the set of optional attributes for this DIT content
   * rule.
   *
   * @return  The set of optional attributes for this DIT content
   *          rule.
   */
  public Iterator<AttributeType> getOptionalAttributes()
  {
    return Pair.valueIterator(optionalAttributes);
  }

    /**
   * Retrieves the set of prohibited attributes for this DIT content
   * rule.
   *
   * @return  The set of prohibited attributes for this DIT content
   *          rule.
   */
  public Iterator<AttributeType> getProhibitedAttributes()
  {
    return Pair.valueIterator(prohibitedAttributes);
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
    buffer.append(structuralClass);

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

    if (! auxiliaryClasses.isEmpty())
    {
      Iterator<String> iterator = Pair.keyIterator(auxiliaryClasses);

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

    if (! requiredAttributes.isEmpty())
    {
      Iterator<String> iterator =
           Pair.keyIterator(requiredAttributes);

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

    if (! optionalAttributes.isEmpty())
    {
      Iterator<String> iterator =
           Pair.keyIterator(optionalAttributes);

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

    if (! prohibitedAttributes.isEmpty())
    {
      Iterator<String> iterator =
           Pair.keyIterator(prohibitedAttributes);

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
  public int hashCode() {
    return structuralClass.hashCode();
  }

  public static DITContentRule decode(String definition)
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
      Message message = ERR_ATTR_SYNTAX_DCR_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }


    // The next character must be an open parenthesis.  If it is not, then that
    // is an error.
    char c = reader.read();
    if (c != '(')
    {
      Message message = ERR_ATTR_SYNTAX_DCR_EXPECTED_OPEN_PARENTHESIS.
          get(definition, (reader.pos()-1), String.valueOf(c));
      throw new DecodeException(message);
    }


    // Skip over any spaces immediately following the opening parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    String structuralClass = SchemaUtils.readNumericOID(reader);

    SortedSet<String> names = SchemaUtils.emptySortedSet();
    String description = "".intern();
    boolean isObsolete = false;
    Set<String> auxiliaryClasses = Collections.emptySet();
    Set<String> optionalAttributes = Collections.emptySet();
    Set<String> prohibitedAttributes = Collections.emptySet();
    Set<String> requiredAttributes = Collections.emptySet();
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
      else if (tokenName.equalsIgnoreCase("aux"))
      {
        auxiliaryClasses = SchemaUtils.readOIDs(reader);
      }
      else if (tokenName.equalsIgnoreCase("must"))
      {
        requiredAttributes = SchemaUtils.readOIDs(reader);
      }
      else if (tokenName.equalsIgnoreCase("may"))
      {
        optionalAttributes = SchemaUtils.readOIDs(reader);
      }
      else if (tokenName.equalsIgnoreCase("not"))
      {
        prohibitedAttributes = SchemaUtils.readOIDs(reader);
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

    return new DITContentRule(structuralClass, names, description,
        isObsolete, auxiliaryClasses, optionalAttributes,
        prohibitedAttributes, requiredAttributes, extraProperties,
        definition);
  }
}
