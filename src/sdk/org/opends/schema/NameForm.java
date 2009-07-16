package org.opends.schema;

import org.opends.server.util.Validator;
import org.opends.ldap.DecodeException;
import org.opends.util.SubstringReader;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.*;
import org.opends.schema.SchemaUtils;

import java.util.*;

/**
 * This class defines a data structure for storing and interacting
 * with a name form, which defines the attribute type(s) that must
 * and/or may be used in the RDN of an entry with a given structural
 * objectclass.
 */
public final class NameForm extends AbstractSchemaElement
{
    // The OID that may be used to reference this definition.
  private final String oid;

  // The reference to the structural objectclass for this name form.
  private final String structuralClass;

  // The set of optional attribute types for this name form.
  private final List<String> optionalAttributes;

  // The set of required attribute types for this name form.
  private final List<String> requiredAttributes;

  // The definition string used to create this objectclass.
  private final String definition;

  public NameForm(String oid,
                     List<String> names,
                     String description,
                     boolean obsolete,
                     String structuralClass,
                     List<String> requiredAttributes,
                     List<String> optionalAttributes,
                     Map<String, List<String>> extraProperties)
  {
    super(names, description, obsolete, extraProperties);

    Validator.ensureNotNull(oid, structuralClass, requiredAttributes,
        optionalAttributes);
    Validator.ensureTrue(requiredAttributes.size() > 0);
    this.oid = oid;
    this.structuralClass = structuralClass;
    this.requiredAttributes = requiredAttributes;
    this.optionalAttributes = optionalAttributes;
    this.definition = buildDefinition();
  }

  private NameForm(String oid,
                     List<String> names,
                     String description,
                     boolean obsolete,
                     String structuralClass,
                     List<String> requiredAttributes,
                     List<String> optionalAttributes,
                     Map<String, List<String>> extraProperties,
                     String definition)
  {
    super(names, description, obsolete, extraProperties);

    Validator.ensureNotNull(oid, structuralClass, requiredAttributes,
        optionalAttributes);
    this.oid = oid;
    this.structuralClass = structuralClass;
    this.requiredAttributes = requiredAttributes;
    this.optionalAttributes = optionalAttributes;
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
   * Retrieves the reference to the structural objectclass for this
   * name form.
   *
   * @return  The reference to the structural objectclass for this
   *          name form.
   */
  public String getStructuralClass()
  {
    return structuralClass;
  }



  /**
   * Retrieves the set of required attributes for this name form.
   *
   * @return  The set of required attributes for this name form.
   */
  public Iterable<String> getRequiredAttributes()
  {
    return requiredAttributes;
  }



  /**
   * Retrieves the set of optional attributes for this name form.
   *
   * @return  The set of optional attributes for this name form.
   */
  public Iterable<String> getOptionalAttributes()
  {
    return optionalAttributes;
  }

  protected String getDefinition() {
    return definition;
  }
  
  protected void toStringContent(StringBuilder buffer)
  {
    buffer.append(" OC ");
    buffer.append(structuralClass);

    if (! requiredAttributes.isEmpty())
    {
      Iterator<String> iterator =
           requiredAttributes.iterator();

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
           optionalAttributes.iterator();

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

  protected String getIdentifier() {
    return oid;
  }

  public static NameForm decode(String definition)
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
      Message message = ERR_ATTR_SYNTAX_NAME_FORM_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }


    // The next character must be an open parenthesis.  If it is not, then that
    // is an error.
    char c = reader.read();
    if (c != '(')
    {
      Message message = ERR_ATTR_SYNTAX_NAME_FORM_EXPECTED_OPEN_PARENTHESIS.
          get(definition, (reader.pos()-1), c);
      throw new DecodeException(message);
    }


    // Skip over any spaces immediately following the opening parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    String oid = SchemaUtils.readNumericOID(reader);

    List<String> names = Collections.emptyList();
    String description = "".intern();
    boolean isObsolete = false;
    String structuralClass = null;
    List<String> optionalAttributes = Collections.emptyList();
    List<String> requiredAttributes = null;
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
      else if (tokenName.equalsIgnoreCase("OC"))
      {
        structuralClass = SchemaUtils.readOID(reader);
      }
      else if(tokenName.equalsIgnoreCase("must"))
      {
        requiredAttributes = SchemaUtils.readOIDs(reader);
      }
      else if(tokenName.equalsIgnoreCase("may"))
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

    // Make sure that a structural class was specified.  If not, then it cannot
    // be valid.
    if (structuralClass == null)
    {
      Message message =
          ERR_ATTR_SYNTAX_NAME_FORM_NO_STRUCTURAL_CLASS.get(definition);
      throw new DecodeException(message);
    }

    if (requiredAttributes == null)
    {
      Message message =
          ERR_ATTR_SYNTAX_NAME_FORM_NO_REQUIRED_ATTR.get(definition);
      throw new DecodeException(message);
    }

    return new NameForm(oid, names, description, isObsolete, structuralClass,
        requiredAttributes, optionalAttributes, extraProperties, definition);
  }
}
