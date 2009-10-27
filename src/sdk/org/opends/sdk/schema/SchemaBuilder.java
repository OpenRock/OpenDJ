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



import static org.opends.messages.CoreMessages.*;
import static org.opends.messages.SchemaMessages.*;
import static org.opends.sdk.schema.SchemaConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.opends.messages.Message;
import org.opends.sdk.DecodeException;
import org.opends.sdk.util.StaticUtils;
import org.opends.sdk.util.SubstringReader;
import org.opends.sdk.util.Validator;



/**
 * Schema builders should be used for incremental construction of new
 * schemas.
 */
public final class SchemaBuilder
{
  private static final String DEFAULT_SYNTAX = SYNTAX_OCTET_STRING_OID;
  private static final String DEFAULT_MATCHING_RULE =
      EMR_OCTET_STRING_OID;



  public static SchemaBuilder buildFrom(Schema schema)
  {
    return new SchemaBuilder(schema);
  }



  public static SchemaBuilder buildFromCore()
  {
    return new SchemaBuilder(Schema.getCoreSchema());
  }



  public static String getDefaultMatchingRule()
  {
    return DEFAULT_MATCHING_RULE;
  }



  public static String getDefaultSyntax()
  {
    return DEFAULT_SYNTAX;
  }

  private Map<String, Syntax> numericOID2Syntaxes;
  private Map<String, MatchingRule> numericOID2MatchingRules;
  private Map<String, MatchingRuleUse> numericOID2MatchingRuleUses;
  private Map<String, AttributeType> numericOID2AttributeTypes;

  private Map<String, ObjectClass> numericOID2ObjectClasses;
  private Map<String, NameForm> numericOID2NameForms;
  private Map<String, DITContentRule> numericOID2ContentRules;
  private Map<Integer, DITStructureRule> id2StructureRules;
  private Map<String, List<MatchingRule>> name2MatchingRules;
  private Map<String, List<MatchingRuleUse>> name2MatchingRuleUses;
  private Map<String, List<AttributeType>> name2AttributeTypes;

  private Map<String, List<ObjectClass>> name2ObjectClasses;
  private Map<String, List<NameForm>> name2NameForms;

  private Map<String, List<DITContentRule>> name2ContentRules;

  private Map<String, List<DITStructureRule>> name2StructureRules;

  private Map<String, List<NameForm>> objectClass2NameForms;

  private Map<String, List<DITStructureRule>> nameForm2StructureRules;

  private List<Message> warnings;

  private Schema schema;



  SchemaBuilder()
  {
    initBuilder();
  }



  SchemaBuilder(Schema schema)
  {
    Validator.ensureNotNull(schema);
    initBuilder();
    try
    {
      for (final Syntax syntax : schema.getSyntaxes())
      {
        addSyntax(syntax.duplicate(), false);
      }

      for (final MatchingRule matchingRule : schema.getMatchingRules())
      {
        addMatchingRule(matchingRule.duplicate(), false);
      }

      for (final MatchingRuleUse matchingRuleUse : schema
          .getMatchingRuleUses())
      {
        addMatchingRuleUse(matchingRuleUse.duplicate(), false);
      }

      for (final AttributeType attributeType : schema
          .getAttributeTypes())
      {
        addAttributeType(attributeType.duplicate(), false);
      }

      for (final ObjectClass objectClass : schema.getObjectClasses())
      {
        addObjectClass(objectClass.duplicate(), false);
      }

      for (final NameForm nameForm : schema.getNameForms())
      {
        addNameForm(nameForm.duplicate(), false);
      }

      for (final DITContentRule contentRule : schema
          .getDITContentRules())
      {
        addDITContentRule(contentRule.duplicate(), false);
      }

      for (final DITStructureRule structureRule : schema
          .getDITStuctureRules())
      {
        addDITStructureRule(structureRule.duplicate(), false);
      }
    }
    catch (final SchemaException se)
    {
      throw new RuntimeException(se);
    }
  }



