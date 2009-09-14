package org.opends.sdk.schema;

import static org.opends.messages.CoreMessages.*;
import static org.opends.messages.SchemaMessages.*;
import static org.opends.server.util.ServerConstants.OID_EXTENSIBLE_OBJECT;
import static org.opends.server.util.ServerConstants.SCHEMA_PROPERTY_APPROX_RULE;

import java.util.*;
import java.util.regex.Pattern;

import org.opends.messages.Message;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.matchingrules.MatchingRuleImplementation;
import org.opends.sdk.schema.syntaxes.SyntaxImplementation;
import org.opends.sdk.util.StaticUtils;
import org.opends.sdk.util.SubstringReader;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;
import static org.opends.server.schema.SchemaConstants.SYNTAX_DIRECTORY_STRING_OID;
import static org.opends.server.schema.SchemaConstants.EMR_CASE_IGNORE_OID;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 16, 2009
 * Time: 5:53:32 PM
 * To change this template use File | Settings | File Templates.
 */
public final class SchemaBuilder
{
  private static final String DEFAULT_SYNTAX = SYNTAX_DIRECTORY_STRING_OID;
  private static final String DEFAULT_MATCHING_RULE = EMR_CASE_IGNORE_OID;

  public static String getDefaultSyntax()
  {
    return DEFAULT_SYNTAX;
  }

  public static String getDefaultMatchingRule()
  {
    return DEFAULT_MATCHING_RULE;
  }

  private final SchemaImpl schema;

  private final class SchemaImpl extends Schema
  {
    private SchemaImpl(Schema schema)
    {
      super();
      try
      {
        for(Syntax syntax : schema.getSyntaxes())
        {
          addSyntax(syntax.duplicate(), false);
        }

        for(MatchingRule matchingRule : schema.getMatchingRules())
        {
          addMatchingRule(matchingRule.duplicate(), false);
        }

        for(MatchingRuleUse matchingRuleUse : schema.getMatchingRuleUses())
        {
          addMatchingRuleUse(matchingRuleUse.duplicate(), false);
        }

        for(AttributeType attributeType : schema.getAttributeTypes())
        {
          addAttributeType(attributeType.duplicate(), false);
        }

        for(ObjectClass objectClass : schema.getObjectClasses())
        {
          addObjectClass(objectClass.duplicate(), false);
        }

        for(NameForm nameForm : schema.getNameForms())
        {
          addNameForm(nameForm.duplicate(), false);
        }

        for(DITContentRule contentRule : schema.getDITContentRules())
        {
          addDITContentRule(contentRule.duplicate(), false);
        }

        for(DITStructureRule structureRule : schema.getDITStuctureRules())
        {
          addDITStructureRule(structureRule.duplicate(), false);
        }
      }
      catch(SchemaException se)
      {
        throw new RuntimeException(se);
      }
    }

    public boolean isStrict() {
      return false;
    }
  }
  
  public SchemaBuilder()
  {
    this.schema = new SchemaImpl(CoreSchema.instance());
  }

  public SchemaBuilder(Schema schema)
  {
    Validator.ensureNotNull(schema);
    this.schema = new SchemaImpl(schema);
  }

  public void addSyntax(String oid, String description,
                        Map<String, List<String>> extraProperties,
                        SyntaxImplementation implementation,
                        boolean overwrite)
      throws SchemaException
  {
    schema.addSyntax(schema.new CachingSyntax(oid, description, extraProperties,
        implementation, null), overwrite);
  }

  public void addSyntax(String oid, String description, String substituteSyntax,
                        boolean overwrite)
      throws SchemaException
  {
    if(oid.equals(substituteSyntax))
    {
      Message message = ERR_ATTR_SYNTAX_CYCLIC_SUB_SYNTAX.get(oid);
      throw new SchemaException(message);
    }

    schema.addSyntax(schema.new SubstitutionSyntax(oid, description,
        Collections.singletonMap("X-SUBST",
            Collections.singletonList(substituteSyntax)),
        substituteSyntax, null), overwrite);
  }

