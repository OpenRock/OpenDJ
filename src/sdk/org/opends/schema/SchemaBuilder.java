package org.opends.schema;

import org.opends.schema.Syntax;
import org.opends.schema.syntaxes.SubstitutionSyntax;
import org.opends.schema.syntaxes.SyntaxImplementation;
import org.opends.schema.syntaxes.RegexSyntax;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.*;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 16, 2009
 * Time: 5:53:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class SchemaBuilder
{
  private Map<String, SyntaxImplementation> syntaxes;
  private Map<String, MatchingRule> matchingRules;
  private Map<String, AttributeType> attributeTypes;
  private Map<String, ObjectClass> objectClasses;
  private Map<String, MatchingRuleUse> matchingRuleUses;
  private Map<String, NameForm> nameForms;
  private Map<String, DITContentRule> contentRules;
  private Map<String, DITStructureRule> structureRules;

  public void addSyntax(Syntax syntax, boolean overwrite)
      throws SchemaException
  {
    SyntaxImplementation implementation = null;

    // See if we need to override the implementation of the syntax
    for(String property : syntax.getExtraPropertyNames())
    {
      if(property.equalsIgnoreCase("x-subst"))
      {
        Iterator<String> values = syntax.getExtraProperty(property).iterator();
        if(values.hasNext())
        {
          String value = values.next();
          SyntaxImplementation substitute = syntaxes.get(value);
          if(substitute == null)
          {
            Message message = WARN_ATTR_SYNTAX_UNKNOWN_SUB_SYNTAX.get(
            syntax.getOID(), value);
            throw new SchemaException(message);
          }
          implementation = new SubstitutionSyntax(syntax, substitute);
          break;
        }
      }
      else if(property.equalsIgnoreCase("x-pattern"))
      {
        Iterator<String> values = syntax.getExtraProperty(property).iterator();
        if(values.hasNext())
        {
          String value = values.next();
          try
          {
            Pattern pattern = Pattern.compile(value);
            implementation = new RegexSyntax(syntax, pattern);            
          }
          catch(Exception e)
          {
            Message message =
                WARN_ATTR_SYNTAX_LDAPSYNTAX_REGEX_INVALID_PATTERN.get
                (syntax.getOID(),value);
            throw new SchemaException(message);
          }
          break;
        }
      }
    }

    // We need to look for an implementation
    if(implementation == null)
    {
      if(syntax instanceof SyntaxImplementation)
      {
        // An implementation is being added. No problem.
        implementation = (SyntaxImplementation)syntax;
      }
      else
      {
        // Need to see if an implementation is already defined in this schema.
        implementation = getSyntax(syntax.getOID());
        if(implementation != null && !syntax.equals(implementation))
        {
          // The syntax being added has different description and/or
          // extra properties. Wrap with a substitute syntax.
          implementation = new SubstitutionSyntax(syntax, implementation);
        }
      }
    }

    // We can't find an implmentation for the syntax. Should we use default?
    if(implementation == null)
    {
      Message message = WARN_ATTR_SYNTAX_NOT_IMPLEMENTED.get(
          syntax.getOID());
      throw new SchemaException(message);
    }

    if(overwrite || !syntaxes.containsKey(syntax.getOID()))
    {
      syntaxes.put(implementation.getOID(), implementation);
    }
  }

  public SyntaxImplementation getSyntax(String numericoid)
  {
    // Should we use a default in this case?
    return syntaxes.get(numericoid);
  }

  public MatchingRule getMatchingRule(String oid)
  {
    return null;
  }

  public ObjectClass getObjectClass(String oid)
  {
    return objectClasses.get(oid.toLowerCase());
  }

  public AttributeType getAttributeType(String oid)
  {
    return attributeTypes.get(oid.toLowerCase());
  }

  public void addMatchingRule(MatchingRule matchingRule,
                              boolean overwrite)
      throws SchemaException
  {
    // Make sure the specifiec syntax is defined in this schema.
    if(getSyntax(matchingRule.getSyntax()) == null)
    {
       Message message = ERR_ATTR_SYNTAX_MR_UNKNOWN_SYNTAX.get(
              matchingRule.getNameOrOID(), matchingRule.getSyntax());
      throw new SchemaException(message);
    }

    if(overwrite || !matchingRules.containsKey(matchingRule.getOID()))
    {
      matchingRules.put(matchingRule.getOID(), matchingRule);
    }
  }

  public void addAttributeType(AttributeType attributeType, boolean overwrite)
      throws SchemaException
  {
    if(attributeType.getSuperiorType() != null)
    {
      AttributeType superiorType =
          getAttributeType(attributeType.getSuperiorType());
      if(superiorType == null)
      {
        // This is bad because we don't know what the superior attribute
        // type is so we can't base this attribute type on it.
        Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_SUPERIOR_TYPE.
            get(attributeType.getNameOrOID(), attributeType.getSuperiorType());
        throw new SchemaException(message);
      }

      // If there is a superior type, then it must have the same usage as the
      // subordinate type.  Also, if the superior type is collective, then so
      // must the subordinate type be collective.
      if (superiorType.getUsage() != attributeType.getUsage())
      {
        Message message = WARN_ATTR_SYNTAX_ATTRTYPE_INVALID_SUPERIOR_USAGE.get(
            attributeType.getNameOrOID(), attributeType.getUsage().toString(),
            superiorType.getNameOrOID());
        throw new SchemaException(message);
      }

      if (superiorType.isCollective() != attributeType.isCollective())
      {
        Message message;
        if (attributeType.isCollective())
        {
          message =
              WARN_ATTR_SYNTAX_ATTRTYPE_COLLECTIVE_FROM_NONCOLLECTIVE.get(
                  attributeType.getNameOrOID(), superiorType.getNameOrOID());
        }
        else
        {
          message =
              WARN_ATTR_SYNTAX_ATTRTYPE_NONCOLLECTIVE_FROM_COLLECTIVE.get(
                  attributeType.getNameOrOID(), superiorType.getNameOrOID());
        }
        throw new SchemaException(message);
      }
    }

    if(attributeType.getEqualityMatchingRule() != null &&
        getMatchingRule(attributeType.getEqualityMatchingRule()) == null)
    {
      // This is bad because we have no idea what the equality matching
      // rule should be.
      Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_EQUALITY_MR.
          get(attributeType.getNameOrOID(), attributeType.getSuperiorType());
      throw new SchemaException(message);
    }

    if(attributeType.getOrderingMatchingRule() != null &&
        getMatchingRule(attributeType.getOrderingMatchingRule()) == null)
    {
      // This is bad because we have no idea what the ordering matching
      // rule should be.
      Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_ORDERING_MR.
          get(attributeType.getNameOrOID(), attributeType.getSuperiorType());
      throw new SchemaException(message);
    }

    if(attributeType.getSubstringMatchingRule() != null &&
        getMatchingRule(attributeType.getSubstringMatchingRule()) == null)
    {
      // This is bad because we have no idea what the substring matching
      // rule should be.
      Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_SUBSTRING_MR.
          get(attributeType.getNameOrOID(), attributeType.getSuperiorType());
      throw new SchemaException(message);
    }

    if(attributeType.getApproximateMatchingRule() != null &&
        getMatchingRule(attributeType.getApproximateMatchingRule()) == null)
    {
      // This is bad because we have no idea what the approximate matching
      // rule should be.
      Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_APPROXIMATE_MR.
          get(attributeType.getNameOrOID(), attributeType.getSuperiorType());
      throw new SchemaException(message);
    }

    if(attributeType.getSyntax() != null &&
        getSyntax(attributeType.getSyntax()) == null)
    {
      Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_SYNTAX.get(
              attributeType.getNameOrOID(), attributeType.getSyntax());
      throw new SchemaException(message);
    }

    // If the attribute type is COLLECTIVE, then it must have a usage of
    // userApplications.
    if (attributeType.isCollective() &&
        (attributeType.getUsage() != AttributeUsage.USER_APPLICATIONS))
    {
      Message message =
          WARN_ATTR_SYNTAX_ATTRTYPE_COLLECTIVE_IS_OPERATIONAL.get(
              attributeType.getNameOrOID());
      throw new SchemaException(message);
    }

    // If the attribute type is NO-USER-MODIFICATION, then it must not have a
    // usage of userApplications.
    if (attributeType.isNoUserModification() &&
        (attributeType.getUsage() == AttributeUsage.USER_APPLICATIONS))
    {
      Message message =
          WARN_ATTR_SYNTAX_ATTRTYPE_NO_USER_MOD_NOT_OPERATIONAL.get(
              attributeType.getNameOrOID());
      throw new SchemaException(message);
    }

    if(overwrite ||
        !attributeTypes.containsKey(attributeType.getOID()))
    {
      attributeTypes.put(attributeType.getOID(), attributeType);
      for(String name : attributeType.getNames())
      {
        attributeTypes.put(name.toLowerCase(), attributeType);
      }
    }
  }

  public void addObjectClass(ObjectClass objectClass, boolean overwrite)
      throws SchemaException
  {
    // Init a flag to check to inheritance from top (only needed for
    // structural object classes) per RFC 4512
    boolean derivesTop =
        objectClass.getObjectClassType() != ObjectClassType.STRUCTURAL;

    for(String superClassOid : objectClass.getSuperiorClasses())
    {
      ObjectClass superiorClass = getObjectClass(superClassOid);
      if(superiorClass == null)
      {
        Message message =
            WARN_ATTR_SYNTAX_OBJECTCLASS_UNKNOWN_SUPERIOR_CLASS.
                get(objectClass.getOID(), superClassOid);
        throw new SchemaException(message);
      }

      // Make sure that the inheritance configuration is acceptable.
      ObjectClassType superiorType = superiorClass.getObjectClassType();
      switch (objectClass.getObjectClassType())
      {
        case ABSTRACT:
          // Abstract classes may only inherit from other abstract classes.
          if (superiorType != ObjectClassType.ABSTRACT)
          {
            Message message =
                WARN_ATTR_SYNTAX_OBJECTCLASS_INVALID_SUPERIOR_TYPE.
                    get(objectClass.getNameOrOID(),
                        objectClass.getObjectClassType().toString(),
                        superiorType.toString(),
                        superiorClass.getNameOrOID());
            throw new SchemaException(message);
          }
          break;

        case AUXILIARY:
          // Auxiliary classes may only inherit from abstract classes or other
          // auxiliary classes.
          if ((superiorType != ObjectClassType.ABSTRACT) &&
              (superiorType != ObjectClassType.AUXILIARY))
          {
            Message message =
                WARN_ATTR_SYNTAX_OBJECTCLASS_INVALID_SUPERIOR_TYPE.
                    get(objectClass.getNameOrOID(),
                        objectClass.getObjectClassType().toString(),
                        superiorType.toString(),
                        superiorClass.getNameOrOID());
            throw new SchemaException(message);
          }
          break;

        case STRUCTURAL:
          // Structural classes may only inherit from abstract classes or other
          // structural classes.
          if ((superiorType != ObjectClassType.ABSTRACT) &&
              (superiorType != ObjectClassType.STRUCTURAL))
          {
            Message message =
                WARN_ATTR_SYNTAX_OBJECTCLASS_INVALID_SUPERIOR_TYPE.
                    get(objectClass.getNameOrOID(),
                        objectClass.getObjectClassType().toString(),
                        superiorType.toString(),
                        superiorClass.getNameOrOID());
            throw new SchemaException(message);
          }
          break;
      }

      // All existing structural object classes defined in this schema
      // are implicitly guaranteed to inherit from top
      if(!derivesTop && (superiorType == ObjectClassType.STRUCTURAL ||
          superiorClass.hasNameOrOID("2.5.6.0")))
      {
        derivesTop = true;
      }
    }

    // Structural classes must have the "top" objectclass somewhere
    // in the superior chain.
    if (!derivesTop)
    {
      Message message =
          WARN_ATTR_SYNTAX_OBJECTCLASS_STRUCTURAL_SUPERIOR_NOT_TOP.
              get(objectClass.getNameOrOID());
      throw new SchemaException(message);
    }

    for(String requiredAttribute : objectClass.getRequiredAttributes())
    {
      if(getAttributeType(requiredAttribute) == null)
      {
        // This isn't good because it means that the objectclass
        // requires an attribute type that we don't know anything about.
        Message message =
            WARN_ATTR_SYNTAX_OBJECTCLASS_UNKNOWN_REQUIRED_ATTR.
                get(objectClass.getNameOrOID(), requiredAttribute);
        throw new SchemaException(message);
      }
    }

    for(String optionalAttribute : objectClass.getOptionalAttributes())
    {
      if(getAttributeType(optionalAttribute) == null)
      {
        // This isn't good because it means that the objectclass
        // requires an attribute type that we don't know anything about.
        Message message =
            WARN_ATTR_SYNTAX_OBJECTCLASS_UNKNOWN_OPTIONAL_ATTR.
                get(objectClass.getNameOrOID(), optionalAttribute);
        throw new SchemaException(message);
      }
    }

    if(overwrite ||
        !objectClasses.containsKey(objectClass.getOID()))
    {
      objectClasses.put(objectClass.getOID(), objectClass);
      for(String name : objectClass.getNames())
      {
        objectClasses.put(name.toLowerCase(), objectClass);
      }
    }
  }
}
