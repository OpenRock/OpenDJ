package org.opends.sdk.schema;

import java.util.*;

import org.opends.sdk.util.Validator;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DSR_UNKNOWN_NAME_FORM;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DSR_UNKNOWN_RULE_ID;

/**
 * This class defines a DIT structure rule, which is used to indicate
 * the types of children that entries may have.
 */
public final class DITStructureRule extends SchemaElement
{
  // The rule ID for this DIT structure rule.
  private final Integer ruleID;

  // The set of user defined names for this definition.
  private final List<String> names;

  // Indicates whether this definition is declared "obsolete".
  private final boolean isObsolete;

  // The name form for this DIT structure rule.
  private final String nameFormOID;

  // The set of superior DIT structure rules.
  private final Set<Integer> superiorRuleIDs;

  // The definition string used to create this objectclass.
  private final String definition;

  private NameForm nameForm;
  private Set<DITStructureRule> superiorRules = Collections.emptySet();

  DITStructureRule(Integer ruleID,
                             List<String> names,
                             String description,
                             boolean obsolete,
                             String nameFormOID,
                             Set<Integer> superiorRuleIDs,
                             Map<String, List<String>> extraProperties,
                             String definition)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(ruleID, nameFormOID, superiorRuleIDs);
    this.ruleID = ruleID;
    this.names = names;
    this.isObsolete = obsolete;
    this.nameFormOID = nameFormOID;
    this.superiorRuleIDs = superiorRuleIDs;

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
   * Retrieves the name or rule ID for this schema definition.
   * If it has one or more names, then the primary name will be returned. If it
   * does not have any names, then the OID will be returned.
   *
   * @return The name or OID for this schema definition.
   */
  public String getNameOrRuleID() {
    if(names.isEmpty())
    {
      return ruleID.toString();
    }
    return names.get(0);
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
    return nameForm;
  }



  /**
   * Retrieves the set of superior rules for this DIT structure rule.
   *
   * @return  The set of superior rules for this DIT structure rule.
   */
  public Iterable<DITStructureRule> getSuperiorRules()
  {
    return superiorRules;
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

  DITStructureRule duplicate() {
    return new DITStructureRule(ruleID, names, description, isObsolete,
        nameFormOID, superiorRuleIDs, extraProperties, definition);
  }

  void validate(List<Message> warnings, Schema schema) throws SchemaException
  {
    try
    {
      nameForm = schema.getNameForm(nameFormOID);
    }
    catch(UnknownSchemaElementException e)
    {
      Message message = ERR_ATTR_SYNTAX_DSR_UNKNOWN_NAME_FORM.get(
          definition, nameFormOID);
      throw new SchemaException(message, e);
    }

    if(!superiorRuleIDs.isEmpty())
    {
      superiorRules = new HashSet<DITStructureRule>(superiorRuleIDs.size());
      DITStructureRule rule;
      for(Integer id : superiorRuleIDs)
      {
        try
        {
          rule = schema.getDITStructureRule(id);
        }
        catch(UnknownSchemaElementException e)
        {
          Message message = ERR_ATTR_SYNTAX_DSR_UNKNOWN_RULE_ID.
              get(definition, id);
          throw new SchemaException(message, e);
        }
        superiorRules.add(rule);
      }
    }
  }

  final void toStringContent(StringBuilder buffer)
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
    buffer.append(nameFormOID);

    if ((superiorRuleIDs != null) && (! superiorRuleIDs.isEmpty()))
    {
      Iterator<Integer> iterator = superiorRuleIDs.iterator();

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
  public final int hashCode() {
    return ruleID.hashCode();
  }
}
