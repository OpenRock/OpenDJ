package org.opends.schema;

import org.opends.messages.Message;
import org.opends.messages.MessageBuilder;
import static org.opends.messages.SchemaMessages.*;
import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_OBJECTCLASS_UNKNOWN_REQUIRED_ATTR;
import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_OBJECTCLASS_UNKNOWN_OPTIONAL_ATTR;
import org.opends.schema.matchingrules.EqualityMatchingRuleImplementation;
import org.opends.schema.matchingrules.OrderingMatchingRuleImplementation;
import org.opends.schema.matchingrules.SubstringMatchingRuleImplementation;
import org.opends.schema.matchingrules.ApproximateMatchingRuleImplementation;
import org.opends.schema.syntaxes.SyntaxImplementation;
import org.opends.types.ConditionResult;
import org.opends.server.types.ByteSequence;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 6, 2009
 * Time: 4:32:59 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Schema
{
  public static final Schema DEFAULT_SCHEMA =
      SchemaUtils.generateDefaultSchema();

  protected final Map<String, Syntax> numericOID2Syntaxes;
  protected final Map<String, MatchingRule> numericOID2MatchingRules;
  protected final Map<String, MatchingRuleUse> numericOID2MatchingRuleUses;
  protected final Map<String, AttributeType> numericOID2AttributeTypes;
  protected final Map<String, ObjectClass> numericOID2ObjectClasses;
  protected final Map<String, NameForm> numericOID2NameForms;
  protected final Map<String, DITContentRule> numericOID2ContentRules;
  protected final Map<Integer, DITStructureRule> id2StructureRules;


  protected final Map<String, List<MatchingRule>> name2MatchingRules;
  protected final Map<String, List<MatchingRuleUse>> name2MatchingRuleUses;
  protected final Map<String, List<AttributeType>> name2AttributeTypes;
  protected final Map<String, List<ObjectClass>> name2ObjectClasses;
  protected final Map<String, List<NameForm>> name2NameForms;
  protected final Map<String, List<DITContentRule>> name2ContentRules;
  protected final Map<String, List<DITStructureRule>> name2StructureRules;

  protected final Map<String, List<NameForm>> objectClass2NameForms;
  protected final Map<String, List<DITStructureRule>> nameForm2StructureRules;


  protected final class RealAttributeType extends AttributeType
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

    protected RealAttributeType(String oid, SortedSet<String> names,
                                String description, boolean obsolete,
                                String superiorType,
                                String equalityMatchingRule,
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

    protected void validate() throws SchemaException
    {
      if(superiorTypeOID != null)
      {
        superiorType = Schema.this.getAttributeType(superiorTypeOID);
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
        syntax = Schema.this.getSyntax(syntaxOID);
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
            Schema.this.getMatchingRule(equalityMatchingRuleOID);
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
            Schema.this.getMatchingRule(orderingMatchingRuleOID);
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
            Schema.this.getMatchingRule(substringMatchingRuleOID);
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
            Schema.this.getMatchingRule(approximateMatchingRuleOID);
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

    protected RealAttributeType duplicate() {
      return new RealAttributeType(oid, names, description, isObsolete,
          superiorTypeOID, equalityMatchingRuleOID, orderingMatchingRuleOID,
          substringMatchingRuleOID, approximateMatchingRuleOID, syntaxOID,
          isSingleValue, isCollective, isNoUserModification, attributeUsage,
          extraProperties, definition);
    }
  }

  protected final class RealEqualityMatchingRule extends EqualityMatchingRule
  {
    private Syntax syntax;
    protected final EqualityMatchingRuleImplementation implementation;

    protected RealEqualityMatchingRule(String oid, SortedSet<String> names,
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

    protected void validate() throws SchemaException
    {
      // Make sure the specifiec syntax is defined in this schema.
      syntax = Schema.this.getSyntax(syntaxOID);
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
      return implementation.valuesMatch(Schema.this, attributeValue,
          assertionValue);
    }

    public ByteSequence normalizeAttributeValue(ByteSequence value) {
      return implementation.normalizeAttributeValue(Schema.this, value);
    }

    public ByteSequence normalizeAssertionValue(ByteSequence value) {
      return implementation.normalizeAssertionValue(Schema.this, value);
    }

    public boolean areEqual(ByteSequence attributeValue,
                            ByteSequence assertionValue) {
      return implementation.areEqual(Schema.this, attributeValue,
          assertionValue);
    }

    protected RealEqualityMatchingRule duplicate() {
      return new RealEqualityMatchingRule(oid, names, description, isObsolete,
          syntaxOID, extraProperties, implementation, definition);
    }
  }

  protected final class RealOrderingMatchingRule extends OrderingMatchingRule
  {
    private Syntax syntax;
    protected final OrderingMatchingRuleImplementation implementation;

    protected RealOrderingMatchingRule(String oid, SortedSet<String> names,
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

    protected void validate() throws SchemaException
    {
      // Make sure the specifiec syntax is defined in this schema.
      syntax = Schema.this.getSyntax(syntaxOID);
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
      return implementation.valuesMatch(Schema.this, attributeValue,
          assertionValue);
    }

    public ByteSequence normalizeAttributeValue(ByteSequence value) {
      return implementation.normalizeAttributeValue(Schema.this, value);
    }

    public ByteSequence normalizeAssertionValue(ByteSequence value) {
      return implementation.normalizeAssertionValue(Schema.this, value);
    }

    public int compareValues(ByteSequence attributeValue,
                             ByteSequence assertionValue) {
      return implementation.compareValues(Schema.this, attributeValue,
          assertionValue);
    }

    protected RealOrderingMatchingRule duplicate() {
      return new RealOrderingMatchingRule(oid, names, description, isObsolete,
          syntaxOID, extraProperties, implementation, definition);
    }
  }

  protected final class RealSubstringMatchingRule extends SubstringMatchingRule
  {
    private Syntax syntax;
    protected final SubstringMatchingRuleImplementation implementation;

    protected RealSubstringMatchingRule(String oid, SortedSet<String> names,
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

    protected void validate() throws SchemaException
    {
      // Make sure the specifiec syntax is defined in this schema.
      syntax = Schema.this.getSyntax(syntaxOID);
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
      return implementation.valuesMatch(Schema.this, attributeValue,
          assertionValue);
    }

    public ByteSequence normalizeAttributeValue(ByteSequence value) {
      return implementation.normalizeAttributeValue(Schema.this, value);
    }

    public ByteSequence normalizeSubInitialValue(ByteSequence value) {
      return implementation.normalizeSubInitialValue(Schema.this, value);
    }

    public ByteSequence normalizeSubAnyValue(ByteSequence value) {
      return implementation.normalizeSubAnyValue(Schema.this, value);
    }

    public ByteSequence normalizeSubFinalValue(ByteSequence value) {
      return implementation.normalizeSubFinalValue(Schema.this, value);
    }

    public boolean valueMatchesSubstring(ByteSequence attributeValue,
                                         ByteSequence subInitial,
                                         List<ByteSequence> subAnyElements,
                                         ByteSequence subFinal) {
      return implementation.valueMatchesSubstring(Schema.this,
          attributeValue, subInitial, subAnyElements, subFinal);
    }

    protected RealSubstringMatchingRule duplicate() {
      return new RealSubstringMatchingRule(oid, names, description, isObsolete,
          syntaxOID, extraProperties, implementation, definition);
    }
  }

  protected final class RealApproximateMatchingRule
      extends ApproximateMatchingRule {
    private Syntax syntax;
    protected final ApproximateMatchingRuleImplementation implementation;

    protected RealApproximateMatchingRule(String oid, SortedSet<String> names,
                                          String description, boolean obsolete,
                                          String syntax,
                                          Map<String, List<String>> extraProperties,
                                          ApproximateMatchingRuleImplementation implementation,
                                          String definition) {
      super(oid, names, description, obsolete, syntax, extraProperties,
          definition);
      this.implementation = implementation;
    }

    /**
     * {@inheritDoc}
     */
    public Syntax getSyntax()
    {
      return syntax;
    }

    protected void validate() throws SchemaException
    {
      // Make sure the specifiec syntax is defined in this schema.
      syntax = Schema.this.getSyntax(syntaxOID);
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
      return implementation.valuesMatch(Schema.this, attributeValue,
          assertionValue);
    }

    /**
     * {@inheritDoc}
     */
    public ByteSequence normalizeAttributeValue(ByteSequence value) {
      return implementation.normalizeAttributeValue(Schema.this, value);
    }

    /**
     * {@inheritDoc}
     */
    public ByteSequence normalizeAssertionValue(ByteSequence value) {
      return implementation.normalizeAssertionValue(Schema.this, value);
    }

    /**
     * {@inheritDoc}
     */
    public boolean approximatelyMatch(ByteSequence attributeValue,
                                      ByteSequence assertionValue) {
      return implementation.approximatelyMatch(Schema.this,
          attributeValue, assertionValue);
    }

    protected RealApproximateMatchingRule duplicate() {
      return new RealApproximateMatchingRule(oid, names, description,
          isObsolete, syntaxOID, extraProperties, implementation, definition);
    }
  }

  protected final class RealMatchingRuleUse extends MatchingRuleUse
  {
    private MatchingRule matchingRule;
    private Set<AttributeType> attributes = Collections.emptySet();

    protected RealMatchingRuleUse(String oid, SortedSet<String> names,
                                  String description, boolean obsolete,
                                  Set<String> attribute,
                                  Map<String, List<String>> extraProperties,
                                  String definition) {
      super(oid, names, description, obsolete, attribute, extraProperties,
          definition);
    }

    /**
     * {@inheritDoc}
     */
    public MatchingRule getMatchingRule() {
      return matchingRule;
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<AttributeType> getAttributes()
    {
      return attributes;
    }

    protected void validate() throws SchemaException
    {
      matchingRule = Schema.this.getMatchingRule(oid);
      if(matchingRule == null)
      {
        // This is bad because the matching rule use is associated with a
        // matching rule that we don't know anything about.
        Message message =
            ERR_ATTR_SYNTAX_MRUSE_UNKNOWN_MATCHING_RULE.get(definition, oid);
        throw new SchemaException(message);
      }

      attributes = new HashSet<AttributeType>(attributeOIDs.size());
      AttributeType attributeType;
      for(String attribute : attributeOIDs)
      {
        attributeType = Schema.this.getAttributeType(attribute);
        if(attributeType == null)
        {
          Message message = ERR_ATTR_SYNTAX_MRUSE_UNKNOWN_ATTR.get(
              oid, attribute);
          throw new SchemaException(message);
        }
        attributes.add(attributeType);
      }
    }

    protected RealMatchingRuleUse duplicate() {
      return new RealMatchingRuleUse(oid, names, description, isObsolete,
          attributeOIDs, extraProperties, definition);
    }
  }

  protected final class RealSyntax extends Syntax
  {
    private EqualityMatchingRule equalityMatchingRule;
    private OrderingMatchingRule orderingMatchingRule;
    private SubstringMatchingRule substringMatchingRule;
    private ApproximateMatchingRule approximateMatchingRule;

    protected final SyntaxImplementation implementation;

    protected RealSyntax(String oid, String description,
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

    protected void validate() throws SchemaException
    {
      // Get references to the default matching rules
      if(implementation.getEqualityMatchingRule() != null)
      {
        MatchingRule rule = Schema.this.getMatchingRule(
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
        MatchingRule rule = Schema.this.getMatchingRule(
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
        MatchingRule rule = Schema.this.getMatchingRule(
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
        MatchingRule rule = Schema.this.getMatchingRule(
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

    protected RealSyntax duplicate() {
      return new RealSyntax(oid, description, extraProperties,
          implementation, definition);
    }
  }

  protected final class SubstitutionSyntax extends Syntax
  {
    private Syntax substitute;
    private final String substituteOID;

    protected SubstitutionSyntax(String oid, String description,
                                 Map<String, List<String>> extraProperties,
                                 String substitute, String definition)
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

    protected void validate() throws SchemaException
    {
      // Get reference to the substitute syntax
      substitute = Schema.this.getSyntax(substituteOID);
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

    protected SubstitutionSyntax duplicate() {
      return new SubstitutionSyntax(oid, description, extraProperties,
          substituteOID, definition);
    }
  }

  protected final class RealDITContentRule extends DITContentRule
  {
    private ObjectClass structuralClass;
    private Set<ObjectClass> auxiliaryClasses = Collections.emptySet();
    private Set<AttributeType> optionalAttributes = Collections.emptySet();
    private Set<AttributeType> prohibitedAttributes = Collections.emptySet();
    private Set<AttributeType> requiredAttributes = Collections.emptySet();

    protected RealDITContentRule(String structuralClassOID,
                                 SortedSet<String> names, String description,
                                 boolean obsolete,
                                 Set<String> auxiliaryClassOIDs,
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

    protected void validate() throws SchemaException
    {
      // Get the objectclass with the specified OID.  If it does not exist or is
      // not structural, then fail.
      if(structuralClassOID != null)
      {
        structuralClass = Schema.this.getObjectClass(structuralClassOID);
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
          objectClass = Schema.this.getObjectClass(oid);
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
          attributeType = Schema.this.getAttributeType(oid);
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
          attributeType = Schema.this.getAttributeType(oid);
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
          attributeType = Schema.this.getAttributeType(oid);
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

    protected RealDITContentRule duplicate() {
      return new RealDITContentRule(structuralClassOID, names, description,
          isObsolete, auxiliaryClassOIDs, optionalAttributeOIDs,
          prohibitedAttributeOIDs, requiredAttributeOIDs, extraProperties,
          definition);
    }
  }

  protected final class RealDITStructureRule extends DITStructureRule
  {
    private NameForm nameForm;
    private Set<DITStructureRule> superiorRules = Collections.emptySet();

    protected RealDITStructureRule(Integer ruleID, SortedSet<String> names,
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

    protected void validate() throws SchemaException
    {
      nameForm = Schema.this.getNameForm(nameFormOID);
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
          rule = Schema.this.getDITStructureRule(id);
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

    protected RealDITStructureRule duplicate() {
      return new RealDITStructureRule(ruleID, names, description, isObsolete,
          nameFormOID, superiorRuleIDs, extraProperties, definition);
    }
  }

  protected final class RealNameForm extends NameForm
  {
    private ObjectClass structuralClass;
    private Set<AttributeType> optionalAttributes = Collections.emptySet();
    private Set<AttributeType> requiredAttributes = Collections.emptySet();

    protected RealNameForm(String oid, SortedSet<String> names,
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

    protected void validate() throws SchemaException
    {
      structuralClass = Schema.this.getObjectClass(structuralClassOID);
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
        attributeType = Schema.this.getAttributeType(oid);
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
          attributeType = Schema.this.getAttributeType(oid);
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

    protected RealNameForm duplicate() {
      return new RealNameForm(oid, names, description, isObsolete,
          structuralClassOID, requiredAttributeOIDs, optionalAttributeOIDs,
          extraProperties, definition);
    }
  }

  protected final class RealObjectClass extends ObjectClass
  {
    private Set<ObjectClass> superiorClasses = Collections.emptySet();
    private Set<AttributeType> declaredRequiredAttributes =
        Collections.emptySet();
    private Set<AttributeType> requiredAttributes = Collections.emptySet();
    private Set<AttributeType> declaredOptionalAttributes =
        Collections.emptySet();
    private Set<AttributeType> optionalAttributes = Collections.emptySet();
    private boolean validated = false;

    protected RealObjectClass(String oid, SortedSet<String> names,
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

    protected void validate() throws SchemaException
    {
      if(validated)
      {
        return;
      }
      validated = true;

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

          // Validate superior object class so we can inherit its attributes.
          superiorClass.validate();

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

    protected RealObjectClass duplicate() {
      return new RealObjectClass(oid, names, description, isObsolete,
          superiorClassOIDs, requiredAttributeOIDs, optionalAttributeOIDs,
          objectClassType, extraProperties, definition);
    }
  }

  protected final class ExtensibleObjectClass extends ObjectClass
      implements Iterable<AttributeType>
  {
    protected ExtensibleObjectClass(String oid, SortedSet<String> names,
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
            Schema.this.getAttributeTypes().iterator();
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
          Schema.this.getObjectClass("2.5.6.0"));
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

    protected void validate() throws SchemaException
    {
      // nothing to do
    }

    protected ExtensibleObjectClass duplicate() {
      return new ExtensibleObjectClass(oid, names, description, isObsolete,
          superiorClassOIDs, requiredAttributeOIDs, optionalAttributeOIDs,
          objectClassType, extraProperties, definition);
    }
  }

  protected Schema() {
    numericOID2Syntaxes = new HashMap<String, Syntax>();
    numericOID2MatchingRules = new HashMap<String, MatchingRule>();
    numericOID2MatchingRuleUses = new HashMap<String, MatchingRuleUse>();
    numericOID2AttributeTypes = new HashMap<String, AttributeType>();
    numericOID2ObjectClasses = new HashMap<String, ObjectClass>();
    numericOID2NameForms = new HashMap<String, NameForm>();
    numericOID2ContentRules = new HashMap<String, DITContentRule>();
    id2StructureRules = new HashMap<Integer, DITStructureRule>();

    name2MatchingRules = new HashMap<String, List<MatchingRule>>();
    name2MatchingRuleUses = new HashMap<String, List<MatchingRuleUse>>();
    name2AttributeTypes = new HashMap<String, List<AttributeType>>();
    name2ObjectClasses = new HashMap<String, List<ObjectClass>>();
    name2NameForms = new HashMap<String, List<NameForm>>();
    name2ContentRules = new HashMap<String, List<DITContentRule>>();
    name2StructureRules = new HashMap<String, List<DITStructureRule>>();

    objectClass2NameForms = new HashMap<String, List<NameForm>>();
    nameForm2StructureRules = new HashMap<String, List<DITStructureRule>>();
  }

  /**
   * Retrieves the attribute syntax definition with the OID.
   *
   * @param numericOID
   *          The numeric OID of the attribute syntax to retrieve.
   * @return The requested attribute syntax, or {@code null} if no
   *         syntax is registered with the provided OID.
   */
  public Syntax getSyntax(String numericOID)
  {
    return numericOID2Syntaxes.get(numericOID);
  }

  public Collection<Syntax> getSyntaxes()
  {
    return Collections.unmodifiableCollection(numericOID2Syntaxes.values());
  }

  /**
   * Retrieves the attribute type definition with the specified name or
   * numeric OID.
   *
   * @param oid
   *          The name or OID of the attribute type to retrieve,
   *          formatted in all lower-case characters.
   * @return The requested attribute type, or {@code null} if no type is
   *         registered with the provided name or OID or the provided name
   *         is ambiguous.
   */
  public AttributeType getAttributeType(String oid)
  {
    AttributeType type = numericOID2AttributeTypes.get(oid);
    if(type != null)
    {
      return type;
    }
    List<AttributeType> attributes = name2AttributeTypes.get(oid);
    if(attributes != null && attributes.size() == 1)
    {
      return attributes.get(0);
    }
    return null;
  }

  public List<AttributeType> getAttributeTypesByName(String lowerName)
  {
    List<AttributeType> attributes = name2AttributeTypes.get(lowerName);
    if(attributes == null)
    {
      return Collections.emptyList();
    }
    else
    {
      return attributes;
    }
  }

  public Collection<AttributeType> getAttributeTypes()
  {
    return numericOID2AttributeTypes.values();
  }



  /**
   * Retrieves the DIT content rule definition for the specified name or
   * structural class numeric OID.
   *
   * @param oid
   *          The structural class numeric OID or the name of the DIT content
   *          rule to retrieve.
   * @return The requested DIT content rule, or {@code null} if no DIT
   *         content rule is registered with the provided name or structural
   *         class numeric OID or the provided name is ambiguous.
   */
  public DITContentRule getDITContentRule(String oid)
  {
    DITContentRule rule = numericOID2ContentRules.get(oid);
    if(rule != null)
    {
      return rule;
    }
    List<DITContentRule> rules = name2ContentRules.get(oid);
    if(rules != null && rules.size() == 1)
    {
      return rules.get(0);
    }
    return null;
  }

  public Collection<DITContentRule> getDITContentRulesByName(String lowerName)
  {
    List<DITContentRule> rules = name2ContentRules.get(lowerName);
    if(rules == null)
    {
      return Collections.emptyList();
    }
    else
    {
      return Collections.unmodifiableCollection(rules);
    }
  }

  public Collection<DITContentRule> getDITContentRules()
  {
    return Collections.unmodifiableCollection(numericOID2ContentRules.values());
  }



  /**
   * Retrieves the DIT structure rule definition with the provided rule
   * ID.
   *
   * @param ruleID
   *          The rule ID for the DIT structure rule to retrieve.
   * @return The requested DIT structure rule, or {@code null} if no DIT
   *         structure rule is registered with the provided rule ID.
   */
  public DITStructureRule getDITStructureRule(int ruleID)
  {
    return id2StructureRules.get(ruleID);
  }

  public Collection<DITStructureRule> getDITStructureRulesByName(
      String lowerName)
  {
    List<DITStructureRule> rules = name2StructureRules.get(lowerName);
    if(rules == null)
    {
      return Collections.emptyList();
    }
    else
    {
      return Collections.unmodifiableCollection(rules);
    }
  }

  public Collection<DITStructureRule> getDITStuctureRules()
  {
    return Collections.unmodifiableCollection(id2StructureRules.values());
  }

  /**
   * Retrieves the DIT structure rules for the provided name form.
   *
   * @param nameForm
   *          The name form.
   * @return The requested DIT structure rules.
   */
  public Collection<DITStructureRule> getDITStructureRulesByNameForm(
      NameForm nameForm)
  {
    List<DITStructureRule> rules =
        nameForm2StructureRules.get(nameForm.getOID());
    if(rules == null)
    {
      return Collections.emptyList();
    }
    else
    {
      return Collections.unmodifiableCollection(rules);
    }
  }


  /**
   * Retrieves the matching rule definition with the specified name or
   * numeric OID.
   *
   * @param oid
   *          The name or OID of the matching rule to retrieve,
   *          formatted in all lower-case characters.
   * @return The requested matching rule, or {@code null} if no rule is
   *         registered with the provided name or OID or the provided name
   *         is ambiguous.
   */
  public MatchingRule getMatchingRule(String oid)
  {
    MatchingRule rule = numericOID2MatchingRules.get(oid);
    if(rule != null)
    {
      return rule;
    }
    List<MatchingRule> rules = name2MatchingRules.get(oid);
    if(rules != null && rules.size() == 1)
    {
      return rules.get(0);
    }
    return null;
  }

  public Collection<MatchingRule> getMatchingRulesByName(String lowerName)
  {
    List<MatchingRule> rules = name2MatchingRules.get(lowerName);
    if(rules == null)
    {
      return Collections.emptyList();
    }
    else
    {
      return Collections.unmodifiableCollection(rules);
    }
  }

  public Collection<MatchingRule> getMatchingRules()
  {
    return Collections.unmodifiableCollection(
        numericOID2MatchingRules.values());
  }



  /**
   * Retrieves the matching rule use definition with the specified name or for
   * the specified matching rule numeric OID.
   *
   * @param oid
   *          The OID of the matching rule or name of the matching rule use
   *          to retrieve.
   * @return The matching rule use definition, or {@code null} if none
   *         exists for the specified matching rule or the provided name
   *         is ambiguous.
   */
  public MatchingRuleUse getMatchingRuleUse(String oid)
  {
    MatchingRuleUse rule = numericOID2MatchingRuleUses.get(oid);
    if(rule != null)
    {
      return rule;
    }
    List<MatchingRuleUse> uses = name2MatchingRuleUses.get(oid);
    if(uses != null && uses.size() == 1)
    {
      return uses.get(0);
    }
    return null;
  }

  /**
   * Retrieves the matching rule use definition for the specified
   * matching rule.
   *
   * @param  matchingRule  The matching rule for which to retrieve the
   *                       matching rule use definition.
   *
   * @return  The matching rule use definition, or <CODE>null</CODE>
   *          if none exists for the specified matching rule.
   */
  public MatchingRuleUse getMatchingRuleUse(MatchingRule matchingRule)
  {
    return numericOID2MatchingRuleUses.get(matchingRule.getOID()); 
  }

  public Collection<MatchingRuleUse> getMatchingRuleUsesByName(String lowerName)
  {
    List<MatchingRuleUse> rules = name2MatchingRuleUses.get(lowerName);
    if(rules == null)
    {
      return Collections.emptyList();
    }
    else
    {
      return Collections.unmodifiableCollection(rules);
    }
  }

  public Collection<MatchingRuleUse> getMatchingRuleUses()
  {
    return Collections.unmodifiableCollection(
        numericOID2MatchingRuleUses.values());
  }



  /**
   * Retrieves the name form definition with the specified name or
   * numeric OID.
   *
   * @param oid
   *          The name or OID of the name form to retrieve, formatted in
   *          all lower-case characters.
   * @return The requested name form, or {@code null} if no name form is
   *         registered with the provided name or OID or the provided name
   *         is ambiguous.
   */
  public NameForm getNameForm(String oid)
  {
    NameForm form = numericOID2NameForms.get(oid);
    if(form != null)
    {
      return form;
    }
    List<NameForm> forms = name2NameForms.get(oid);
    if(forms != null && forms.size() == 1)
    {
      return forms.get(0);
    }
    return null;
  }

  public Collection<NameForm> getNameFormsByName(String lowerName)
  {
    List<NameForm> forms = name2NameForms.get(lowerName);
    if(forms == null)
    {
      return Collections.emptyList();
    }
    else
    {
      return Collections.unmodifiableCollection(forms);
    }
  }

  public Collection<NameForm> getNameForms()
  {
    return Collections.unmodifiableCollection(numericOID2NameForms.values());
  }

  /**
   * Retrieves the name forms for the specified structural objectclass.
   *
   * @param  structuralClass  The structural objectclass for the name form to
   *                          retrieve.
   *
   * @return  The requested name forms
   */
  public Collection<NameForm> getNameFormByObjectClass(
      ObjectClass structuralClass)
  {
    List<NameForm> forms =
            objectClass2NameForms.get(structuralClass.getOID());
    if(forms == null)
    {
      return Collections.emptyList();
    }
    else
    {
      return Collections.unmodifiableCollection(forms);
    }
  }

  /**
   * Retrieves the object class definition with the specified name or
   * numeric OID.
   *
   * @param oid
   *          The name or OID of the object class to retrieve, formatted
   *          in all lower-case characters.
   * @return The requested object class, or {@code null} if no object
   *         class is registered with the provided name or OID or the
   *         provided name is ambiguous.
   */
  public ObjectClass getObjectClass(String oid)
  {
    ObjectClass oc = numericOID2ObjectClasses.get(oid);
    if(oc != null)
    {
      return oc;
    }
    List<ObjectClass> classes = name2ObjectClasses.get(oid);
    if(classes != null && classes.size() == 1)
    {
      return classes.get(0);
    }
    return null;
  }

  public Collection<ObjectClass> getObjectClassesByName(String lowerName)
  {
    List<ObjectClass> classes = name2ObjectClasses.get(lowerName);
    if(classes == null)
    {
      return Collections.emptyList();
    }
    else
    {
      return Collections.unmodifiableCollection(classes);
    }
  }

  public Collection<ObjectClass> getObjectClasses()
  {
    return Collections.unmodifiableCollection(
        numericOID2ObjectClasses.values());
  }

  /**
   * Indicates whether this schema is strict. A strict schema will not
   * create default object classes, attribute types, and syntaxes on
   * demand.
   *
   * @return {@code true} if this schema is strict.
   */
  public abstract boolean isStrict();

  protected void validate() throws SchemaException
  {
    // Verify all references in all elements
    for(Syntax syntax : numericOID2Syntaxes.values())
    {
      syntax.validate();
    }

    for(MatchingRule rule : numericOID2MatchingRules.values())
    {
      rule.validate();
    }

    for(AttributeType attribute : numericOID2AttributeTypes.values())
    {
      attribute.validate();
    }

    for(ObjectClass oc : numericOID2ObjectClasses.values())
    {
      oc.validate();
    }

    for(MatchingRuleUse use : numericOID2MatchingRuleUses.values())
    {
      use.validate();
    }

    for(NameForm form : numericOID2NameForms.values())
    {
      form.validate();

      // build the objectClass2NameForms map
      List<NameForm> forms;
      String ocOID = form.getStructuralClass().getOID();
      if((forms = objectClass2NameForms.get(ocOID)) == null)
      {
        objectClass2NameForms.put(ocOID, Collections.singletonList(form));
      }
      else if(forms.size() == 1)
      {
        forms = new ArrayList<NameForm>(forms);
        forms.add(form);
        objectClass2NameForms.put(ocOID, forms);
      }
      else
      {
        forms.add(form);
      }
    }

    for(DITContentRule rule : numericOID2ContentRules.values())
    {
      rule.validate();
    }

    for(DITStructureRule rule : id2StructureRules.values())
    {
      rule.validate();

      // build the nameForm2StructureRules map
      List<DITStructureRule> rules;
      String ocOID = rule.getNameForm().getOID();
      if((rules = nameForm2StructureRules.get(ocOID)) == null)
      {
        nameForm2StructureRules.put(ocOID, Collections.singletonList(rule));
      }
      else if(rules.size() == 1)
      {
        rules = new ArrayList<DITStructureRule>(rules);
        rules.add(rule);
        nameForm2StructureRules.put(ocOID, rules);
      }
      else
      {
        rules.add(rule);
      }
    }

  }

}