  public void addAttributeType(String definition, boolean overwrite)
      throws DecodeException, SchemaException
  {
    Validator.ensureNotNull(definition);
    final SubstringReader reader = new SubstringReader(definition);

    // We'll do this a character at a time. First, skip over any leading
    // whitespace.
    reader.skipWhitespaces();

    if (reader.remaining() <= 0)
    {
      // This means that the definition was empty or contained only
      // whitespace. That is illegal.
      final Message message =
          ERR_ATTR_SYNTAX_ATTRTYPE_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }

    // The next character must be an open parenthesis. If it is not,
    // then that is an error.
    final char c = reader.read();
    if (c != '(')
    {
      final Message message =
          ERR_ATTR_SYNTAX_ATTRTYPE_EXPECTED_OPEN_PARENTHESIS.get(
              definition, (reader.pos() - 1), String.valueOf(c));
      throw new DecodeException(message);
    }

    // Skip over any spaces immediately following the opening
    // parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    final String oid = SchemaUtils.readOID(reader);

    List<String> names = Collections.emptyList();
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

    // At this point, we should have a pretty specific syntax that
    // describes what may come next, but some of the components are
    // optional and it would be pretty easy to put something in the
    // wrong order, so we will be very flexible about what we can
    // accept. Just look at the next token, figure out what it is and
    // how to treat what comes after it, then repeat until we get to the
    // end of the definition. But before we start, set default values
    // for everything else we might need to know.
    while (true)
    {
      final String tokenName = SchemaUtils.readTokenName(reader);

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
        // This specifies the description for the attribute type. It is
        // an arbitrary string of characters enclosed in single quotes.
        description = SchemaUtils.readQuotedString(reader);
      }
      else if (tokenName.equalsIgnoreCase("obsolete"))
      {
        // This indicates whether the attribute type should be
        // considered obsolete. We do not need to do any more parsing
        // for this token.
        isObsolete = true;
      }
      else if (tokenName.equalsIgnoreCase("sup"))
      {
        // This specifies the name or OID of the superior attribute type
        // from which this attribute type should inherit its properties.
        superiorType = SchemaUtils.readOID(reader);
      }
      else if (tokenName.equalsIgnoreCase("equality"))
      {
        // This specifies the name or OID of the equality matching rule
        // to use for this attribute type.
        equalityMatchingRule = SchemaUtils.readOID(reader);
      }
      else if (tokenName.equalsIgnoreCase("ordering"))
      {
        // This specifies the name or OID of the ordering matching rule
        // to use for this attribute type.
        orderingMatchingRule = SchemaUtils.readOID(reader);
      }
      else if (tokenName.equalsIgnoreCase("substr"))
      {
        // This specifies the name or OID of the substring matching rule
        // to use for this attribute type.
        substringMatchingRule = SchemaUtils.readOID(reader);
      }
      else if (tokenName.equalsIgnoreCase("syntax"))
      {
        // This specifies the numeric OID of the syntax for this
        // matching rule. It may optionally be immediately followed by
        // an open curly brace, an integer definition, and a close curly
        // brace to suggest the minimum number of characters that should
        // be allowed in values of that type. This implementation will
        // ignore any such length because it does not impose any
        // practical limit on the length of attribute values.
        syntax = SchemaUtils.readOIDLen(reader);
      }
      else if (tokenName.equalsIgnoreCase("single-definition"))
      {
        // This indicates that attributes of this type are allowed to
        // have at most one definition. We do not need any more parsing
        // for this token.
        isSingleValue = true;
      }
      else if (tokenName.equalsIgnoreCase("single-value"))
      {
        // This indicates that attributes of this type are allowed to
        // have at most one value. We do not need any more parsing for
        // this token.
        isSingleValue = true;
      }
      else if (tokenName.equalsIgnoreCase("collective"))
      {
        // This indicates that attributes of this type are collective
        // (i.e., have their values generated dynamically in some way).
        // We do not need any more parsing for this token.
        isCollective = true;
      }
      else if (tokenName.equalsIgnoreCase("no-user-modification"))
      {
        // This indicates that the values of attributes of this type are
        // not to be modified by end users. We do not need any more
        // parsing for this token.
        isNoUserModification = true;
      }
      else if (tokenName.equalsIgnoreCase("usage"))
      {
        // This specifies the usage string for this attribute type. It
        // should be followed by one of the strings "userApplications",
        // "directoryOperation", "distributedOperation", or
        // "dSAOperation".
        int length = 0;

        reader.skipWhitespaces();
        reader.mark();

        while (reader.read() != ' ')
        {
          length++;
        }

        reader.reset();
        final String usageStr = reader.read(length);
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
          final Message message =
              WARN_ATTR_SYNTAX_ATTRTYPE_INVALID_ATTRIBUTE_USAGE.get(
                  String.valueOf(oid), usageStr);
          throw new DecodeException(message);
        }
      }
      else if (tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed
        // by either a single definition in single quotes or an open
        // parenthesis followed by one or more values in single quotes
        // separated by spaces followed by a close parenthesis.
        if (extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName, SchemaUtils
            .readExtensions(reader));
      }
      else
      {
        final Message message =
            ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
      }
    }

    final List<String> approxRules =
        extraProperties.get(SCHEMA_PROPERTY_APPROX_RULE);
    if (approxRules != null && !approxRules.isEmpty())
    {
      approximateMatchingRule = approxRules.get(0);
    }

    final AttributeType attrType =
        new AttributeType(oid, names, description, isObsolete,
            superiorType, equalityMatchingRule, orderingMatchingRule,
            substringMatchingRule, approximateMatchingRule, syntax,
            isSingleValue, isCollective, isNoUserModification,
            attributeUsage, extraProperties, definition);

    addAttributeType(attrType, overwrite);
  }



  public void addAttributeType(String oid, List<String> names,
      String description, boolean obsolete, String superiorType,
      String equalityMatchingRule, String orderingMatchingRule,
      String substringMatchingRule, String approximateMatchingRule,
      String syntax, boolean singleValue, boolean collective,
      boolean noUserModification, AttributeUsage attributeUsage,
      Map<String, List<String>> extraProperties, boolean overwrite)
      throws SchemaException
  {
    final AttributeType attrType =
        new AttributeType(oid, names, description, obsolete,
            superiorType, equalityMatchingRule, orderingMatchingRule,
            substringMatchingRule, approximateMatchingRule, syntax,
            singleValue, collective, noUserModification,
            attributeUsage, extraProperties, null);
    addAttributeType(attrType, overwrite);
  }



  public void addDITContentRule(String definition, boolean overwrite)
      throws DecodeException, SchemaException
  {
    Validator.ensureNotNull(definition);
    final SubstringReader reader = new SubstringReader(definition);

    // We'll do this a character at a time. First, skip over any leading
    // whitespace.
    reader.skipWhitespaces();

    if (reader.remaining() <= 0)
    {
      // This means that the value was empty or contained only
      // whitespace. That is illegal.
      final Message message = ERR_ATTR_SYNTAX_DCR_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }

    // The next character must be an open parenthesis. If it is not,
    // then that is an error.
    final char c = reader.read();
    if (c != '(')
    {
      final Message message =
          ERR_ATTR_SYNTAX_DCR_EXPECTED_OPEN_PARENTHESIS.get(definition,
              (reader.pos() - 1), String.valueOf(c));
      throw new DecodeException(message);
    }

    // Skip over any spaces immediately following the opening
    // parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    final String structuralClass = SchemaUtils.readOID(reader);

    List<String> names = Collections.emptyList();
    String description = "".intern();
    boolean isObsolete = false;
    Set<String> auxiliaryClasses = Collections.emptySet();
    Set<String> optionalAttributes = Collections.emptySet();
    Set<String> prohibitedAttributes = Collections.emptySet();
    Set<String> requiredAttributes = Collections.emptySet();
    Map<String, List<String>> extraProperties = Collections.emptyMap();

    // At this point, we should have a pretty specific syntax that
    // describes what may come next, but some of the components are
    // optional and it would be pretty easy to put something in the
    // wrong order, so we will be very flexible about what we can
    // accept. Just look at the next token, figure out what it is and
    // how to treat what comes after it, then repeat until we get to the
    // end of the value. But before we start, set default values for
    // everything else we might need to know.
    while (true)
    {
      final String tokenName = SchemaUtils.readTokenName(reader);

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
        // This specifies the description for the attribute type. It is
        // an arbitrary string of characters enclosed in single quotes.
        description = SchemaUtils.readQuotedString(reader);
      }
      else if (tokenName.equalsIgnoreCase("obsolete"))
      {
        // This indicates whether the attribute type should be
        // considered obsolete. We do not need to do any more parsing
        // for this token.
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
      else if (tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed
        // by either a single definition in single quotes or an open
        // parenthesis followed by one or more values in single quotes
        // separated by spaces followed by a close parenthesis.
        if (extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName, SchemaUtils
            .readExtensions(reader));
      }
      else
      {
        final Message message =
            ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
      }
    }

    final DITContentRule rule =
        new DITContentRule(structuralClass, names, description,
            isObsolete, auxiliaryClasses, optionalAttributes,
            prohibitedAttributes, requiredAttributes, extraProperties,
            definition);
    addDITContentRule(rule, overwrite);
  }



  public void addDITContentRule(String structuralClass,
      List<String> names, String description, boolean obsolete,
      Set<String> auxiliaryClasses, Set<String> optionalAttributes,
      Set<String> prohibitedAttributes, Set<String> requiredAttributes,
      Map<String, List<String>> extraProperties, boolean overwrite)
      throws SchemaException
  {
    final DITContentRule rule =
        new DITContentRule(structuralClass, names, description,
            obsolete, auxiliaryClasses, optionalAttributes,
            prohibitedAttributes, requiredAttributes, extraProperties,
            null);
    addDITContentRule(rule, overwrite);
  }



  public void addDITStructureRule(Integer ruleID, List<String> names,
      String description, boolean obsolete, String nameForm,
      Set<Integer> superiorRules,
      Map<String, List<String>> extraProperties, boolean overwrite)
      throws SchemaException
  {
    final DITStructureRule rule =
        new DITStructureRule(ruleID, names, description, obsolete,
            nameForm, superiorRules, extraProperties, null);
    addDITStructureRule(rule, overwrite);
  }



  public void addDITStructureRule(String definition, boolean overwrite)
      throws DecodeException, SchemaException
  {
    Validator.ensureNotNull(definition);
    final SubstringReader reader = new SubstringReader(definition);

    // We'll do this a character at a time. First, skip over any leading
    // whitespace.
    reader.skipWhitespaces();

    if (reader.remaining() <= 0)
    {
      // This means that the value was empty or contained only
      // whitespace. That is illegal.
      final Message message = ERR_ATTR_SYNTAX_DSR_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }

    // The next character must be an open parenthesis. If it is not,
    // then that is an error.
    final char c = reader.read();
    if (c != '(')
    {
      final Message message =
          ERR_ATTR_SYNTAX_DSR_EXPECTED_OPEN_PARENTHESIS.get(definition,
              (reader.pos() - 1), String.valueOf(c));
      throw new DecodeException(message);
    }

    // Skip over any spaces immediately following the opening
    // parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    final Integer ruleID = SchemaUtils.readRuleID(reader);

    List<String> names = Collections.emptyList();
    String description = "".intern();
    boolean isObsolete = false;
    String nameForm = null;
    Set<Integer> superiorRules = Collections.emptySet();
    Map<String, List<String>> extraProperties = Collections.emptyMap();

    // At this point, we should have a pretty specific syntax that
    // describes what may come next, but some of the components are
    // optional and it would be pretty easy to put something in the
    // wrong order, so we will be very flexible about what we can
    // accept. Just look at the next token, figure out what it is and
    // how to treat what comes after it, then repeat until we get to the
    // end of the value. But before we start, set default values for
    // everything else we might need to know.
    while (true)
    {
      final String tokenName = SchemaUtils.readTokenName(reader);

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
        // This specifies the description for the attribute type. It is
        // an arbitrary string of characters enclosed in single quotes.
        description = SchemaUtils.readQuotedString(reader);
      }
      else if (tokenName.equalsIgnoreCase("obsolete"))
      {
        // This indicates whether the attribute type should be
        // considered obsolete. We do not need to do any more parsing
        // for this token.
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
      else if (tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed
        // by either a single definition in single quotes or an open
        // parenthesis followed by one or more values in single quotes
        // separated by spaces followed by a close parenthesis.
        if (extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName, SchemaUtils
            .readExtensions(reader));
      }
      else
      {
        final Message message =
            ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
      }
    }

    if (nameForm == null)
    {
      final Message message =
          ERR_ATTR_SYNTAX_DSR_NO_NAME_FORM.get(definition);
      throw new DecodeException(message);
    }

    final DITStructureRule rule =
        new DITStructureRule(ruleID, names, description, isObsolete,
            nameForm, superiorRules, extraProperties, definition);
    addDITStructureRule(rule, overwrite);
  }



  public void addMatchingRule(String definition, boolean overwrite)
      throws DecodeException, SchemaException
  {
    Validator.ensureNotNull(definition);
    final SubstringReader reader = new SubstringReader(definition);

    // We'll do this a character at a time. First, skip over any leading
    // whitespace.
    reader.skipWhitespaces();

    if (reader.remaining() <= 0)
    {
      // This means that the value was empty or contained only
      // whitespace. That is illegal.
      final Message message = ERR_ATTR_SYNTAX_MR_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }

    // The next character must be an open parenthesis. If it is not,
    // then that is an error.
    final char c = reader.read();
    if (c != '(')
    {
      final Message message =
          ERR_ATTR_SYNTAX_MR_EXPECTED_OPEN_PARENTHESIS.get(definition,
              (reader.pos() - 1), String.valueOf(c));
      throw new DecodeException(message);
    }

    // Skip over any spaces immediately following the opening
    // parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    final String oid = SchemaUtils.readOID(reader);

    List<String> names = Collections.emptyList();
    String description = "".intern();
    boolean isObsolete = false;
    String syntax = null;
    Map<String, List<String>> extraProperties = Collections.emptyMap();

    // At this point, we should have a pretty specific syntax that
    // describes what may come next, but some of the components are
    // optional and it would be pretty easy to put something in the
    // wrong order, so we will be very flexible about what we can
    // accept. Just look at the next token, figure out what it is and
    // how to treat what comes after it, then repeat until we get to the
    // end of the value. But before we start, set default values for
    // everything else we might need to know.
    while (true)
    {
      final String tokenName = SchemaUtils.readTokenName(reader);

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
        // This specifies the description for the matching rule. It is
        // an arbitrary string of characters enclosed in single quotes.
        description = SchemaUtils.readQuotedString(reader);
      }
      else if (tokenName.equalsIgnoreCase("obsolete"))
      {
        // This indicates whether the matching rule should be considered
        // obsolete. We do not need to do any more parsing for this
        // token.
        isObsolete = true;
      }
      else if (tokenName.equalsIgnoreCase("syntax"))
      {
        syntax = SchemaUtils.readOID(reader);
      }
      else if (tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed
        // by either a single definition in single quotes or an open
        // parenthesis followed by one or more values in single quotes
        // separated by spaces followed by a close parenthesis.
        if (extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName, SchemaUtils
            .readExtensions(reader));
      }
      else
      {
        final Message message =
            ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
      }
    }

    // Make sure that a syntax was specified.
    if (syntax == null)
    {
      final Message message =
          ERR_ATTR_SYNTAX_MR_NO_SYNTAX.get(definition);
      throw new DecodeException(message);
    }

    addMatchingRule(new MatchingRule(oid, names, description,
        isObsolete, syntax, extraProperties, definition, null),
        overwrite);
  }



  public void addMatchingRule(String oid, List<String> names,
      String description, boolean obsolete, String syntax,
      Map<String, List<String>> extraProperties,
      MatchingRuleImpl implementation, boolean overwrite)
      throws SchemaException
  {
    Validator.ensureNotNull(implementation);
    final MatchingRule matchingRule =
        new MatchingRule(oid, names, description, obsolete, syntax,
            extraProperties, null, implementation);
    addMatchingRule(matchingRule, overwrite);
  }



  public void addMatchingRuleUse(String definition, boolean overwrite)
      throws DecodeException, SchemaException
  {
    Validator.ensureNotNull(definition);
    final SubstringReader reader = new SubstringReader(definition);

    // We'll do this a character at a time. First, skip over any leading
    // whitespace.
    reader.skipWhitespaces();

    if (reader.remaining() <= 0)
    {
      // This means that the value was empty or contained only
      // whitespace. That is illegal.
      final Message message = ERR_ATTR_SYNTAX_MRUSE_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }

    // The next character must be an open parenthesis. If it is not,
    // then that is an error.
    final char c = reader.read();
    if (c != '(')
    {
      final Message message =
          ERR_ATTR_SYNTAX_MRUSE_EXPECTED_OPEN_PARENTHESIS.get(
              definition, (reader.pos() - 1), String.valueOf(c));
      throw new DecodeException(message);
    }

    // Skip over any spaces immediately following the opening
    // parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    final String oid = SchemaUtils.readOID(reader);

    List<String> names = Collections.emptyList();
    String description = "".intern();
    boolean isObsolete = false;
    Set<String> attributes = null;
    Map<String, List<String>> extraProperties = Collections.emptyMap();

    // At this point, we should have a pretty specific syntax that
    // describes what may come next, but some of the components are
    // optional and it would be pretty easy to put something in the
    // wrong order, so we will be very flexible about what we can
    // accept. Just look at the next token, figure out what it is and
    // how to treat what comes after it, then repeat until we get to the
    // end of the value. But before we start, set default values for
    // everything else we might need to know.
    while (true)
    {
      final String tokenName = SchemaUtils.readTokenName(reader);

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
        // This specifies the description for the attribute type. It is
        // an arbitrary string of characters enclosed in single quotes.
        description = SchemaUtils.readQuotedString(reader);
      }
      else if (tokenName.equalsIgnoreCase("obsolete"))
      {
        // This indicates whether the attribute type should be
        // considered obsolete. We do not need to do any more parsing
        // for this token.
        isObsolete = true;
      }
      else if (tokenName.equalsIgnoreCase("applies"))
      {
        attributes = SchemaUtils.readOIDs(reader);
      }
      else if (tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed
        // by either a single definition in single quotes or an open
        // parenthesis followed by one or more values in single quotes
        // separated by spaces followed by a close parenthesis.
        if (extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName, SchemaUtils
            .readExtensions(reader));
      }
      else
      {
        final Message message =
            ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
      }
    }

    // Make sure that the set of attributes was defined.
    if (attributes == null || attributes.size() == 0)
    {
      final Message message =
          ERR_ATTR_SYNTAX_MRUSE_NO_ATTR.get(definition);
      throw new DecodeException(message);
    }

    final MatchingRuleUse use =
        new MatchingRuleUse(oid, names, description, isObsolete,
            attributes, extraProperties, definition);
    addMatchingRuleUse(use, overwrite);
  }



  public void addMatchingRuleUse(String oid, List<String> names,
      String description, boolean obsolete, Set<String> attributeOIDs,
      Map<String, List<String>> extraProperties, boolean overwrite)
      throws SchemaException
  {
    final MatchingRuleUse use =
        new MatchingRuleUse(oid, names, description, obsolete,
            attributeOIDs, extraProperties, null);
    addMatchingRuleUse(use, overwrite);
  }



  public void addNameForm(String definition, boolean overwrite)
      throws DecodeException, SchemaException
  {
    Validator.ensureNotNull(definition);
    final SubstringReader reader = new SubstringReader(definition);

    // We'll do this a character at a time. First, skip over any leading
    // whitespace.
    reader.skipWhitespaces();

    if (reader.remaining() <= 0)
    {
      // This means that the value was empty or contained only
      // whitespace. That is illegal.
      final Message message =
          ERR_ATTR_SYNTAX_NAME_FORM_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }

    // The next character must be an open parenthesis. If it is not,
    // then that is an error.
    final char c = reader.read();
    if (c != '(')
    {
      final Message message =
          ERR_ATTR_SYNTAX_NAME_FORM_EXPECTED_OPEN_PARENTHESIS.get(
              definition, (reader.pos() - 1), c);
      throw new DecodeException(message);
    }

    // Skip over any spaces immediately following the opening
    // parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    final String oid = SchemaUtils.readOID(reader);

    List<String> names = Collections.emptyList();
    String description = "".intern();
    boolean isObsolete = false;
    String structuralClass = null;
    Set<String> optionalAttributes = Collections.emptySet();
    Set<String> requiredAttributes = null;
    Map<String, List<String>> extraProperties = Collections.emptyMap();

    // At this point, we should have a pretty specific syntax that
    // describes what may come next, but some of the components are
    // optional and it would be pretty easy to put something in the
    // wrong order, so we will be very flexible about what we can
    // accept. Just look at the next token, figure out what it is and
    // how to treat what comes after it, then repeat until we get to the
    // end of the value. But before we start, set default values for
    // everything else we might need to know.
    while (true)
    {
      final String tokenName = SchemaUtils.readTokenName(reader);

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
        // This specifies the description for the attribute type. It is
        // an arbitrary string of characters enclosed in single quotes.
        description = SchemaUtils.readQuotedString(reader);
      }
      else if (tokenName.equalsIgnoreCase("obsolete"))
      {
        // This indicates whether the attribute type should be
        // considered obsolete. We do not need to do any more parsing
        // for this token.
        isObsolete = true;
      }
      else if (tokenName.equalsIgnoreCase("oc"))
      {
        structuralClass = SchemaUtils.readOID(reader);
      }
      else if (tokenName.equalsIgnoreCase("must"))
      {
        requiredAttributes = SchemaUtils.readOIDs(reader);
      }
      else if (tokenName.equalsIgnoreCase("may"))
      {
        optionalAttributes = SchemaUtils.readOIDs(reader);
      }
      else if (tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed
        // by either a single definition in single quotes or an open
        // parenthesis followed by one or more values in single quotes
        // separated by spaces followed by a close parenthesis.
        if (extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName, SchemaUtils
            .readExtensions(reader));
      }
      else
      {
        final Message message =
            ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
      }
    }

    // Make sure that a structural class was specified. If not, then it
    // cannot be valid.
    if (structuralClass == null)
    {
      final Message message =
          ERR_ATTR_SYNTAX_NAME_FORM_NO_STRUCTURAL_CLASS.get(definition);
      throw new DecodeException(message);
    }

    if (requiredAttributes == null || requiredAttributes.size() == 0)
    {
      final Message message =
          ERR_ATTR_SYNTAX_NAME_FORM_NO_REQUIRED_ATTR.get(definition);
      throw new DecodeException(message);
    }

    final NameForm nameForm =
        new NameForm(oid, names, description, isObsolete,
            structuralClass, requiredAttributes, optionalAttributes,
            extraProperties, definition);
    addNameForm(nameForm, overwrite);
  }



  public void addNameForm(String oid, List<String> names,
      String description, boolean obsolete, String structuralClass,
      Set<String> requiredAttributes, Set<String> optionalAttributes,
      Map<String, List<String>> extraProperties, boolean overwrite)
      throws SchemaException
  {
    final NameForm nameForm =
        new NameForm(oid, names, description, obsolete,
            structuralClass, requiredAttributes, optionalAttributes,
            extraProperties, null);
    addNameForm(nameForm, overwrite);
  }



  public void addObjectClass(String definition, boolean overwrite)
      throws DecodeException, SchemaException
  {
    Validator.ensureNotNull(definition);
    final SubstringReader reader = new SubstringReader(definition);

    // We'll do this a character at a time. First, skip over any leading
    // whitespace.
    reader.skipWhitespaces();

    if (reader.remaining() <= 0)
    {
      // This means that the value was empty or contained only
      // whitespace. That is illegal.
      final Message message =
          ERR_ATTR_SYNTAX_OBJECTCLASS_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }

    // The next character must be an open parenthesis. If it is not,
    // then that is an error.
    final char c = reader.read();
    if (c != '(')
    {
      final Message message =
          ERR_ATTR_SYNTAX_OBJECTCLASS_EXPECTED_OPEN_PARENTHESIS.get(
              definition, (reader.pos() - 1), String.valueOf(c));
      throw new DecodeException(message);
    }

    // Skip over any spaces immediately following the opening
    // parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    final String oid = SchemaUtils.readOID(reader);

    List<String> names = Collections.emptyList();
    String description = "".intern();
    boolean isObsolete = false;
    Set<String> superiorClasses = Collections.emptySet();
    Set<String> requiredAttributes = Collections.emptySet();
    Set<String> optionalAttributes = Collections.emptySet();
    ObjectClassType objectClassType = ObjectClassType.STRUCTURAL;
    Map<String, List<String>> extraProperties = Collections.emptyMap();

    // At this point, we should have a pretty specific syntax that
    // describes what may come next, but some of the components are
    // optional and it would be pretty easy to put something in the
    // wrong order, so we will be very flexible about what we can
    // accept. Just look at the next token, figure out what it is and
    // how to treat what comes after it, then repeat until we get to the
    // end of the value. But before we start, set default values for
    // everything else we might need to know.
    while (true)
    {
      final String tokenName = SchemaUtils.readTokenName(reader);

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
        // This specifies the description for the attribute type. It is
        // an arbitrary string of characters enclosed in single quotes.
        description = SchemaUtils.readQuotedString(reader);
      }
      else if (tokenName.equalsIgnoreCase("obsolete"))
      {
        // This indicates whether the attribute type should be
        // considered obsolete. We do not need to do any more parsing
        // for this token.
        isObsolete = true;
      }
      else if (tokenName.equalsIgnoreCase("sup"))
      {
        superiorClasses = SchemaUtils.readOIDs(reader);
      }
      else if (tokenName.equalsIgnoreCase("abstract"))
      {
        // This indicates that entries must not include this objectclass
        // unless they also include a non-abstract objectclass that
        // inherits from this class. We do not need any more parsing for
        // this token.
        objectClassType = ObjectClassType.ABSTRACT;
      }
      else if (tokenName.equalsIgnoreCase("structural"))
      {
        // This indicates that this is a structural objectclass. We do
        // not need any more parsing for this token.
        objectClassType = ObjectClassType.STRUCTURAL;
      }
      else if (tokenName.equalsIgnoreCase("auxiliary"))
      {
        // This indicates that this is an auxiliary objectclass. We do
        // not need any more parsing for this token.
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
      else if (tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed
        // by either a single definition in single quotes or an open
        // parenthesis followed by one or more values in single quotes
        // separated by spaces followed by a close parenthesis.
        if (extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName, SchemaUtils
            .readExtensions(reader));
      }
      else
      {
        final Message message =
            ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
      }
    }

    if (oid.equals(EXTENSIBLE_OBJECT_OBJECTCLASS_OID))
    {
      addObjectClass(new ObjectClass(description, extraProperties),
          overwrite);
    }
    else
    {
      if (objectClassType == ObjectClassType.STRUCTURAL
          && superiorClasses.isEmpty())
      {
        superiorClasses = Collections.singleton(TOP_OBJECTCLASS_NAME);
      }

      addObjectClass(new ObjectClass(oid, names, description,
          isObsolete, superiorClasses, requiredAttributes,
          optionalAttributes, objectClassType, extraProperties,
          definition), overwrite);
    }
  }



  public void addObjectClass(String oid, List<String> names,
      String description, boolean obsolete,
      Set<String> superiorClassOIDs, Set<String> requiredAttributeOIDs,
      Set<String> optionalAttributeOIDs,
      ObjectClassType objectClassType,
      Map<String, List<String>> extraProperties, boolean overwrite)
      throws SchemaException
  {
    if (oid.equals(EXTENSIBLE_OBJECT_OBJECTCLASS_OID))
    {
      addObjectClass(new ObjectClass(description, extraProperties),
          overwrite);
    }
    else
    {
      if (objectClassType == ObjectClassType.STRUCTURAL
          && superiorClassOIDs.isEmpty())
      {
        superiorClassOIDs = Collections.singleton(TOP_OBJECTCLASS_NAME);
      }

      addObjectClass(
          new ObjectClass(oid, names, description, obsolete,
              superiorClassOIDs, requiredAttributeOIDs,
              optionalAttributeOIDs, objectClassType, extraProperties,
              null), overwrite);
    }
  }



  public void addSyntax(String definition, boolean overwrite)
      throws DecodeException, SchemaException
  {
    Validator.ensureNotNull(definition);
    final SubstringReader reader = new SubstringReader(definition);

    // We'll do this a character at a time. First, skip over any leading
    // whitespace.
    reader.skipWhitespaces();

    if (reader.remaining() <= 0)
    {
      // This means that the value was empty or contained only
      // whitespace. That is illegal.
      final Message message =
          ERR_ATTR_SYNTAX_ATTRSYNTAX_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }

    // The next character must be an open parenthesis. If it is not,
    // then that is an error.
    final char c = reader.read();
    if (c != '(')
    {
      final Message message =
          ERR_ATTR_SYNTAX_ATTRSYNTAX_EXPECTED_OPEN_PARENTHESIS.get(
              definition, (reader.pos() - 1), String.valueOf(c));
      throw new DecodeException(message);
    }

    // Skip over any spaces immediately following the opening
    // parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    final String oid = SchemaUtils.readOID(reader);

    String description = "".intern();
    Map<String, List<String>> extraProperties = Collections.emptyMap();

    // At this point, we should have a pretty specific syntax that
    // describes what may come next, but some of the components are
    // optional and it would be pretty easy to put something in the
    // wrong order, so we will be very flexible about what we can
    // accept. Just look at the next token, figure out what it is and
    // how to treat what comes after it, then repeat until we get to the
    // end of the value. But before we start, set default values for
    // everything else we might need to know.
    while (true)
    {
      final String tokenName = SchemaUtils.readTokenName(reader);

      if (tokenName == null)
      {
        // No more tokens.
        break;
      }
      else if (tokenName.equalsIgnoreCase("desc"))
      {
        // This specifies the description for the syntax. It is an
        // arbitrary string of characters enclosed in single quotes.
        description = SchemaUtils.readQuotedString(reader);
      }
      else if (tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed
        // by either a single definition in single quotes or an open
        // parenthesis followed by one or more values in single quotes
        // separated by spaces followed by a close parenthesis.
        if (extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName, SchemaUtils
            .readExtensions(reader));
      }
      else
      {
        final Message message =
            ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
      }
    }

    // See if it is a enum syntax
    for (final Map.Entry<String, List<String>> property : extraProperties
        .entrySet())
    {
      if (property.getKey().equalsIgnoreCase("x-enum"))
      {
        final EnumSyntaxImpl enumImpl =
            new EnumSyntaxImpl(oid, property.getValue());
        final Syntax enumSyntax =
            new Syntax(oid, description, extraProperties, definition,
                enumImpl);
        final MatchingRule enumOMR =
            new MatchingRule(enumImpl.getOrderingMatchingRule(),
                Collections.singletonList(OMR_GENERIC_ENUM_NAME + oid),
                "", false, oid, CoreSchema.OPENDS_ORIGIN, null,
                new EnumOrderingMatchingRule(enumImpl));

        addSyntax(enumSyntax, overwrite);
        addMatchingRule(enumOMR, overwrite);
        return;
      }
    }

    addSyntax(new Syntax(oid, description, extraProperties, definition,
        null), overwrite);
  }



  public void addSyntaxEnumeration(String oid, String description,
      boolean overwrite, String... enumerations) throws SchemaException
  {
    Validator.ensureNotNull((Object) enumerations);

    final EnumSyntaxImpl enumImpl =
        new EnumSyntaxImpl(oid, Arrays.asList(enumerations));
    final Syntax enumSyntax =
        new Syntax(oid, description, Collections.singletonMap("X-ENUM",
            Arrays.asList(enumerations)), null, enumImpl);
    final MatchingRule enumOMR =
        new MatchingRule(enumImpl.getOrderingMatchingRule(),
            Collections.singletonList(OMR_GENERIC_ENUM_NAME + oid), "",
            false, oid, CoreSchema.OPENDS_ORIGIN, null,
            new EnumOrderingMatchingRule(enumImpl));

    addSyntax(enumSyntax, overwrite);
    addMatchingRule(enumOMR, overwrite);
  }



  public void addSyntax(String oid, String description,
      Map<String, List<String>> extraProperties,
      SyntaxImpl implementation, boolean overwrite)
      throws SchemaException
  {
    addSyntax(new Syntax(oid, description, extraProperties, null,
        implementation), overwrite);
  }



  public void addSyntaxPattern(String oid, String description,
      Pattern pattern, boolean overwrite) throws SchemaException
  {
    Validator.ensureNotNull(pattern);

    addSyntax(new Syntax(oid, description, Collections.singletonMap(
        "X-PATTERN", Collections.singletonList(pattern.toString())),
        null, null), overwrite);
  }



  public void addSyntaxSubstitution(String oid, String description,
      String substituteSyntax, boolean overwrite)
      throws SchemaException
  {
    Validator.ensureNotNull(substituteSyntax);

    addSyntax(new Syntax(oid, description, Collections.singletonMap(
        "X-SUBST", Collections.singletonList(substituteSyntax)), null,
        null), overwrite);
  }



  public boolean removeAttributeType(String oid)
  {
    if (schema.hasAttributeType(oid))
    {
      removeAttributeType(schema.getAttributeType(oid));
      return true;
    }
    return false;
  }



  public boolean removeDITContentRule(String numericOID)
  {
    if (schema.hasDITContentRule(numericOID))
    {
      removeDITContentRule(schema.getDITContentRule(numericOID));
      return true;
    }
    return false;
  }



  public boolean removeDITStructureRule(Integer ruleID)
  {
    if (schema.hasDITStructureRule(ruleID))
    {
      removeDITStructureRule(schema.getDITStructureRule(ruleID));
      return true;
    }
    return false;
  }



  public boolean removeMatchingRule(String oid)
  {
    if (schema.hasMatchingRule(oid))
    {
      removeMatchingRule(schema.getMatchingRule(oid));
      return true;
    }
    return false;
  }



  public boolean removeMatchingRuleUse(String numericOID)
  {
    if (schema.hasMatchingRuleUse(numericOID))
    {
      removeMatchingRuleUse(schema.getMatchingRuleUse(numericOID));
      return true;
    }
    return false;
  }



  public boolean removeNameForm(String oid)
  {
    if (schema.hasNameForm(oid))
    {
      removeNameForm(schema.getNameForm(oid));
      return true;
    }
    return false;
  }



  public boolean removeObjectClass(String oid)
  {
    if (schema.hasObjectClass(oid))
    {
      removeObjectClass(schema.getObjectClass(oid));
      return true;
    }
    return false;
  }



  public boolean removeSyntax(String numericOID)
  {
    if (schema.hasSyntax(numericOID))
    {
      removeSyntax(schema.getSyntax(numericOID));
      return true;
    }
    return false;
  }



  public Schema toSchema()
  {
    validate(warnings);
    final Schema builtSchema = schema;
    initBuilder();
    return builtSchema;
  }



  private synchronized void addAttributeType(AttributeType attribute,
      boolean overwrite) throws SchemaException
  {
    AttributeType conflictingAttribute;
    if (numericOID2AttributeTypes.containsKey(attribute.getOID()))
    {
      conflictingAttribute =
          numericOID2AttributeTypes.get(attribute.getOID());
      if (!overwrite)
      {
        final Message message =
            ERR_SCHEMA_CONFLICTING_ATTRIBUTE_OID.get(attribute
                .getNameOrOID(), attribute.getOID(),
                conflictingAttribute.getNameOrOID());
        throw new SchemaException(message);
      }
      removeAttributeType(conflictingAttribute);
    }

    numericOID2AttributeTypes.put(attribute.getOID(), attribute);
    for (final String name : attribute.getNames())
    {
      final String lowerName = StaticUtils.toLowerCase(name);
      List<AttributeType> attrs;
      if ((attrs = name2AttributeTypes.get(lowerName)) == null)
      {
        name2AttributeTypes.put(lowerName, Collections
            .singletonList(attribute));
      }
      else if (attrs.size() == 1)
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



  private synchronized void addDITContentRule(DITContentRule rule,
      boolean overwrite) throws SchemaException
  {
    DITContentRule conflictingRule;
    if (numericOID2ContentRules.containsKey(rule
        .getStructuralClassOID()))
    {
      conflictingRule =
          numericOID2ContentRules.get(rule.getStructuralClassOID());
      if (!overwrite)
      {
        final Message message =
            ERR_SCHEMA_CONFLICTING_DIT_CONTENT_RULE.get(rule
                .getNameOrOID(), rule.getStructuralClassOID(),
                conflictingRule.getNameOrOID());
        throw new SchemaException(message);
      }
      removeDITContentRule(conflictingRule);
    }

    numericOID2ContentRules.put(rule.getStructuralClassOID(), rule);
    for (final String name : rule.getNames())
    {
      final String lowerName = StaticUtils.toLowerCase(name);
      List<DITContentRule> rules;
      if ((rules = name2ContentRules.get(lowerName)) == null)
      {
        name2ContentRules.put(lowerName, Collections
            .singletonList(rule));
      }
      else if (rules.size() == 1)
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



  private synchronized void addDITStructureRule(DITStructureRule rule,
      boolean overwrite) throws SchemaException
  {
    DITStructureRule conflictingRule;
    if (id2StructureRules.containsKey(rule.getRuleID()))
    {
      conflictingRule = id2StructureRules.get(rule.getRuleID());
      if (!overwrite)
      {
        final Message message =
            ERR_SCHEMA_CONFLICTING_DIT_STRUCTURE_RULE_ID.get(rule
                .getNameOrRuleID(), rule.getRuleID(), conflictingRule
                .getNameOrRuleID());
        throw new SchemaException(message);
      }
      removeDITStructureRule(conflictingRule);
    }

    id2StructureRules.put(rule.getRuleID(), rule);
    for (final String name : rule.getNames())
    {
      final String lowerName = StaticUtils.toLowerCase(name);
      List<DITStructureRule> rules;
      if ((rules = name2StructureRules.get(lowerName)) == null)
      {
        name2StructureRules.put(lowerName, Collections
            .singletonList(rule));
      }
      else if (rules.size() == 1)
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



  private synchronized void addMatchingRule(MatchingRule rule,
      boolean overwrite) throws SchemaException
  {
    MatchingRule conflictingRule;
    if (numericOID2MatchingRules.containsKey(rule.getOID()))
    {
      conflictingRule = numericOID2MatchingRules.get(rule.getOID());
      if (!overwrite)
      {
        final Message message =
            ERR_SCHEMA_CONFLICTING_MR_OID.get(rule.getNameOrOID(), rule
                .getOID(), conflictingRule.getNameOrOID());
        throw new SchemaException(message);
      }
      removeMatchingRule(conflictingRule);
    }

    numericOID2MatchingRules.put(rule.getOID(), rule);
    for (final String name : rule.getNames())
    {
      final String lowerName = StaticUtils.toLowerCase(name);
      List<MatchingRule> rules;
      if ((rules = name2MatchingRules.get(lowerName)) == null)
      {
        name2MatchingRules.put(lowerName, Collections
            .singletonList(rule));
      }
      else if (rules.size() == 1)
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



  private synchronized void addMatchingRuleUse(MatchingRuleUse use,
      boolean overwrite) throws SchemaException
  {
    MatchingRuleUse conflictingUse;
    if (numericOID2MatchingRuleUses.containsKey(use
        .getMatchingRuleOID()))
    {
      conflictingUse =
          numericOID2MatchingRuleUses.get(use.getMatchingRuleOID());
      if (!overwrite)
      {
        final Message message =
            ERR_SCHEMA_CONFLICTING_MATCHING_RULE_USE.get(use
                .getNameOrOID(), use.getMatchingRuleOID(),
                conflictingUse.getNameOrOID());
        throw new SchemaException(message);
      }
      removeMatchingRuleUse(conflictingUse);
    }

    numericOID2MatchingRuleUses.put(use.getMatchingRuleOID(), use);
    for (final String name : use.getNames())
    {
      final String lowerName = StaticUtils.toLowerCase(name);
      List<MatchingRuleUse> uses;
      if ((uses = name2MatchingRuleUses.get(lowerName)) == null)
      {
        name2MatchingRuleUses.put(lowerName, Collections
            .singletonList(use));
      }
      else if (uses.size() == 1)
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



  private synchronized void addNameForm(NameForm form, boolean overwrite)
      throws SchemaException
  {
    NameForm conflictingForm;
    if (numericOID2NameForms.containsKey(form.getOID()))
    {
      conflictingForm = numericOID2NameForms.get(form.getOID());
      if (!overwrite)
      {
        final Message message =
            ERR_SCHEMA_CONFLICTING_NAME_FORM_OID.get(form
                .getNameOrOID(), form.getOID(), conflictingForm
                .getNameOrOID());
        throw new SchemaException(message);
      }
      removeNameForm(conflictingForm);
    }

    numericOID2NameForms.put(form.getOID(), form);
    for (final String name : form.getNames())
    {
      final String lowerName = StaticUtils.toLowerCase(name);
      List<NameForm> forms;
      if ((forms = name2NameForms.get(lowerName)) == null)
      {
        name2NameForms.put(lowerName, Collections.singletonList(form));
      }
      else if (forms.size() == 1)
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



  private synchronized void addObjectClass(ObjectClass oc,
      boolean overwrite) throws SchemaException
  {
    ObjectClass conflictingOC;
    if (numericOID2ObjectClasses.containsKey(oc.getOID()))
    {
      conflictingOC = numericOID2ObjectClasses.get(oc.getOID());
      if (!overwrite)
      {
        final Message message =
            ERR_SCHEMA_CONFLICTING_OBJECTCLASS_OID.get(oc
                .getNameOrOID(), oc.getOID(), conflictingOC
                .getNameOrOID());
        throw new SchemaException(message);
      }
      removeObjectClass(conflictingOC);
    }

    numericOID2ObjectClasses.put(oc.getOID(), oc);
    for (final String name : oc.getNames())
    {
      final String lowerName = StaticUtils.toLowerCase(name);
      List<ObjectClass> classes;
      if ((classes = name2ObjectClasses.get(lowerName)) == null)
      {
        name2ObjectClasses
            .put(lowerName, Collections.singletonList(oc));
      }
      else if (classes.size() == 1)
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



  private synchronized void addSyntax(Syntax syntax, boolean overwrite)
      throws SchemaException
  {
    Syntax conflictingSyntax;
    if (numericOID2Syntaxes.containsKey(syntax.getOID()))
    {
      conflictingSyntax = numericOID2Syntaxes.get(syntax.getOID());
      if (!overwrite)
      {
        final Message message =
            ERR_SCHEMA_CONFLICTING_SYNTAX_OID.get(syntax.toString(),
                syntax.getOID(), conflictingSyntax.getOID());
        throw new SchemaException(message);
      }
      removeSyntax(conflictingSyntax);
    }
    numericOID2Syntaxes.put(syntax.getOID(), syntax);
  }



  private void initBuilder()
  {
    numericOID2Syntaxes = new HashMap<String, Syntax>();
    numericOID2MatchingRules = new HashMap<String, MatchingRule>();
    numericOID2MatchingRuleUses =
        new HashMap<String, MatchingRuleUse>();
    numericOID2AttributeTypes = new HashMap<String, AttributeType>();
    numericOID2ObjectClasses = new HashMap<String, ObjectClass>();
    numericOID2NameForms = new HashMap<String, NameForm>();
    numericOID2ContentRules = new HashMap<String, DITContentRule>();
    id2StructureRules = new HashMap<Integer, DITStructureRule>();

    name2MatchingRules = new HashMap<String, List<MatchingRule>>();
    name2MatchingRuleUses =
        new HashMap<String, List<MatchingRuleUse>>();
    name2AttributeTypes = new HashMap<String, List<AttributeType>>();
    name2ObjectClasses = new HashMap<String, List<ObjectClass>>();
    name2NameForms = new HashMap<String, List<NameForm>>();
    name2ContentRules = new HashMap<String, List<DITContentRule>>();
    name2StructureRules = new HashMap<String, List<DITStructureRule>>();

    objectClass2NameForms = new HashMap<String, List<NameForm>>();
    nameForm2StructureRules =
        new HashMap<String, List<DITStructureRule>>();

    warnings = new LinkedList<Message>();

    schema =
        new Schema(numericOID2Syntaxes, numericOID2MatchingRules,
            numericOID2MatchingRuleUses, numericOID2AttributeTypes,
            numericOID2ObjectClasses, numericOID2NameForms,
            numericOID2ContentRules, id2StructureRules,
            name2MatchingRules, name2MatchingRuleUses,
            name2AttributeTypes, name2ObjectClasses, name2NameForms,
            name2ContentRules, name2StructureRules,
            objectClass2NameForms, nameForm2StructureRules, warnings);
  }



  private synchronized void removeAttributeType(
      AttributeType attributeType)
  {
    numericOID2AttributeTypes.remove(attributeType.getOID());
    for (final String name : attributeType.getNames())
    {
      final String lowerName = StaticUtils.toLowerCase(name);
      final List<AttributeType> attributes =
          name2AttributeTypes.get(lowerName);
      if (attributes != null && attributes.contains(attributeType))
      {
        if (attributes.size() <= 1)
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



  private synchronized void removeDITContentRule(DITContentRule rule)
  {
    numericOID2ContentRules.remove(rule.getStructuralClassOID());
    for (final String name : rule.getNames())
    {
      final String lowerName = StaticUtils.toLowerCase(name);
      final List<DITContentRule> rules =
          name2ContentRules.get(lowerName);
      if (rules != null && rules.contains(rule))
      {
        if (rules.size() <= 1)
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



  private synchronized void removeDITStructureRule(DITStructureRule rule)
  {
    id2StructureRules.remove(rule.getRuleID());
    for (final String name : rule.getNames())
    {
      final String lowerName = StaticUtils.toLowerCase(name);
      final List<DITStructureRule> rules =
          name2StructureRules.get(lowerName);
      if (rules != null && rules.contains(rule))
      {
        if (rules.size() <= 1)
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



  private synchronized void removeMatchingRule(MatchingRule rule)
  {
    numericOID2MatchingRules.remove(rule.getOID());
    for (final String name : rule.getNames())
    {
      final String lowerName = StaticUtils.toLowerCase(name);
      final List<MatchingRule> rules =
          name2MatchingRules.get(lowerName);
      if (rules != null && rules.contains(rule))
      {
        if (rules.size() <= 1)
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



  private synchronized void removeMatchingRuleUse(MatchingRuleUse use)
  {
    numericOID2MatchingRuleUses.remove(use.getMatchingRuleOID());
    for (final String name : use.getNames())
    {
      final String lowerName = StaticUtils.toLowerCase(name);
      final List<MatchingRuleUse> uses =
          name2MatchingRuleUses.get(lowerName);
      if (uses != null && uses.contains(use))
      {
        if (uses.size() <= 1)
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



  private synchronized void removeNameForm(NameForm form)
  {
    numericOID2NameForms.remove(form.getOID());
    name2NameForms.remove(form.getOID());
    for (final String name : form.getNames())
    {
      final String lowerName = StaticUtils.toLowerCase(name);
      final List<NameForm> forms = name2NameForms.get(lowerName);
      if (forms != null && forms.contains(form))
      {
        if (forms.size() <= 1)
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



  private synchronized void removeObjectClass(ObjectClass oc)
  {
    numericOID2ObjectClasses.remove(oc.getOID());
    name2ObjectClasses.remove(oc.getOID());
    for (final String name : oc.getNames())
    {
      final String lowerName = StaticUtils.toLowerCase(name);
      final List<ObjectClass> classes =
          name2ObjectClasses.get(lowerName);
      if (classes != null && classes.contains(oc))
      {
        if (classes.size() <= 1)
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



  private synchronized void removeSyntax(Syntax syntax)
  {
    numericOID2Syntaxes.remove(syntax.getOID());
  }



  private synchronized void validate(List<Message> warnings)
  {
    // Verify all references in all elements
    for (final Syntax syntax : numericOID2Syntaxes.values().toArray(
        new Syntax[numericOID2Syntaxes.values().size()]))
    {
      try
      {
        syntax.validate(warnings, schema);
      }
      catch (final SchemaException e)
      {
        removeSyntax(syntax);
        warnings.add(ERR_SYNTAX_VALIDATION_FAIL.get(syntax.toString(),
            e.toString()));
      }
    }

    for (final MatchingRule rule : numericOID2MatchingRules.values()
        .toArray(
            new MatchingRule[numericOID2MatchingRules.values().size()]))
    {
      try
      {
        rule.validate(warnings, schema);
      }
      catch (final SchemaException e)
      {
        removeMatchingRule(rule);
        warnings.add(ERR_MR_VALIDATION_FAIL.get(rule.toString(), e
            .toString()));
      }
    }

    for (final AttributeType attribute : numericOID2AttributeTypes
        .values()
        .toArray(
            new AttributeType[numericOID2AttributeTypes.values().size()]))
    {
      try
      {
        attribute.validate(warnings, schema);
      }
      catch (final SchemaException e)
      {
        removeAttributeType(attribute);
        warnings.add(ERR_ATTR_TYPE_VALIDATION_FAIL.get(attribute
            .toString(), e.toString()));
      }
    }

    for (final ObjectClass oc : numericOID2ObjectClasses.values()
        .toArray(
            new ObjectClass[numericOID2ObjectClasses.values().size()]))
    {
      try
      {
        oc.validate(warnings, schema);
      }
      catch (final SchemaException e)
      {
        removeObjectClass(oc);
        warnings.add(ERR_OC_VALIDATION_FAIL.get(oc.toString(), e
            .toString()));
      }
    }

    for (final MatchingRuleUse use : numericOID2MatchingRuleUses
        .values().toArray(
            new MatchingRuleUse[numericOID2MatchingRuleUses.values()
                .size()]))
    {
      try
      {
        use.validate(warnings, schema);
      }
      catch (final SchemaException e)
      {
        removeMatchingRuleUse(use);
        warnings.add(ERR_MRU_VALIDATION_FAIL.get(use.toString(), e
            .toString()));
      }
    }

    for (final NameForm form : numericOID2NameForms.values().toArray(
        new NameForm[numericOID2NameForms.values().size()]))
    {
      try
      {
        form.validate(warnings, schema);

        // build the objectClass2NameForms map
        List<NameForm> forms;
        final String ocOID = form.getStructuralClass().getOID();
        if ((forms = objectClass2NameForms.get(ocOID)) == null)
        {
          objectClass2NameForms.put(ocOID, Collections
              .singletonList(form));
        }
        else if (forms.size() == 1)
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
      catch (final SchemaException e)
      {
        removeNameForm(form);
        warnings.add(ERR_NAMEFORM_VALIDATION_FAIL.get(form.toString(),
            e.toString()));
      }
    }

    for (final DITContentRule rule : numericOID2ContentRules
        .values()
        .toArray(
            new DITContentRule[numericOID2ContentRules.values().size()]))
    {
      try
      {
        rule.validate(warnings, schema);
      }
      catch (final SchemaException e)
      {
        removeDITContentRule(rule);
        warnings.add(ERR_DCR_VALIDATION_FAIL.get(rule.toString(), e
            .toString()));
      }
    }

    for (final DITStructureRule rule : id2StructureRules.values()
        .toArray(
            new DITStructureRule[id2StructureRules.values().size()]))
    {
      try
      {
        rule.validate(warnings, schema);

        // build the nameForm2StructureRules map
        List<DITStructureRule> rules;
        final String ocOID = rule.getNameForm().getOID();
        if ((rules = nameForm2StructureRules.get(ocOID)) == null)
        {
          nameForm2StructureRules.put(ocOID, Collections
              .singletonList(rule));
        }
        else if (rules.size() == 1)
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
      catch (final SchemaException e)
      {
        removeDITStructureRule(rule);
        warnings.add(ERR_DSR_VALIDATION_FAIL.get(rule.toString(), e
            .toString()));
      }
    }

  }
}
