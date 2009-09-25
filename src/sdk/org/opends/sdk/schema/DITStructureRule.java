package org.opends.sdk.schema;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.opends.sdk.util.Validator;

/**
 * This class defines a DIT structure rule, which is used to indicate
 * the types of children that entries may have.
 */
public abstract class DITStructureRule extends AbstractSchemaElement
{
  // The rule ID for this DIT structure rule.
  protected final Integer ruleID;

  // The set of user defined names for this definition.
  protected final List<String> names;

  // Indicates whether this definition is declared "obsolete".
  protected final boolean isObsolete;

  // The name form for this DIT structure rule.
  protected final String nameFormOID;

  // The set of superior DIT structure rules.
  protected final Set<Integer> superiorRuleIDs;

  // The definition string used to create this objectclass.
  protected final String definition;

  protected DITStructureRule(Integer ruleID,
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
  public abstract NameForm getNameForm();



  /**
   * Retrieves the set of superior rules for this DIT structure rule.
   *
   * @return  The set of superior rules for this DIT structure rule.
   */
  public abstract Iterable<DITStructureRule> getSuperiorRules();



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

  protected abstract DITStructureRule duplicate();
}