  public void addSyntax(String oid, String description, Pattern pattern,
                        boolean overwrite)
      throws SchemaException
  {
    schema.addSyntax(schema.new RegexSyntax(oid, description,
        Collections.singletonMap("X-PATTERN",
            Collections.singletonList(pattern.toString())),
        pattern, null), overwrite);
  }

  private void addSyntax(String oid, String description,
                         Map<String, List<String>> extraProperties,
                         String definition, boolean overwrite,
                         String... enumerations)
      throws SchemaException
  {
    Validator.ensureNotNull(enumerations);

    List<ByteSequence> values = new LinkedList<ByteSequence>();
    List<String> strings = new LinkedList<String>();
    for(String e : enumerations)
    {
      if(!strings.contains(e))
      {
        values.add(ByteString.valueOf(e));
        strings.add(e);
      }
    }
    schema.addSyntax(schema.new EnumSyntax(oid, description,
        extraProperties == null ? Collections.singletonMap("X-ENUM", strings) :
        extraProperties, values, definition), overwrite);
    schema.addMatchingRule(schema.new EnumOrderingMatchingRule(oid), overwrite);
  }

  public void addSyntax(String oid, String description, boolean overwrite,
                        String... enumerations)
      throws SchemaException
  {
    addSyntax(oid, description, null, null, overwrite, enumerations);
  }

