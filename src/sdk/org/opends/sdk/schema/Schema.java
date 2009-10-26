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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.opends.messages.Message;
import org.opends.sdk.Attribute;
import org.opends.sdk.AttributeValueSequence;
import org.opends.sdk.Connection;
import org.opends.sdk.DecodeException;
import org.opends.sdk.Entry;
import org.opends.sdk.ErrorResultException;
import org.opends.sdk.SortedEntry;
import org.opends.sdk.responses.SearchResultEntry;
import org.opends.sdk.util.StaticUtils;
import org.opends.sdk.util.Validator;
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
public final class Schema
{
  private static Schema DEFAULT_SCHEMA = CoreSchema.instance();
  private static Schema CORE_SCHEMA = CoreSchema.instance();
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

  /**
   * Returns the core schema.
   *
   * @return The core schema.
   */
  public static Schema getCoreSchema()
  {
    return CORE_SCHEMA;
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
    Entry entry = new SortedEntry(result, Schema.getCoreSchema());

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

    return builder.toSchema();
  }

  public static Schema nonStrict(Schema schema)
  {
    if(schema.impl instanceof NonStrictImpl)
    {
      return schema;
    }
    return new Schema((StrictImpl)schema.impl);
  }

  private final Impl impl;

  Schema(Map<String, Syntax> numericOID2Syntaxes,
         Map<String, MatchingRule> numericOID2MatchingRules,
         Map<String, MatchingRuleUse> numericOID2MatchingRuleUses,
         Map<String, AttributeType> numericOID2AttributeTypes,
         Map<String, ObjectClass> numericOID2ObjectClasses,
         Map<String, NameForm> numericOID2NameForms,
         Map<String, DITContentRule> numericOID2ContentRules,
         Map<Integer, DITStructureRule> id2StructureRules,
         Map<String, List<MatchingRule>> name2MatchingRules,
         Map<String, List<MatchingRuleUse>> name2MatchingRuleUses,
         Map<String, List<AttributeType>> name2AttributeTypes,
         Map<String, List<ObjectClass>> name2ObjectClasses,
         Map<String, List<NameForm>> name2NameForms,
         Map<String, List<DITContentRule>> name2ContentRules,
         Map<String, List<DITStructureRule>> name2StructureRules,
         Map<String, List<NameForm>> objectClass2NameForms,
         Map<String, List<DITStructureRule>> nameForm2StructureRules,
         List<Message> warnings)
  {
    impl = new StrictImpl(numericOID2Syntaxes, numericOID2MatchingRules,
        numericOID2MatchingRuleUses, numericOID2AttributeTypes,
        numericOID2ObjectClasses, numericOID2NameForms, numericOID2ContentRules,
        id2StructureRules, name2MatchingRules, name2MatchingRuleUses,
        name2AttributeTypes, name2ObjectClasses, name2NameForms,
        name2ContentRules, name2StructureRules, objectClass2NameForms,
        nameForm2StructureRules, warnings);
  }

  Schema(StrictImpl strictImpl)
  {
    impl = new NonStrictImpl(strictImpl);
  }

  private static interface Impl
  {
    public Syntax getSyntax(String numericOID);

    public boolean hasSyntax(String numericOID);

    public Collection<Syntax> getSyntaxes();

    public AttributeType getAttributeType(String oid);

    public boolean hasAttributeType(String oid);

    public List<AttributeType> getAttributeTypesByName(String lowerName);

    public Collection<AttributeType> getAttributeTypes();

    public DITContentRule getDITContentRule(String oid);

    public boolean hasDITContentRule(String oid);

    public Collection<DITContentRule> getDITContentRulesByName(String lowerName);

    public Collection<DITContentRule> getDITContentRules();

    public DITStructureRule getDITStructureRule(int ruleID);

    public boolean hasDITStructureRule(int ruleID);

    public Collection<DITStructureRule> getDITStructureRulesByName(
        String lowerName);

    public Collection<DITStructureRule> getDITStuctureRules();

