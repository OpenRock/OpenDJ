package org.opends.schema;

import org.opends.schema.syntaxes.*;
import static org.opends.server.util.ServerConstants.SCHEMA_PROPERTY_ORIGIN;
import org.opends.server.types.*;
import org.opends.util.SubstringReader;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_ATTRSYNTAX_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_ATTRSYNTAX_EXPECTED_OPEN_PARENTHESIS;
import org.opends.ldap.DecodeException;

import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 15, 2009
 * Time: 5:48:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultSyntaxFactory implements SyntaxFactory
{
  private Map<String, SyntaxDescription> syntaxes;
  private Map<String, AttributeType> attributeTypes;
  private Map<String, ObjectClass> objectClasses;

  private DefaultSyntaxFactory()
  {
    syntaxes = new HashMap<String, SyntaxDescription>();
  }

  public void initializeRFC4512Syntaxes()
  {
    registerSyntax(new AttributeTypeSyntax());
    registerSyntax(new BinarySyntax());
    registerSyntax(new BitStringSyntax());
    registerSyntax(new BooleanSyntax());
    registerSyntax(new CertificateListSyntax());
    registerSyntax(new CertificatePairSyntax());
    registerSyntax(new CertificateSyntax());
    registerSyntax(new CountryStringSyntax());
    registerSyntax(new DeliveryMethodSyntax());
    registerSyntax(new DirectoryStringSyntax(false));
    registerSyntax(new DITContentRuleSyntax());
    registerSyntax(new DITStructureRuleSyntax());
    registerSyntax(new EnhancedGuideSyntax());
    registerSyntax(new FacsimileNumberSyntax());
    registerSyntax(new FaxSyntax());
    registerSyntax(new GeneralizedTimeSyntax());
    registerSyntax(new GuideSyntax());
    registerSyntax(new IA5StringSyntax());
    registerSyntax(new IntegerSyntax());
    registerSyntax(new JPEGSyntax());
    registerSyntax(new MatchingRuleSyntax());
    registerSyntax(new MatchingRuleUseSyntax());
    registerSyntax(new NameAndOptionalUIDSyntax());
    registerSyntax(new NameFormSyntax());
    registerSyntax(new NumericStringSyntax());
    registerSyntax(new ObjectClassSyntax());
    registerSyntax(new OctetStringSyntax());
    registerSyntax(new OIDSyntax());
    registerSyntax(new OtherMailboxSyntax());
    registerSyntax(new PostalAddressSyntax());
    registerSyntax(new PresentationAddressSyntax());
    registerSyntax(new PrintableStringSyntax());
    registerSyntax(new ProtocolInformationSyntax());
    registerSyntax(new SubstringAssertionSyntax());
    registerSyntax(new SupportedAlgorithmSyntax());
    registerSyntax(new TelephoneNumberSyntax(false));
    registerSyntax(new TeletexTerminalIdentifierSyntax());
    registerSyntax(new TelexNumberSyntax());
    registerSyntax(new UTCTimeSyntax());
  }

  public void initializeOtherSyntaxes()
  {
    registerSyntax(new UUIDSyntax());
  }

  public void initializeRFC2252AttributeTypes()
  {
    new AttributeType("2.5.18.1",
        Collections.singletonList("createTimestamp"),
        "",
        false,
        null,
        "generalizedTimeMatch",
        "generalizedTimeOrderingMatch",
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.24",
        true,
        false,
        true,
        AttributeUsage.DIRECTORY_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("2.5.18.2",
        Collections.singletonList("modifyTimestamp"),
        "",
        false,
        null,
        "generalizedTimeMatch",
        "generalizedTimeOrderingMatch",
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.24",
        true,
        false,
        true,
        AttributeUsage.DIRECTORY_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("2.5.18.3",
        Collections.singletonList("creatorsName"),
        "",
        false,
        null,
        "distinguishedNameMatch",
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.12",
        true,
        false,
        true,
        AttributeUsage.DIRECTORY_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("2.5.18.4",
        Collections.singletonList("modifiersName"),
        "",
        false,
        null,
        "distinguishedNameMatch",
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.12",
        true,
        false,
        true,
        AttributeUsage.DIRECTORY_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("2.5.18.10",
        Collections.singletonList("subschemaSubentry"),
        "",
        false,
        null,
        "distinguishedNameMatch",
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.12",
        true,
        false,
        true,
        AttributeUsage.DIRECTORY_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("2.5.21.5",
        Collections.singletonList("attributeTypes"),
        "",
        false,
        null,
        "objectIdentifierFirstComponentMatch",
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.3",
        false,
        false,
        false,
        AttributeUsage.DIRECTORY_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("2.5.21.6",
        Collections.singletonList("objectClasses"),
        "",
        false,
        null,
        "objectIdentifierFirstComponentMatch",
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.37",
        false,
        false,
        false,
        AttributeUsage.DIRECTORY_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("2.5.21.4",
        Collections.singletonList("matchingRules"),
        "",
        false,
        null,
        "objectIdentifierFirstComponentMatch",
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.30",
        false,
        false,
        false,
        AttributeUsage.DIRECTORY_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("2.5.21.8",
        Collections.singletonList("matchingRuleUse"),
        "",
        false,
        null,
        "objectIdentifierFirstComponentMatch",
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.31",
        false,
        false,
        false,
        AttributeUsage.DIRECTORY_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("1.3.6.1.4.1.1466.101.120.5",
        Collections.singletonList("namingContexts"),
        "",
        false,
        null,
        null,
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.12",
        false,
        false,
        false,
        AttributeUsage.DSA_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("1.3.6.1.4.1.1466.101.120.6",
        Collections.singletonList("altServer"),
        "",
        false,
        null,
        null,
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.26",
        false,
        false,
        false,
        AttributeUsage.DSA_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("1.3.6.1.4.1.1466.101.120.7",
        Collections.singletonList("supportedExtension"),
        "",
        false,
        null,
        null,
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.38",
        false,
        false,
        false,
        AttributeUsage.DSA_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("1.3.6.1.4.1.1466.101.120.7",
        Collections.singletonList("supportedExtension"),
        "",
        false,
        null,
        null,
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.38",
        false,
        false,
        false,
        AttributeUsage.DSA_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("1.3.6.1.4.1.1466.101.120.13",
        Collections.singletonList("supportedControl"),
        "",
        false,
        null,
        null,
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.38",
        false,
        false,
        false,
        AttributeUsage.DSA_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("1.3.6.1.4.1.1466.101.120.14",
        Collections.singletonList("supportedSASLMechanisms"),
        "",
        false,
        null,
        null,
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.15",
        false,
        false,
        false,
        AttributeUsage.DSA_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("1.3.6.1.4.1.1466.101.120.15",
        Collections.singletonList("supportedLDAPVersion"),
        "",
        false,
        null,
        null,
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.27",
        false,
        false,
        false,
        AttributeUsage.DSA_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("1.3.6.1.4.1.1466.101.120.16",
        Collections.singletonList("ldapSyntaxes"),
        "",
        false,
        null,
        "objectIdentifierFirstComponentMatch",
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.54",
        false,
        false,
        false,
        AttributeUsage.DIRECTORY_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("2.5.21.1",
        Collections.singletonList("dITStructureRules"),
        "",
        false,
        null,
        "integerFirstComponentMatch",
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.17",
        false,
        false,
        false,
        AttributeUsage.DIRECTORY_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("2.5.21.7",
        Collections.singletonList("nameForms"),
        "",
        false,
        null,
        "objectIdentifierFirstComponentMatch",
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.35",
        false,
        false,
        false,
        AttributeUsage.DIRECTORY_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);

    new AttributeType("2.5.21.2",
        Collections.singletonList("dITContentRules"),
        "",
        false,
        null,
        "objectIdentifierFirstComponentMatch",
        null,
        null,
        null,
        "1.3.6.1.4.1.1466.115.121.1.16",
        false,
        false,
        false,
        AttributeUsage.DIRECTORY_OPERATION,
        SchemaUtils.RFC4512_ORIGIN);
  }

  public SyntaxDescription registerSyntax(SyntaxDescription syntax)
  {
    return syntaxes.put(syntax.getOID(), syntax);
  }

  public void registerAttributeType(AttributeType attributeType,
                                    boolean overwrite)
  {
    if(!attributeTypes.containsKey(attributeType.getOID()))
    {
      attributeTypes.put(attributeType.getOID(), attributeType);
      for(String name : attributeType.getNames())
      {
        attributeTypes.put(name.toLowerCase(), attributeType);
      }
    }
  }

  public SyntaxDescription decode(String definition)
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

    String description = null;
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

    for(String property : extraProperties.keySet())
    {
      if(property.equalsIgnoreCase("x-subst"))
      {
        List<String> values = extraProperties.get(property);
        if(!values.isEmpty())
        {
          SyntaxDescription substitute = syntaxes.get(values.get(0));
          if(substitute != null)
          {
            return new SubstitutionSyntax(oid, description,
                extraProperties, definition, substitute);
          }
          else
          {
            // TODO: Warn
          }
        }
      }
      else if(property.equalsIgnoreCase("x-pattern"))
      {

      }
    }

    // This is just a static syntax
    SyntaxDescription syntax = getSyntax(oid);
    if(syntax == null)
    {
      // Use a default substitute?
    }
    else if(description != null || !extraProperties.isEmpty())
    {
      // There are some customizations.
      // Use a wrapper
    }

    return syntax;
  }

  public SyntaxDescription getSyntax(String oid) {
    return syntaxes.get(oid);
  }
}
