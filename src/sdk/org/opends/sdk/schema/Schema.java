/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2009 Sun Microsystems, Inc.
 */
package org.opends.sdk.schema;

import static org.opends.messages.SchemaMessages.*;
import static org.opends.sdk.schema.SchemaConstants.*;

import java.util.*;
import java.util.regex.Pattern;

import org.opends.messages.Message;
import org.opends.messages.MessageBuilder;
import static org.opends.messages.CoreMessages.*;
import static org.opends.messages.CoreMessages.ERR_SCHEMA_CONFLICTING_OBJECTCLASS_OID;
import org.opends.sdk.*;
import org.opends.sdk.schema.matchingrules.MatchingRuleImplementation;
import org.opends.sdk.schema.matchingrules.AbstractMatchingRuleImplementation;
import org.opends.sdk.schema.syntaxes.SyntaxImplementation;
import org.opends.sdk.util.StaticUtils;
import org.opends.sdk.util.Validator;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;

/**
 * This class defines a data structure that holds information about
 * the components of the LDAP schema.  It includes the
 * following kinds of elements:
 *
 * <UL>
 *   <LI>Attribute type definitions</LI>
 *   <LI>Objectclass definitions</LI>
 *   <LI>Attribute syntax definitions</LI>
 *   <LI>Matching rule definitions</LI>
 *   <LI>Matching rule use definitions</LI>
 *   <LI>DIT content rule definitions</LI>
 *   <LI>DIT structure rule definitions</LI>
 *   <LI>Name form definitions</LI>
 * </UL>
 */
public abstract class Schema
{
  private static Schema DEFAULT_SCHEMA = CoreSchema.instance();
  private static String ATTR_LDAP_SYNTAXES = "ldapSyntaxes";
  private static String ATTR_ATTRIBUTE_TYPES = "attributeTypes";
  private static String ATTR_DIT_CONTENT_RULES = "dITContentRules";
  private static String ATTR_DIT_STRUCTURE_RULES = "dITStructureRules";
  private static String ATTR_MATCHING_RULE_USE = "matchingRuleUse";
  private static String ATTR_MATCHING_RULES = "matchingRules";
  private static String ATTR_NAME_FORMS = "nameForms";
  private static String ATTR_OBJECT_CLASSES = "objectClasses";
  private static String ATTR_SUBSCHEMA_SUBENTRY="subschemaSubentry";
  private static String[] SUBSCHEMA_ATTRS=new String[]{ ATTR_LDAP_SYNTAXES,
      ATTR_ATTRIBUTE_TYPES, ATTR_DIT_CONTENT_RULES, ATTR_DIT_STRUCTURE_RULES,
      ATTR_MATCHING_RULE_USE, ATTR_MATCHING_RULES, ATTR_NAME_FORMS,
      ATTR_OBJECT_CLASSES };

  /**
   * Returns the default schema which should be used by this
   * application.
   *
   * @return The default schema which should be used by this
   *         application.
   */
  public static Schema getDefaultSchema()
  {
    return DEFAULT_SCHEMA;
  }

  public static Schema getSchema(Connection connection, String dn,
                                 List<Message> warnings)
      throws ErrorResultException, InterruptedException, DecodeException,
      SchemaException
  {
    Validator.ensureNotNull(connection, dn, warnings);
    SearchResultEntry result = connection.get(dn, ATTR_SUBSCHEMA_SUBENTRY);
    AttributeValueSequence subentryAttr;
    if((subentryAttr = result.getAttribute(ATTR_SUBSCHEMA_SUBENTRY)) == null ||
       subentryAttr.isEmpty())
    {
      throw new SchemaException(ERR_NO_SUBSCHEMA_SUBENTRY_ATTR.get(dn));
    }

    result = connection.get(subentryAttr.iterator().next().toString(),
                            SUBSCHEMA_ATTRS);
    Entry entry = new SortedEntry(result, CoreSchema.instance());

    SchemaBuilder builder = new SchemaBuilder();
    Attribute attr = entry.getAttribute(ATTR_LDAP_SYNTAXES);

    if(attr != null)
    {
      for(ByteString def : attr)
      {
        builder.addSyntax(def.toString(), true);
      }
    }

    attr = entry.getAttribute(ATTR_ATTRIBUTE_TYPES);
    if(attr != null)
    {
      for(ByteString def : attr)
      {
        try
        {
        builder.addAttributeType(def.toString(), true);
        }
        catch(DecodeException e)
        {
          warnings.add(e.getMessageObject());
        }
      }
    }

    attr = entry.getAttribute(ATTR_OBJECT_CLASSES);
    if(attr != null)
    {
      for(ByteString def : attr)
      {
        builder.addObjectClass(def.toString(), true);
      }
    }

    attr = entry.getAttribute(ATTR_MATCHING_RULE_USE);
    if(attr != null)
    {
      for(ByteString def : attr)
      {
        builder.addMatchingRuleUse(def.toString(), true);
      }
    }

    attr = entry.getAttribute(ATTR_MATCHING_RULES);
    if(attr != null)
    {
      for(ByteString def : attr)
      {
        try
        {
        builder.addMatchingRule(def.toString(), true);
        }
        catch(DecodeException e)
        {
          warnings.add(e.getMessageObject());
        }
      }
    }

    attr = entry.getAttribute(ATTR_DIT_CONTENT_RULES);
    if(attr != null)
    {
      for(ByteString def : attr)
      {
        builder.addDITContentRule(def.toString(), true);
      }
    }

    attr = entry.getAttribute(ATTR_DIT_STRUCTURE_RULES);
    if(attr != null)
    {
      for(ByteString def : attr)
      {
        builder.addDITStructureRule(def.toString(), true);
      }
    }

    attr = entry.getAttribute(ATTR_NAME_FORMS);
    if(attr != null)
    {
      for(ByteString def : attr)
      {
        builder.addNameForm(def.toString(), true);
      }
    }


    Schema schema = builder.toSchema(warnings);
    return schema;
  }



  /**
   * Sets the default schema which should be used by this application.
   *
   * @param schema
   *          The default schema which should be used by this
   *          application.
   * @throws NullPointerException
   *           If {@code schema} was {@code null}.
   */
  public static void setDefaultSchema(Schema schema)
      throws NullPointerException
  {
    Validator.ensureNotNull(schema);
    DEFAULT_SCHEMA = schema;
  }



  private final Map<String, Syntax> numericOID2Syntaxes;
  private final Map<String, MatchingRule> numericOID2MatchingRules;
  private final Map<String, MatchingRuleUse> numericOID2MatchingRuleUses;
  private final Map<String, AttributeType> numericOID2AttributeTypes;
  private final Map<String, ObjectClass> numericOID2ObjectClasses;
  private final Map<String, NameForm> numericOID2NameForms;
  private final Map<String, DITContentRule> numericOID2ContentRules;
  private final Map<Integer, DITStructureRule> id2StructureRules;


  private final Map<String, List<MatchingRule>> name2MatchingRules;
  private final Map<String, List<MatchingRuleUse>> name2MatchingRuleUses;
  private final Map<String, List<AttributeType>> name2AttributeTypes;
  private final Map<String, List<ObjectClass>> name2ObjectClasses;
  private final Map<String, List<NameForm>> name2NameForms;
  private final Map<String, List<DITContentRule>> name2ContentRules;
  private final Map<String, List<DITStructureRule>> name2StructureRules;

  private final Map<String, List<NameForm>> objectClass2NameForms;
  private final Map<String, List<DITStructureRule>> nameForm2StructureRules;

  private final Map<SchemaAttachment<?>, Object> attachments;

  protected final class DefaultAttributeType extends AttributeType
  {
    public DefaultAttributeType(String name)
    {
      super(name + "-oid", Collections.singletonList(name), "", false, null,
            SchemaBuilder.getDefaultMatchingRule(), null, null, null,
            SchemaBuilder.getDefaultSyntax(), false, false, false,
            AttributeUsage.USER_APPLICATIONS, Collections.EMPTY_MAP, 
            null);
    }

    public MatchingRule getApproximateMatchingRule()
    {
      return null;
    }

    public MatchingRule getEqualityMatchingRule()
    {
      return Schema.this.getMatchingRule(
          SchemaBuilder.getDefaultMatchingRule());
    }

    public MatchingRule getOrderingMatchingRule()
    {
      return null;
    }

    public MatchingRule getSubstringMatchingRule()
    {
      return null;
    }

    public AttributeType getSuperiorType()
    {
      return null;
    }

    public Syntax getSyntax()
    {
      return Schema.this.getSyntax(SchemaBuilder.getDefaultSyntax());
    }

    protected AttributeType duplicate()
    {
      return new DefaultAttributeType(getNameOrOID());
    }

    protected void validate(List<Message> warnings) throws SchemaException
    {
      // Nothing to do
    }
  }

