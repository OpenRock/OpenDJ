package org.opends.schema;

import static org.opends.server.schema.SchemaConstants.*;
import static org.opends.server.util.ServerConstants.OID_EXTENSIBLE_OBJECT;
import static org.opends.server.util.ServerConstants.SCHEMA_PROPERTY_ORIGIN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.opends.schema.matchingrules.ApproximateMatchingRuleImplementation;
import org.opends.schema.matchingrules.BitStringEqualityMatchingRule;
import org.opends.schema.matchingrules.BooleanEqualityMatchingRule;
import org.opends.schema.matchingrules.CaseExactEqualityMatchingRule;
import org.opends.schema.matchingrules.CaseExactIA5EqualityMatchingRule;
import org.opends.schema.matchingrules.CaseExactOrderingMatchingRule;
import org.opends.schema.matchingrules.CaseExactSubstringMatchingRule;
import org.opends.schema.matchingrules.CaseIgnoreEqualityMatchingRule;
import org.opends.schema.matchingrules.CaseIgnoreIA5EqualityMatchingRule;
import org.opends.schema.matchingrules.CaseIgnoreIA5SubstringMatchingRule;
import org.opends.schema.matchingrules.CaseIgnoreListEqualityMatchingRule;
import org.opends.schema.matchingrules.CaseIgnoreListSubstringMatchingRule;
import org.opends.schema.matchingrules.CaseIgnoreOrderingMatchingRule;
import org.opends.schema.matchingrules.CaseIgnoreSubstringMatchingRule;
import org.opends.schema.matchingrules.DirectoryStringFirstComponentEqualityMatchingRule;
import org.opends.schema.matchingrules.DistinguishedNameEqualityMatchingRule;
import org.opends.schema.matchingrules.DoubleMetaphoneApproximateMatchingRule;
import org.opends.schema.matchingrules.EqualLengthApproximateMatchingRule;
import org.opends.schema.matchingrules.EqualityMatchingRuleImplementation;
import org.opends.schema.matchingrules.GeneralizedTimeEqualityMatchingRule;
import org.opends.schema.matchingrules.GeneralizedTimeOrderingMatchingRule;
import org.opends.schema.matchingrules.IntegerEqualityMatchingRule;
import org.opends.schema.matchingrules.IntegerFirstComponentEqualityMatchingRule;
import org.opends.schema.matchingrules.IntegerOrderingMatchingRule;
import org.opends.schema.matchingrules.KeywordEqualityMatchingRule;
import org.opends.schema.matchingrules.NumericStringEqualityMatchingRule;
import org.opends.schema.matchingrules.NumericStringOrderingMatchingRule;
import org.opends.schema.matchingrules.NumericStringSubstringMatchingRule;
import org.opends.schema.matchingrules.ObjectIdentifierEqualityMatchingRule;
import org.opends.schema.matchingrules.ObjectIdentifierFirstComponentEqualityMatchingRule;
import org.opends.schema.matchingrules.OctetStringEqualityMatchingRule;
import org.opends.schema.matchingrules.OctetStringOrderingMatchingRule;
import org.opends.schema.matchingrules.OrderingMatchingRuleImplementation;
import org.opends.schema.matchingrules.SubstringMatchingRuleImplementation;
import org.opends.schema.matchingrules.TelephoneNumberEqualityMatchingRule;
import org.opends.schema.matchingrules.TelephoneNumberSubstringMatchingRule;
import org.opends.schema.matchingrules.UniqueMemberEqualityMatchingRule;
import org.opends.schema.matchingrules.WordEqualityMatchingRule;
import org.opends.schema.syntaxes.AttributeTypeSyntax;
import org.opends.schema.syntaxes.BinarySyntax;
import org.opends.schema.syntaxes.BitStringSyntax;
import org.opends.schema.syntaxes.BooleanSyntax;
import org.opends.schema.syntaxes.CertificateListSyntax;
import org.opends.schema.syntaxes.CertificatePairSyntax;
import org.opends.schema.syntaxes.CertificateSyntax;
import org.opends.schema.syntaxes.CountryStringSyntax;
import org.opends.schema.syntaxes.DITContentRuleSyntax;
import org.opends.schema.syntaxes.DITStructureRuleSyntax;
import org.opends.schema.syntaxes.DeliveryMethodSyntax;
import org.opends.schema.syntaxes.DirectoryStringSyntax;
import org.opends.schema.syntaxes.DistinguishedNameSyntax;
import org.opends.schema.syntaxes.EnhancedGuideSyntax;
import org.opends.schema.syntaxes.FacsimileNumberSyntax;
import org.opends.schema.syntaxes.FaxSyntax;
import org.opends.schema.syntaxes.GeneralizedTimeSyntax;
import org.opends.schema.syntaxes.GuideSyntax;
import org.opends.schema.syntaxes.IA5StringSyntax;
import org.opends.schema.syntaxes.IntegerSyntax;
import org.opends.schema.syntaxes.JPEGSyntax;
import org.opends.schema.syntaxes.LDAPSyntaxDescriptionSyntax;
import org.opends.schema.syntaxes.MatchingRuleSyntax;
import org.opends.schema.syntaxes.MatchingRuleUseSyntax;
import org.opends.schema.syntaxes.NameAndOptionalUIDSyntax;
import org.opends.schema.syntaxes.NameFormSyntax;
import org.opends.schema.syntaxes.NumericStringSyntax;
import org.opends.schema.syntaxes.OIDSyntax;
import org.opends.schema.syntaxes.ObjectClassSyntax;
import org.opends.schema.syntaxes.OctetStringSyntax;
import org.opends.schema.syntaxes.OtherMailboxSyntax;
import org.opends.schema.syntaxes.PostalAddressSyntax;
import org.opends.schema.syntaxes.PresentationAddressSyntax;
import org.opends.schema.syntaxes.PrintableStringSyntax;
import org.opends.schema.syntaxes.ProtocolInformationSyntax;
import org.opends.schema.syntaxes.SubstringAssertionSyntax;
import org.opends.schema.syntaxes.SupportedAlgorithmSyntax;
import org.opends.schema.syntaxes.SyntaxImplementation;
import org.opends.schema.syntaxes.TelephoneNumberSyntax;
import org.opends.schema.syntaxes.TeletexTerminalIdentifierSyntax;
import org.opends.schema.syntaxes.TelexNumberSyntax;
import org.opends.schema.syntaxes.UTCTimeSyntax;
import org.opends.schema.syntaxes.UUIDSyntax;
import org.opends.util.StaticUtils;