  public void addSyntax(String definition, boolean overwrite)
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
      else if(tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed by
        // either a single definition in single quotes or an open parenthesis
        // followed by one or more values in single quotes separated by spaces
        // followed by a close parenthesis.
        if(extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName,
            SchemaUtils.readExtensions(reader));
      }
      else
      {
        Message message = ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
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
          if(value.equals(oid))
          {
            Message message = ERR_ATTR_SYNTAX_CYCLIC_SUB_SYNTAX.get(oid);
            throw new SchemaException(message);
          }
          schema.addSyntax(
              schema.new SubstitutionSyntax(oid, description, extraProperties,
                                            value, definition), overwrite);
          return;
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
            schema.addSyntax(
                schema.new RegexSyntax(oid, description, extraProperties,
                                       pattern, definition), overwrite);
            return;
          }
          catch(Exception e)
          {
            Message message =
                WARN_ATTR_SYNTAX_LDAPSYNTAX_REGEX_INVALID_PATTERN.get
                    (oid, value);
            throw new DecodeException(message);
          }
        }
      }
      else if(property.getKey().equalsIgnoreCase("x-enum"))
      {
        addSyntax(oid, description, extraProperties, definition, overwrite,
                  property.getValue().toArray(new String[0]));
        return;
      }
    }

    schema.addSyntax(schema.new CachingSyntax(oid, description, extraProperties,
            definition), overwrite);
  }

  public void addMatchingRule(String oid,
                              List<String> names,
                              String description,
                              boolean obsolete,
                              String syntax,
                              Map<String, List<String>> extraProperties,
                              MatchingRuleImplementation implementation,
                              boolean overwrite)
      throws SchemaException
  {
    Validator.ensureNotNull(implementation);
    MatchingRule matchingRule = schema.new CachingMatchingRule(oid,
        names, description, obsolete, syntax, extraProperties, implementation,
        null);
    schema.addMatchingRule(matchingRule, overwrite);
  }

  public void addMatchingRule(String definition, boolean overwrite)
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

    List<String> names = Collections.emptyList();
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
      else if(tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed by
        // either a single definition in single quotes or an open parenthesis
        // followed by one or more values in single quotes separated by spaces
        // followed by a close parenthesis.
        if(extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName,
            SchemaUtils.readExtensions(reader));
      }
      else
      {
        Message message = ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
      }
    }

    // Make sure that a syntax was specified.
    if (syntax == null)
    {
      Message message = ERR_ATTR_SYNTAX_MR_NO_SYNTAX.get(definition);
      throw new DecodeException(message);
    }


    schema.addMatchingRule(
        schema.new CachingMatchingRule(oid, names, description,
                                       isObsolete, syntax, extraProperties,
                                       definition), overwrite);
  }

  public void addMatchingRuleUse(String oid,
                                 List<String> names,
                                 String description,
                                 boolean obsolete,
                                 Set<String> attributeOIDs,
                                 Map<String, List<String>> extraProperties,
                                 boolean overwrite)
      throws SchemaException
  {
    MatchingRuleUse use = schema.new CachingMatchingRuleUse(oid, names,
        description, obsolete, attributeOIDs, extraProperties, null);
    schema.addMatchingRuleUse(use, overwrite);
  }

  public void addMatchingRuleUse(String definition, boolean overwrite)
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
      Message message = ERR_ATTR_SYNTAX_MRUSE_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }


    // The next character must be an open parenthesis.  If it is not, then that
    // is an error.
    char c = reader.read();
    if (c != '(')
    {
      Message message = ERR_ATTR_SYNTAX_MRUSE_EXPECTED_OPEN_PARENTHESIS.
          get(definition, (reader.pos()-1), String.valueOf(c));
      throw new DecodeException(message);
    }


    // Skip over any spaces immediately following the opening parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    String oid = SchemaUtils.readNumericOID(reader);

    List<String> names = Collections.emptyList();
    String description = "".intern();
    boolean isObsolete = false;
    Set<String> attributes = null;
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
      else if (tokenName.equalsIgnoreCase("applies"))
      {
        attributes = SchemaUtils.readOIDs(reader);
      }
      else if(tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed by
        // either a single definition in single quotes or an open parenthesis
        // followed by one or more values in single quotes separated by spaces
        // followed by a close parenthesis.
        if(extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName,
            SchemaUtils.readExtensions(reader));
      }
      else
      {
        Message message = ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
      }
    }

    // Make sure that the set of attributes was defined.
    if (attributes == null || attributes.size() == 0)
    {
      Message message = ERR_ATTR_SYNTAX_MRUSE_NO_ATTR.get(definition);
      throw new DecodeException(message);
    }

    MatchingRuleUse use = schema.new CachingMatchingRuleUse(oid, names,
        description, isObsolete, attributes, extraProperties, definition);
    schema.addMatchingRuleUse(use, overwrite);
  }

  public void addAttributeType(String oid, List<String> names,
                               String description, boolean obsolete,
                               String superiorType, String equalityMatchingRule,
                               String orderingMatchingRule,
                               String substringMatchingRule,
                               String approximateMatchingRule,
                               String syntax, boolean singleValue,
                               boolean collective, boolean noUserModification,
                               AttributeUsage attributeUsage,
                               Map<String, List<String>> extraProperties,
                               boolean overwrite)
      throws SchemaException
  {
    AttributeType attrType = schema.new CachingAttributeType(
        oid, names, description, obsolete, superiorType,
        equalityMatchingRule, orderingMatchingRule, substringMatchingRule,
        approximateMatchingRule, syntax, singleValue, collective,
        noUserModification, attributeUsage, extraProperties, null);
    schema.addAttributeType(attrType, overwrite);
  }

  public void addAttributeType(String definition, boolean overwrite)
      throws DecodeException, SchemaException
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
        syntax = SchemaUtils.readNumericOIDLen(reader);
      }
      else if (tokenName.equalsIgnoreCase("single-definition"))
      {
        // This indicates that attributes of this type are allowed to have at
        // most one definition.  We do not need any more parsing for this token.
        isSingleValue = true;
      }
      else if (tokenName.equalsIgnoreCase("single-value"))
      {
        // This indicates that attributes of this type are allowed to have at
        // most one value.  We do not need any more parsing for this token.
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
      else if(tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed by
        // either a single definition in single quotes or an open parenthesis
        // followed by one or more values in single quotes separated by spaces
        // followed by a close parenthesis.
        if(extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName,
            SchemaUtils.readExtensions(reader));
      }
      else
      {
        Message message = ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
      }
    }

    List<String> approxRules =
        extraProperties.get(SCHEMA_PROPERTY_APPROX_RULE);
    if ((approxRules != null) && (! approxRules.isEmpty()))
    {
      approximateMatchingRule = approxRules.get(0);
    }

    AttributeType attrType = schema.new CachingAttributeType(
        oid, names, description, isObsolete, superiorType,
        equalityMatchingRule, orderingMatchingRule, substringMatchingRule,
        approximateMatchingRule, syntax, isSingleValue, isCollective,
        isNoUserModification, attributeUsage, extraProperties, definition);

    schema.addAttributeType(attrType, overwrite);
  }

  public void addDITContentRule(String structuralClass,
                                List<String> names,
                                String description,
                                boolean obsolete,
                                Set<String> auxiliaryClasses,
                                Set<String> optionalAttributes,
                                Set<String> prohibitedAttributes,
                                Set<String> requiredAttributes,
                                Map<String, List<String>> extraProperties,
                                boolean overwrite)
      throws SchemaException
  {
    DITContentRule rule = schema.new CachingDITContentRule(structuralClass, names,
        description, obsolete, auxiliaryClasses, optionalAttributes,
        prohibitedAttributes, requiredAttributes, extraProperties, null);
    schema.addDITContentRule(rule, overwrite);
  }

  public void addDITStructureRule(Integer ruleID,
                                  List<String> names,
                                  String description,
                                  boolean obsolete,
                                  String nameForm,
                                  Set<Integer> superiorRules,
                                  Map<String, List<String>> extraProperties,
                                  boolean overwrite)
      throws SchemaException
  {
    DITStructureRule rule = schema.new CachingDITStructureRule(ruleID, names,
        description, obsolete, nameForm, superiorRules, extraProperties,
        null);
    schema.addDITStructureRule(rule, overwrite);
  }

  public void addNameForm(String oid,
                          List<String> names,
                          String description,
                          boolean obsolete,
                          String structuralClass,
                          Set<String> requiredAttributes,
                          Set<String> optionalAttributes,
                          Map<String, List<String>> extraProperties,
                          boolean overwrite)
      throws SchemaException
  {
    NameForm nameForm = schema.new CachingNameForm(oid, names, description,
        obsolete, structuralClass, requiredAttributes, optionalAttributes,
        extraProperties, null);
    schema.addNameForm(nameForm, overwrite);
  }

  public void addDITContentRule(String definition, boolean overwrite)
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

    List<String> names = Collections.emptyList();
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
      else if(tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed by
        // either a single definition in single quotes or an open parenthesis
        // followed by one or more values in single quotes separated by spaces
        // followed by a close parenthesis.
        if(extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName,
            SchemaUtils.readExtensions(reader));
      }
      else
      {
        Message message = ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
      }
    }

    DITContentRule rule = schema.new CachingDITContentRule(structuralClass, names,
        description, isObsolete, auxiliaryClasses, optionalAttributes,
        prohibitedAttributes, requiredAttributes, extraProperties,
        definition);
    schema.addDITContentRule(rule, overwrite);
  }

  public void addDITStructureRule(String definition, boolean overwrite)
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
      else if(tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed by
        // either a single definition in single quotes or an open parenthesis
        // followed by one or more values in single quotes separated by spaces
        // followed by a close parenthesis.
        if(extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName,
            SchemaUtils.readExtensions(reader));
      }
      else
      {
        Message message = ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
      }
    }

    if (nameForm == null)
    {
      Message message = ERR_ATTR_SYNTAX_DSR_NO_NAME_FORM.get(definition);
      throw new DecodeException(message);
    }

    DITStructureRule rule = schema.new CachingDITStructureRule(ruleID, names,
        description, isObsolete, nameForm, superiorRules, extraProperties,
        definition);
    schema.addDITStructureRule(rule, overwrite);
  }

  public void addNameForm(String definition, boolean overwrite)
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

    List<String> names = Collections.emptyList();
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
      else if (tokenName.equalsIgnoreCase("oc"))
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
      else if(tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed by
        // either a single definition in single quotes or an open parenthesis
        // followed by one or more values in single quotes separated by spaces
        // followed by a close parenthesis.
        if(extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName,
            SchemaUtils.readExtensions(reader));
      }
      else
      {
        Message message = ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
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

    NameForm nameForm = schema.new CachingNameForm(oid, names, description,
        isObsolete, structuralClass, requiredAttributes, optionalAttributes,
        extraProperties, definition);
    schema.addNameForm(nameForm, overwrite);
  }

  public void addObjectClass(String oid,
                             List<String> names,
                             String description,
                             boolean obsolete,
                             Set<String> superiorClassOIDs,
                             Set<String> requiredAttributeOIDs,
                             Set<String> optionalAttributeOIDs,
                             ObjectClassType objectClassType,
                             Map<String, List<String>> extraProperties,
                             boolean overwrite)
      throws SchemaException
  {
    ObjectClass c;
    if(oid.equals(OID_EXTENSIBLE_OBJECT))
    {
      c = schema.new ExtensibleObjectClass(oid, names, description, obsolete,
          superiorClassOIDs, requiredAttributeOIDs, optionalAttributeOIDs,
          objectClassType, extraProperties, null);
    }
    else
    {
      c = schema.new CachingObjectClass(oid, names, description, obsolete,
          superiorClassOIDs, requiredAttributeOIDs, optionalAttributeOIDs,
          objectClassType, extraProperties, null);
    }
    schema.addObjectClass(c, overwrite);
  }

  public void addObjectClass(String definition, boolean overwrite)
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

    List<String> names = Collections.emptyList();
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
      else if(tokenName.matches("^X-[A-Za-z_-]+$"))
      {
        // This must be a non-standard property and it must be followed by
        // either a single definition in single quotes or an open parenthesis
        // followed by one or more values in single quotes separated by spaces
        // followed by a close parenthesis.
        if(extraProperties.isEmpty())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName,
            SchemaUtils.readExtensions(reader));
      }
      else
      {
        Message message = ERR_ATTR_SYNTAX_ILLEGAL_TOKEN.get(tokenName);
        throw new DecodeException(message);
      }
    }

    ObjectClass objectClass;
    if(oid.equals(OID_EXTENSIBLE_OBJECT))
    {
      objectClass = schema.new ExtensibleObjectClass(oid, names, description,
          isObsolete, superiorClasses, requiredAttributes, optionalAttributes,
          objectClassType, extraProperties, definition);
    }
    else
    {
      objectClass = schema.new CachingObjectClass(oid, names, description,
          isObsolete, superiorClasses, requiredAttributes, optionalAttributes,
          objectClassType, extraProperties, definition);
    }
    schema.addObjectClass(objectClass, overwrite);
  }

  public boolean removeAttributeType(String oid)
  {
    if(schema.hasAttributeType(oid))
    {
      schema.removeAttributeType(schema.getAttributeType(oid));
      return true;
    }
    return false;
  }

  public boolean removeDITContentRule(String numericOID)
  {
    if(schema.hasDITContentRule(numericOID))
    {
      schema.removeDITContentRule(schema.getDITContentRule(numericOID));
      return true;
    }
    return false;
  }

  public boolean removeDITStructureRule(Integer ruleID)
  {
    if(schema.hasDITStructureRule(ruleID))
    {
      schema.removeDITStructureRule(schema.getDITStructureRule(ruleID));
      return true;
    }
    return false;
  }

  public boolean removeMatchingRule(String oid)
  {
    if(schema.hasMatchingRule(oid))
    {
      schema.removeMatchingRule(schema.getMatchingRule(oid));
      return true;
    }
    return false;
  }

  public boolean removeMatchingRuleUse(String numericOID)
  {
    if(schema.hasMatchingRuleUse(numericOID))
    {
      schema.removeMatchingRuleUse(schema.getMatchingRuleUse(numericOID));
      return true;
    }
    return false;
  }

  public boolean removeNameForm(String oid)
  {
    if(schema.hasNameForm(oid))
    {
      schema.removeNameForm(schema.getNameForm(oid));
      return true;
    }
    return false;
  }

  public boolean removeObjectClass(String oid)
  {
    if(schema.hasObjectClass(oid))
    {
      schema.removeObjectClass(schema.getObjectClass(oid));
      return true;
    }
    return false;
  }

  public boolean removeSyntax(String numericOID)
  {
    if(schema.hasSyntax(numericOID))
    {
      schema.removeSyntax(schema.getSyntax(numericOID));
      return true;
    }
    return false;
  }

  public Schema toSchema(List<Message> warnings)
  {
    Validator.ensureNotNull(warnings);
    schema.validate(warnings);
    return schema;
  }

  public Schema toSchema() throws SchemaException
  {
    schema.validate();
    return schema;
  }
}