    public Collection<DITStructureRule> getDITStructureRulesByNameForm(
        NameForm nameForm);

    public MatchingRule getMatchingRule(String oid);

    public boolean hasMatchingRule(String oid);

    public Collection<MatchingRule> getMatchingRulesByName(String lowerName);

    public Collection<MatchingRule> getMatchingRules();
    public MatchingRuleUse getMatchingRuleUse(String oid);

    public boolean hasMatchingRuleUse(String oid);

    public MatchingRuleUse getMatchingRuleUse(MatchingRule matchingRule);

    public Collection<MatchingRuleUse> getMatchingRuleUsesByName(
        String lowerName);

    public Collection<MatchingRuleUse> getMatchingRuleUses();

    public NameForm getNameForm(String oid);

    public boolean hasNameForm(String oid);

    public Collection<NameForm> getNameFormsByName(String lowerName);

    public Collection<NameForm> getNameForms();

    public Collection<NameForm> getNameFormByObjectClass(
        ObjectClass structuralClass);

    public ObjectClass getObjectClass(String oid);

    public boolean hasObjectClass(String oid);

    public Collection<ObjectClass> getObjectClassesByName(String lowerName);

    public Collection<ObjectClass> getObjectClasses();

    public Collection<Message> getWarnings();

    public boolean isStrict();

    public <T> T getAttachment(SchemaAttachment<T> attachment);



    public <T> T removeAttachment(SchemaAttachment<T> attachment);

    public <T> void setAttachment(SchemaAttachment<T> attachment, T value);
  }

  private static class NonStrictImpl implements Impl
  {
    private final Impl strictImpl;

    private NonStrictImpl(Impl strictImpl) {
      this.strictImpl = strictImpl;
    }

    public Syntax getSyntax(String numericOID) {
      return strictImpl.getSyntax(numericOID);
    }

    public boolean hasSyntax(String numericOID) {
      return strictImpl.hasSyntax(numericOID);
    }

    public Collection<Syntax> getSyntaxes() {
      return strictImpl.getSyntaxes();
    }

    public AttributeType getAttributeType(String oid) {
      if(!strictImpl.hasAttributeType(oid))
      {
        // Construct an placeholder attribute type with the given name, the
        // default matching rule, and the default syntax. The OID of the
        // attribute will be an OID alias with "-oid" appended to the given
        // name.

        return new AttributeType(oid + "-oid",
            Collections.singletonList(oid), "",
            getMatchingRule(SchemaBuilder.getDefaultMatchingRule()),
            getSyntax(SchemaBuilder.getDefaultSyntax()));
      }
      return strictImpl.getAttributeType(oid);
    }

    public boolean hasAttributeType(String oid) {
      return strictImpl.hasAttributeType(oid);
    }

    public List<AttributeType> getAttributeTypesByName(String lowerName) {
      return strictImpl.getAttributeTypesByName(lowerName);
    }

    public Collection<AttributeType> getAttributeTypes() {
      return strictImpl.getAttributeTypes();
    }

    public DITContentRule getDITContentRule(String oid) {
      return strictImpl.getDITContentRule(oid);
    }

    public boolean hasDITContentRule(String oid) {
      return strictImpl.hasDITContentRule(oid);
    }

    public Collection<DITContentRule> getDITContentRulesByName(String lowerName) {
      return strictImpl.getDITContentRulesByName(lowerName);
    }

    public Collection<DITContentRule> getDITContentRules() {
      return strictImpl.getDITContentRules();
    }

    public DITStructureRule getDITStructureRule(int ruleID) {
      return strictImpl.getDITStructureRule(ruleID);
    }

    public boolean hasDITStructureRule(int ruleID) {
      return strictImpl.hasDITStructureRule(ruleID);
    }

    public Collection<DITStructureRule> getDITStructureRulesByName(String lowerName) {
      return strictImpl.getDITStructureRulesByName(lowerName);
    }

    public Collection<DITStructureRule> getDITStuctureRules() {
      return strictImpl.getDITStuctureRules();
    }