public class CoreSchema extends Schema
{
  private static final Map<String, List<String>> RFC4512_ORIGIN =
      Collections.singletonMap(SCHEMA_PROPERTY_ORIGIN,
          Collections.singletonList("RFC 4512"));
  private static final Map<String, List<String>> RFC4517_ORIGIN =
      Collections.singletonMap(SCHEMA_PROPERTY_ORIGIN,
          Collections.singletonList("RFC 4517"));
  private static final Map<String, List<String>> RFC4530_ORIGIN =
      Collections.singletonMap(SCHEMA_PROPERTY_ORIGIN,
          Collections.singletonList("RFC 4530"));
  public static final Map<String, List<String>> OPENDS_ORIGIN =
      Collections.singletonMap(SCHEMA_PROPERTY_ORIGIN,
          Collections.singletonList("OpenDS Directory Server"));
  private static final String EMPTY_STRING = "".intern();
  private static final Set<String> EMPTY_STRING_SET =
      Collections.emptySet();

  private static final CoreSchema SINGLETON = new CoreSchema();

  private CoreSchema()
  {
    defaultSyntaxes();
    defaultMatchingRules();
    defaultAttributeTypes();
    defaultObjectClasses();

    try
    {
      validate();
    }
    catch(SchemaException se)
    {
      throw new RuntimeException(se);
    }
  }

  public static Schema instance()
  {
    return SINGLETON;
  }

