package org.opends.schema;

import org.opends.schema.syntaxes.*;
import org.opends.schema.matchingrules.*;

import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 16, 2009
 * Time: 3:31:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoreSchema extends SchemaBuilder
{
  public static final CoreSchema INSTANCE = new CoreSchema();
  
  private static final String EMPTY_STRING = "".intern();
  private static final List<String> EMPTY_STRING_LIST =
      Collections.emptyList();

  public void initializeSyntaxes() throws SchemaException
  {
    // All RFC 4512
    addSyntax(new AttributeTypeSyntax(), true);
    addSyntax(new BinarySyntax(), true);
    addSyntax(new BitStringSyntax(), true);
    addSyntax(new BooleanSyntax(), true);
    addSyntax(new CertificateListSyntax(), true);
    addSyntax(new CertificatePairSyntax(), true);
    addSyntax(new CertificateSyntax(), true);
    addSyntax(new CountryStringSyntax(), true);
    addSyntax(new DeliveryMethodSyntax(), true);
    addSyntax(new DirectoryStringSyntax(false), true);
    addSyntax(new DITContentRuleSyntax(), true);
    addSyntax(new DITStructureRuleSyntax(), true);
    addSyntax(new EnhancedGuideSyntax(), true);
    addSyntax(new FacsimileNumberSyntax(), true);
    addSyntax(new FaxSyntax(), true);
    addSyntax(new GeneralizedTimeSyntax(), true);
    addSyntax(new GuideSyntax(), true);
    addSyntax(new IA5StringSyntax(), true);
    addSyntax(new IntegerSyntax(), true);
    addSyntax(new JPEGSyntax(), true);
    addSyntax(new MatchingRuleSyntax(), true);
    addSyntax(new MatchingRuleUseSyntax(), true);
    addSyntax(new NameAndOptionalUIDSyntax(), true);
    addSyntax(new NameFormSyntax(), true);
    addSyntax(new NumericStringSyntax(), true);
    addSyntax(new ObjectClassSyntax(), true);
    addSyntax(new OctetStringSyntax(), true);
    addSyntax(new OIDSyntax(), true);
    addSyntax(new OtherMailboxSyntax(), true);
    addSyntax(new PostalAddressSyntax(), true);
    addSyntax(new PresentationAddressSyntax(), true);
    addSyntax(new PrintableStringSyntax(), true);
    addSyntax(new ProtocolInformationSyntax(), true);
    addSyntax(new SubstringAssertionSyntax(), true);
    addSyntax(new SupportedAlgorithmSyntax(), true);
    addSyntax(new TelephoneNumberSyntax(false), true);
    addSyntax(new TeletexTerminalIdentifierSyntax(), true);
    addSyntax(new TelexNumberSyntax(), true);
    addSyntax(new UTCTimeSyntax(), true);

    // Extras
    addSyntax(new UUIDSyntax(), true);
  }

  public void initializeMatchingRules() throws SchemaException
  {
    addMatchingRule(new BitStringEqualityMatchingRule(), true);
    addMatchingRule(new BooleanEqualityMatchingRule(), true);
    addMatchingRule(new CaseExactIA5EqualityMatchingRule(), true);
    addMatchingRule(new CaseExactEqualityMatchingRule(), true);
    addMatchingRule(new CaseExactOrderingMatchingRule(), true);
    addMatchingRule(new CaseExactSubstringMatchingRule(), true);
    addMatchingRule(new CaseIgnoreIA5EqualityMatchingRule(), true);
    addMatchingRule(new CaseIgnoreIA5SubstringMatchingRule(), true);
    addMatchingRule(new CaseIgnoreListEqualityMatchingRule(), true);
    addMatchingRule(new CaseIgnoreListSubstringMatchingRule(), true);
    addMatchingRule(new CaseIgnoreEqualityMatchingRule(), true);
    addMatchingRule(new CaseIgnoreOrderingMatchingRule(), true);
    addMatchingRule(new CaseIgnoreSubstringMatchingRule(), true);
    addMatchingRule(new DirectoryStringFirstComponentEqualityMatchingRule(),
        true);
    addMatchingRule(new DistinguishedNameEqualityMatchingRule(), true);
    addMatchingRule(new GeneralizedTimeEqualityMatchingRule(), true);
    addMatchingRule(new GeneralizedTimeOrderingMatchingRule(), true);
    addMatchingRule(new IntegerFirstComponentEqualityMatchingRule(), true);
    addMatchingRule(new IntegerEqualityMatchingRule(), true);
    addMatchingRule(new IntegerOrderingMatchingRule(), true);
    addMatchingRule(new KeywordEqualityMatchingRule(), true);
    addMatchingRule(new NumericStringEqualityMatchingRule(), true);
    addMatchingRule(new NumericStringOrderingMatchingRule(), true);
    addMatchingRule(new NumericStringSubstringMatchingRule(), true);
    addMatchingRule(new ObjectIdentifierFirstComponentEqualityMatchingRule(),
        true);
    addMatchingRule(new ObjectIdentifierEqualityMatchingRule(), true);
    addMatchingRule(new OctetStringEqualityMatchingRule(), true);
    addMatchingRule(new OctetStringOrderingMatchingRule(), true);
    addMatchingRule(new TelephoneNumberEqualityMatchingRule(), true);
    addMatchingRule(new TelephoneNumberSubstringMatchingRule(), true);
    addMatchingRule(new UniqueMemberEqualityMatchingRule(), true);
    addMatchingRule(new WordEqualityMatchingRule(), true);
  }

  public void initializeAttributeTypes() throws SchemaException
  {
    addAttributeType(
        new AttributeType("2.5.4.0",
            Collections.singletonList("objectClass"),
            EMPTY_STRING,
            false,
            null,
            "objectIdentifierMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.38",
            false,
            false,
            false,
            AttributeUsage.USER_APPLICATIONS,
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("2.5.4.1",
            Collections.singletonList("aliasedObjectName"),
            EMPTY_STRING,
            false,
            null,
            "distinguishedNameMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.12",
            true,
            false,
            false,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("2.5.18.1",
            Collections.singletonList("createTimestamp"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("2.5.18.2",
            Collections.singletonList("modifyTimestamp"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("2.5.18.3",
            Collections.singletonList("creatorsName"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("2.5.18.4",
            Collections.singletonList("modifiersName"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("2.5.18.10",
            Collections.singletonList("subschemaSubentry"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("2.5.21.5",
            Collections.singletonList("attributeTypes"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("2.5.21.6",
            Collections.singletonList("objectClasses"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("2.5.21.4",
            Collections.singletonList("matchingRules"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("2.5.21.8",
            Collections.singletonList("matchingRuleUse"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("2.5.21.9",
            Collections.singletonList("structuralObjectClass"),
            EMPTY_STRING,
            false,
            null,
            "objectIdentifierMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.38",
            true,
            false,
            true,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("2.5.21.10",
            Collections.singletonList("governingStructureRule"),
            EMPTY_STRING,
            false,
            null,
            "integerMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.27",
            true,
            false,
            true,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("1.3.6.1.4.1.1466.101.120.5",
            Collections.singletonList("namingContexts"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("1.3.6.1.4.1.1466.101.120.6",
            Collections.singletonList("altServer"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("1.3.6.1.4.1.1466.101.120.7",
            Collections.singletonList("supportedExtension"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("1.3.6.1.4.1.1466.101.120.13",
            Collections.singletonList("supportedControl"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("1.3.6.1.4.1.1466.101.120.14",
            Collections.singletonList("supportedSASLMechanisms"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("1.3.6.1.4.1.4203.1.3.5",
            Collections.singletonList("supportedFeatures"),
            EMPTY_STRING,
            false,
            null,
            "objectIdentifierMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.38",
            false,
            false,
            false,
            AttributeUsage.DSA_OPERATION,
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("1.3.6.1.4.1.1466.101.120.15",
            Collections.singletonList("supportedLDAPVersion"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("1.3.6.1.4.1.1466.101.120.16",
            Collections.singletonList("ldapSyntaxes"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("2.5.21.1",
            Collections.singletonList("dITStructureRules"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("2.5.21.7",
            Collections.singletonList("nameForms"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);

    addAttributeType(
        new AttributeType("2.5.21.2",
            Collections.singletonList("dITContentRules"),
            EMPTY_STRING,
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
            SchemaUtils.RFC4512_ORIGIN), true);
  }

  public void initializeObjectClasses() throws SchemaException
  {
    addObjectClass(
        new ObjectClass("2.5.6.0",
        Collections.singletonList("top"),
        EMPTY_STRING,
        false,
        EMPTY_STRING_LIST,
        Collections.singletonList("objectClass"),
        EMPTY_STRING_LIST,
        ObjectClassType.ABSTRACT,
        SchemaUtils.RFC4512_ORIGIN), true);

    addObjectClass(
        new ObjectClass("2.5.6.1",
        Collections.singletonList("alias"),
        EMPTY_STRING,
        false,
        Collections.singletonList("top"),
        Collections.singletonList("aliasedObjectName"),
        EMPTY_STRING_LIST,
        ObjectClassType.STRUCTURAL,
        SchemaUtils.RFC4512_ORIGIN), true);

    addObjectClass(
        new ObjectClass("1.3.6.1.4.1.1466.101.120.111",
        Collections.singletonList("extensibleObject"),
        EMPTY_STRING,
        false,
        Collections.singletonList("top"),
        Collections.singletonList("aliasedObjectName"),
        EMPTY_STRING_LIST,
        ObjectClassType.AUXILIARY,
        SchemaUtils.RFC4512_ORIGIN), true);

    List<String> subschemaAttrs = new ArrayList<String>();
    subschemaAttrs.add("dITStructureRules");
    subschemaAttrs.add("nameForms");
    subschemaAttrs.add("ditContentRules");
    subschemaAttrs.add("objectClasses");
    subschemaAttrs.add("attributeTypes");
    subschemaAttrs.add("matchingRules");
    subschemaAttrs.add("matchingRuleUse");

    addObjectClass(
        new ObjectClass("2.5.20.1",
        Collections.singletonList("subschema"),
        EMPTY_STRING,
        false,
        Collections.singletonList("top"),
        EMPTY_STRING_LIST,
        subschemaAttrs,
        ObjectClassType.AUXILIARY,
        SchemaUtils.RFC4512_ORIGIN), true);
  }
}
