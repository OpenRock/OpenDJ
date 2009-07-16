package org.opends.schema;

import org.opends.server.util.Validator;
import org.opends.ldap.DecodeException;
import org.opends.util.SubstringReader;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DCR_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DCR_EXPECTED_OPEN_PARENTHESIS;
import org.opends.schema.SchemaUtils;

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
  private final String structuralClass;

  // The set of auxiliary objectclasses that entries with this content
  // rule may contain, in a mapping between the objectclass and the
  // user-defined name for that class.
  private final List<String> auxiliaryClasses;

  // The set of optional attribute types for this DIT content rule.
  private final List<String> optionalAttributes;

  // The set of prohibited attribute types for this DIT content rule.
  private final List<String> prohibitedAttributes;

  // The set of required attribute types for this DIT content rule.
  private final List<String> requiredAttributes;

  // The definition string used to create this objectclass.
  private final String definition;

  public DITContentRule(String structuralClass,
                        List<String> names,
                        String description,
                        boolean obsolete,
                        List<String> auxiliaryClasses,
                        List<String> optionalAttributes,
                        List<String> prohibitedAttributes,
                        List<String> requiredAttributes,
                        Map<String, List<String>> extraProperties)
  {
    super(names, description, obsolete, extraProperties);

    Validator.ensureNotNull(structuralClass);
    Validator.ensureNotNull(auxiliaryClasses, optionalAttributes,
        prohibitedAttributes, requiredAttributes);
    this.structuralClass = structuralClass;
    this.auxiliaryClasses = auxiliaryClasses;
    this.optionalAttributes = optionalAttributes;
    this.prohibitedAttributes = prohibitedAttributes;
    this.requiredAttributes = requiredAttributes;
    this.definition = buildDefinition();
  }

  private DITContentRule(String structuralClass,
                        List<String> names,
                        String description,
                        boolean obsolete,
                        List<String> auxiliaryClasses,
                        List<String> optionalAttributes,
                        List<String> prohibitedAttributes,
                        List<String> requiredAttributes,
                        Map<String, List<String>> extraProperties,
                     String definition)
  {
    super(names, description, obsolete, extraProperties);

    Validator.ensureNotNull(structuralClass);
    Validator.ensureNotNull(auxiliaryClasses, optionalAttributes,
        prohibitedAttributes, requiredAttributes);
    this.structuralClass = structuralClass;
    this.auxiliaryClasses = auxiliaryClasses;
    this.optionalAttributes = optionalAttributes;
    this.prohibitedAttributes = prohibitedAttributes;
    this.requiredAttributes = requiredAttributes;
    this.definition = definition;
  }

    /**
   * Retrieves the structural objectclass for this DIT content rule.
   *
   * @return  The structural objectclass for this DIT content rule.
   */
  public String getStructuralClass()
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
  public Iterable<String> getAuxiliaryClasses()
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
  public Iterable<String> getRequiredAttributes()
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
  public Iterable<String> getOptionalAttributes()
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
  public Iterable<String> getProhibitedAttributes()
  {
    return prohibitedAttributes;
  }

  protected String getDefinition() {
    return definition;
  }

  protected void toStringContent(StringBuilder buffer)
  {
if (! auxiliaryClasses.isEmpty())
    {
      Iterator<String> iterator = auxiliaryClasses.iterator();

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

    if (! prohibitedAttributes.isEmpty())
    {
      Iterator<String> iterator =
           prohibitedAttributes.iterator();

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

  protected String getIdentifier() {
    return structuralClass;
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

    List<String> names = Collections.emptyList();
    String description = "".intern();
    boolean isObsolete = false;
    List<String> auxiliaryClasses = Collections.emptyList();
    List<String> optionalAttributes = Collections.emptyList();
    List<String> prohibitedAttributes = Collections.emptyList();
    List<String> requiredAttributes = Collections.emptyList();
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