  private void defaultSyntaxes()
  {
    // All RFC 4512 / 4517
    addSyntax(SYNTAX_ATTRIBUTE_TYPE_OID,
        SYNTAX_ATTRIBUTE_TYPE_DESCRIPTION,
        RFC4512_ORIGIN, new AttributeTypeSyntax());
    addSyntax(SYNTAX_BINARY_OID, SYNTAX_BINARY_DESCRIPTION,
        RFC4512_ORIGIN, new BinarySyntax());
    addSyntax(SYNTAX_BIT_STRING_OID, SYNTAX_ATTRIBUTE_TYPE_DESCRIPTION,
        RFC4512_ORIGIN, new BitStringSyntax());
    addSyntax(SYNTAX_BOOLEAN_OID, SYNTAX_BOOLEAN_DESCRIPTION,
        RFC4512_ORIGIN, new BooleanSyntax());
    addSyntax(SYNTAX_CERTLIST_OID, SYNTAX_CERTLIST_DESCRIPTION,
        RFC4512_ORIGIN, new CertificateListSyntax());
    addSyntax(SYNTAX_CERTPAIR_OID, SYNTAX_CERTPAIR_DESCRIPTION,
        RFC4512_ORIGIN, new CertificatePairSyntax());
    addSyntax(SYNTAX_CERTIFICATE_OID, SYNTAX_CERTIFICATE_DESCRIPTION,
        RFC4512_ORIGIN, new CertificateSyntax());
    addSyntax(SYNTAX_COUNTRY_STRING_OID,
        SYNTAX_COUNTRY_STRING_DESCRIPTION,
        RFC4512_ORIGIN, new CountryStringSyntax());
    addSyntax(SYNTAX_DELIVERY_METHOD_OID,
        SYNTAX_DELIVERY_METHOD_DESCRIPTION,
        RFC4512_ORIGIN, new DeliveryMethodSyntax());
    addSyntax(SYNTAX_DIRECTORY_STRING_OID,
        SYNTAX_DIRECTORY_STRING_DESCRIPTION,
        RFC4512_ORIGIN, new DirectoryStringSyntax(false));
    addSyntax(SYNTAX_DIT_CONTENT_RULE_OID,
        SYNTAX_DIT_CONTENT_RULE_DESCRIPTION,
        RFC4512_ORIGIN, new DITContentRuleSyntax());
    addSyntax(SYNTAX_DIT_STRUCTURE_RULE_OID,
        SYNTAX_DIT_STRUCTURE_RULE_DESCRIPTION,
        RFC4512_ORIGIN, new DITStructureRuleSyntax());
    addSyntax(SYNTAX_DN_OID, SYNTAX_DN_DESCRIPTION,
        RFC4512_ORIGIN, new DistinguishedNameSyntax());
    addSyntax(SYNTAX_ENHANCED_GUIDE_OID,
        SYNTAX_ENHANCED_GUIDE_DESCRIPTION,
        RFC4512_ORIGIN, new EnhancedGuideSyntax());
    addSyntax(SYNTAX_FAXNUMBER_OID, SYNTAX_FAXNUMBER_DESCRIPTION,
        RFC4512_ORIGIN, new FacsimileNumberSyntax());
    addSyntax(SYNTAX_FAX_OID, SYNTAX_FAX_DESCRIPTION,
        RFC4512_ORIGIN, new FaxSyntax());
    addSyntax(SYNTAX_GENERALIZED_TIME_OID,
        SYNTAX_GENERALIZED_TIME_DESCRIPTION,
        RFC4512_ORIGIN, new GeneralizedTimeSyntax());
    addSyntax(SYNTAX_GUIDE_OID, SYNTAX_GUIDE_DESCRIPTION,
        RFC4512_ORIGIN, new GuideSyntax());
    addSyntax(SYNTAX_IA5_STRING_OID, SYNTAX_IA5_STRING_DESCRIPTION,
        RFC4512_ORIGIN, new IA5StringSyntax());
    addSyntax(SYNTAX_INTEGER_OID, SYNTAX_INTEGER_DESCRIPTION,
        RFC4512_ORIGIN, new IntegerSyntax());
    addSyntax(SYNTAX_JPEG_OID, SYNTAX_JPEG_DESCRIPTION,
        RFC4512_ORIGIN, new JPEGSyntax());
    addSyntax(SYNTAX_MATCHING_RULE_OID,
        SYNTAX_MATCHING_RULE_DESCRIPTION,
        RFC4512_ORIGIN, new MatchingRuleSyntax());
    addSyntax(SYNTAX_MATCHING_RULE_USE_OID,
        SYNTAX_MATCHING_RULE_USE_DESCRIPTION,
        RFC4512_ORIGIN, new MatchingRuleUseSyntax());
    addSyntax(SYNTAX_LDAP_SYNTAX_OID, SYNTAX_LDAP_SYNTAX_DESCRIPTION,
        RFC4512_ORIGIN, new LDAPSyntaxDescriptionSyntax());
    addSyntax(SYNTAX_NAME_AND_OPTIONAL_UID_OID,
        SYNTAX_NAME_AND_OPTIONAL_UID_DESCRIPTION,
        RFC4517_ORIGIN, new NameAndOptionalUIDSyntax());
    addSyntax(SYNTAX_NAME_FORM_OID, SYNTAX_NAME_FORM_DESCRIPTION,
        RFC4512_ORIGIN, new NameFormSyntax());
    addSyntax(SYNTAX_NUMERIC_STRING_OID,
        SYNTAX_NUMERIC_STRING_DESCRIPTION,
        RFC4512_ORIGIN, new NumericStringSyntax());
    addSyntax(SYNTAX_OBJECTCLASS_OID, SYNTAX_OBJECTCLASS_DESCRIPTION,
        RFC4512_ORIGIN, new ObjectClassSyntax());
    addSyntax(SYNTAX_OCTET_STRING_OID, SYNTAX_OCTET_STRING_DESCRIPTION,
        RFC4512_ORIGIN, new OctetStringSyntax());
    addSyntax(SYNTAX_OID_OID, SYNTAX_OID_DESCRIPTION,
        RFC4512_ORIGIN, new OIDSyntax());
    addSyntax(SYNTAX_OTHER_MAILBOX_OID,
        SYNTAX_OTHER_MAILBOX_DESCRIPTION,
        RFC4512_ORIGIN, new OtherMailboxSyntax());
    addSyntax(SYNTAX_POSTAL_ADDRESS_OID,
        SYNTAX_POSTAL_ADDRESS_DESCRIPTION,
        RFC4512_ORIGIN, new PostalAddressSyntax());
    addSyntax(SYNTAX_PRESENTATION_ADDRESS_OID,
        SYNTAX_PRESENTATION_ADDRESS_DESCRIPTION,
        RFC4512_ORIGIN, new PresentationAddressSyntax());
    addSyntax(SYNTAX_PRINTABLE_STRING_OID,
        SYNTAX_PRINTABLE_STRING_DESCRIPTION,
        RFC4512_ORIGIN, new PrintableStringSyntax());
    addSyntax(SYNTAX_PROTOCOL_INFORMATION_OID,
        SYNTAX_PROTOCOL_INFORMATION_DESCRIPTION,
        RFC4512_ORIGIN, new ProtocolInformationSyntax());
    addSyntax(SYNTAX_SUBSTRING_ASSERTION_OID,
        SYNTAX_SUBSTRING_ASSERTION_DESCRIPTION,
        RFC4512_ORIGIN, new SubstringAssertionSyntax());
    addSyntax(SYNTAX_SUPPORTED_ALGORITHM_OID,
        SYNTAX_SUPPORTED_ALGORITHM_DESCRIPTION,
        RFC4512_ORIGIN, new SupportedAlgorithmSyntax());
    addSyntax(SYNTAX_TELEPHONE_OID, SYNTAX_TELEPHONE_DESCRIPTION,
        RFC4512_ORIGIN, new TelephoneNumberSyntax(false));
    addSyntax(SYNTAX_TELETEX_TERM_ID_OID,
        SYNTAX_TELETEX_TERM_ID_DESCRIPTION,
        RFC4512_ORIGIN, new TeletexTerminalIdentifierSyntax());
    addSyntax(SYNTAX_TELEX_OID, SYNTAX_TELEX_DESCRIPTION,
        RFC4512_ORIGIN, new TelexNumberSyntax());
    addSyntax(SYNTAX_UTC_TIME_OID, SYNTAX_UTC_TIME_DESCRIPTION,
        RFC4512_ORIGIN, new UTCTimeSyntax());

    // Extras
    addSyntax(SYNTAX_UUID_OID, SYNTAX_UUID_DESCRIPTION,
        RFC4530_ORIGIN, new UUIDSyntax());
  }

