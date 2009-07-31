package org.opends.schema;

import org.opends.server.util.Validator;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.ResultCode;
import org.opends.util.SubstringReader;
import org.opends.ldap.DecodeException;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_MRUSE_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_MRUSE_EXPECTED_OPEN_PARENTHESIS;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_MRUSE_NO_ATTR;
import org.opends.messages.Message;
import org.opends.schema.SchemaUtils;

import java.util.*;

/**
 * This class defines a data structure for storing and interacting
 * with a matching rule use definition, which may be used to restrict
 * the set of attribute types that may be used for a given matching
 * rule.
 */
public final class MatchingRuleUse extends AbstractSchemaElement
{
  // The OID that may be used to reference this definition.
  private final String oid;

  // The set of user defined names for this definition.
  private final SortedSet<String> names;

  // Indicates whether this definition is declared "obsolete".
  private final boolean isObsolete;

  // The set of attribute types with which this matching rule use is
  // associated.
  private final Set<Pair<String, AttributeType>> attributes;

  // The definition string used to create this objectclass.
  private final String definition;

    public MatchingRuleUse(String oid,
                     SortedSet<String> names,
                     String description,
                     boolean obsolete,
                     Set<String> attributes,
                     Map<String, List<String>> extraProperties)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(oid, names, attributes);
    this.oid = oid;
    this.names = names;
    this.isObsolete = obsolete;
    this.attributes = Pair.createPairs(attributes);
    this.definition = buildDefinition();
  }

  private MatchingRuleUse(String oid,
                     SortedSet<String> names,
                     String description,
                     boolean obsolete,
                     Set<String> attributes,
                     Map<String, List<String>> extraProperties,
                     String definition)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(oid, names, attributes);
    this.oid = oid;
    this.names = names;
    this.isObsolete = obsolete;
    this.attributes = Pair.createPairs(attributes);
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
   * Retrieves the set of attributes associated with this matching
   * rule use.
   *
   * @return  The set of attributes associated with this matching
   *          rule use.
   */
  public Iterator<AttributeType> getAttributes()
  {
    return Pair.valueIterator(attributes);
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

    if (!attributes.isEmpty()) {
      Iterator<String> iterator = Pair.keyIterator(attributes);

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

  public static MatchingRuleUse decode(String definition)
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
      Message message = ERR_ATTR_SYNTAX_MRUSE_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }


    // The next character must be an open parenthesis.  If it is not, then that
    // is an error.
    char c = reader.read();
    if (c != '(')
    {
      Message message = ERR_ATTR_SYNTAX_MRUSE_EXPECTED_OPEN_PARENTHESIS.
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
    Set<String> attributes = null;
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
      else if (tokenName.equalsIgnoreCase("applies"))
      {
        attributes = SchemaUtils.readOIDs(reader);
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

    // Make sure that the set of attributes was defined.
    if (attributes == null || attributes.size() == 0)
    {
      Message message = ERR_ATTR_SYNTAX_MRUSE_NO_ATTR.get(definition);
      throw new DecodeException(message);
    }

    return new MatchingRuleUse(oid, names, description, isObsolete, attributes,
        extraProperties, definition);
  }
}
