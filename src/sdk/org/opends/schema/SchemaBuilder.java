package org.opends.schema;

import org.opends.schema.matchingrules.*;
import org.opends.schema.syntaxes.SyntaxImplementation;
import org.opends.schema.syntaxes.RegexSyntax;
import org.opends.messages.Message;
import org.opends.messages.MessageBuilder;
import static org.opends.messages.SchemaMessages.*;
import org.opends.ldap.DecodeException;
import org.opends.util.SubstringReader;
import static org.opends.server.util.ServerConstants.*;
import org.opends.server.util.Validator;
import org.opends.server.types.ByteSequence;
import org.opends.types.ConditionResult;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 16, 2009
 * Time: 5:53:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class SchemaBuilder implements Schema
{
  private final class RealAttributeType extends AttributeType
  {
    // The superior attribute type from which this attribute type
    // inherits.
    private AttributeType superiorType;

    // The equality matching rule for this attribute type.
    private MatchingRule equalityMatchingRule;

    // The ordering matching rule for this attribute type.
    private MatchingRule orderingMatchingRule;

    // The substring matching rule for this attribute type.
    private MatchingRule substringMatchingRule;

    // The approximate matching rule for this attribute type.
    private MatchingRule approximateMatchingRule;

    // The syntax for this attribute type.
    private Syntax syntax;

    private RealAttributeType(String oid, SortedSet<String> names,
                              String description, boolean obsolete,
                              String superiorType, String equalityMatchingRule,
                              String orderingMatchingRule,
                              String substringMatchingRule,
                              String approximateMatchingRule, String syntax,
                              boolean singleValue, boolean collective,
                              boolean noUserModification,
                              AttributeUsage attributeUsage,
                              Map<String, List<String>> extraProperties,
                              String definition) {
      super(oid, names, description, obsolete, superiorType,
          equalityMatchingRule, orderingMatchingRule, substringMatchingRule,
          approximateMatchingRule, syntax, singleValue, collective,
          noUserModification, attributeUsage, extraProperties, definition);
    }

    /**
     * {@inheritDoc}
     */
    public AttributeType getSuperiorType()
    {
      return superiorType;
    }

    /**
     * {@inheritDoc}
     */
    public Syntax getSyntax()
    {
      return syntax;
    }

    /**
     * {@inheritDoc}
     */
    public MatchingRule getApproximateMatchingRule()
    {
      return approximateMatchingRule;
    }

    /**
     * {@inheritDoc}
     */
    public MatchingRule getEqualityMatchingRule()
    {
      return equalityMatchingRule;
    }

    /**
     * {@inheritDoc}
     */
    public MatchingRule getOrderingMatchingRule()
    {
      return orderingMatchingRule;
    }

    /**
     * {@inheritDoc}
     */
    public MatchingRule getSubstringMatchingRule()
    {
      return substringMatchingRule;
    }

    private void resolveReferences() throws SchemaException
    {
      if(superiorTypeOID != null)
      {
        superiorType = SchemaBuilder.this.getAttributeType(superiorTypeOID);
        if(superiorType == null)
        {
          // This is bad because we don't know what the superior attribute
          // type is so we can't base this attribute type on it.
          Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_SUPERIOR_TYPE.
              get(getNameOrOID(), superiorTypeOID);
          throw new SchemaException(message);
        }

        // If there is a superior type, then it must have the same usage as the
        // subordinate type.  Also, if the superior type is collective, then so
        // must the subordinate type be collective.
        if (superiorType.getUsage() != getUsage())
        {
          Message message =
              WARN_ATTR_SYNTAX_ATTRTYPE_INVALID_SUPERIOR_USAGE.get(
                  getNameOrOID(), getUsage().toString(),
                  superiorType.getNameOrOID());
          throw new SchemaException(message);
        }

        if (superiorType.isCollective() != isCollective())
        {
          Message message;
          if (isCollective())
          {
            message =
                WARN_ATTR_SYNTAX_ATTRTYPE_COLLECTIVE_FROM_NONCOLLECTIVE.get(
                    getNameOrOID(), superiorType.getNameOrOID());
          }
          else
          {
            message =
                WARN_ATTR_SYNTAX_ATTRTYPE_NONCOLLECTIVE_FROM_COLLECTIVE.get(
                    getNameOrOID(), superiorType.getNameOrOID());
          }
          throw new SchemaException(message);
        }
      }

      if(syntaxOID != null)
      {
        syntax = SchemaBuilder.this.getSyntax(syntaxOID);
        if(syntax == null)
        {
          Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_SYNTAX.get(
              getNameOrOID(), syntaxOID);
          throw new SchemaException(message);
        }
      }
      else if(getSuperiorType() != null &&
          getSuperiorType().getSyntax() != null)
      {
        // Try to inherit the syntax from the superior type if possible
        syntax = getSuperiorType().getSyntax();
      }

      if(equalityMatchingRuleOID != null)
      {
        // Use explicitly defined matching rule first.
        equalityMatchingRule =
            SchemaBuilder.this.getMatchingRule(equalityMatchingRuleOID);
        if(equalityMatchingRule == null)
        {
          // This is bad because we have no idea what the equality matching
          // rule should be.
          Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_EQUALITY_MR.
              get(getNameOrOID(), equalityMatchingRuleOID);
          throw new SchemaException(message);
        }
      }
      else if(getSuperiorType() != null &&
          getSuperiorType().getEqualityMatchingRule() != null)
      {
        // Inherit matching rule from superior type if possible
        equalityMatchingRule = getSuperiorType().getEqualityMatchingRule();
      }
      else if(getSyntax() != null &&
          getSyntax().getEqualityMatchingRule() != null)
      {
        // Use default for syntax
        equalityMatchingRule = getSyntax().getEqualityMatchingRule();
      }

      if(orderingMatchingRuleOID != null)
      {
        orderingMatchingRule =
            SchemaBuilder.this.getMatchingRule(orderingMatchingRuleOID);
        if(orderingMatchingRule == null)
        {
          // This is bad because we have no idea what the ordering matching
          // rule should be.
          Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_ORDERING_MR.
              get(getNameOrOID(), orderingMatchingRuleOID);
          throw new SchemaException(message);
        }
      }
      else if(getSuperiorType() != null &&
          getSuperiorType().getOrderingMatchingRule() != null)
      {
        // Inherit matching rule from superior type if possible
        orderingMatchingRule = getSuperiorType().getOrderingMatchingRule();
      }
      else if(getSyntax() != null &&
          getSyntax().getOrderingMatchingRule() != null)
      {
        // Use default for syntax
        orderingMatchingRule = getSyntax().getOrderingMatchingRule();
      }

      if(substringMatchingRule != null)
      {
        substringMatchingRule =
            SchemaBuilder.this.getMatchingRule(substringMatchingRuleOID);
        if(substringMatchingRule == null)
        {
          // This is bad because we have no idea what the substring matching
          // rule should be.
          Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_SUBSTRING_MR.
              get(getNameOrOID(), substringMatchingRuleOID);
          throw new SchemaException(message);
        }
      }
      else if(getSuperiorType() != null &&
          getSuperiorType().getSubstringMatchingRule() != null)
      {
        // Inherit matching rule from superior type if possible
        substringMatchingRule = getSuperiorType().getSubstringMatchingRule();
      }
      else if(getSyntax() != null &&
          getSyntax().getSubstringMatchingRule() != null)
      {
        // Use default for syntax
        substringMatchingRule = getSyntax().getSubstringMatchingRule();
      }

      if(approximateMatchingRuleOID != null)
      {
        approximateMatchingRule =
            SchemaBuilder.this.getMatchingRule(approximateMatchingRuleOID);
        if(approximateMatchingRule == null)
        {
          // This is bad because we have no idea what the approximate matching
          // rule should be.
          Message message = WARN_ATTR_SYNTAX_ATTRTYPE_UNKNOWN_APPROXIMATE_MR.
              get(getNameOrOID(), approximateMatchingRuleOID);
          throw new SchemaException(message);
        }
      }
      else if(getSuperiorType() != null &&
          getSuperiorType().getApproximateMatchingRule() != null)
      {
        // Inherit matching rule from superior type if possible
        approximateMatchingRule =
            getSuperiorType().getApproximateMatchingRule();
      }
      else if(getSyntax() != null &&
          getSyntax().getApproximateMatchingRule() != null)
      {
        // Use default for syntax
        approximateMatchingRule =
            getSyntax().getApproximateMatchingRule();
      }

      // If the attribute type is COLLECTIVE, then it must have a usage of
      // userApplications.
      if (isCollective() && getUsage() != AttributeUsage.USER_APPLICATIONS)
      {
        Message message =
            WARN_ATTR_SYNTAX_ATTRTYPE_COLLECTIVE_IS_OPERATIONAL.get(
                getNameOrOID());
        throw new SchemaException(message);
      }

      // If the attribute type is NO-USER-MODIFICATION, then it must not have a
      // usage of userApplications.
      if (isNoUserModification() &&
          getUsage() == AttributeUsage.USER_APPLICATIONS)
      {
        Message message =
            WARN_ATTR_SYNTAX_ATTRTYPE_NO_USER_MOD_NOT_OPERATIONAL.get(
                getNameOrOID());
        throw new SchemaException(message);
      }
    }
  }

  private final class RealEqualityMatchingRule extends EqualityMatchingRule
  {
    private Syntax syntax;
    private final EqualityMatchingRuleImplementation implementation;

    private RealEqualityMatchingRule(String oid, SortedSet<String> names,
                             String description, boolean obsolete,
                             String syntax,
                             Map<String, List<String>> extraProperties,
                             EqualityMatchingRuleImplementation implementation,
                             String definition) {
      super(oid, names, description, obsolete, syntax, extraProperties,
          definition);
      this.implementation = implementation;
    }

    public Syntax getSyntax()
    {
      return syntax;
    }

    private void resolveReferences() throws SchemaException
    {
      // Make sure the specifiec syntax is defined in this schema.
      syntax = SchemaBuilder.this.getSyntax(syntaxOID);
      if(syntax == null)
      {
        Message message = ERR_ATTR_SYNTAX_MR_UNKNOWN_SYNTAX.get(getNameOrOID(),
            syntaxOID);
        throw new SchemaException(message);
      }
    }

    /**
     * {@inheritDoc}
     */
    public ConditionResult valuesMatch(ByteSequence attributeValue,
                                       ByteSequence assertionValue) {
      return implementation.valuesMatch(SchemaBuilder.this, attributeValue,
          assertionValue);
    }

    public ByteSequence normalizeAttributeValue(ByteSequence value) {
      return implementation.normalizeAttributeValue(SchemaBuilder.this, value);
    }

    public ByteSequence normalizeAssertionValue(ByteSequence value) {
      return implementation.normalizeAssertionValue(SchemaBuilder.this, value);
    }

    public boolean areEqual(ByteSequence attributeValue,
                            ByteSequence assertionValue) {
      return implementation.areEqual(SchemaBuilder.this, attributeValue,
          assertionValue);
    }
  }

  private final class RealOrderingMatchingRule extends OrderingMatchingRule
  {
    private Syntax syntax;
    private final OrderingMatchingRuleImplementation implementation;

    private RealOrderingMatchingRule(String oid, SortedSet<String> names,
                             String description, boolean obsolete,
                             String syntax,
                             Map<String, List<String>> extraProperties,
                             OrderingMatchingRuleImplementation implementation,
                             String definition) {
      super(oid, names, description, obsolete, syntax, extraProperties,
          definition);
      this.implementation = implementation;
    }

    public Syntax getSyntax()
    {
      return syntax;
    }

    private void resolveReferences() throws SchemaException
    {
      // Make sure the specifiec syntax is defined in this schema.
      syntax = SchemaBuilder.this.getSyntax(syntaxOID);
      if(syntax == null)
      {
        Message message = ERR_ATTR_SYNTAX_MR_UNKNOWN_SYNTAX.get(getNameOrOID(),
            syntaxOID);
        throw new SchemaException(message);
      }
    }

    /**
     * {@inheritDoc}
     */
    public ConditionResult valuesMatch(ByteSequence attributeValue,
                                       ByteSequence assertionValue) {
      return implementation.valuesMatch(SchemaBuilder.this, attributeValue,
          assertionValue);
    }

    public ByteSequence normalizeAttributeValue(ByteSequence value) {
      return implementation.normalizeAttributeValue(SchemaBuilder.this, value);
    }

    public ByteSequence normalizeAssertionValue(ByteSequence value) {
      return implementation.normalizeAssertionValue(SchemaBuilder.this, value);
    }

    public int compareValues(ByteSequence attributeValue,
                             ByteSequence assertionValue) {
      return implementation.compareValues(SchemaBuilder.this, attributeValue,
          assertionValue);
    }
  }

  private final class RealSubstringMatchingRule extends SubstringMatchingRule
  {
    private Syntax syntax;
    private final SubstringMatchingRuleImplementation implementation;

    private RealSubstringMatchingRule(String oid, SortedSet<String> names,
                             String description, boolean obsolete,
                             String syntax,
                             Map<String, List<String>> extraProperties,
                             SubstringMatchingRuleImplementation implementation,
                             String definition) {
      super(oid, names, description, obsolete, syntax, extraProperties,
          definition);
      this.implementation = implementation;
    }

    public Syntax getSyntax()
    {
      return syntax;
    }

    private void resolveReferences() throws SchemaException
    {
      // Make sure the specifiec syntax is defined in this schema.
      syntax = SchemaBuilder.this.getSyntax(syntaxOID);
      if(syntax == null)
      {
        Message message = ERR_ATTR_SYNTAX_MR_UNKNOWN_SYNTAX.get(getNameOrOID(),
            syntaxOID);
        throw new SchemaException(message);
      }
    }

    /**
     * {@inheritDoc}
     */
    public ConditionResult valuesMatch(ByteSequence attributeValue,
                                       ByteSequence assertionValue) {
      return implementation.valuesMatch(SchemaBuilder.this, attributeValue,
          assertionValue);
    }

    public ByteSequence normalizeAttributeValue(ByteSequence value) {
      return implementation.normalizeAttributeValue(SchemaBuilder.this, value);
    }

    public ByteSequence normalizeSubInitialValue(ByteSequence value) {
      return implementation.normalizeSubInitialValue(SchemaBuilder.this, value);
    }

    public ByteSequence normalizeSubAnyValue(ByteSequence value) {
      return implementation.normalizeSubAnyValue(SchemaBuilder.this, value);
    }

    public ByteSequence normalizeSubFinalValue(ByteSequence value) {
      return implementation.normalizeSubFinalValue(SchemaBuilder.this, value);
    }

    public boolean valueMatchesSubstring(ByteSequence attributeValue,
                                         ByteSequence subInitial,
                                         List<ByteSequence> subAnyElements,
                                         ByteSequence subFinal) {
      return implementation.valueMatchesSubstring(SchemaBuilder.this,
          attributeValue, subInitial, subAnyElements, subFinal);
    }
  }

  private final class RealApproximateMatchingRule
      extends ApproximateMatchingRule {
    private Syntax syntax;
    private final ApproximateMatchingRuleImplementation implementation;

    private RealApproximateMatchingRule(String oid, SortedSet<String> names,
                             String description, boolean obsolete,
                             String syntax,
                             Map<String, List<String>> extraProperties,
                           ApproximateMatchingRuleImplementation implementation,
                             String definition) {
      super(oid, names, description, obsolete, syntax, extraProperties,
          definition);
      this.implementation = implementation;
    }

    public Syntax getSyntax()
    {
      return syntax;
    }

    private void resolveReferences() throws SchemaException
    {
      // Make sure the specifiec syntax is defined in this schema.
      syntax = SchemaBuilder.this.getSyntax(syntaxOID);
      if(syntax == null)
      {
        Message message = ERR_ATTR_SYNTAX_MR_UNKNOWN_SYNTAX.get(getNameOrOID(),
            syntaxOID);
        throw new SchemaException(message);
      }
    }

    /**
     * {@inheritDoc}
     */
    public ConditionResult valuesMatch(ByteSequence attributeValue,
                                       ByteSequence assertionValue) {
      return implementation.valuesMatch(SchemaBuilder.this, attributeValue,
          assertionValue);
    }

    public ByteSequence normalizeAttributeValue(ByteSequence value) {
      return implementation.normalizeAttributeValue(SchemaBuilder.this, value);
    }

    public ByteSequence normalizeAssertionValue(ByteSequence value) {
      return implementation.normalizeAssertionValue(SchemaBuilder.this, value);
    }

    public boolean approximatelyMatch(ByteSequence attributeValue,
                                      ByteSequence assertionValue) {
      return implementation.approximatelyMatch(SchemaBuilder.this,
          attributeValue, assertionValue);
    }
  }

  private final class RealSyntax extends Syntax
  {
    private EqualityMatchingRule equalityMatchingRule;
    private OrderingMatchingRule orderingMatchingRule;
    private SubstringMatchingRule substringMatchingRule;
    private ApproximateMatchingRule approximateMatchingRule;

    private final SyntaxImplementation implementation;

    private RealSyntax(String oid, String description,
                       Map<String, List<String>> extraProperties,
                       SyntaxImplementation implementation, String definition)
    {
      super(oid, description, extraProperties, definition);
      this.implementation = implementation;
    }

    public EqualityMatchingRule getEqualityMatchingRule() {
      return equalityMatchingRule;
    }

    public OrderingMatchingRule getOrderingMatchingRule() {
      return orderingMatchingRule;
    }

    public SubstringMatchingRule getSubstringMatchingRule() {
      return substringMatchingRule;
    }

    public ApproximateMatchingRule getApproximateMatchingRule() {
      return approximateMatchingRule;
    }

    private void resolveReferences() throws SchemaException
    {
      // Get references to the default matching rules
      if(implementation.getEqualityMatchingRule() != null)
      {
        MatchingRule rule = SchemaBuilder.this.getMatchingRule(
            implementation.getEqualityMatchingRule());
        if(rule == null || !(rule instanceof EqualityMatchingRule))
        {
          Message message =
              ERR_ATTR_SYNTAX_UNKNOWN_EQUALITY_MATCHING_RULE.get(
                  implementation.getEqualityMatchingRule(),
                  implementation.getName());
          throw new SchemaException(message);
        }
        equalityMatchingRule = (EqualityMatchingRule)rule;
      }

      if(implementation.getOrderingMatchingRule() != null)
      {
        MatchingRule rule = SchemaBuilder.this.getMatchingRule(
            implementation.getOrderingMatchingRule());
        if(rule == null || !(rule instanceof OrderingMatchingRule))
        {
          Message message =
              ERR_ATTR_SYNTAX_UNKNOWN_ORDERING_MATCHING_RULE.get(
                  implementation.getOrderingMatchingRule(),
                  implementation.getName());
          throw new SchemaException(message);
        }
        orderingMatchingRule = (OrderingMatchingRule)rule;
      }

      if(implementation.getSubstringMatchingRule() != null)
      {
        MatchingRule rule = SchemaBuilder.this.getMatchingRule(
            implementation.getSubstringMatchingRule());
        if(rule == null || !(rule instanceof SubstringMatchingRule))
        {
          Message message =
              ERR_ATTR_SYNTAX_UNKNOWN_SUBSTRING_MATCHING_RULE.get(
                  implementation.getSubstringMatchingRule(),
                  implementation.getName());
          throw new SchemaException(message);
        }
        substringMatchingRule = (SubstringMatchingRule)rule;
      }

      if(implementation.getApproximateMatchingRule() != null)
      {
        MatchingRule rule = SchemaBuilder.this.getMatchingRule(
            implementation.getApproximateMatchingRule());
        if(rule == null || !(rule instanceof ApproximateMatchingRule))
        {
          Message message =
              ERR_ATTR_SYNTAX_UNKNOWN_APPROXIMATE_MATCHING_RULE.get(
                  implementation.getApproximateMatchingRule(), 
                  implementation.getName());
          throw new SchemaException(message);
        }
        approximateMatchingRule = (ApproximateMatchingRule)rule;
      }
    }

    public boolean isHumanReadable() {
      return implementation.isHumanReadable();
    }

    public boolean valueIsAcceptable(ByteSequence value,
                                     MessageBuilder invalidReason) {
      return implementation.valueIsAcceptable(null, value, invalidReason);
    }
  }

  private final class SubstitutionSyntax extends Syntax
  {
    private Syntax substitute;
    private final String substituteOID;

    private SubstitutionSyntax(String oid, String description,
                       Map<String, List<String>> extraProperties,
                       String definition, String substitute)
    {
      super(oid, description, extraProperties, definition);
      this.substituteOID = substitute;
    }

    public EqualityMatchingRule getEqualityMatchingRule() {
      return substitute.getEqualityMatchingRule();
    }

    public OrderingMatchingRule getOrderingMatchingRule() {
      return substitute.getOrderingMatchingRule();
    }

    public SubstringMatchingRule getSubstringMatchingRule() {
      return substitute.getSubstringMatchingRule();
    }

    public ApproximateMatchingRule getApproximateMatchingRule() {
      return substitute.getApproximateMatchingRule();
    }

    private void resolveReferences() throws SchemaException
    {
      // Get reference to the substitute syntax
      substitute = SchemaBuilder.this.getSyntax(substituteOID);
      if(substitute == null)
      {
        Message message = WARN_ATTR_SYNTAX_UNKNOWN_SUB_SYNTAX.get(
            oid, substituteOID);
        throw new SchemaException(message);
      }
    }

    public boolean isHumanReadable() {
      return substitute.isHumanReadable();
    }

    public boolean valueIsAcceptable(ByteSequence value,
                                     MessageBuilder invalidReason) {
      return substitute.valueIsAcceptable(value, invalidReason);
    }
  }

  private final class RealDITContentRule extends DITContentRule
  {
    private ObjectClass structuralClass;
    private Set<ObjectClass> auxiliaryClasses = Collections.emptySet();
    private Set<AttributeType> optionalAttributes = Collections.emptySet();
    private Set<AttributeType> prohibitedAttributes = Collections.emptySet();
    private Set<AttributeType> requiredAttributes = Collections.emptySet();

    private RealDITContentRule(String structuralClassOID,
                               SortedSet<String> names, String description,
                               boolean obsolete, Set<String> auxiliaryClassOIDs,
                               Set<String> optionalAttributeOIDs,
                               Set<String> prohibitedAttributeOIDs,
                               Set<String> requiredAttributeOIDs,
                               Map<String, List<String>> extraProperties,
                               String definition) {
      super(structuralClassOID, names, description, obsolete,
          auxiliaryClassOIDs, optionalAttributeOIDs, prohibitedAttributeOIDs,
          requiredAttributeOIDs, extraProperties, definition);
    }

    /**
     * {@inheritDoc}
     */
    public ObjectClass getStructuralClass()
    {
      return structuralClass;
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<ObjectClass> getAuxiliaryClasses()
    {
      return auxiliaryClasses;
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<AttributeType> getRequiredAttributes()
    {
      return requiredAttributes;
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<AttributeType> getOptionalAttributes()
    {
      return optionalAttributes;
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<AttributeType> getProhibitedAttributes()
    {
      return prohibitedAttributes;
    }

    private void resolveReferences() throws SchemaException
    {
      // Get the objectclass with the specified OID.  If it does not exist or is
    // not structural, then fail.
      if(structuralClassOID != null)
      {
        structuralClass = SchemaBuilder.this.getObjectClass(structuralClassOID);
        if(structuralClass == null)
        {
          Message message = ERR_ATTR_SYNTAX_DCR_UNKNOWN_STRUCTURAL_CLASS.get(
              definition, structuralClassOID);
          throw new SchemaException(message);
        }
        if(structuralClass.getObjectClassType() != ObjectClassType.STRUCTURAL)
        {
          Message message = ERR_ATTR_SYNTAX_DCR_STRUCTURAL_CLASS_NOT_STRUCTURAL.
              get(definition, structuralClass.getOID(),
                  structuralClass.getNameOrOID(),
                  structuralClass.getObjectClassType().toString());
          throw new SchemaException(message);
        }
      }

      if(!auxiliaryClassOIDs.isEmpty())
      {
        auxiliaryClasses =
            new HashSet<ObjectClass>(auxiliaryClassOIDs.size());
        ObjectClass objectClass;
        for(String oid : auxiliaryClassOIDs)
        {
          objectClass = SchemaBuilder.this.getObjectClass(oid);
          if(objectClass == null)
          {
            // This isn't good because it is an unknown auxiliary class.
            Message message = ERR_ATTR_SYNTAX_DCR_UNKNOWN_AUXILIARY_CLASS.get(
                definition, oid);
            throw new SchemaException(message);
          }
          if(objectClass.getObjectClassType() != ObjectClassType.AUXILIARY)
          {
            // This isn't good because it isn't an auxiliary class.
            Message message = ERR_ATTR_SYNTAX_DCR_AUXILIARY_CLASS_NOT_AUXILIARY.
                get(definition, structuralClass.getOID(),
                    structuralClass.getObjectClassType().toString());
            throw new SchemaException(message);
          }
          auxiliaryClasses.add(objectClass);
        }
      }

      if(!requiredAttributeOIDs.isEmpty())
      {
        requiredAttributes =
            new HashSet<AttributeType>(requiredAttributeOIDs.size());
        AttributeType attributeType;
        for(String oid : requiredAttributeOIDs)
        {
          attributeType = SchemaBuilder.this.getAttributeType(oid);
          if(attributeType == null)
          {
            // This isn't good because it means that the DIT content rule
            // requires an attribute type that we don't know anything about.
            Message message = ERR_ATTR_SYNTAX_DCR_UNKNOWN_REQUIRED_ATTR.get(
                    definition, oid);
            throw new SchemaException(message);
          }
          requiredAttributes.add(attributeType);
        }
      }

      if(!optionalAttributeOIDs.isEmpty())
      {
        optionalAttributes =
            new HashSet<AttributeType>(optionalAttributeOIDs.size());
        AttributeType attributeType;
        for(String oid : optionalAttributeOIDs)
        {
          attributeType = SchemaBuilder.this.getAttributeType(oid);
          if(attributeType == null)
          {
            // This isn't good because it means that the DIT content rule
            // requires an attribute type that we don't know anything about.
            Message message = ERR_ATTR_SYNTAX_DCR_UNKNOWN_OPTIONAL_ATTR.get(
                    definition, oid);
            throw new SchemaException(message);
          }
           optionalAttributes.add(attributeType);
        }
      }

      if(!prohibitedAttributeOIDs.isEmpty())
      {
        prohibitedAttributes =
            new HashSet<AttributeType>(prohibitedAttributeOIDs.size());
        AttributeType attributeType;
        for(String oid : prohibitedAttributeOIDs)
        {
          attributeType = SchemaBuilder.this.getAttributeType(oid);
          if(attributeType == null)
          {
            // This isn't good because it means that the DIT content rule
            // requires an attribute type that we don't know anything about.
            Message message = ERR_ATTR_SYNTAX_DCR_UNKNOWN_PROHIBITED_ATTR.get(
                    definition, oid);
            throw new SchemaException(message);
          }
           prohibitedAttributes.add(attributeType);
        }
      }

      // Make sure that none of the prohibited attributes is required by the
      // structural or any of the auxiliary classes.
      for (AttributeType t : prohibitedAttributes)
      {
        if (structuralClass.isRequired(t))
        {
          Message message = ERR_ATTR_SYNTAX_DCR_PROHIBITED_REQUIRED_BY_STRUCTURAL.
              get(definition, t.getNameOrOID(), structuralClass.getNameOrOID());
          throw new SchemaException(message);
        }

        for (ObjectClass oc : auxiliaryClasses)
        {
          if (oc.isRequired(t))
          {
            Message message =
                ERR_ATTR_SYNTAX_DCR_PROHIBITED_REQUIRED_BY_AUXILIARY.
                    get(definition, t.getNameOrOID(), oc.getNameOrOID());
            throw new SchemaException(message);
          }
        }
      }
    }
  }

  private final class RealDITStructureRule extends DITStructureRule
  {
    private NameForm nameForm;
    private Set<DITStructureRule> superiorRules = Collections.emptySet();

    private RealDITStructureRule(Integer ruleID, SortedSet<String> names,
                                 String description, boolean obsolete,
                                 String nameFormOID,
                                 Set<Integer> superiorRuleIDs,
                                 Map<String, List<String>> extraProperties,
                                 String definition) {
      super(ruleID, names, description, obsolete, nameFormOID, superiorRuleIDs,
          extraProperties, definition);
    }

    /**
     * {@inheritDoc}
     */
    public NameForm getNameForm()
    {
      return nameForm;
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<DITStructureRule> getSuperiorRules()
    {
      return superiorRules;
    }

    private void resolveReferences() throws SchemaException
    {
      nameForm = SchemaBuilder.this.getNameForm(nameFormOID);
      if(nameForm == null)
      {
        Message message = ERR_ATTR_SYNTAX_DSR_UNKNOWN_NAME_FORM.get(
              definition, nameFormOID);
        throw new SchemaException(message);
      }

      if(!superiorRuleIDs.isEmpty())
      {
        superiorRules = new HashSet<DITStructureRule>(superiorRuleIDs.size());
        DITStructureRule rule;
        for(Integer id : superiorRuleIDs)
        {
          rule = SchemaBuilder.this.getDITStructureRule(id);
          if(rule == null)
          {
            Message message = ERR_ATTR_SYNTAX_DSR_UNKNOWN_RULE_ID.
                get(definition, id);
            throw new SchemaException(message);
          }
          superiorRules.add(rule);
        }
      }
    }
  }

  private final class RealNameForm extends NameForm
  {
    private ObjectClass structuralClass;
    private Set<AttributeType> optionalAttributes = Collections.emptySet();
    private Set<AttributeType> requiredAttributes = Collections.emptySet();

    private RealNameForm(String oid, SortedSet<String> names,
                         String description, boolean obsolete,
                         String structuralClassOID,
                         Set<String> requiredAttributeOIDs,
                         Set<String> optionalAttributeOIDs,
                         Map<String, List<String>> extraProperties,
                         String definition) {
      super(oid, names, description, obsolete, structuralClassOID,
          requiredAttributeOIDs, optionalAttributeOIDs, extraProperties,
          definition);
    }

    /**
     * {@inheritDoc}
     */
    public ObjectClass getStructuralClass()
    {
      return structuralClass;
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<AttributeType> getRequiredAttributes()
    {
      return requiredAttributes;
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<AttributeType> getOptionalAttributes()
    {
      return optionalAttributes;
    }

    private void resolveReferences() throws SchemaException
    {
      structuralClass = SchemaBuilder.this.getObjectClass(structuralClassOID);
      if(structuralClass == null)
      {
        Message message =
            ERR_ATTR_SYNTAX_NAME_FORM_UNKNOWN_STRUCTURAL_CLASS.
                get(oid, structuralClassOID);
        throw new SchemaException(message);
      }
      if(structuralClass.getObjectClassType() != ObjectClassType.STRUCTURAL)
      {
        // This is bad because the associated structural class type is not
        // structural.
        Message message =
            ERR_ATTR_SYNTAX_NAME_FORM_STRUCTURAL_CLASS_NOT_STRUCTURAL.
                get(oid, structuralClass.getOID(),
                    structuralClass.getNameOrOID(),
                    String.valueOf(structuralClass.getObjectClassType()));
        throw new SchemaException(message);
      }

      requiredAttributes =
          new HashSet<AttributeType>(requiredAttributeOIDs.size());
      AttributeType attributeType;
      for(String oid : requiredAttributeOIDs)
      {
        attributeType = SchemaBuilder.this.getAttributeType(oid);
        if(attributeType == null)
        {
          // This isn't good because it means that the name form requires
          // an attribute type that we don't know anything about.
          Message message =
              ERR_ATTR_SYNTAX_NAME_FORM_UNKNOWN_REQUIRED_ATTR.
                  get(this.oid, oid);
          throw new SchemaException(message);
        }
        requiredAttributes.add(attributeType);
      }

      if(!optionalAttributeOIDs.isEmpty())
      {
        optionalAttributes =
            new HashSet<AttributeType>(optionalAttributeOIDs.size());
        for(String oid : optionalAttributeOIDs)
        {
          attributeType = SchemaBuilder.this.getAttributeType(oid);
          if(attributeType == null)
          {
            // This isn't good because it means that the name form requires
            // an attribute type that we don't know anything about.
            Message message =
                ERR_ATTR_SYNTAX_NAME_FORM_UNKNOWN_OPTIONAL_ATTR.
                    get(this.oid, oid);
            throw new SchemaException(message);
          }
          optionalAttributes.add(attributeType);
        }
      }
    }
  }

  private final class RealObjectClass extends ObjectClass
  {
    private Set<ObjectClass> superiorClasses = Collections.emptySet();
    private Set<AttributeType> declaredRequiredAttributes =
        Collections.emptySet();
    private Set<AttributeType> requiredAttributes = Collections.emptySet();
    private Set<AttributeType> declaredOptionalAttributes =
        Collections.emptySet();
    private Set<AttributeType> optionalAttributes = Collections.emptySet();

    private RealObjectClass(String oid, SortedSet<String> names,
                            String description, boolean obsolete,
                            Set<String> superiorClassOIDs,
                            Set<String> requiredAttributeOIDs,
                            Set<String> optionalAttributeOIDs,
                            ObjectClassType objectClassType,
                            Map<String, List<String>> extraProperties,
                            String definition) {
      super(oid, names, description, obsolete, superiorClassOIDs,
          requiredAttributeOIDs, optionalAttributeOIDs, objectClassType,
          extraProperties, definition);
    }

    public boolean isDescendantOf(ObjectClass objectClass) {
      for(ObjectClass sup : superiorClasses)
      {
        if(sup.equals(objectClass) || sup.isDescendantOf(objectClass))
        {
          return true;
        }
      }
      return false;
    }

    public Iterable<ObjectClass> getSuperiorClasses() {
      return superiorClasses;
    }

    public Iterable<AttributeType> getDeclaredRequiredAttributes() {
      return declaredRequiredAttributes;
    }

    public Iterable<AttributeType> getRequiredAttributes() {
      return requiredAttributes;
    }

    public Iterable<AttributeType> getOptionalAttributes() {
      return optionalAttributes;
    }

    public Iterable<AttributeType> getDeclaredOptionalAttributes() {
      return declaredOptionalAttributes;
    }

    public boolean isRequired(AttributeType attributeType) {
      return requiredAttributes.contains(attributeType);
    }

    public boolean isOptional(AttributeType attributeType) {
      return optionalAttributes.contains(attributeType);
    }

    public boolean isRequiredOrOptional(AttributeType attributeType) {
      return isRequired(attributeType) || isOptional(attributeType);
    }

    private void resolveReferences() throws SchemaException
    {
      // Init a flag to check to inheritance from top (only needed for
      // structural object classes) per RFC 4512
      boolean derivesTop =
          objectClassType != ObjectClassType.STRUCTURAL;

      if(!superiorClassOIDs.isEmpty())
      {
        superiorClasses = new HashSet<ObjectClass>(superiorClassOIDs.size());
        ObjectClass superiorClass;
        for(String superClassOid : superiorClassOIDs)
        {
          superiorClass = getObjectClass(superClassOid);
          if(superiorClass == null)
          {
            Message message =
                WARN_ATTR_SYNTAX_OBJECTCLASS_UNKNOWN_SUPERIOR_CLASS.
                    get(oid, superClassOid);
            throw new SchemaException(message);
          }

          // Make sure that the inheritance configuration is acceptable.
          ObjectClassType superiorType = superiorClass.getObjectClassType();
          switch (objectClassType)
          {
            case ABSTRACT:
              // Abstract classes may only inherit from other abstract classes.
              if (superiorType != ObjectClassType.ABSTRACT)
              {
                Message message =
                    WARN_ATTR_SYNTAX_OBJECTCLASS_INVALID_SUPERIOR_TYPE.
                        get(oid,
                            objectClassType.toString(),
                            superiorType.toString(),
                            superiorClass.getNameOrOID());
                throw new SchemaException(message);
              }
              break;

            case AUXILIARY:
              // Auxiliary classes may only inherit from abstract classes or
              // other auxiliary classes.
              if ((superiorType != ObjectClassType.ABSTRACT) &&
                  (superiorType != ObjectClassType.AUXILIARY))
              {
                Message message =
                    WARN_ATTR_SYNTAX_OBJECTCLASS_INVALID_SUPERIOR_TYPE.
                        get(oid,
                            objectClassType.toString(),
                            superiorType.toString(),
                            superiorClass.getNameOrOID());
                throw new SchemaException(message);
              }
              break;

            case STRUCTURAL:
              // Structural classes may only inherit from abstract classes or
              // other structural classes.
              if ((superiorType != ObjectClassType.ABSTRACT) &&
                  (superiorType != ObjectClassType.STRUCTURAL))
              {
                Message message =
                    WARN_ATTR_SYNTAX_OBJECTCLASS_INVALID_SUPERIOR_TYPE.
                        get(oid,
                            objectClassType.toString(),
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

          // Inherit all required attributes from superior class.
          Iterator<AttributeType> i =
              superiorClass.getRequiredAttributes().iterator();
          if(i.hasNext() && requiredAttributes == Collections.EMPTY_SET)
          {
            requiredAttributes = new HashSet<AttributeType>();
          }
          while(i.hasNext())
          {
            requiredAttributes.add(i.next());
          }

          // Inherit all optional attributes from superior class.
          i = superiorClass.getRequiredAttributes().iterator();
          if(i.hasNext() && requiredAttributes == Collections.EMPTY_SET)
          {
            requiredAttributes = new HashSet<AttributeType>();
          }
          while(i.hasNext())
          {
            requiredAttributes.add(i.next());
          }

          superiorClasses.add(superiorClass);
        }
      }

      // Structural classes must have the "top" objectclass somewhere
      // in the superior chain.
      if (!derivesTop)
      {
        Message message =
            WARN_ATTR_SYNTAX_OBJECTCLASS_STRUCTURAL_SUPERIOR_NOT_TOP.
                get(oid);
        throw new SchemaException(message);
      }

      if(!requiredAttributeOIDs.isEmpty())
      {
        declaredRequiredAttributes =
            new HashSet<AttributeType>(requiredAttributeOIDs.size());
        AttributeType attributeType;
        for(String requiredAttribute : requiredAttributeOIDs)
        {
          if((attributeType = getAttributeType(requiredAttribute)) == null)
          {
            // This isn't good because it means that the objectclass
            // requires an attribute type that we don't know anything about.
            Message message =
                WARN_ATTR_SYNTAX_OBJECTCLASS_UNKNOWN_REQUIRED_ATTR.
                    get(oid, requiredAttribute);
            throw new SchemaException(message);
          }
          declaredRequiredAttributes.add(attributeType);
        }
        if(requiredAttributes == Collections.EMPTY_SET)
        {
          requiredAttributes = declaredRequiredAttributes;
        }
        else
        {
          requiredAttributes.addAll(declaredRequiredAttributes);
        }
      }

      if(!optionalAttributeOIDs.isEmpty())
      {
        declaredOptionalAttributes =
            new HashSet<AttributeType>(requiredAttributeOIDs.size());
        AttributeType attributeType;
        for(String optionalAttribute : optionalAttributeOIDs)
        {
          if((attributeType = getAttributeType(optionalAttribute)) == null)
          {
            // This isn't good because it means that the objectclass
            // requires an attribute type that we don't know anything about.
            Message message =
                WARN_ATTR_SYNTAX_OBJECTCLASS_UNKNOWN_OPTIONAL_ATTR.
                    get(oid, optionalAttribute);
            throw new SchemaException(message);
          }
          declaredOptionalAttributes.add(attributeType);
        }
        if(optionalAttributes == Collections.EMPTY_SET)
        {
          optionalAttributes = declaredOptionalAttributes;
        }
        else
        {
          optionalAttributes.addAll(declaredOptionalAttributes);
        }
      }
    }
  }

  private final class ExtensibleObjectClass extends ObjectClass
      implements Iterable<AttributeType>
  {

    private ExtensibleObjectClass(String oid, SortedSet<String> names,
                                  String description, boolean obsolete,
                                  Set<String> superiorClassOIDs,
                                  Set<String> requiredAttributeOIDs,
                                  Set<String> optionalAttributeOIDs,
                                  ObjectClassType objectClassType,
                                  Map<String, List<String>> extraProperties,
                                  String definition) {
      super(oid, names, description, obsolete, superiorClassOIDs,
          requiredAttributeOIDs, optionalAttributeOIDs, objectClassType,
          extraProperties, definition);
    }

    public boolean isDescendantOf(ObjectClass objectClass) {
      return objectClass.getOID().equals("2.5.6.0");
    }

    public Iterator<AttributeType> iterator() {
      return new Iterator<AttributeType>()
      {
        Iterator<AttributeType> i =
            SchemaBuilder.this.getAttributeTypes().iterator();
        AttributeType next = findNext();

        public boolean hasNext() {
          return next != null;
        }

        public AttributeType next() {
          AttributeType t = next;
          next = findNext();
          return t;
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }

        private AttributeType findNext()
        {
          AttributeType t;
          while(i.hasNext())
          {
            if((t = i.next()).getUsage() == AttributeUsage.USER_APPLICATIONS)
            {
              return t;
            }
          }
          return null;
        }
      };
    }

    public Iterable<ObjectClass> getSuperiorClasses() {
      return Collections.singleton(
          SchemaBuilder.this.getObjectClass("2.5.6.0"));
    }

    public Iterable<AttributeType> getDeclaredRequiredAttributes() {
      return Collections.emptySet();
    }

    public Iterable<AttributeType> getRequiredAttributes() {
      return Collections.emptySet();
    }

    public Iterable<AttributeType> getOptionalAttributes() {
      return this;
    }

    public Iterable<AttributeType> getDeclaredOptionalAttributes() {
      return this;
    }

    public boolean isRequired(AttributeType attributeType) {
      return false;
    }

    public boolean isOptional(AttributeType attributeType) {
      AttributeType t = getAttributeType(attributeType.getOID());
      return t != null && t.attributeUsage == AttributeUsage.USER_APPLICATIONS;
    }

    public boolean isRequiredOrOptional(AttributeType attributeType) {
      return isOptional(attributeType);
    }

    private void resolveReferences() throws SchemaException
    {

    }
  }

  private Map<String, RealSyntax> syntaxes;
  private Map<String, RealEqualityMatchingRule> matchingRules;
  private Map<String, AttributeType> attributeTypes;
  private Map<String, ObjectClass> objectClasses;
  private Map<String, MatchingRuleUse> matchingRuleUses;
  private Map<String, NameForm> nameForms;
  private Map<String, DITContentRule> contentRules;
  private Map<String, DITStructureRule> structureRules;

  public void addSyntax(String oid, String description,
                        Map<String, List<String>> extraProperties,
                        SyntaxImplementation implementation)
  {
    Validator.ensureNotNull(implementation);
    RealSyntax syntax = new RealSyntax(oid, description, extraProperties,
        implementation, null);
  }

  public void addSyntax(String definition)
      throws DecodeException, SchemaException
  {
    Validator.ensureNotNull(definition);
    SubstringReader reader = new SubstringReader(definition);

    // We'll do this a character at a time.  First, skip over any leading
    // whitespace.
    reader.skipWhitespaces();

    if (reader.remaining() <= 0)
    {
      // This means that the value was empty or contained only whitespace.  That
      // is illegal.
      Message message = ERR_ATTR_SYNTAX_ATTRSYNTAX_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }


    // The next character must be an open parenthesis.  If it is not, then that
    // is an error.
    char c = reader.read();
    if (c != '(')
    {
      Message message = ERR_ATTR_SYNTAX_ATTRSYNTAX_EXPECTED_OPEN_PARENTHESIS.
          get(definition, (reader.pos()-1), String.valueOf(c));
      throw new DecodeException(message);
    }


    // Skip over any spaces immediately following the opening parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    String oid = SchemaUtils.readNumericOID(reader);

    String description = "".intern();
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
      else if (tokenName.equalsIgnoreCase("desc"))
      {
        // This specifies the description for the syntax.  It is an
        // arbitrary string of characters enclosed in single quotes.
        description = SchemaUtils.readQuotedString(reader);
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

    // See if we need to override the implementation of the syntax
    for(Map.Entry<String, List<String>> property : extraProperties.entrySet())
    {
      if(property.getKey().equalsIgnoreCase("x-subst"))
      {
        Iterator<String> values = property.getValue().iterator();
        if(values.hasNext())
        {
          String value = values.next();
          SubstitutionSyntax syntax =
              new SubstitutionSyntax(oid, description, extraProperties,
                  definition, value);
          break;
        }
      }
      else if(property.getKey().equalsIgnoreCase("x-pattern"))
      {
        Iterator<String> values = property.getValue().iterator();
        if(values.hasNext())
        {
          String value = values.next();
          try
          {
            Pattern pattern = Pattern.compile(value);
            RealSyntax syntax =
                new RealSyntax(oid, description, extraProperties,
                    new RegexSyntax(pattern), definition);
          }
          catch(Exception e)
          {
            Message message =
                WARN_ATTR_SYNTAX_LDAPSYNTAX_REGEX_INVALID_PATTERN.get
                    (oid, value);
            throw new SchemaException(message);
          }
          break;
        }
      }
    }

    // Try to find an implementation in the core schema
    Syntax syntax = CoreSchema.INSTANCE.getSyntax(oid);
    if(syntax != null)
    {
      if(syntax instanceof RealSyntax)
      {
        // The core schema syntax MUST have a concrete implementation
        // (ie. not a substitute syntax)
        RealSyntax coreSyntax = (RealSyntax)syntax;
        RealSyntax newSyntax =
            new RealSyntax(oid, description, extraProperties,
                coreSyntax.implementation, definition);
      }
    }

    // We can't find an implmentation for the syntax. Should we use default?
    Message message = WARN_ATTR_SYNTAX_NOT_IMPLEMENTED.get(oid);
    throw new SchemaException(message);
  }

  public Syntax getSyntax(String numericoid)
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

  public void addMatchingRule(String oid,
                              SortedSet<String> names,
                              String description,
                              boolean obsolete,
                              String syntax,
                              Map<String, List<String>> extraProperties,
                              EqualityMatchingRuleImplementation implementation)
  {
    Validator.ensureNotNull(implementation);
    new RealEqualityMatchingRule(oid, names, description, obsolete, syntax,
        extraProperties, implementation, null);
  }

  public void addMatchingRule(String oid,
                              SortedSet<String> names,
                              String description,
                              boolean obsolete,
                              String syntax,
                              Map<String, List<String>> extraProperties,
                              OrderingMatchingRuleImplementation implementation)
  {
    Validator.ensureNotNull(implementation);
    new RealOrderingMatchingRule(oid, names, description, obsolete, syntax,
        extraProperties, implementation, null);
  }

  public void addMatchingRule(String oid,
                              SortedSet<String> names,
                              String description,
                              boolean obsolete,
                              String syntax,
                              Map<String, List<String>> extraProperties,
                             SubstringMatchingRuleImplementation implementation)
  {
    Validator.ensureNotNull(implementation);
    new RealSubstringMatchingRule(oid, names, description, obsolete, syntax,
        extraProperties, implementation, null);
  }

  public void addMatchingRule(String oid,
                              SortedSet<String> names,
                              String description,
                              boolean obsolete,
                              String syntax,
                              Map<String, List<String>> extraProperties,
                           ApproximateMatchingRuleImplementation implementation)
  {
    Validator.ensureNotNull(implementation);
    new RealApproximateMatchingRule(oid, names, description, obsolete, syntax,
        extraProperties, implementation, null);
  }

  public void addMatchingRule(String definition)
      throws DecodeException
  {
    Validator.ensureNotNull(definition);
    SubstringReader reader = new SubstringReader(definition);

    // We'll do this a character at a time.  First, skip over any leading
    // whitespace.
    reader.skipWhitespaces();

    if (reader.remaining() <= 0)
    {
      // This means that the value was empty or contained only whitespace.  That
      // is illegal.
      Message message = ERR_ATTR_SYNTAX_MR_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }


    // The next character must be an open parenthesis.  If it is not, then that
    // is an error.
    char c = reader.read();
    if (c != '(')
    {
      Message message = ERR_ATTR_SYNTAX_MR_EXPECTED_OPEN_PARENTHESIS.
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
    String syntax = null;
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
        // This specifies the description for the matching rule.  It is an
        // arbitrary string of characters enclosed in single quotes.
        description = SchemaUtils.readQuotedString(reader);
      }
      else if (tokenName.equalsIgnoreCase("obsolete"))
      {
        // This indicates whether the matching rule should be considered
        // obsolete.  We do not need to do any more parsing for this token.
        isObsolete = true;
      }
      else if (tokenName.equalsIgnoreCase("syntax"))
      {
        syntax = SchemaUtils.readNumericOID(reader);
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

    // Make sure that a syntax was specified.
    if (syntax == null)
    {
      Message message = ERR_ATTR_SYNTAX_MR_NO_SYNTAX.get(definition);
      throw new DecodeException(message);
    }

    // Try finding an implementation in the core schema
    MatchingRule rule = CoreSchema.INSTANCE.getMatchingRule(oid);
    MatchingRule newRule = null;
    if(rule != null)
    {
      if(rule instanceof RealEqualityMatchingRule)
      {
        RealEqualityMatchingRule coreRule = (RealEqualityMatchingRule)rule;
        newRule =
            new RealEqualityMatchingRule(oid, names, description, isObsolete,
                syntax, extraProperties, coreRule.implementation, definition);
      }
      else if(rule instanceof RealOrderingMatchingRule)
      {
        RealOrderingMatchingRule coreRule = (RealOrderingMatchingRule)rule;
        newRule =
            new RealOrderingMatchingRule(oid, names, description, isObsolete,
                syntax, extraProperties, coreRule.implementation, definition);
      }
      else if(rule instanceof RealSubstringMatchingRule)
      {
        RealSubstringMatchingRule coreRule = (RealSubstringMatchingRule)rule;
        newRule =
            new RealSubstringMatchingRule(oid, names, description, isObsolete,
                syntax, extraProperties, coreRule.implementation, definition);
      }
      else if(rule instanceof RealApproximateMatchingRule)
      {
        RealApproximateMatchingRule coreRule =
            (RealApproximateMatchingRule)rule;
        newRule =
            new RealApproximateMatchingRule(oid, names, description, isObsolete,
                syntax, extraProperties, coreRule.implementation, definition);
      }
    }

    // We can't find an implmentation for the matching rule.
    //  Should we use default?
    if(newRule == null)
    {
      Message message = WARN_MATCHING_RULE_NOT_IMPLEMENTED.get(oid);
      throw new SchemaException(message);
    }
  }

  public void addAttributeType(String oid, SortedSet<String> names,
                               String description, boolean obsolete,
                               String superiorType, String equalityMatchingRule,
                               String orderingMatchingRule,
                               String substringMatchingRule,
                               String approximateMatchingRule,
                               String syntax, boolean singleValue,
                               boolean collective, boolean noUserModification,
                               AttributeUsage attributeUsage,
                               Map<String, List<String>> extraProperties)
      throws SchemaException
  {
    RealAttributeType attrType = new RealAttributeType(
        oid, names, description, obsolete, superiorType,
        equalityMatchingRule, orderingMatchingRule, substringMatchingRule,
        approximateMatchingRule, syntax, singleValue, collective,
        noUserModification, attributeUsage, extraProperties, null);
    attributeTypes.put(attrType.getOID(), attrType);
  }

  public void addAttributeType(String definition)
      throws DecodeException
  {
    Validator.ensureNotNull(definition);
    SubstringReader reader = new SubstringReader(definition);

    // We'll do this a character at a time.  First, skip over any leading
    // whitespace.
    reader.skipWhitespaces();

    if (reader.remaining() <= 0)
    {
      // This means that the definition was empty or contained only whitespace.  That
      // is illegal.
      Message message = ERR_ATTR_SYNTAX_ATTRTYPE_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }


    // The next character must be an open parenthesis.  If it is not, then that
    // is an error.
    char c = reader.read();
    if (c != '(')
    {
      Message message = ERR_ATTR_SYNTAX_ATTRTYPE_EXPECTED_OPEN_PARENTHESIS.get(
          definition, (reader.pos()-1), String.valueOf(c));
      throw new DecodeException( message);
    }


    // Skip over any spaces immediately following the opening parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    String oid = SchemaUtils.readNumericOID(reader);

    SortedSet<String> names = SchemaUtils.emptySortedSet();
    String description = "".intern();
    boolean isObsolete = false;
    String superiorType = null;
    String equalityMatchingRule = null;
    String orderingMatchingRule = null;
    String substringMatchingRule = null;
    String approximateMatchingRule = null;
    String syntax = null;
    boolean isSingleValue = false;
    boolean isCollective = false;
    boolean isNoUserModification = false;
    AttributeUsage attributeUsage = AttributeUsage.USER_APPLICATIONS;
    Map<String, List<String>> extraProperties = Collections.emptyMap();

    // At this point, we should have a pretty specific syntax that describes
    // what may come next, but some of the components are optional and it would
    // be pretty easy to put something in the wrong order, so we will be very
    // flexible about what we can accept.  Just look at the next token, figure
    // out what it is and how to treat what comes after it, then repeat until
    // we get to the end of the definition.  But before we start, set default
    // values for everything else we might need to know.
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
        // This specifies the name or OID of the superior attribute type from
        // which this attribute type should inherit its properties.
        superiorType = SchemaUtils.readOID(reader);
      }
      else if (tokenName.equalsIgnoreCase("equality"))
      {
        // This specifies the name or OID of the equality matching rule to use
        // for this attribute type.
        equalityMatchingRule = SchemaUtils.readOID(reader);
      }
      else if (tokenName.equalsIgnoreCase("ordering"))
      {
        // This specifies the name or OID of the ordering matching rule to use
        // for this attribute type.
        orderingMatchingRule = SchemaUtils.readOID(reader);
      }
      else if (tokenName.equalsIgnoreCase("substr"))
      {
        // This specifies the name or OID of the substring matching rule to use
        // for this attribute type.
        substringMatchingRule = SchemaUtils.readOID(reader);
      }
      else if (tokenName.equalsIgnoreCase("syntax"))
      {
        // This specifies the numeric OID of the syntax for this matching rule.
        // It may optionally be immediately followed by an open curly brace, an
        // integer definition, and a close curly brace to suggest the minimum
        // number of characters that should be allowed in values of that type.
        // This implementation will ignore any such length because it does not
        // impose any practical limit on the length of attribute values.
        syntax = substringMatchingRule = SchemaUtils.readNumericOIDLen(reader);
      }
      else if (tokenName.equalsIgnoreCase("single-definition"))
      {
        // This indicates that attributes of this type are allowed to have at
        // most one definition.  We do not need any more parsing for this token.
        isSingleValue = true;
      }
      else if (tokenName.equalsIgnoreCase("collective"))
      {
        // This indicates that attributes of this type are collective (i.e.,
        // have their values generated dynamically in some way).  We do not need
        // any more parsing for this token.
        isCollective = true;
      }
      else if (tokenName.equalsIgnoreCase("no-user-modification"))
      {
        // This indicates that the values of attributes of this type are not to
        // be modified by end users.  We do not need any more parsing for this
        // token.
        isNoUserModification = true;
      }
      else if (tokenName.equalsIgnoreCase("usage"))
      {
        // This specifies the usage string for this attribute type.  It should
        // be followed by one of the strings "userApplications",
        // "directoryOperation", "distributedOperation", or "dSAOperation".
        int length = 0;

        reader.skipWhitespaces();
        reader.mark();

        while(reader.read() != ' ')
        {
          length++;
        }

        reader.reset();
        String usageStr = reader.read(length);
        if (usageStr.equalsIgnoreCase("userapplications"))
        {
          attributeUsage = AttributeUsage.USER_APPLICATIONS;
        }
        else if (usageStr.equalsIgnoreCase("directoryoperation"))
        {
          attributeUsage = AttributeUsage.DIRECTORY_OPERATION;
        }
        else if (usageStr.equalsIgnoreCase("distributedoperation"))
        {
          attributeUsage = AttributeUsage.DISTRIBUTED_OPERATION;
        }
        else if (usageStr.equalsIgnoreCase("dsaoperation"))
        {
          attributeUsage = AttributeUsage.DSA_OPERATION;
        }
        else
        {
          Message message = WARN_ATTR_SYNTAX_ATTRTYPE_INVALID_ATTRIBUTE_USAGE.
              get(String.valueOf(oid), usageStr);
          throw new DecodeException(message);
        }
      }
      else
      {
        // This must be a non-standard property and it must be followed by
        // either a single definition in single quotes or an open parenthesis
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

    List<String> approxRules =
        extraProperties.get(SCHEMA_PROPERTY_APPROX_RULE);
    if ((approxRules != null) && (! approxRules.isEmpty()))
    {
      approximateMatchingRule = approxRules.get(0);
    }

    RealAttributeType attrType = new RealAttributeType(
        oid, names, description, isObsolete, superiorType,
        equalityMatchingRule, orderingMatchingRule, substringMatchingRule,
        approximateMatchingRule, syntax, isSingleValue, isCollective,
        isNoUserModification, attributeUsage, extraProperties, definition);

    attributeTypes.put(attrType.getOID(), attrType);
  }

  public void addDITContentRule(String structuralClass,
                                SortedSet<String> names,
                                String description,
                                boolean obsolete,
                                Set<String> auxiliaryClasses,
                                Set<String> optionalAttributes,
                                Set<String> prohibitedAttributes,
                                Set<String> requiredAttributes,
                                Map<String, List<String>> extraProperties)
  {
    RealDITContentRule rule = new RealDITContentRule(structuralClass, names,
        description, obsolete, auxiliaryClasses, optionalAttributes,
        prohibitedAttributes, requiredAttributes, extraProperties, null);
  }

  public void addDITStructureRule(Integer ruleID,
                             SortedSet<String> names,
                             String description,
                             boolean obsolete,
                             String nameForm,
                             Set<Integer> superiorRules,
                             Map<String, List<String>> extraProperties)
  {
    RealDITStructureRule rule = new RealDITStructureRule(ruleID, names,
        description, obsolete, nameForm, superiorRules, extraProperties,
        null);
  }

  public void addNameForm(String oid,
                          SortedSet<String> names,
                          String description,
                          boolean obsolete,
                          String structuralClass,
                          Set<String> requiredAttributes,
                          Set<String> optionalAttributes,
                          Map<String, List<String>> extraProperties)
  {
    RealNameForm nameForm = new RealNameForm(oid, names, description, obsolete,
        structuralClass, requiredAttributes, optionalAttributes,
        extraProperties, null);
  }

  public void addDITContentRule(String definition) throws DecodeException
  {
    Validator.ensureNotNull(definition);
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

    RealDITContentRule rule = new RealDITContentRule(structuralClass, names,
        description, isObsolete, auxiliaryClasses, optionalAttributes,
        prohibitedAttributes, requiredAttributes, extraProperties,
        definition);
  }

  public void addDITStructureRule(String definition) throws DecodeException
  {
    Validator.ensureNotNull(definition);
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

    RealDITStructureRule rule = new RealDITStructureRule(ruleID, names,
        description, isObsolete, nameForm, superiorRules, extraProperties,
        definition);
  }

  public void addNameForm(String definition) throws DecodeException
  {
    Validator.ensureNotNull(definition);
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

    SortedSet<String> names = SchemaUtils.emptySortedSet();
    String description = "".intern();
    boolean isObsolete = false;
    String structuralClass = null;
    Set<String> optionalAttributes = Collections.emptySet();
    Set<String> requiredAttributes = null;
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

    if (requiredAttributes == null || requiredAttributes.size() == 0)
    {
      Message message =
          ERR_ATTR_SYNTAX_NAME_FORM_NO_REQUIRED_ATTR.get(definition);
      throw new DecodeException(message);
    }

    RealNameForm nameForm = new RealNameForm(oid, names, description,
        isObsolete, structuralClass, requiredAttributes, optionalAttributes,
        extraProperties, definition);
  }

  public void addObjectClass(String oid,
                             SortedSet<String> names,
                             String description,
                             boolean obsolete,
                             Set<String> superiorClassOIDs,
                             Set<String> requiredAttributeOIDs,
                             Set<String> optionalAttributeOIDs,
                             ObjectClassType objectClassType,
                             Map<String, List<String>> extraProperties)
  {
    ObjectClass c;
    if(oid.equals(OID_EXTENSIBLE_OBJECT))
    {
      c = new ExtensibleObjectClass(oid, names, description, obsolete,
          superiorClassOIDs, requiredAttributeOIDs, optionalAttributeOIDs,
          objectClassType, extraProperties, null);
    }
    else
    {
      c = new RealObjectClass(oid, names, description, obsolete,
          superiorClassOIDs, requiredAttributeOIDs, optionalAttributeOIDs,
          objectClassType, extraProperties, null);
    }
  }

  public void addObjectClass(String definition) throws DecodeException
  {
    Validator.ensureNotNull(definition);
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

    ObjectClass objectClass;
    if(oid.equals(OID_EXTENSIBLE_OBJECT))
    {
      objectClass = new ExtensibleObjectClass(oid, names, description,
          isObsolete, superiorClasses, requiredAttributes, optionalAttributes,
          objectClassType, extraProperties, definition);
    }
    else
    {
      objectClass = new RealObjectClass(oid, names, description, isObsolete,
          superiorClasses, requiredAttributes, optionalAttributes,
          objectClassType, extraProperties, definition);
    }
  }
}