  private void defaultMatchingRules()
  {
    addMatchingRule(EMR_BIT_STRING_OID,
        SchemaUtils.singletonSortedSet(EMR_BIT_STRING_NAME),
        EMPTY_STRING, false, SYNTAX_BIT_STRING_OID, RFC4512_ORIGIN,
        new BitStringEqualityMatchingRule());
    addMatchingRule(EMR_BOOLEAN_OID,
        SchemaUtils.singletonSortedSet(EMR_BOOLEAN_NAME),
        EMPTY_STRING, false, SYNTAX_BOOLEAN_OID, RFC4512_ORIGIN,
        new BooleanEqualityMatchingRule());
    addMatchingRule(EMR_CASE_EXACT_IA5_OID,
        SchemaUtils.singletonSortedSet(EMR_CASE_EXACT_IA5_NAME),
        EMPTY_STRING, false, SYNTAX_IA5_STRING_OID, RFC4512_ORIGIN,
        new CaseExactIA5EqualityMatchingRule());
    addMatchingRule(EMR_CASE_EXACT_OID,
        SchemaUtils.singletonSortedSet(EMR_CASE_EXACT_NAME),
        EMPTY_STRING, false, SYNTAX_DIRECTORY_STRING_OID,
        RFC4512_ORIGIN, new CaseExactEqualityMatchingRule());
    addMatchingRule(OMR_CASE_EXACT_OID,
        SchemaUtils.singletonSortedSet(OMR_CASE_EXACT_NAME),
        EMPTY_STRING, false, SYNTAX_DIRECTORY_STRING_OID,
        RFC4512_ORIGIN, new CaseExactOrderingMatchingRule());
    addMatchingRule(SMR_CASE_EXACT_OID,
        SchemaUtils.singletonSortedSet(SMR_CASE_EXACT_NAME),
        EMPTY_STRING, false, SYNTAX_SUBSTRING_ASSERTION_OID,
        RFC4512_ORIGIN, new CaseExactSubstringMatchingRule());
    addMatchingRule(EMR_CASE_IGNORE_IA5_OID,
        SchemaUtils.singletonSortedSet(EMR_CASE_IGNORE_IA5_NAME),
        EMPTY_STRING, false, SYNTAX_IA5_STRING_OID,
        RFC4512_ORIGIN, new CaseIgnoreIA5EqualityMatchingRule());
    addMatchingRule(SMR_CASE_IGNORE_IA5_OID,
        SchemaUtils.singletonSortedSet(SMR_CASE_IGNORE_IA5_NAME),
        EMPTY_STRING, false, SYNTAX_SUBSTRING_ASSERTION_OID,
        RFC4512_ORIGIN, new CaseIgnoreIA5SubstringMatchingRule());
    addMatchingRule(EMR_CASE_IGNORE_LIST_OID,
        SchemaUtils.singletonSortedSet(EMR_CASE_IGNORE_LIST_NAME),
        EMPTY_STRING, false, SYNTAX_POSTAL_ADDRESS_OID,
        RFC4512_ORIGIN, new CaseIgnoreListEqualityMatchingRule());
    addMatchingRule(SMR_CASE_IGNORE_LIST_OID,
        SchemaUtils.singletonSortedSet(SMR_CASE_IGNORE_LIST_NAME),
        EMPTY_STRING, false, SYNTAX_SUBSTRING_ASSERTION_OID,
        RFC4512_ORIGIN, new CaseIgnoreListSubstringMatchingRule());
    addMatchingRule(EMR_CASE_IGNORE_OID,
        SchemaUtils.singletonSortedSet(EMR_CASE_IGNORE_NAME),
        EMPTY_STRING, false, SYNTAX_DIRECTORY_STRING_OID,
        RFC4512_ORIGIN, new CaseIgnoreEqualityMatchingRule());
    addMatchingRule(OMR_CASE_IGNORE_OID,
        SchemaUtils.singletonSortedSet(OMR_CASE_IGNORE_NAME),
        EMPTY_STRING, false,  SYNTAX_DIRECTORY_STRING_OID,
        RFC4512_ORIGIN, new CaseIgnoreOrderingMatchingRule());
    addMatchingRule(SMR_CASE_IGNORE_OID,
        SchemaUtils.singletonSortedSet(SMR_CASE_IGNORE_NAME),
        EMPTY_STRING, false, SYNTAX_SUBSTRING_ASSERTION_OID,
        RFC4512_ORIGIN, new CaseIgnoreSubstringMatchingRule());
    addMatchingRule(EMR_DIRECTORY_STRING_FIRST_COMPONENT_OID,
        SchemaUtils.singletonSortedSet(
            EMR_DIRECTORY_STRING_FIRST_COMPONENT_NAME), EMPTY_STRING, false,
        SYNTAX_DIRECTORY_STRING_OID,  RFC4512_ORIGIN,
        new DirectoryStringFirstComponentEqualityMatchingRule());
    addMatchingRule(EMR_DN_OID,
        SchemaUtils.singletonSortedSet(EMR_DN_NAME), EMPTY_STRING, false,
        SYNTAX_DN_OID, RFC4512_ORIGIN,
        new DistinguishedNameEqualityMatchingRule());
    addMatchingRule(EMR_GENERALIZED_TIME_OID,
        SchemaUtils.singletonSortedSet(EMR_GENERALIZED_TIME_NAME),
        EMPTY_STRING, false, SYNTAX_GENERALIZED_TIME_OID,
        RFC4512_ORIGIN, new GeneralizedTimeEqualityMatchingRule());
    addMatchingRule(OMR_GENERALIZED_TIME_OID,
        SchemaUtils.singletonSortedSet(OMR_GENERALIZED_TIME_NAME),
        EMPTY_STRING, false, SYNTAX_GENERALIZED_TIME_OID,
        RFC4512_ORIGIN, new GeneralizedTimeOrderingMatchingRule());
    addMatchingRule(EMR_INTEGER_FIRST_COMPONENT_OID,
        SchemaUtils.singletonSortedSet(EMR_INTEGER_FIRST_COMPONENT_NAME),
        EMPTY_STRING, false, SYNTAX_INTEGER_OID, RFC4512_ORIGIN,
        new IntegerFirstComponentEqualityMatchingRule());
    addMatchingRule(EMR_INTEGER_OID,
        SchemaUtils.singletonSortedSet(EMR_INTEGER_NAME),
        EMPTY_STRING, false, SYNTAX_INTEGER_OID, RFC4512_ORIGIN,
        new IntegerEqualityMatchingRule());
    addMatchingRule(OMR_INTEGER_OID,
        SchemaUtils.singletonSortedSet(OMR_INTEGER_NAME),
        EMPTY_STRING, false, SYNTAX_INTEGER_OID, RFC4512_ORIGIN,
        new IntegerOrderingMatchingRule());
    addMatchingRule(EMR_KEYWORD_OID,
        SchemaUtils.singletonSortedSet(EMR_KEYWORD_NAME),
        EMPTY_STRING, false, SYNTAX_DIRECTORY_STRING_OID,
        RFC4512_ORIGIN, new KeywordEqualityMatchingRule());
    addMatchingRule(EMR_NUMERIC_STRING_OID,
        SchemaUtils.singletonSortedSet(EMR_NUMERIC_STRING_NAME),
        EMPTY_STRING, false, SYNTAX_NUMERIC_STRING_OID,
        RFC4512_ORIGIN, new NumericStringEqualityMatchingRule());
    addMatchingRule(OMR_NUMERIC_STRING_OID,
        SchemaUtils.singletonSortedSet(OMR_NUMERIC_STRING_NAME),
        EMPTY_STRING, false, SYNTAX_NUMERIC_STRING_OID,
        RFC4512_ORIGIN, new NumericStringOrderingMatchingRule());
    addMatchingRule(SMR_NUMERIC_STRING_OID,
        SchemaUtils.singletonSortedSet(SMR_NUMERIC_STRING_NAME),
        EMPTY_STRING, false, SYNTAX_SUBSTRING_ASSERTION_OID,
        RFC4512_ORIGIN, new NumericStringSubstringMatchingRule());
    addMatchingRule(EMR_OID_FIRST_COMPONENT_OID,
        SchemaUtils.singletonSortedSet(EMR_OID_FIRST_COMPONENT_NAME),
        EMPTY_STRING, false, SYNTAX_OID_OID, RFC4512_ORIGIN,
        new ObjectIdentifierFirstComponentEqualityMatchingRule());
    addMatchingRule(EMR_OID_OID,
        SchemaUtils.singletonSortedSet(EMR_OID_NAME),
        EMPTY_STRING, false, SYNTAX_OID_OID, RFC4512_ORIGIN,
        new ObjectIdentifierEqualityMatchingRule());
    addMatchingRule(EMR_OCTET_STRING_OID,
        SchemaUtils.singletonSortedSet(EMR_OCTET_STRING_NAME),
        EMPTY_STRING, false, SYNTAX_OCTET_STRING_OID,
        RFC4512_ORIGIN, new OctetStringEqualityMatchingRule());
    addMatchingRule(OMR_OCTET_STRING_OID,
        SchemaUtils.singletonSortedSet(OMR_OCTET_STRING_NAME),
        EMPTY_STRING, false, SYNTAX_OCTET_STRING_OID,
        RFC4512_ORIGIN, new OctetStringOrderingMatchingRule());
    addMatchingRule(EMR_TELEPHONE_OID,
        SchemaUtils.singletonSortedSet(EMR_TELEPHONE_NAME),
        EMPTY_STRING, false, SYNTAX_TELEPHONE_OID, RFC4512_ORIGIN,
        new TelephoneNumberEqualityMatchingRule());
    addMatchingRule(SMR_TELEPHONE_OID,
        SchemaUtils.singletonSortedSet(SMR_TELEPHONE_NAME),
        EMPTY_STRING, false, SYNTAX_SUBSTRING_ASSERTION_OID,
        RFC4512_ORIGIN, new TelephoneNumberSubstringMatchingRule());
    addMatchingRule(EMR_UNIQUE_MEMBER_OID,
        SchemaUtils.singletonSortedSet(EMR_UNIQUE_MEMBER_NAME),
        EMPTY_STRING, false, SYNTAX_NAME_AND_OPTIONAL_UID_OID,
        RFC4512_ORIGIN, new UniqueMemberEqualityMatchingRule());
    addMatchingRule(EMR_WORD_OID,
        SchemaUtils.singletonSortedSet(EMR_WORD_NAME),
        EMPTY_STRING, false, SYNTAX_DIRECTORY_STRING_OID,
        RFC4512_ORIGIN, new WordEqualityMatchingRule());
    addMatchingRule(AMR_DOUBLE_METAPHONE_OID,
        SchemaUtils.singletonSortedSet(AMR_DOUBLE_METAPHONE_NAME),
        AMR_DOUBLE_METAPHONE_DESCRIPTION, false, SYNTAX_DIRECTORY_STRING_OID,
        OPENDS_ORIGIN, new DoubleMetaphoneApproximateMatchingRule());
    addMatchingRule("1.3.6.1.4.1.26027.1.999.26",
        SchemaUtils.singletonSortedSet("equalLengthApproximateMatch"),
        EMPTY_STRING, false, SYNTAX_DIRECTORY_STRING_OID,
        OPENDS_ORIGIN, new EqualLengthApproximateMatchingRule());
  }