  protected final class CachingAttributeType extends AttributeType
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

    protected CachingAttributeType(String oid, List<String> names,
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
    @Override
    public AttributeType getSuperiorType()
    {
      return superiorType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Syntax getSyntax()
    {
      return syntax;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatchingRule getApproximateMatchingRule()
    {
      return approximateMatchingRule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatchingRule getEqualityMatchingRule()
    {
      return equalityMatchingRule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatchingRule getOrderingMatchingRule()
    {
      return orderingMatchingRule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatchingRule getSubstringMatchingRule()
    {
      return substringMatchingRule;
    }

    @Override
    protected void validate(List<Message> warnings) throws SchemaException
    {
      if(superiorTypeOID != null)
      {
        superiorType = Schema.this.getAttributeType(superiorTypeOID);

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
        // Use explicitly defined matching rule first.
        orderingMatchingRule =
            Schema.this.getMatchingRule(orderingMatchingRuleOID);
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

      if(substringMatchingRuleOID != null)
      {
        // Use explicitly defined matching rule first.
        substringMatchingRule =
            Schema.this.getMatchingRule(substringMatchingRuleOID);
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
        // Use explicitly defined matching rule first.
        approximateMatchingRule =
            Schema.this.getMatchingRule(approximateMatchingRuleOID);
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

    @Override
    protected CachingAttributeType duplicate() {
      return new CachingAttributeType(oid, names, description, isObsolete,
          superiorTypeOID, equalityMatchingRuleOID, orderingMatchingRuleOID,
          substringMatchingRuleOID, approximateMatchingRuleOID, syntaxOID,
          isSingleValue, isCollective, isNoUserModification, attributeUsage,
          extraProperties, definition);
    }
  }

  protected final class CachingMatchingRule extends MatchingRule
  {
    private Syntax syntax;
    protected MatchingRuleImplementation implementation;

    protected CachingMatchingRule(String oid, List<String> names,
                                  String description, boolean obsolete,
                                  String syntax,
                                  Map<String,
                                      List<String>> extraProperties,
                                  String definition) {
      super(oid, names, description, obsolete, syntax, extraProperties,
            definition);
      this.implementation = null;
    }

    protected CachingMatchingRule(String oid, List<String> names,
                                  String description, boolean obsolete,
                                  String syntax,
                                  Map<String,
                                      List<String>> extraProperties,
                                  MatchingRuleImplementation
                                      implementation,
                                  String definition) {
      super(oid, names, description, obsolete, syntax, extraProperties,
          definition);
      Validator.ensureNotNull(implementation);
      this.implementation = implementation;
    }

    @Override
    public Syntax getSyntax()
    {
      return syntax;
    }

    @Override
    protected void validate(List<Message> warnings) throws SchemaException
    {
      // Try finding an implementation in the core schema
      if(implementation == null && DEFAULT_SCHEMA.hasMatchingRule(oid) &&
         DEFAULT_SCHEMA.getMatchingRule(oid) instanceof CachingMatchingRule)
      {
        implementation = ((CachingMatchingRule)
            DEFAULT_SCHEMA.getMatchingRule(oid)).implementation;
      }
      if(implementation == null &&
              CoreSchema.instance().hasMatchingRule(oid) &&
              CoreSchema.instance().getMatchingRule(oid) instanceof
                  CachingMatchingRule)
      {
        implementation = ((CachingMatchingRule)
            CoreSchema.instance().getMatchingRule(oid)).implementation;
      }

      if(implementation == null)
      {
        implementation = ((CachingMatchingRule)
            CoreSchema.instance().getMatchingRule(
                SchemaBuilder.getDefaultMatchingRule())).implementation;
        Message message = WARN_MATCHING_RULE_NOT_IMPLEMENTED.get(oid,
          SchemaBuilder.getDefaultMatchingRule());
        warnings.add(message);
      }

      try
      {
        // Make sure the specifiec syntax is defined in this schema.
        syntax = Schema.this.getSyntax(syntaxOID);
      }
      catch(UnknownSchemaElementException e)
      {
        Message message = ERR_ATTR_SYNTAX_MR_UNKNOWN_SYNTAX.get(getNameOrOID(),
            syntaxOID);
        throw new SchemaException(message, e);
      }
    }

    @Override
    public ByteString normalizeAttributeValue(ByteSequence value)
        throws DecodeException {
      return implementation.normalizeAttributeValue(Schema.this, value);
    }

    public Comparator<ByteSequence> comparator() {
      return implementation.comparator(Schema.this);
    }

    public Assertion getAssertion(ByteSequence value) throws DecodeException {
      return implementation.getAssertion(Schema.this, value);
    }

    public Assertion getAssertion(ByteSequence subInitial,
                                  List<ByteSequence> subAnyElements,
                                  ByteSequence subFinal) throws DecodeException
    {
      return implementation.getAssertion(Schema.this, subInitial,
          subAnyElements, subFinal);
    }

    public Assertion getGreaterOrEqualAssertion(ByteSequence value) throws DecodeException {
      return implementation.getGreaterOrEqualAssertion(Schema.this, value);
    }

    public Assertion getLessOrEqualAssertion(ByteSequence value) throws DecodeException {
      return implementation.getLessOrEqualAssertion(Schema.this, value);
    }

    @Override
    protected CachingMatchingRule duplicate() {
      return new CachingMatchingRule(oid, names, description, isObsolete,
          syntaxOID, extraProperties, implementation, definition);
    }
  }

  protected class EnumOrderingMatchingRule extends MatchingRule
  {
    private EnumSyntax syntax;
    private final Comparator<ByteSequence> comparator =
        new Comparator<ByteSequence>()
        {
          public int compare(ByteSequence o1, ByteSequence o2) {
            return syntax.entries.indexOf(o1) -
                syntax.entries.indexOf(o2);
          }
        };

    protected EnumOrderingMatchingRule(String syntax)
    {
      super(OMR_OID_GENERIC_ENUM + "." + syntax,
          Collections.singletonList(OMR_GENERIC_ENUM_NAME + syntax),
          "".intern(), false, syntax, CoreSchema.OPENDS_ORIGIN,
          null);
    }

    @Override
    public ByteString normalizeAttributeValue(ByteSequence value) {
      return normalizeValue(value);
    }

    public Comparator<ByteSequence> comparator() {
      return comparator;
    }

    public Assertion getGreaterOrEqualAssertion(ByteSequence value)
        throws DecodeException {
      final ByteString normAssertion = normalizeValue(value);
      return new Assertion()
      {
        public ConditionResult matches(ByteString attributeValue) {
          return comparator.compare(attributeValue, normAssertion) >= 0 ?
              ConditionResult.TRUE : ConditionResult.FALSE;
        }
      };
    }

    public Assertion getLessOrEqualAssertion(ByteSequence value)
        throws DecodeException {
      final ByteString normAssertion = normalizeValue(value);
      return new Assertion()
      {
        public ConditionResult matches(ByteString attributeValue) {
          return comparator.compare(attributeValue, normAssertion) <= 0 ?
              ConditionResult.TRUE : ConditionResult.FALSE;
        }
      };
    }

    public Assertion getAssertion(ByteSequence value) throws DecodeException {
      final ByteString normAssertion = normalizeValue(value);
      return new Assertion()
      {
        public ConditionResult matches(ByteString attributeValue) {
          return comparator.compare(attributeValue, normAssertion) < 0 ?
              ConditionResult.TRUE : ConditionResult.FALSE;
        }
      };
    }

    public Assertion getAssertion(ByteSequence subInitial,
                                  List<ByteSequence> subAnyElements,
                                  ByteSequence subFinal) throws DecodeException
    {
      return AbstractMatchingRuleImplementation.UNDEFINED_ASSERTION;
    }

    public Syntax getSyntax() {
      return syntax;
    }

    @Override
    protected EnumOrderingMatchingRule duplicate() {
      return new EnumOrderingMatchingRule(syntax.getOID());
    }

    @Override
    protected void validate(List<Message> warnings) throws SchemaException {
      Syntax syntax;
      try
      {
        // Make sure the specifiec syntax is defined in this schema.
        syntax = Schema.this.getSyntax(syntaxOID);
      }
      catch(UnknownSchemaElementException e)
      {
        Message message = ERR_ATTR_SYNTAX_MR_UNKNOWN_SYNTAX.get(getNameOrOID(),
            syntaxOID);
        throw new SchemaException(message, e);
      }

      if(!(syntax instanceof EnumSyntax))
      {
        Message message = ERR_ATTR_SYNTAX_MR_UNKNOWN_SYNTAX.get(getNameOrOID(),
            syntaxOID);
        throw new SchemaException(message);
      }
      this.syntax = (EnumSyntax)syntax;
    }

    private ByteString normalizeValue(ByteSequence value)
    {
      StringBuilder buffer = new StringBuilder();
      StringPrepProfile.prepareUnicode(buffer, value,
          StringPrepProfile.TRIM, StringPrepProfile.CASE_FOLD);

      int bufferLength = buffer.length();
      if (bufferLength == 0)
      {
        if (value.length() > 0)
        {
          // This should only happen if the value is composed entirely
          // of spaces. In that case, the normalized value is a single space.
          return SINGLE_SPACE_VALUE;
        }
        else
        {
          // The value is empty, so it is already normalized.
          return ByteString.empty();
        }
      }


      // Replace any consecutive spaces with a single space.
      for (int pos = bufferLength-1; pos > 0; pos--)
      {
        if (buffer.charAt(pos) == ' ')
        {
          if (buffer.charAt(pos-1) == ' ')
          {
            buffer.delete(pos, pos+1);
          }
        }
      }

      return ByteString.valueOf(buffer.toString());
    }
  }

  protected final class CachingMatchingRuleUse extends MatchingRuleUse
  {
    private MatchingRule matchingRule;
    private Set<AttributeType> attributes = Collections.emptySet();

    protected CachingMatchingRuleUse(String oid, List<String> names,
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
    @Override
    public MatchingRule getMatchingRule() {
      return matchingRule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<AttributeType> getAttributes()
    {
      return attributes;
    }

    @Override
    protected void validate(List<Message> warnings) throws SchemaException
    {
      try
      {
        matchingRule = Schema.this.getMatchingRule(oid);
      }
      catch(UnknownSchemaElementException e)
      {
        // This is bad because the matching rule use is associated with a
        // matching rule that we don't know anything about.
        Message message =
            ERR_ATTR_SYNTAX_MRUSE_UNKNOWN_MATCHING_RULE.get(definition, oid);
        throw new SchemaException(message, e);
      }

      attributes = new HashSet<AttributeType>(attributeOIDs.size());
      AttributeType attributeType;
      for(String attribute : attributeOIDs)
      {
        try
        {
          attributeType = Schema.this.getAttributeType(attribute);
        }
        catch(UnknownSchemaElementException e)
        {
          Message message = ERR_ATTR_SYNTAX_MRUSE_UNKNOWN_ATTR.get(
              oid, attribute);
          throw new SchemaException(message, e);
        }
        attributes.add(attributeType);
      }
    }

    @Override
    protected CachingMatchingRuleUse duplicate() {
      return new CachingMatchingRuleUse(oid, names, description, isObsolete,
          attributeOIDs, extraProperties, definition);
    }
  }

  protected final class CachingSyntax extends Syntax
  {
    private MatchingRule equalityMatchingRule;
    private MatchingRule orderingMatchingRule;
    private MatchingRule substringMatchingRule;
    private MatchingRule approximateMatchingRule;

    protected SyntaxImplementation implementation;

    protected CachingSyntax(String oid, String description,
                            Map<String, List<String>> extraProperties,
                            SyntaxImplementation implementation, String definition)
    {
      super(oid, description, extraProperties, definition);
      Validator.ensureNotNull(implementation);
      this.implementation = implementation;
    }

    protected CachingSyntax(String oid, String description,
                            Map<String, List<String>> extraProperties,
                            String definition)
    {
      super(oid, description, extraProperties, definition);
      this.implementation = null;
    }

    @Override
    public MatchingRule getEqualityMatchingRule() {
      return equalityMatchingRule;
    }

    @Override
    public MatchingRule getOrderingMatchingRule() {
      return orderingMatchingRule;
    }

    @Override
    public MatchingRule getSubstringMatchingRule() {
      return substringMatchingRule;
    }

    @Override
    public MatchingRule getApproximateMatchingRule() {
      return approximateMatchingRule;
    }

    @Override
    protected void validate(List<Message> warnings) throws SchemaException
    {
      // Try to find an implementation in the core schema
      if(implementation == null && DEFAULT_SCHEMA.hasSyntax(oid) &&
         DEFAULT_SCHEMA.getSyntax(oid) instanceof CachingSyntax)
      {
        implementation =
            ((CachingSyntax)DEFAULT_SCHEMA.getSyntax(oid)).implementation;
      }
      if(implementation == null && CoreSchema.instance().hasSyntax(oid) &&
         CoreSchema.instance().getSyntax(oid) instanceof CachingSyntax)
      {
        implementation =
           ((CachingSyntax)CoreSchema.instance().getSyntax(oid)).implementation;
      }

      if(implementation == null)
      {
        implementation =
           ((CachingSyntax)CoreSchema.instance().getSyntax(
               SchemaBuilder.getDefaultSyntax())).implementation;
        Message message = WARN_ATTR_SYNTAX_NOT_IMPLEMENTED.get(oid,
          SchemaBuilder.getDefaultSyntax());
        warnings.add(message);
      }

      // Get references to the default matching rules. It will be ok
      // if we can't find some. Just warn.
      if(implementation.getEqualityMatchingRule() != null)
      {
        if(Schema.this.hasMatchingRule(
            implementation.getEqualityMatchingRule()))
        {
          equalityMatchingRule = Schema.this.getMatchingRule(
              implementation.getEqualityMatchingRule());
        }
        else
        {
          Message message =
              ERR_ATTR_SYNTAX_UNKNOWN_EQUALITY_MATCHING_RULE.get(
                  implementation.getEqualityMatchingRule(),
                  implementation.getName());
          warnings.add(message);
        }
      }

      if(implementation.getOrderingMatchingRule() != null)
      {
        if(Schema.this.hasMatchingRule(
            implementation.getOrderingMatchingRule()))
        {
          orderingMatchingRule = Schema.this.getMatchingRule(
              implementation.getOrderingMatchingRule());
        }
        else
        {
          Message message =
              ERR_ATTR_SYNTAX_UNKNOWN_ORDERING_MATCHING_RULE.get(
                  implementation.getOrderingMatchingRule(),
                  implementation.getName());
          warnings.add(message);
        }
      }

      if(implementation.getSubstringMatchingRule() != null)
      {
        if(Schema.this.hasMatchingRule(
            implementation.getSubstringMatchingRule()))
        {
          substringMatchingRule = Schema.this.getMatchingRule(
              implementation.getSubstringMatchingRule());
        }
        else
        {
          Message message =
              ERR_ATTR_SYNTAX_UNKNOWN_SUBSTRING_MATCHING_RULE.get(
                  implementation.getSubstringMatchingRule(),
                  implementation.getName());
          warnings.add(message);
        }
      }

      if(implementation.getApproximateMatchingRule() != null)
      {
        if(Schema.this.hasMatchingRule(
            implementation.getApproximateMatchingRule()))
        {
          approximateMatchingRule = Schema.this.getMatchingRule(
              implementation.getApproximateMatchingRule());
        }
        else
        {
          Message message =
              ERR_ATTR_SYNTAX_UNKNOWN_APPROXIMATE_MATCHING_RULE.get(
                  implementation.getApproximateMatchingRule(),
                  implementation.getName());
          warnings.add(message);
        }
      }
    }

    @Override
    public boolean isHumanReadable() {
      return implementation.isHumanReadable();
    }

    @Override
    public boolean valueIsAcceptable(ByteSequence value,
                                     MessageBuilder invalidReason) {
      return implementation.valueIsAcceptable(Schema.this, value,
          invalidReason);
    }

    @Override
    protected CachingSyntax duplicate() {
      return new CachingSyntax(oid, description, extraProperties,
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

    @Override
    public MatchingRule getEqualityMatchingRule() {
      return substitute.getEqualityMatchingRule();
    }

    @Override
    public MatchingRule getOrderingMatchingRule() {
      return substitute.getOrderingMatchingRule();
    }

    @Override
    public MatchingRule getSubstringMatchingRule() {
      return substitute.getSubstringMatchingRule();
    }

    @Override
    public MatchingRule getApproximateMatchingRule() {
      return substitute.getApproximateMatchingRule();
    }

    @Override
    protected void validate(List<Message> warnings) throws SchemaException
    {
      try
      {
        // Get reference to the substitute syntax
        substitute = Schema.this.getSyntax(substituteOID);
      }
      catch(UnknownSchemaElementException e)
      {
        Message message = ERR_ATTR_SYNTAX_UNKNOWN_SUB_SYNTAX.get(
            oid, substituteOID);
        throw new SchemaException(message, e);
      }
    }

    @Override
    public boolean isHumanReadable() {
      return substitute.isHumanReadable();
    }

    @Override
    public boolean valueIsAcceptable(ByteSequence value,
                                     MessageBuilder invalidReason) {
      return substitute.valueIsAcceptable(value, invalidReason);
    }

    @Override
    protected SubstitutionSyntax duplicate() {
      return new SubstitutionSyntax(oid, description, extraProperties,
          substituteOID, definition);
    }
  }

  /**
   * This class provides an enumeration-based mechanism where a new syntax
   * and its corresponding matching rules can be created on-the-fly. An enum
   * syntax is an LDAPSyntaxDescriptionSyntax with X-ENUM extension.
   */
  protected final class EnumSyntax extends Syntax
  {
    private MatchingRule equalityMatchingRule;
    private MatchingRule orderingMatchingRule;
    private MatchingRule substringMatchingRule;
    private MatchingRule approximateMatchingRule;

    //Set of read-only enum entries.
    private final List<ByteSequence> entries;

    public EnumSyntax(String oid, String description,
                      Map<String, List<String>> extraProperties,
                      List<ByteSequence> entries, String definition)
    {
      super(oid, description, extraProperties, definition);
      this.entries = entries;
    }

    @Override
    public MatchingRule getEqualityMatchingRule() {
      return equalityMatchingRule;
    }

    @Override
    public MatchingRule getOrderingMatchingRule() {
      return orderingMatchingRule;
    }

    @Override
    public MatchingRule getSubstringMatchingRule() {
      return substringMatchingRule;
    }

    @Override
    public MatchingRule getApproximateMatchingRule() {
      return approximateMatchingRule;
    }

    @Override
    protected void validate(List<Message> warnings) throws SchemaException
    {
      MatchingRule rule;
      try
      {
        // Get references to the default matching rules
        rule = Schema.this.getMatchingRule(EMR_CASE_IGNORE_OID);
      }
      catch(UnknownSchemaElementException e)
      {
        Message message =
            ERR_ATTR_SYNTAX_UNKNOWN_EQUALITY_MATCHING_RULE.get(
                EMR_CASE_IGNORE_OID, oid);
        throw new SchemaException(message, e);
      }
      equalityMatchingRule = rule;

      try
      {
        orderingMatchingRule = Schema.this.getMatchingRule(
            OMR_OID_GENERIC_ENUM + "." + oid);
      }
      catch(UnknownSchemaElementException e)
      {
        Message message =
            ERR_ATTR_SYNTAX_UNKNOWN_ORDERING_MATCHING_RULE.get(
                OMR_OID_GENERIC_ENUM + "." + oid, oid);
        throw new SchemaException(message, e);
      }

      try
      {
        substringMatchingRule = Schema.this.getMatchingRule(SMR_CASE_IGNORE_OID);
      }
      catch(UnknownSchemaElementException e)
      {
        Message message =
            ERR_ATTR_SYNTAX_UNKNOWN_SUBSTRING_MATCHING_RULE.get(
                SMR_CASE_IGNORE_OID, oid);
        throw new SchemaException(message, e);
      }

      try
      {
        rule = Schema.this.getMatchingRule(AMR_DOUBLE_METAPHONE_OID);
      }
      catch(UnknownSchemaElementException e)
      {
        Message message =
            ERR_ATTR_SYNTAX_UNKNOWN_APPROXIMATE_MATCHING_RULE.get(
                AMR_DOUBLE_METAPHONE_OID, oid);
        throw new SchemaException(message, e);
      }
      approximateMatchingRule = rule;
    }

    @Override
    public boolean isHumanReadable() {
      return true;
    }

    @Override
    public boolean valueIsAcceptable(ByteSequence value,
                                     MessageBuilder invalidReason)
    {
      //The value is acceptable if it belongs to the set.
      boolean isAllowed = entries.contains(value);

      if(!isAllowed)
      {
        Message message = WARN_ATTR_SYNTAX_LDAPSYNTAX_ENUM_INVALID_VALUE.get(
            value.toString(), oid);
        invalidReason.append(message);
      }

      return isAllowed;
    }

    @Override
    protected Syntax duplicate() {
      return new EnumSyntax(oid, description, extraProperties, entries,
          definition);
    }
  }

  /**
   * This class provides a regex mechanism where a new syntax and its
   * corresponding matching rules can be created on-the-fly. A regex
   * syntax is an LDAPSyntaxDescriptionSyntax with X-PATTERN extension.
   */
  protected final class RegexSyntax extends Syntax
  {
    private MatchingRule equalityMatchingRule;
    private MatchingRule orderingMatchingRule;
    private MatchingRule substringMatchingRule;
    private MatchingRule approximateMatchingRule;

    // The Pattern associated with the regex.
    private final Pattern pattern;

    public RegexSyntax(String oid, String description,
                       Map<String, List<String>> extraProperties,
                       Pattern pattern, String definition)
    {
      super(oid, description, extraProperties, definition);
      this.pattern = pattern;
    }

    @Override
    public MatchingRule getEqualityMatchingRule() {
      return equalityMatchingRule;
    }

    @Override
    public MatchingRule getOrderingMatchingRule() {
      return orderingMatchingRule;
    }

    @Override
    public MatchingRule getSubstringMatchingRule() {
      return substringMatchingRule;
    }

    @Override
    public MatchingRule getApproximateMatchingRule() {
      return approximateMatchingRule;
    }

    @Override
    protected void validate(List<Message> warnings) throws SchemaException
    {
      MatchingRule rule;
      try
      {
        // Get references to the default matching rules
        rule = Schema.this.getMatchingRule(EMR_CASE_IGNORE_OID);
      }
      catch(UnknownSchemaElementException e)
      {
        Message message =
            ERR_ATTR_SYNTAX_UNKNOWN_EQUALITY_MATCHING_RULE.get(
                EMR_CASE_IGNORE_OID, oid);
        throw new SchemaException(message, e);
      }
      equalityMatchingRule = rule;

      try
      {
        orderingMatchingRule = Schema.this.getMatchingRule(OMR_CASE_IGNORE_OID);
      }
      catch(UnknownSchemaElementException e)
      {
        Message message =
            ERR_ATTR_SYNTAX_UNKNOWN_ORDERING_MATCHING_RULE.get(
                OMR_OID_GENERIC_ENUM + "." + oid, oid);
        throw new SchemaException(message, e);
      }

      try
      {
        substringMatchingRule =
            Schema.this.getMatchingRule(SMR_CASE_IGNORE_OID);
      }
      catch(UnknownSchemaElementException e)
      {
        Message message =
            ERR_ATTR_SYNTAX_UNKNOWN_SUBSTRING_MATCHING_RULE.get(
                SMR_CASE_IGNORE_OID, oid);
        throw new SchemaException(message, e);
      }

      try
      {
        rule = Schema.this.getMatchingRule(AMR_DOUBLE_METAPHONE_OID);
      }
      catch(UnknownSchemaElementException e)
      {
        Message message =
            ERR_ATTR_SYNTAX_UNKNOWN_APPROXIMATE_MATCHING_RULE.get(
                AMR_DOUBLE_METAPHONE_OID, oid);
        throw new SchemaException(message, e);
      }
      approximateMatchingRule = rule;
    }

    @Override
    public boolean isHumanReadable() {
      return true;
    }

    @Override
    public boolean valueIsAcceptable(ByteSequence value,
                                     MessageBuilder invalidReason)
    {
      String strValue = value.toString();
      boolean matches = pattern.matcher(strValue).matches();
      if(!matches)
      {
        Message message = WARN_ATTR_SYNTAX_LDAPSYNTAX_REGEX_INVALID_VALUE.get(
            strValue, pattern.pattern());
        invalidReason.append(message);
      }
      return matches;
    }

    @Override
    protected Syntax duplicate() {
      return new RegexSyntax(oid, description, extraProperties, pattern,
          definition);
    }
  }

  protected final class CachingDITContentRule extends DITContentRule
  {
    private ObjectClass structuralClass;
    private Set<ObjectClass> auxiliaryClasses = Collections.emptySet();
    private Set<AttributeType> optionalAttributes = Collections.emptySet();
    private Set<AttributeType> prohibitedAttributes = Collections.emptySet();
    private Set<AttributeType> requiredAttributes = Collections.emptySet();

    protected CachingDITContentRule(String structuralClassOID,
                                    List<String> names, String description,
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
    @Override
    public ObjectClass getStructuralClass()
    {
      return structuralClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<ObjectClass> getAuxiliaryClasses()
    {
      return auxiliaryClasses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<AttributeType> getRequiredAttributes()
    {
      return requiredAttributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<AttributeType> getOptionalAttributes()
    {
      return optionalAttributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<AttributeType> getProhibitedAttributes()
    {
      return prohibitedAttributes;
    }

    @Override
    protected void validate(List<Message> warnings) throws SchemaException
    {
      // Get the objectclass with the specified OID.  If it does not exist or is
      // not structural, then fail.
      if(structuralClassOID != null)
      {
        try
        {
          structuralClass = Schema.this.getObjectClass(structuralClassOID);
        }
        catch(UnknownSchemaElementException e)
        {
          Message message = ERR_ATTR_SYNTAX_DCR_UNKNOWN_STRUCTURAL_CLASS.get(
              definition, structuralClassOID);
          throw new SchemaException(message, e);
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
          try
          {
            objectClass = Schema.this.getObjectClass(oid);
          }
          catch(UnknownSchemaElementException e)
          {
            // This isn't good because it is an unknown auxiliary class.
            Message message = ERR_ATTR_SYNTAX_DCR_UNKNOWN_AUXILIARY_CLASS.get(
                definition, oid);
            throw new SchemaException(message, e);
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
          try
          {
            attributeType = Schema.this.getAttributeType(oid);
          }
          catch(UnknownSchemaElementException e)
          {
            // This isn't good because it means that the DIT content rule
            // requires an attribute type that we don't know anything about.
            Message message = ERR_ATTR_SYNTAX_DCR_UNKNOWN_REQUIRED_ATTR.get(
                definition, oid);
            throw new SchemaException(message, e);
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
          try
          {
            attributeType = Schema.this.getAttributeType(oid);
          }
          catch(UnknownSchemaElementException e)
          {
            // This isn't good because it means that the DIT content rule
            // requires an attribute type that we don't know anything about.
            Message message = ERR_ATTR_SYNTAX_DCR_UNKNOWN_OPTIONAL_ATTR.get(
                definition, oid);
            throw new SchemaException(message, e);
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
          try
          {
            attributeType = Schema.this.getAttributeType(oid);
          }
          catch(UnknownSchemaElementException e)
          {
            // This isn't good because it means that the DIT content rule
            // requires an attribute type that we don't know anything about.
            Message message = ERR_ATTR_SYNTAX_DCR_UNKNOWN_PROHIBITED_ATTR.get(
                definition, oid);
            throw new SchemaException(message, e);
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

    @Override
    protected CachingDITContentRule duplicate() {
      return new CachingDITContentRule(structuralClassOID, names, description,
          isObsolete, auxiliaryClassOIDs, optionalAttributeOIDs,
          prohibitedAttributeOIDs, requiredAttributeOIDs, extraProperties,
          definition);
    }
  }

  protected final class CachingDITStructureRule extends DITStructureRule
  {
    private NameForm nameForm;
    private Set<DITStructureRule> superiorRules = Collections.emptySet();

    protected CachingDITStructureRule(Integer ruleID, List<String> names,
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
    @Override
    public NameForm getNameForm()
    {
      return nameForm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<DITStructureRule> getSuperiorRules()
    {
      return superiorRules;
    }

    @Override
    protected void validate(List<Message> warnings) throws SchemaException
    {
      try
      {
        nameForm = Schema.this.getNameForm(nameFormOID);
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
            rule = Schema.this.getDITStructureRule(id);
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

    @Override
    protected CachingDITStructureRule duplicate() {
      return new CachingDITStructureRule(ruleID, names, description, isObsolete,
          nameFormOID, superiorRuleIDs, extraProperties, definition);
    }
  }

  protected final class CachingNameForm extends NameForm
  {
    private ObjectClass structuralClass;
    private Set<AttributeType> optionalAttributes = Collections.emptySet();
    private Set<AttributeType> requiredAttributes = Collections.emptySet();

    protected CachingNameForm(String oid, List<String> names,
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
    @Override
    public ObjectClass getStructuralClass()
    {
      return structuralClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<AttributeType> getRequiredAttributes()
    {
      return requiredAttributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<AttributeType> getOptionalAttributes()
    {
      return optionalAttributes;
    }

    @Override
    protected void validate(List<Message> warnings) throws SchemaException
    {
      try
      {
        structuralClass = Schema.this.getObjectClass(structuralClassOID);
      }
      catch(UnknownSchemaElementException e)
      {
        Message message =
            ERR_ATTR_SYNTAX_NAME_FORM_UNKNOWN_STRUCTURAL_CLASS.
                get(oid, structuralClassOID);
        throw new SchemaException(message, e);
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
        try
        {
          attributeType = Schema.this.getAttributeType(oid);
        }
        catch(UnknownSchemaElementException e)
        {
          // This isn't good because it means that the name form requires
          // an attribute type that we don't know anything about.
          Message message =
              ERR_ATTR_SYNTAX_NAME_FORM_UNKNOWN_REQUIRED_ATTR.
                  get(this.oid, oid);
          throw new SchemaException(message, e);
        }
        requiredAttributes.add(attributeType);
      }

      if(!optionalAttributeOIDs.isEmpty())
      {
        optionalAttributes =
            new HashSet<AttributeType>(optionalAttributeOIDs.size());
        for(String oid : optionalAttributeOIDs)
        {
          try
          {
            attributeType = Schema.this.getAttributeType(oid);
          }
          catch(UnknownSchemaElementException e)
          {
            // This isn't good because it means that the name form requires
            // an attribute type that we don't know anything about.
            Message message =
                ERR_ATTR_SYNTAX_NAME_FORM_UNKNOWN_OPTIONAL_ATTR.
                    get(this.oid, oid);
            throw new SchemaException(message, e);
          }
          optionalAttributes.add(attributeType);
        }
      }
    }

    @Override
    protected CachingNameForm duplicate() {
      return new CachingNameForm(oid, names, description, isObsolete,
          structuralClassOID, requiredAttributeOIDs, optionalAttributeOIDs,
          extraProperties, definition);
    }
  }

  protected final class CachingObjectClass extends ObjectClass
  {
    private Set<ObjectClass> superiorClasses = Collections.emptySet();
    private Set<AttributeType> declaredRequiredAttributes =
        Collections.emptySet();
    private Set<AttributeType> requiredAttributes = Collections.emptySet();
    private Set<AttributeType> declaredOptionalAttributes =
        Collections.emptySet();
    private Set<AttributeType> optionalAttributes = Collections.emptySet();
    private boolean validated = false;

    protected CachingObjectClass(String oid, List<String> names,
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

    @Override
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

    @Override
    public Iterable<ObjectClass> getSuperiorClasses() {
      return superiorClasses;
    }

    @Override
    public Iterable<AttributeType> getDeclaredRequiredAttributes() {
      return declaredRequiredAttributes;
    }

    @Override
    public Iterable<AttributeType> getRequiredAttributes() {
      return requiredAttributes;
    }

    @Override
    public Iterable<AttributeType> getOptionalAttributes() {
      return optionalAttributes;
    }

    @Override
    public Iterable<AttributeType> getDeclaredOptionalAttributes() {
      return declaredOptionalAttributes;
    }

    @Override
    public boolean isRequired(AttributeType attributeType) {
      return requiredAttributes.contains(attributeType);
    }

    @Override
    public boolean isOptional(AttributeType attributeType) {
      return optionalAttributes.contains(attributeType);
    }

    @Override
    public boolean isRequiredOrOptional(AttributeType attributeType) {
      return isRequired(attributeType) || isOptional(attributeType);
    }

    @Override
    protected void validate(List<Message> warnings) throws SchemaException
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
          try
          {
            superiorClass = getObjectClass(superClassOid);
          }
          catch(UnknownSchemaElementException e)
          {
            Message message =
                WARN_ATTR_SYNTAX_OBJECTCLASS_UNKNOWN_SUPERIOR_CLASS.
                    get(oid, superClassOid);
            throw new SchemaException(message, e);
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
          if(!derivesTop && superiorType == ObjectClassType.STRUCTURAL)
          {
            derivesTop = true;
          }

          // Validate superior object class so we can inherit its attributes.
          superiorClass.validate(warnings);

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

      if(!derivesTop)
      {
        derivesTop = isDescendantOf(getObjectClass("2.5.6.0"));
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
          try
          {
            attributeType = getAttributeType(requiredAttribute);
          }
          catch(UnknownSchemaElementException e)
          {
            // This isn't good because it means that the objectclass
            // requires an attribute type that we don't know anything about.
            Message message =
                WARN_ATTR_SYNTAX_OBJECTCLASS_UNKNOWN_REQUIRED_ATTR.
                    get(oid, requiredAttribute);
            throw new SchemaException(message, e);
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
          try
          {
            attributeType = getAttributeType(optionalAttribute);
          }
          catch(UnknownSchemaElementException e)
          {
            // This isn't good because it means that the objectclass
            // requires an attribute type that we don't know anything about.
            Message message =
                WARN_ATTR_SYNTAX_OBJECTCLASS_UNKNOWN_OPTIONAL_ATTR.
                    get(oid, optionalAttribute);
            throw new SchemaException(message, e);
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

    @Override
    protected CachingObjectClass duplicate() {
      return new CachingObjectClass(oid, names, description, isObsolete,
          superiorClassOIDs, requiredAttributeOIDs, optionalAttributeOIDs,
          objectClassType, extraProperties, definition);
    }
  }

  protected final class ExtensibleObjectClass extends ObjectClass
      implements Iterable<AttributeType>
  {
    protected ExtensibleObjectClass(String oid, List<String> names,
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

    @Override
    public boolean isDescendantOf(ObjectClass objectClass) {
      return objectClass.getOID().equals(TOP_OBJECTCLASS_OID);
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

    @Override
    public Iterable<ObjectClass> getSuperiorClasses() {
      return Collections.singleton(
          Schema.this.getObjectClass(TOP_OBJECTCLASS_OID));
    }

    @Override
    public Iterable<AttributeType> getDeclaredRequiredAttributes() {
      return Collections.emptySet();
    }

    @Override
    public Iterable<AttributeType> getRequiredAttributes() {
      return Collections.emptySet();
    }

    @Override
    public Iterable<AttributeType> getOptionalAttributes() {
      return this;
    }

    @Override
    public Iterable<AttributeType> getDeclaredOptionalAttributes() {
      return this;
    }

    @Override
    public boolean isRequired(AttributeType attributeType) {
      return false;
    }

    @Override
    public boolean isOptional(AttributeType attributeType) {
      return hasAttributeType(attributeType.getOID()) &&
          attributeType.getUsage() == AttributeUsage.USER_APPLICATIONS;
    }

    @Override
    public boolean isRequiredOrOptional(AttributeType attributeType) {
      return isOptional(attributeType);
    }

    @Override
    protected void validate(List<Message> warnings) throws SchemaException
    {
      // nothing to do
    }

    @Override
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

    attachments = new WeakHashMap<SchemaAttachment<?>, Object>();
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
    Syntax syntax = numericOID2Syntaxes.get(numericOID);
    if(syntax == null)
    {
      throw new UnknownSchemaElementException(
          WARN_SYNTAX_UNKNOWN.get(numericOID));
    }
    return syntax;
  }

  public boolean hasSyntax(String numericOID)
  {
    return numericOID2Syntaxes.containsKey(numericOID);
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
    List<AttributeType> attributes = name2AttributeTypes.get(
        StaticUtils.toLowerCase(oid));
    if(attributes != null)
    {
      if(attributes.size() == 1)
      {
        return attributes.get(0);
      }
      throw new UnknownSchemaElementException(
          WARN_ATTR_TYPE_AMBIGIOUS.get(oid));
    }
    throw new UnknownSchemaElementException(
        WARN_ATTR_TYPE_UNKNOWN.get(oid));
  }

  public boolean hasAttributeType(String oid)
  {
    if(numericOID2AttributeTypes.containsKey(oid))
    {
      return true;
    }
    List<AttributeType> attributes = name2AttributeTypes.get(
        StaticUtils.toLowerCase(oid));
    return attributes != null && attributes.size() == 1;
  }

  public List<AttributeType> getAttributeTypesByName(String lowerName)
  {
    List<AttributeType> attributes = name2AttributeTypes.get(
        StaticUtils.toLowerCase(lowerName));
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
    List<DITContentRule> rules = name2ContentRules.get(
        StaticUtils.toLowerCase(oid));
    if(rules != null)
    {
      if(rules.size() == 1)
      {
        return rules.get(0);
      }
      throw new UnknownSchemaElementException(
          WARN_DCR_AMBIGIOUS.get(oid));
    }
    throw new UnknownSchemaElementException(
        WARN_DCR_UNKNOWN.get(oid));
  }

  public boolean hasDITContentRule(String oid)
  {
    if(numericOID2ContentRules.containsKey(oid))
    {
      return true;
    }
    List<DITContentRule> rules = name2ContentRules.get(
        StaticUtils.toLowerCase(oid));
    return rules != null && rules.size() == 1;
  }

  public Collection<DITContentRule> getDITContentRulesByName(String lowerName)
  {
    List<DITContentRule> rules = name2ContentRules.get(
        StaticUtils.toLowerCase(lowerName));
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
    DITStructureRule rule = id2StructureRules.get(ruleID);
    if(rule == null)
    {
      throw new UnknownSchemaElementException(
          WARN_DSR_UNKNOWN.get(String.valueOf(ruleID)));
    }
    return rule;
  }

  public boolean hasDITStructureRule(int ruleID)
  {
    return id2StructureRules.containsKey(ruleID);
  }

  public Collection<DITStructureRule> getDITStructureRulesByName(
      String lowerName)
  {
    List<DITStructureRule> rules = name2StructureRules.get(
        StaticUtils.toLowerCase(lowerName));
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
    List<MatchingRule> rules = name2MatchingRules.get(
        StaticUtils.toLowerCase(oid));
    if(rules != null)
    {
      if(rules.size() == 1)
      {
        return rules.get(0);
      }
      throw new UnknownSchemaElementException(
          WARN_MR_AMBIGIOUS.get(oid));
    }
    throw new UnknownSchemaElementException(
        WARN_MR_UNKNOWN.get(oid));
  }

  public boolean hasMatchingRule(String oid)
  {
    if(numericOID2MatchingRules.containsKey(oid))
    {
      return true;
    }
    List<MatchingRule> rules = name2MatchingRules.get(
        StaticUtils.toLowerCase(oid));
    return rules != null && rules.size() == 1;
  }

  public Collection<MatchingRule> getMatchingRulesByName(String lowerName)
  {
    List<MatchingRule> rules = name2MatchingRules.get(
        StaticUtils.toLowerCase(lowerName));
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
    List<MatchingRuleUse> uses = name2MatchingRuleUses.get(
        StaticUtils.toLowerCase(oid));
    if(uses != null)
    {
      if(uses.size() == 1)
      {
        return uses.get(0);
      }
      throw new UnknownSchemaElementException(
          WARN_MRU_AMBIGIOUS.get(oid));
    }
    throw new UnknownSchemaElementException(
        WARN_MRU_UNKNOWN.get(oid));
  }

  public boolean hasMatchingRuleUse(String oid)
  {
    if(numericOID2MatchingRuleUses.containsKey(oid))
    {
      return true;
    }
    List<MatchingRuleUse> uses = name2MatchingRuleUses.get(
        StaticUtils.toLowerCase(oid));
    return uses != null && uses.size() == 1;
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
    List<MatchingRuleUse> rules = name2MatchingRuleUses.get(
        StaticUtils.toLowerCase(lowerName));
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
    List<NameForm> forms = name2NameForms.get(
        StaticUtils.toLowerCase(oid));
    if(forms != null)
    {
      if(forms.size() == 1)
      {
        return forms.get(0);
      }
      throw new UnknownSchemaElementException(
          WARN_NAMEFORM_AMBIGIOUS.get(oid));
    }
    throw new UnknownSchemaElementException(
        WARN_NAMEFORM_UNKNOWN.get(oid));
  }

  public boolean hasNameForm(String oid)
  {
    if(numericOID2NameForms.containsKey(oid))
    {
      return true;
    }
    List<NameForm> forms = name2NameForms.get(
        StaticUtils.toLowerCase(oid));
    return forms != null && forms.size() == 1;
  }

  public Collection<NameForm> getNameFormsByName(String lowerName)
  {
    List<NameForm> forms = name2NameForms.get(
        StaticUtils.toLowerCase(lowerName));
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
    List<ObjectClass> classes = name2ObjectClasses.get(
        StaticUtils.toLowerCase(oid));
    if(classes != null)
    {
      if(classes.size() == 1)
      {
        return classes.get(0);
      }
      throw new UnknownSchemaElementException(
          WARN_OBJECTCLASS_AMBIGIOUS.get(oid));
    }
    throw new UnknownSchemaElementException(
        WARN_OBJECTCLASS_UNKNOWN.get(oid));
  }

  public boolean hasObjectClass(String oid)
  {
    if(numericOID2ObjectClasses.containsKey(oid))
    {
      return true;
    }
    List<ObjectClass> classes = name2ObjectClasses.get(
        StaticUtils.toLowerCase(oid));
    return classes != null && classes.size() == 1;
  }

  public Collection<ObjectClass> getObjectClassesByName(String lowerName)
  {
    List<ObjectClass> classes = name2ObjectClasses.get(
        StaticUtils.toLowerCase(lowerName));
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

  void addSyntax(Syntax syntax , boolean overwrite)
      throws SchemaException
  {
    Syntax conflictingSyntax;
    synchronized(numericOID2Syntaxes)
    {
      if(hasSyntax(syntax.oid))
      {
        conflictingSyntax = getSyntax(syntax.oid);
        if(!overwrite)
        {
          Message message = ERR_SCHEMA_CONFLICTING_SYNTAX_OID.
              get(syntax.toString(), syntax.oid, conflictingSyntax.getOID());
          throw new SchemaException(message);
        }
        removeSyntax(conflictingSyntax);
      }
      numericOID2Syntaxes.put(syntax.oid, syntax);
    }
  }

  void addAttributeType(AttributeType attribute,
                        boolean overwrite)
      throws SchemaException
  {
    AttributeType conflictingAttribute;
    synchronized(numericOID2AttributeTypes)
    {
      if(hasAttributeType(attribute.oid))
      {
        conflictingAttribute = getAttributeType(attribute.oid);
        if(!overwrite)
        {
          Message message = ERR_SCHEMA_CONFLICTING_ATTRIBUTE_OID.
              get(attribute.getNameOrOID(), attribute.oid,
                  conflictingAttribute.getNameOrOID());
          throw new SchemaException(message);
        }
        removeAttributeType(conflictingAttribute);
      }

      numericOID2AttributeTypes.put(attribute.oid, attribute);
      for(String name : attribute.names)
      {
        String lowerName = StaticUtils.toLowerCase(name);
        List<AttributeType> attrs;
        if((attrs = name2AttributeTypes.get(lowerName)) == null)
        {
          name2AttributeTypes.put(lowerName,
              Collections.singletonList(attribute));
        }
        else if(attrs.size() == 1)
        {
          attrs = new ArrayList<AttributeType>(attrs);
          attrs.add(attribute);
          name2AttributeTypes.put(lowerName, attrs);
        }
        else
        {
          attrs.add(attribute);
        }
      }
    }
  }

  void addDITContentRule(DITContentRule rule, boolean overwrite)
      throws SchemaException
  {
    DITContentRule conflictingRule;
    synchronized(numericOID2ContentRules)
    {
      if(hasDITContentRule(rule.structuralClassOID))
      {
        conflictingRule = getDITContentRule(rule.structuralClassOID);
        if(!overwrite)
        {
          Message message = ERR_SCHEMA_CONFLICTING_DIT_CONTENT_RULE.
              get(rule.getNameOrOID(), rule.structuralClassOID,
                  conflictingRule.getNameOrOID());
          throw new SchemaException(message);
        }
        removeDITContentRule(conflictingRule);
      }

      numericOID2ContentRules.put(rule.structuralClassOID, rule);
      for(String name : rule.names)
      {
        String lowerName = StaticUtils.toLowerCase(name);
        List<DITContentRule> rules;
        if((rules = name2ContentRules.get(lowerName)) == null)
        {
          name2ContentRules.put(lowerName, Collections.singletonList(rule));
        }
        else if(rules.size() == 1)
        {
          rules = new ArrayList<DITContentRule>(rules);
          rules.add(rule);
          name2ContentRules.put(lowerName, rules);
        }
        else
        {
          rules.add(rule);
        }
      }
    }
  }

  void addDITStructureRule(DITStructureRule rule, boolean overwrite)
      throws SchemaException
  {
    DITStructureRule conflictingRule;
    synchronized(id2StructureRules)
    {
      if(hasDITStructureRule(rule.ruleID))
      {
        conflictingRule = getDITStructureRule(rule.ruleID);
        if(!overwrite)
        {
          Message message = ERR_SCHEMA_CONFLICTING_DIT_STRUCTURE_RULE_ID.
              get(rule.getNameOrRuleID(), rule.getRuleID(),
                  conflictingRule.getNameOrRuleID());
          throw new SchemaException(message);
        }
        removeDITStructureRule(conflictingRule);
      }

      id2StructureRules.put(rule.ruleID, rule);
      for(String name : rule.names)
      {
        String lowerName = StaticUtils.toLowerCase(name);
        List<DITStructureRule> rules;
        if((rules = name2StructureRules.get(lowerName)) == null)
        {
          name2StructureRules.put(lowerName, Collections.singletonList(rule));
        }
        else if(rules.size() == 1)
        {
          rules = new ArrayList<DITStructureRule>(rules);
          rules.add(rule);
          name2StructureRules.put(lowerName, rules);
        }
        else
        {
          rules.add(rule);
        }
      }
    }
  }

  void addMatchingRule(MatchingRule rule, boolean overwrite)
      throws SchemaException
  {
    MatchingRule conflictingRule;
    synchronized(numericOID2MatchingRules)
    {
      if(hasMatchingRule(rule.oid))
      {
        conflictingRule = getMatchingRule(rule.oid);
        if(!overwrite)
        {
          Message message = ERR_SCHEMA_CONFLICTING_MR_OID.
              get(rule.getNameOrOID(), rule.oid,
                  conflictingRule.getNameOrOID());
          throw new SchemaException(message);
        }
        removeMatchingRule(conflictingRule);
      }

      numericOID2MatchingRules.put(rule.oid, rule);
      for(String name : rule.names)
      {
        String lowerName = StaticUtils.toLowerCase(name);
        List<MatchingRule> rules;
        if((rules = name2MatchingRules.get(lowerName)) == null)
        {
          name2MatchingRules.put(lowerName, Collections.singletonList(rule));
        }
        else if(rules.size() == 1)
        {
          rules = new ArrayList<MatchingRule>(rules);
          rules.add(rule);
          name2MatchingRules.put(lowerName, rules);
        }
        else
        {
          rules.add(rule);
        }
      }
    }
  }

  void addMatchingRuleUse(MatchingRuleUse use, boolean overwrite)
      throws SchemaException
  {
    MatchingRuleUse conflictingUse;
    synchronized(numericOID2MatchingRuleUses)
    {
      if(hasMatchingRuleUse(use.oid))
      {
        conflictingUse = getMatchingRuleUse(use.oid);
        if(!overwrite)
        {
          Message message = ERR_SCHEMA_CONFLICTING_MATCHING_RULE_USE.
              get(use.getNameOrOID(), use.oid,
                  conflictingUse.getNameOrOID());
          throw new SchemaException(message);
        }
        removeMatchingRuleUse(conflictingUse);
      }

      numericOID2MatchingRuleUses.put(use.oid, use);
      for(String name : use.names)
      {
        String lowerName = StaticUtils.toLowerCase(name);
        List<MatchingRuleUse> uses;
        if((uses = name2MatchingRuleUses.get(lowerName)) == null)
        {
          name2MatchingRuleUses.put(lowerName, Collections.singletonList(use));
        }
        else if(uses.size() == 1)
        {
          uses = new ArrayList<MatchingRuleUse>(uses);
          uses.add(use);
          name2MatchingRuleUses.put(lowerName, uses);
        }
        else
        {
          uses.add(use);
        }
      }
    }
  }

  void addNameForm(NameForm form, boolean overwrite)
      throws SchemaException
  {
    NameForm conflictingForm;
    synchronized(numericOID2NameForms)
    {
      if(hasNameForm(form.oid))
      {
        conflictingForm = getNameForm(form.oid);
        if(!overwrite)
        {
          Message message = ERR_SCHEMA_CONFLICTING_NAME_FORM_OID.
              get(form.getNameOrOID(), form.oid,
                  conflictingForm.getNameOrOID());
          throw new SchemaException(message);
        }
        removeNameForm(conflictingForm);
      }

      numericOID2NameForms.put(form.oid, form);
      for(String name : form.names)
      {
        String lowerName = StaticUtils.toLowerCase(name);
        List<NameForm> forms;
        if((forms = name2NameForms.get(lowerName)) == null)
        {
          name2NameForms.put(lowerName, Collections.singletonList(form));
        }
        else if(forms.size() == 1)
        {
          forms = new ArrayList<NameForm>(forms);
          forms.add(form);
          name2NameForms.put(lowerName, forms);
        }
        else
        {
          forms.add(form);
        }
      }
    }
  }

  void addObjectClass(ObjectClass oc, boolean overwrite)
      throws SchemaException
  {
    ObjectClass conflictingOC;
    synchronized(numericOID2ObjectClasses)
    {

      if(hasObjectClass(oc.oid))
      {
        conflictingOC = getObjectClass(oc.oid);
        if(!overwrite)
        {
          Message message = ERR_SCHEMA_CONFLICTING_OBJECTCLASS_OID.
              get(oc.getNameOrOID(), oc.oid,
                  conflictingOC.getNameOrOID());
          throw new SchemaException(message);
        }
        removeObjectClass(conflictingOC);
      }

      numericOID2ObjectClasses.put(oc.oid, oc);
      for(String name : oc.names)
      {
        String lowerName = StaticUtils.toLowerCase(name);
        List<ObjectClass> classes;
        if((classes = name2ObjectClasses.get(lowerName)) == null)
        {
          name2ObjectClasses.put(lowerName, Collections.singletonList(oc));
        }
        else if(classes.size() == 1)
        {
          classes = new ArrayList<ObjectClass>(classes);
          classes.add(oc);
          name2ObjectClasses.put(lowerName, classes);
        }
        else
        {
          classes.add(oc);
        }
      }
    }
  }

  void removeSyntax(Syntax syntax)
  {
    synchronized(numericOID2Syntaxes)
    {
      numericOID2Syntaxes.remove(syntax.oid);
    }
  }

  void removeAttributeType(AttributeType attributeType)
  {
    synchronized(numericOID2AttributeTypes)
    {
      numericOID2AttributeTypes.remove(attributeType.oid);
      for(String name : attributeType.names)
      {
        String lowerName = StaticUtils.toLowerCase(name);
        List<AttributeType> attributes = name2AttributeTypes.get(lowerName);
        if(attributes != null && attributes.contains(attributeType))
        {
          if(attributes.size() <= 1)
          {
            name2AttributeTypes.remove(lowerName);
          }
          else
          {
            attributes.remove(attributeType);
          }
        }
      }
    }
  }

  void removeDITContentRule(DITContentRule rule)
  {
    synchronized(numericOID2ContentRules)
    {
      numericOID2ContentRules.remove(rule.structuralClassOID);
      for(String name : rule.names)
      {
        String lowerName = StaticUtils.toLowerCase(name);
        List<DITContentRule> rules = name2ContentRules.get(lowerName);
        if(rules != null && rules.contains(rule))
        {
          if(rules.size() <= 1)
          {
            name2AttributeTypes.remove(lowerName);
          }
          else
          {
            rules.remove(rule);
          }
        }
      }
    }
  }

  void removeDITStructureRule(DITStructureRule rule)
  {
    synchronized(id2StructureRules)
    {
      id2StructureRules.remove(rule.ruleID);
      for(String name : rule.names)
      {
        String lowerName = StaticUtils.toLowerCase(name);
        List<DITStructureRule> rules = name2StructureRules.get(lowerName);
        if(rules != null && rules.contains(rule))
        {
          if(rules.size() <= 1)
          {
            name2StructureRules.remove(lowerName);
          }
          else
          {
            rules.remove(rule);
          }
        }
      }
    }
  }

  void removeMatchingRule(MatchingRule rule)
  {
    synchronized(id2StructureRules)
    {
      numericOID2MatchingRules.remove(rule.oid);
      for(String name : rule.names)
      {
        String lowerName = StaticUtils.toLowerCase(name);
        List<MatchingRule> rules = name2MatchingRules.get(lowerName);
        if(rules != null && rules.contains(rule))
        {
          if(rules.size() <= 1)
          {
            name2MatchingRules.remove(lowerName);
          }
          else
          {
            rules.remove(rule);
          }
        }
      }
    }
  }

  void removeMatchingRuleUse(MatchingRuleUse use)
  {
    synchronized(numericOID2MatchingRuleUses)
    {
      numericOID2MatchingRuleUses.remove(use.oid);
      for(String name : use.names)
      {
        String lowerName = StaticUtils.toLowerCase(name);
        List<MatchingRuleUse> uses = name2MatchingRuleUses.get(lowerName);
        if(uses != null && uses.contains(use))
        {
          if(uses.size() <= 1)
          {
            name2MatchingRuleUses.remove(lowerName);
          }
          else
          {
            uses.remove(use);
          }
        }
      }
    }
  }

  void removeNameForm(NameForm form)
  {
    synchronized(numericOID2NameForms)
    {
      numericOID2NameForms.remove(form.oid);
      name2NameForms.remove(form.oid);
      for(String name : form.names)
      {
        String lowerName = StaticUtils.toLowerCase(name);
        List<NameForm> forms = name2NameForms.get(lowerName);
        if(forms != null && forms.contains(form))
        {
          if(forms.size() <= 1)
          {
            name2NameForms.remove(lowerName);
          }
          else
          {
            forms.remove(form);
          }
        }
      }
    }
  }

  void removeObjectClass(ObjectClass oc)
  {
    synchronized(numericOID2NameForms)
    {
      numericOID2ObjectClasses.remove(oc.oid);
      name2ObjectClasses.remove(oc.oid);
      for(String name : oc.names)
      {
        String lowerName = StaticUtils.toLowerCase(name);
        List<ObjectClass> classes = name2ObjectClasses.get(lowerName);
        if(classes != null && classes.contains(oc))
        {
          if(classes.size() <= 1)
          {
            name2ObjectClasses.remove(lowerName);
          }
          else
          {
            classes.remove(oc);
          }
        }
      }
    }
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
    List<Message> warnings = new LinkedList<Message>();
    // Verify all references in all elements
    for(Syntax syntax : numericOID2Syntaxes.values())
    {
      syntax.validate(warnings);
    }

    for(MatchingRule rule : numericOID2MatchingRules.values())
    {
      rule.validate(warnings);
    }

    for(AttributeType attribute : numericOID2AttributeTypes.values())
    {
      attribute.validate(warnings);
    }

    for(ObjectClass oc : numericOID2ObjectClasses.values())
    {
      oc.validate(warnings);
    }

    for(MatchingRuleUse use : numericOID2MatchingRuleUses.values())
    {
      use.validate(warnings);
    }

    for(NameForm form : numericOID2NameForms.values())
    {
      form.validate(warnings);

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
      rule.validate(warnings);
    }

    for(DITStructureRule rule : id2StructureRules.values())
    {
      rule.validate(warnings);

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

  protected void validate(List<Message> warnings)
  {
    // Verify all references in all elements
    for(Syntax syntax : numericOID2Syntaxes.values().toArray(new Syntax[0]))
    {
      try
      {
        syntax.validate(warnings);
      }
      catch(SchemaException e)
      {
        removeSyntax(syntax);
        warnings.add(ERR_SYNTAX_VALIDATION_FAIL.get(syntax.toString(),
            e.toString()));
      }
    }

    for(MatchingRule rule : numericOID2MatchingRules.values().toArray(
        new MatchingRule[0]))
    {
      try
      {
        rule.validate(warnings);
      }
      catch(SchemaException e)
      {
        removeMatchingRule(rule);
        warnings.add(ERR_MR_VALIDATION_FAIL.get(rule.toString(),
            e.toString()));
      }
    }

    for(AttributeType attribute : numericOID2AttributeTypes.values().toArray(
        new AttributeType[0]))
    {
      try
      {
        attribute.validate(warnings);
      }
      catch(SchemaException e)
      {
        removeAttributeType(attribute);
        warnings.add(ERR_ATTR_TYPE_VALIDATION_FAIL.get(attribute.toString(),
            e.toString()));
      }
    }

    for(ObjectClass oc : numericOID2ObjectClasses.values().toArray(
        new ObjectClass[0]))
    {
      try
      {
        oc.validate(warnings);
      }
      catch(SchemaException e)
      {
        removeObjectClass(oc);
        warnings.add(ERR_OC_VALIDATION_FAIL.get(oc.toString(),
            e.toString()));
      }
    }

    for(MatchingRuleUse use : numericOID2MatchingRuleUses.values().toArray(
        new MatchingRuleUse[0]))
    {
      try
      {
        use.validate(warnings);
      }
      catch(SchemaException e)
      {
        removeMatchingRuleUse(use);
        warnings.add(ERR_MRU_VALIDATION_FAIL.get(use.toString(),
            e.toString()));
      }
    }

    for(NameForm form : numericOID2NameForms.values().toArray(new NameForm[0]))
    {
      try
      {
        form.validate(warnings);

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
      catch(SchemaException e)
      {
        removeNameForm(form);
        warnings.add(ERR_NAMEFORM_VALIDATION_FAIL.get(form.toString(),
            e.toString()));
      }
    }

    for(DITContentRule rule : numericOID2ContentRules.values().toArray(
        new DITContentRule[0]))
    {
      try
      {
        rule.validate(warnings);
      }
      catch(SchemaException e)
      {
        removeDITContentRule(rule);
        warnings.add(ERR_DCR_VALIDATION_FAIL.get(rule.toString(),
            e.toString()));
      }
    }

    for(DITStructureRule rule : id2StructureRules.values().toArray(
        new DITStructureRule[0]))
    {
      try
      {
        rule.validate(warnings);

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
      catch(SchemaException e)
      {
        removeDITStructureRule(rule);
        warnings.add(ERR_DSR_VALIDATION_FAIL.get(rule.toString(),
            e.toString()));
      }
    }

  }

  @SuppressWarnings("unchecked")
  <T> T getAttachment(SchemaAttachment<T> attachment)
  {
    T o;
    synchronized (attachments)
    {
      o = (T) attachments.get(attachment);
      if (o == null)
      {
        o = attachment.initialValue();
        if (o != null)
        {
          attachments.put(attachment, o);
        }
      }
    }
    return o;
  }



  @SuppressWarnings("unchecked")
  <T> T removeAttachment(SchemaAttachment<T> attachment)
  {
    T o;
    synchronized (attachments)
    {
      o = (T) attachments.remove(attachment);
    }
    return o;
  }

  <T> void setAttachment(SchemaAttachment<T> attachment, T value)
  {
    synchronized (attachments)
    {
      attachments.put(attachment, value);
    }
  }
}