    public Collection<DITStructureRule> getDITStructureRulesByNameForm(NameForm nameForm) {
      return strictImpl.getDITStructureRulesByNameForm(nameForm);
    }

    public MatchingRule getMatchingRule(String oid) {
      return strictImpl.getMatchingRule(oid);
    }

    public boolean hasMatchingRule(String oid) {
      return strictImpl.hasMatchingRule(oid);
    }

    public Collection<MatchingRule> getMatchingRulesByName(String lowerName) {
      return strictImpl.getMatchingRulesByName(lowerName);
    }

    public Collection<MatchingRule> getMatchingRules() {
      return strictImpl.getMatchingRules();
    }

    public MatchingRuleUse getMatchingRuleUse(String oid) {
      return strictImpl.getMatchingRuleUse(oid);
    }

    public boolean hasMatchingRuleUse(String oid) {
      return strictImpl.hasMatchingRuleUse(oid);
    }

    public MatchingRuleUse getMatchingRuleUse(MatchingRule matchingRule) {
      return strictImpl.getMatchingRuleUse(matchingRule);
    }

    public Collection<MatchingRuleUse> getMatchingRuleUsesByName(String lowerName) {
      return strictImpl.getMatchingRuleUsesByName(lowerName);
    }

    public Collection<MatchingRuleUse> getMatchingRuleUses() {
      return strictImpl.getMatchingRuleUses();
    }

    public NameForm getNameForm(String oid) {
      return strictImpl.getNameForm(oid);
    }

    public boolean hasNameForm(String oid) {
      return strictImpl.hasNameForm(oid);
    }

    public Collection<NameForm> getNameFormsByName(String lowerName) {
      return strictImpl.getNameFormsByName(lowerName);
    }

    public Collection<NameForm> getNameForms() {
      return strictImpl.getNameForms();
    }

    public Collection<NameForm> getNameFormByObjectClass(ObjectClass structuralClass) {
      return strictImpl.getNameFormByObjectClass(structuralClass);
    }

    public ObjectClass getObjectClass(String oid) {
      return strictImpl.getObjectClass(oid);
    }

    public boolean hasObjectClass(String oid) {
      return strictImpl.hasObjectClass(oid);
    }

    public Collection<ObjectClass> getObjectClassesByName(String lowerName) {
      return strictImpl.getObjectClassesByName(lowerName);
    }

    public Collection<ObjectClass> getObjectClasses() {
      return strictImpl.getObjectClasses();
    }

    public Collection<Message> getWarnings()
    {
      return strictImpl.getWarnings();
    }

    public boolean isStrict()
    {
      return false;
    }

    public <T> T getAttachment(SchemaAttachment<T> attachment) {
      return strictImpl.getAttachment(attachment);
    }

    public <T> T removeAttachment(SchemaAttachment<T> attachment) {
      return strictImpl.removeAttachment(attachment);
    }

    public <T> void setAttachment(SchemaAttachment<T> attachment, T value) {
      strictImpl.setAttachment(attachment, value);
    }
  }

  private static class StrictImpl implements Impl
  {
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

    private final List<Message> warnings;

    private final Map<SchemaAttachment<?>, Object> attachments;