  private void defaultAttributeTypes()
  {
    addAttributeType("2.5.4.0",
        SchemaUtils.singletonSortedSet("objectClass"),
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
        RFC4512_ORIGIN);

    addAttributeType("2.5.4.1",
        SchemaUtils.singletonSortedSet("aliasedObjectName"),
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
        RFC4512_ORIGIN);

    addAttributeType("2.5.18.1",
        SchemaUtils.singletonSortedSet("createTimestamp"),
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
        RFC4512_ORIGIN);

    addAttributeType("2.5.18.2",
        SchemaUtils.singletonSortedSet("modifyTimestamp"),
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
        RFC4512_ORIGIN);

    addAttributeType("2.5.18.3",
        SchemaUtils.singletonSortedSet("creatorsName"),
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
        RFC4512_ORIGIN);

    addAttributeType("2.5.18.4",
        SchemaUtils.singletonSortedSet("modifiersName"),
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
        RFC4512_ORIGIN);

    addAttributeType("2.5.18.10",
        SchemaUtils.singletonSortedSet("subschemaSubentry"),
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
        RFC4512_ORIGIN);

    addAttributeType("2.5.21.5",
        SchemaUtils.singletonSortedSet("attributeTypes"),
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
        RFC4512_ORIGIN);

    addAttributeType("2.5.21.6",
        SchemaUtils.singletonSortedSet("objectClasses"),
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
        RFC4512_ORIGIN);

    addAttributeType("2.5.21.4",
        SchemaUtils.singletonSortedSet("matchingRules"),
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
        RFC4512_ORIGIN);

    addAttributeType("2.5.21.8",
        SchemaUtils.singletonSortedSet("matchingRuleUse"),
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
        RFC4512_ORIGIN);

    addAttributeType("2.5.21.9",
        SchemaUtils.singletonSortedSet("structuralObjectClass"),
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
        RFC4512_ORIGIN);

    addAttributeType("2.5.21.10",
        SchemaUtils.singletonSortedSet("governingStructureRule"),
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
        RFC4512_ORIGIN);

    addAttributeType("1.3.6.1.4.1.1466.101.120.5",
        SchemaUtils.singletonSortedSet("namingContexts"),
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
        RFC4512_ORIGIN);

    addAttributeType("1.3.6.1.4.1.1466.101.120.6",
        SchemaUtils.singletonSortedSet("altServer"),
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
        RFC4512_ORIGIN);

    addAttributeType("1.3.6.1.4.1.1466.101.120.7",
        SchemaUtils.singletonSortedSet("supportedExtension"),
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
        RFC4512_ORIGIN);

    addAttributeType("1.3.6.1.4.1.1466.101.120.13",
        SchemaUtils.singletonSortedSet("supportedControl"),
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
        RFC4512_ORIGIN);

    addAttributeType("1.3.6.1.4.1.1466.101.120.14",
        SchemaUtils.singletonSortedSet("supportedSASLMechanisms"),
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
        RFC4512_ORIGIN);

    addAttributeType("1.3.6.1.4.1.4203.1.3.5",
        SchemaUtils.singletonSortedSet("supportedFeatures"),
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
        RFC4512_ORIGIN);

    addAttributeType("1.3.6.1.4.1.1466.101.120.15",
        SchemaUtils.singletonSortedSet("supportedLDAPVersion"),
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
        RFC4512_ORIGIN);

    addAttributeType("1.3.6.1.4.1.1466.101.120.16",
        SchemaUtils.singletonSortedSet("ldapSyntaxes"),
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
        RFC4512_ORIGIN);

    addAttributeType("2.5.21.1",
        SchemaUtils.singletonSortedSet("dITStructureRules"),
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
        RFC4512_ORIGIN);

    addAttributeType("2.5.21.7",
        SchemaUtils.singletonSortedSet("nameForms"),
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
        RFC4512_ORIGIN);

    addAttributeType("2.5.21.2",
        SchemaUtils.singletonSortedSet("dITContentRules"),
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
        RFC4512_ORIGIN);
  }

