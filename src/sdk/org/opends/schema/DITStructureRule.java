package org.opends.schema;

import org.opends.server.util.Validator;
import org.opends.ldap.DecodeException;
import org.opends.util.SubstringReader;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DSR_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DSR_EXPECTED_OPEN_PARENTHESIS;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DSR_NO_NAME_FORM;
import org.opends.messages.Message;
import org.opends.schema.SchemaUtils;

import java.util.*;

/**
 * This class defines a DIT structure rule, which is used to indicate
 * the types of children that entries may have.
 */
public final class DITStructureRule extends AbstractSchemaElement
{
  // The rule ID for this DIT structure rule.
  private final Integer ruleID;

  // The set of user defined names for this definition.
  private final SortedSet<String> names;

  // Indicates whether this definition is declared "obsolete".
  private final boolean isObsolete;

  // The name form for this DIT structure rule.
  private final Pair<String, NameForm> nameForm;

  // The set of superior DIT structure rules.
  private final Set<Pair<Integer, DITStructureRule>> superiorRules;

  // The definition string used to create this objectclass.
  private final String definition;

  public DITStructureRule(Integer ruleID,
                     SortedSet<String> names,
                     String description,
                     boolean obsolete,
                     String nameForm,
                     Set<Integer> superiorRules,
                     Map<String, List<String>> extraProperties)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(ruleID, nameForm, superiorRules);
    this.ruleID = ruleID;
    this.names = names;
    this.isObsolete = obsolete;
    this.nameForm = Pair.createPair(nameForm);
    this.superiorRules = Pair.createPairs(superiorRules);
    this.definition = buildDefinition();
  }

  private DITStructureRule(Integer ruleID,
                     SortedSet<String> names,
                     String description,
                     boolean obsolete,
                     String nameForm,
                     Set<Integer> superiorRules,
                     Map<String, List<String>> extraProperties,
                     String definition)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(ruleID, names, nameForm, superiorRules);
    this.ruleID = ruleID;
    this.names = names;
    this.isObsolete = obsolete;
    this.nameForm = Pair.createPair(nameForm);
    this.superiorRules = Pair.createPairs(superiorRules);
    this.definition = definition;
  }

  /**
   * Retrieves the rule ID for this DIT structure rule.
   *
   * @return  The rule ID for this DIT structure rule.
   */
  public Integer getRuleID()
  {
    return ruleID;
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
   * Retrieves the name form for this DIT structure rule.
   *
   * @return  The name form for this DIT structure rule.
   */
  public NameForm getNameForm()
  {
    return nameForm.getValue();
  }



  /**
   * Retrieves the set of superior rules for this DIT structure rule.
   *
   * @return  The set of superior rules for this DIT structure rule.
   */
  public Iterator<DITStructureRule> getSuperiorRules()
  {
    return Pair.valueIterator(superiorRules);
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
    buffer.append(ruleID);

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

    buffer.append(" FORM ");
    buffer.append(nameForm);

    if ((superiorRules != null) && (! superiorRules.isEmpty()))
    {
      Iterator<Integer> iterator = Pair.keyIterator(superiorRules);

      Integer firstRule = iterator.next();
      if (iterator.hasNext())
      {
        buffer.append(" SUP ( ");
        buffer.append(firstRule);

        while (iterator.hasNext())
        {
          buffer.append(" ");
          buffer.append(iterator.next());
        }

        buffer.append(" )");
      }
      else
      {
        buffer.append(" SUP ");
        buffer.append(firstRule);
      }
    }
  }

  @Override
  public int hashCode() {
    return ruleID.hashCode();
  }

  public static DITStructureRule decode(String definition)
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
      Message message = ERR_ATTR_SYNTAX_DSR_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }


    // The next character must be an open parenthesis.  If it is not, then that
    // is an error.
    char c = reader.read();
    if (c != '(')
    {
      Message message = ERR_ATTR_SYNTAX_DSR_EXPECTED_OPEN_PARENTHESIS.
          get(definition, (reader.pos()-1), String.valueOf(c));
      throw new DecodeException(message);
    }


    // Skip over any spaces immediately following the opening parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    Integer ruleID = SchemaUtils.readRuleID(reader);

    SortedSet<String> names = SchemaUtils.emptySortedSet();
    String description = "".intern();
    boolean isObsolete = false;
    String nameForm = null;
    Set<Integer> superiorRules = Collections.emptySet();
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
      else if (tokenName.equalsIgnoreCase("form"))
      {
        nameForm = SchemaUtils.readOID(reader);
      }
      else if (tokenName.equalsIgnoreCase("sup"))
      {
        superiorRules = SchemaUtils.readRuleIDs(reader);
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

    if (nameForm == null)
    {
      Message message = ERR_ATTR_SYNTAX_DSR_NO_NAME_FORM.get(definition);
      throw new DecodeException(message);
    }

    return new DITStructureRule(ruleID, names, description, isObsolete,
        nameForm, superiorRules, extraProperties, definition);
  }
}
