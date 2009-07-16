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
public final class DITStructuralRule extends AbstractSchemaElement
{
  // The rule ID for this DIT structure rule.
  private final Integer ruleID;

  // The name form for this DIT structure rule.
  private final String nameForm;

  // The set of superior DIT structure rules.
  private final List<Integer> superiorRules;

  // The definition string used to create this objectclass.
  private final String definition;

  public DITStructuralRule(Integer ruleID,
                     List<String> names,
                     String description,
                     boolean obsolete,
                     String nameForm,
                     List<Integer> superiorRules,
                     Map<String, List<String>> extraProperties)
  {
    super(names, description, obsolete, extraProperties);

    Validator.ensureNotNull(ruleID, nameForm, superiorRules);
    this.ruleID = ruleID;
    this.nameForm = nameForm;
    this.superiorRules = superiorRules;
    this.definition = buildDefinition();
  }

  private DITStructuralRule(Integer ruleID,
                     List<String> names,
                     String description,
                     boolean obsolete,
                     String nameForm,
                     List<Integer> superiorRules,
                     Map<String, List<String>> extraProperties,
                     String definition)
  {
    super(names, description, obsolete, extraProperties);

    Validator.ensureNotNull(ruleID, nameForm, superiorRules);
    this.ruleID = ruleID;
    this.nameForm = nameForm;
    this.superiorRules = superiorRules;
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
   * Retrieves the name form for this DIT structure rule.
   *
   * @return  The name form for this DIT structure rule.
   */
  public String getNameForm()
  {
    return nameForm;
  }



  /**
   * Retrieves the set of superior rules for this DIT structure rule.
   *
   * @return  The set of superior rules for this DIT structure rule.
   */
  public Iterable<Integer> getSuperiorRules()
  {
    return superiorRules;
  }

  protected String getDefinition() {
    return definition;
  }

  protected void toStringContent(StringBuilder buffer)
  {
    buffer.append(" FORM ");
    buffer.append(nameForm);

    if ((superiorRules != null) && (! superiorRules.isEmpty()))
    {
      Iterator<Integer> iterator = superiorRules.iterator();

      int firstRule = iterator.next();
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

  protected String getIdentifier() {
    return ruleID.toString();
  }

  public static DITStructuralRule decode(String definition)
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

    List<String> names = Collections.emptyList();
    String description = "".intern();
    boolean isObsolete = false;
    String nameForm = null;
    List<Integer> superiorRules = Collections.emptyList();
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

    return new DITStructuralRule(ruleID, names, description, isObsolete,
        nameForm, superiorRules, extraProperties, definition);
  }
}