  private void defaultObjectClasses()
  {
    addObjectClass("2.5.6.0",
        SchemaUtils.singletonSortedSet("top"),
        EMPTY_STRING,
        false,
        EMPTY_STRING_SET,
        SchemaUtils.singletonSortedSet("objectClass"),
        EMPTY_STRING_SET,
        ObjectClassType.ABSTRACT,
        RFC4512_ORIGIN);

    addObjectClass("2.5.6.1",
        SchemaUtils.singletonSortedSet("alias"),
        EMPTY_STRING,
        false,
        SchemaUtils.singletonSortedSet("top"),
        SchemaUtils.singletonSortedSet("aliasedObjectName"),
        EMPTY_STRING_SET,
        ObjectClassType.STRUCTURAL,
        RFC4512_ORIGIN);

    addObjectClass("1.3.6.1.4.1.1466.101.120.111",
        SchemaUtils.singletonSortedSet("extensibleObject"),
        EMPTY_STRING,
        false,
        SchemaUtils.singletonSortedSet("top"),
        SchemaUtils.singletonSortedSet("aliasedObjectName"),
        EMPTY_STRING_SET,
        ObjectClassType.AUXILIARY,
        RFC4512_ORIGIN);

    Set<String> subschemaAttrs = new HashSet<String>();
    subschemaAttrs.add("dITStructureRules");
    subschemaAttrs.add("nameForms");
    subschemaAttrs.add("ditContentRules");
    subschemaAttrs.add("objectClasses");
    subschemaAttrs.add("attributeTypes");
    subschemaAttrs.add("matchingRules");
    subschemaAttrs.add("matchingRuleUse");

    addObjectClass("2.5.20.1",
        SchemaUtils.singletonSortedSet("subschema"),
        EMPTY_STRING,
        false,
        SchemaUtils.singletonSortedSet("top"),
        EMPTY_STRING_SET,
        subschemaAttrs,
        ObjectClassType.AUXILIARY,
        RFC4512_ORIGIN);
  }