    private StrictImpl(
        Map<String, Syntax> numericOID2Syntaxes,
        Map<String, MatchingRule> numericOID2MatchingRules,
        Map<String, MatchingRuleUse> numericOID2MatchingRuleUses,
        Map<String, AttributeType> numericOID2AttributeTypes,
        Map<String, ObjectClass> numericOID2ObjectClasses,
        Map<String, NameForm> numericOID2NameForms,
        Map<String, DITContentRule> numericOID2ContentRules,
        Map<Integer, DITStructureRule> id2StructureRules,
        Map<String, List<MatchingRule>> name2MatchingRules,
        Map<String, List<MatchingRuleUse>> name2MatchingRuleUses,
        Map<String, List<AttributeType>> name2AttributeTypes,
        Map<String, List<ObjectClass>> name2ObjectClasses,
        Map<String, List<NameForm>> name2NameForms,
        Map<String, List<DITContentRule>> name2ContentRules,
        Map<String, List<DITStructureRule>> name2StructureRules,
        Map<String, List<NameForm>> objectClass2NameForms,
        Map<String, List<DITStructureRule>> nameForm2StructureRules,
        List<Message> warnings) {
      this.numericOID2Syntaxes =
          Collections.unmodifiableMap(numericOID2Syntaxes);
      this.numericOID2MatchingRules =
          Collections.unmodifiableMap(numericOID2MatchingRules);
      this.numericOID2MatchingRuleUses =
          Collections.unmodifiableMap(numericOID2MatchingRuleUses);
      this.numericOID2AttributeTypes =
          Collections.unmodifiableMap(numericOID2AttributeTypes);
      this.numericOID2ObjectClasses =
          Collections.unmodifiableMap(numericOID2ObjectClasses);
      this.numericOID2NameForms =
          Collections.unmodifiableMap(numericOID2NameForms);
      this.numericOID2ContentRules =
          Collections.unmodifiableMap(numericOID2ContentRules);
      this.id2StructureRules =
          Collections.unmodifiableMap(id2StructureRules);
      this.name2MatchingRules =
          Collections.unmodifiableMap(name2MatchingRules);
      this.name2MatchingRuleUses =
          Collections.unmodifiableMap(name2MatchingRuleUses);
      this.name2AttributeTypes =
          Collections.unmodifiableMap(name2AttributeTypes);
      this.name2ObjectClasses =
          Collections.unmodifiableMap(name2ObjectClasses);
      this.name2NameForms =
          Collections.unmodifiableMap(name2NameForms);
      this.name2ContentRules =
          Collections.unmodifiableMap(name2ContentRules);
      this.name2StructureRules =
          Collections.unmodifiableMap(name2StructureRules);
      this.objectClass2NameForms =
          Collections.unmodifiableMap(objectClass2NameForms);
      this.nameForm2StructureRules =
          Collections.unmodifiableMap(nameForm2StructureRules);
      this.warnings = Collections.unmodifiableList(warnings);

      attachments = new WeakHashMap<SchemaAttachment<?>, Object>();
    }


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
      return numericOID2Syntaxes.values();
    }

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
        return rules;
      }
    }

    public Collection<DITContentRule> getDITContentRules()
    {
      return numericOID2ContentRules.values();
    }

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
        return rules;
      }
    }

    public Collection<DITStructureRule> getDITStuctureRules()
    {
      return id2StructureRules.values();
    }

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
        return rules;
      }
    }

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
        return rules;
      }
    }

    public Collection<MatchingRule> getMatchingRules()
    {
      return numericOID2MatchingRules.values();
    }

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
        return rules;
      }
    }

    public Collection<MatchingRuleUse> getMatchingRuleUses()
    {
      return numericOID2MatchingRuleUses.values();
    }

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
        return forms;
      }
    }

    public Collection<NameForm> getNameForms()
    {
      return numericOID2NameForms.values();
    }

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
        return forms;
      }
    }

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
        return classes;
      }
    }

    public Collection<ObjectClass> getObjectClasses()
    {
      return numericOID2ObjectClasses.values();
    }

    public Collection<Message> getWarnings()
    {
      return warnings;
    }

    public boolean isStrict()
    {
      return true;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttachment(SchemaAttachment<T> attachment)
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
    public <T> T removeAttachment(SchemaAttachment<T> attachment)
    {
      T o;
      synchronized (attachments)
      {
        o = (T) attachments.remove(attachment);
      }
      return o;
    }

    public <T> void setAttachment(SchemaAttachment<T> attachment, T value)
    {
      synchronized (attachments)
      {
        attachments.put(attachment, value);
      }
    }
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
    return impl.getSyntax(numericOID);
  }

  public boolean hasSyntax(String numericOID)
  {
    return impl.hasSyntax(numericOID);
  }

  public Collection<Syntax> getSyntaxes()
  {
    return impl.getSyntaxes();
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
    return impl.getAttributeType(oid);
  }

  public boolean hasAttributeType(String oid)
  {
    return impl.hasAttributeType(oid);
  }

  public List<AttributeType> getAttributeTypesByName(String lowerName)
  {
    return impl.getAttributeTypesByName(lowerName);
  }

  public Collection<AttributeType> getAttributeTypes()
  {
    return impl.getAttributeTypes();
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
    return impl.getDITContentRule(oid);
  }

  public boolean hasDITContentRule(String oid)
  {
    return impl.hasDITContentRule(oid);
  }

  public Collection<DITContentRule> getDITContentRulesByName(String lowerName)
  {
    return impl.getDITContentRulesByName(lowerName);
  }

  public Collection<DITContentRule> getDITContentRules()
  {
    return impl.getDITContentRules();
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
    return impl.getDITStructureRule(ruleID);
  }

  public boolean hasDITStructureRule(int ruleID)
  {
    return impl.hasDITStructureRule(ruleID);
  }

  public Collection<DITStructureRule> getDITStructureRulesByName(
      String lowerName)
  {
    return impl.getDITStructureRulesByName(lowerName);
  }

  public Collection<DITStructureRule> getDITStuctureRules()
  {
    return impl.getDITStuctureRules();
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
    return impl.getDITStructureRulesByNameForm(nameForm);
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
    return impl.getMatchingRule(oid);
  }

  public boolean hasMatchingRule(String oid)
  {
    return impl.hasMatchingRule(oid);
  }

  public Collection<MatchingRule> getMatchingRulesByName(String lowerName)
  {
    return impl.getMatchingRulesByName(lowerName);
  }

  public Collection<MatchingRule> getMatchingRules()
  {
    return impl.getMatchingRules();
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
    return impl.getMatchingRuleUse(oid);
  }

  public boolean hasMatchingRuleUse(String oid)
  {
    return impl.hasMatchingRuleUse(oid);
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
    return getMatchingRuleUse(matchingRule.getOID());
  }

  public Collection<MatchingRuleUse> getMatchingRuleUsesByName(String lowerName)
  {
    return impl.getMatchingRuleUsesByName(lowerName);
  }

  public Collection<MatchingRuleUse> getMatchingRuleUses()
  {
    return impl.getMatchingRuleUses();
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
    return impl.getNameForm(oid);
  }

  public boolean hasNameForm(String oid)
  {
    return impl.hasNameForm(oid);
  }

  public Collection<NameForm> getNameFormsByName(String lowerName)
  {
    return impl.getNameFormsByName(lowerName);
  }

  public Collection<NameForm> getNameForms()
  {
    return impl.getNameForms();
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
    return impl.getNameFormByObjectClass(structuralClass);
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
    return impl.getObjectClass(oid);
  }

  public boolean hasObjectClass(String oid)
  {
    return impl.hasObjectClass(oid);
  }

  public Collection<ObjectClass> getObjectClassesByName(String lowerName)
  {
    return impl.getObjectClassesByName(lowerName);
  }

  public Collection<ObjectClass> getObjectClasses()
  {
    return impl.getObjectClasses();
  }

  public Collection<Message> getWarnings()
  {
    return impl.getWarnings();
  }

  /**
   * Indicates whether this schema is strict. A strict schema will not
   * create default object classes, attribute types, and syntaxes on
   * demand.
   *
   * @return {@code true} if this schema is strict.
   */
  public boolean isStrict()
  {
    return impl.isStrict();
  }

  @SuppressWarnings("unchecked")
  public <T> T getAttachment(SchemaAttachment<T> attachment)
  {
    return impl.getAttachment(attachment);
  }

  @SuppressWarnings("unchecked")
  public <T> T removeAttachment(SchemaAttachment<T> attachment)
  {
    return impl.removeAttachment(attachment);
  }

  public <T> void setAttachment(SchemaAttachment<T> attachment, T value)
  {
    impl.setAttachment(attachment, value);
  }
}