  private void addSyntax(String oid, String description,
                         Map<String, List<String>> extraProperties,
                         SyntaxImplementation implementation)
  {
    numericOID2Syntaxes.put(oid, new CachingSyntax(oid, description,
        extraProperties,implementation, null));
  }

  private void addMatchingRule(String oid,
                               SortedSet<String> names,
                               String description,
                               boolean obsolete,
                               String syntax,
                               Map<String, List<String>> extraProperties,
                               EqualityMatchingRuleImplementation implementation)
  {
    MatchingRule matchingRule = new CachingEqualityMatchingRule(oid,
        names, description, obsolete, syntax, extraProperties, implementation,
        null);

    addMatchingRule(matchingRule);
  }

  private void addMatchingRule(String oid,
                               SortedSet<String> names,
                               String description,
                               boolean obsolete,
                               String syntax,
                               Map<String, List<String>> extraProperties,
                               OrderingMatchingRuleImplementation implementation)
  {
    MatchingRule matchingRule = new CachingOrderingMatchingRule(oid,
        names, description, obsolete, syntax, extraProperties, implementation,
        null);

    addMatchingRule(matchingRule);
  }

  private void addMatchingRule(String oid,
                               SortedSet<String> names,
                               String description,
                               boolean obsolete,
                               String syntax,
                               Map<String, List<String>> extraProperties,
                               SubstringMatchingRuleImplementation implementation)
  {
    MatchingRule matchingRule = new CachingSubstringMatchingRule(oid,
        names, description, obsolete, syntax, extraProperties, implementation,
        null);

    addMatchingRule(matchingRule);
  }

  private void addMatchingRule(String oid,
                               SortedSet<String> names,
                               String description,
                               boolean obsolete,
                               String syntax,
                               Map<String, List<String>> extraProperties,
                               ApproximateMatchingRuleImplementation implementation)
  {
    MatchingRule matchingRule = new CachingApproximateMatchingRule(oid,
        names, description, obsolete, syntax, extraProperties, implementation,
        null);

    addMatchingRule(matchingRule);
  }

  private void addMatchingRule(MatchingRule matchingRule)
  {
    numericOID2MatchingRules.put(matchingRule.oid, matchingRule);
    for(String name : matchingRule.names)
    {
      String lowerName = StaticUtils.toLowerCase(name);
      List<MatchingRule> rules;
      if((rules = name2MatchingRules.get(lowerName)) == null)
      {
        name2MatchingRules.put(lowerName,
            Collections.singletonList(matchingRule));
      }
      else if(rules.size() == 1)
      {
        rules = new ArrayList<MatchingRule>(rules);
        rules.add(matchingRule);
        name2MatchingRules.put(lowerName, rules);
      }
      else
      {
        rules.add(matchingRule);
      }
    }
  }

  private void addAttributeType(String oid, SortedSet<String> names,
                                String description, boolean obsolete,
                                String superiorType, String equalityMatchingRule,
                                String orderingMatchingRule,
                                String substringMatchingRule,
                                String approximateMatchingRule,
                                String syntax, boolean singleValue,
                                boolean collective, boolean noUserModification,
                                AttributeUsage attributeUsage,
                                Map<String, List<String>> extraProperties)
  {
    AttributeType attrType = new CachingAttributeType(
        oid, names, description, obsolete, superiorType,
        equalityMatchingRule, orderingMatchingRule, substringMatchingRule,
        approximateMatchingRule, syntax, singleValue, collective,
        noUserModification, attributeUsage, extraProperties, null);

    numericOID2AttributeTypes.put(oid, attrType);
    for(String name : names)
    {
      String lowerName = StaticUtils.toLowerCase(name);
      List<AttributeType> attrs;
      if((attrs = name2AttributeTypes.get(lowerName)) == null)
      {
        name2AttributeTypes.put(lowerName,
            Collections.singletonList(attrType));
      }
      else if(attrs.size() == 1)
      {
        attrs = new ArrayList<AttributeType>(attrs);
        attrs.add(attrType);
        name2AttributeTypes.put(lowerName, attrs);
      }
      else
      {
        attrs.add(attrType);
      }
    }
  }

  private void addObjectClass(String oid,
                              SortedSet<String> names,
                              String description,
                              boolean obsolete,
                              Set<String> superiorClassOIDs,
                              Set<String> requiredAttributeOIDs,
                              Set<String> optionalAttributeOIDs,
                              ObjectClassType objectClassType,
                              Map<String, List<String>> extraProperties)
  {
    ObjectClass oc;
    if(oid.equals(OID_EXTENSIBLE_OBJECT))
    {
      oc = new ExtensibleObjectClass(oid, names, description, obsolete,
          superiorClassOIDs, requiredAttributeOIDs, optionalAttributeOIDs,
          objectClassType, extraProperties, null);
    }
    else
    {
      oc = new CachingObjectClass(oid, names, description, obsolete,
          superiorClassOIDs, requiredAttributeOIDs, optionalAttributeOIDs,
          objectClassType, extraProperties, null);
    }

    numericOID2ObjectClasses.put(oid, oc);
    for(String name : names)
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

  public boolean isStrict() {
    return true;
  }
}
