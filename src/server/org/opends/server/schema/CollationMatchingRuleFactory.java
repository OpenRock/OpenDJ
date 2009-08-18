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
 *      Copyright 2008-2009 Sun Microsystems, Inc.
 */

package org.opends.server.schema;



import static org.opends.messages.ConfigMessages.*;
import static org.opends.messages.CoreMessages.*;
import static org.opends.messages.SchemaMessages.*;
import org.opends.server.backends.index.IndexConfig;
import org.opends.server.backends.index.KeySet;
import static org.opends.server.loggers.ErrorLogger.*;
import static org.opends.server.schema.SchemaConstants.*;
import org.opends.server.types.Attribute;
import static org.opends.server.util.ServerConstants.*;
import static org.opends.server.util.StaticUtils.*;
import static org.opends.server.backends.index.MatchingRuleIndexProvider.*;

import java.nio.CharBuffer;
import java.text.CollationKey;
import java.text.Collator;
import java.util.*;

import org.opends.messages.Message;
import org.opends.server.admin.server.ConfigurationChangeListener;
import org.opends.server.backends.index.MatchingRuleIndexProvider;
import org.opends.server.admin.std.meta.
  CollationMatchingRuleCfgDefn.MatchingRuleType;
import org.opends.server.admin.std.server.CollationMatchingRuleCfg;
import org.opends.server.api.EqualityMatchingRule;
import org.opends.server.backends.index.IndexQueryFactory;
import org.opends.server.api.MatchingRule;
import org.opends.server.api.MatchingRuleFactory;
import org.opends.server.api.OrderingMatchingRule;
import org.opends.server.api.SubstringMatchingRule;
import org.opends.server.backends.index.IndexKeyFactory;
import org.opends.server.backends.index.SubstringIndexKeyFactory;
import org.opends.server.config.ConfigException;
import org.opends.server.core.DirectoryServer;
import org.opends.server.types.AttributeValue;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.types.ConditionResult;
import org.opends.server.types.ConfigChangeResult;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.InitializationException;
import org.opends.server.types.ResultCode;
import org.opends.server.util.StaticUtils;



/**
 * This class is a factory class for Collation matching rules. It
 * creates different matching rules based on the configuration entries.
 */
public final class CollationMatchingRuleFactory extends
    MatchingRuleFactory<CollationMatchingRuleCfg> implements
    ConfigurationChangeListener<CollationMatchingRuleCfg>
{

  // Whether equality matching rules are enabled.
  private boolean equalityMatchingRuleType;

  // Whether less-than matching rules are enabled.
  private boolean lessThanMatchingRuleType;

  // Whether less-than-equal-to matching rules are enabled.
  private boolean lessThanEqualToMatchingRuleType;

  // Whether less-than-equal-to matching rules are enabled.
  private boolean greaterThanMatchingRuleType;

  // Whether greater-than matching rules are enabled.
  private boolean greaterThanEqualToMatchingRuleType;

  // Whether greater-than-equal-to matching rules are enabled.
  private boolean substringMatchingRuleType;

  // Stores the list of available locales on this JVM.
  private static final Set<Locale> supportedLocales;

  // Current Configuration.
  private CollationMatchingRuleCfg currentConfig;

  // Map of OID and the Collation equality matching Rules.
  private final Map<String,
          CollationEqualityMatchingRule> equalityMatchingRules;

  // Map of OID and the Collation ordering matching rules.
  private final Map<String,
          CollationOrderingMatchingRule> orderingMatchingRules;

  // Map of OID and the Collation substring matching rules.
  private final Map<String,
          CollationSubstringMatchingRule> substringMatchingRules;

  //Set of index providers.
  private Map<String,MatchingRuleIndexProvider> indexProviders;

  static
  {
    supportedLocales = new HashSet<Locale>();
    for (Locale l : Locale.getAvailableLocales())
    {
      supportedLocales.add(l);
    }
  }



  /**
   * Creates a new instance of CollationMatchingRuleFactory.
   */
  public CollationMatchingRuleFactory()
  {
    // Initialize the matchingRules.
    equalityMatchingRules = new HashMap<String, CollationEqualityMatchingRule>();
    orderingMatchingRules = new HashMap<String,CollationOrderingMatchingRule>();
    substringMatchingRules = new HashMap<String,CollationSubstringMatchingRule>();
    indexProviders = new HashMap<String,MatchingRuleIndexProvider>();
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public final Collection<MatchingRule> getMatchingRules()
  {
    Collection<MatchingRule> matchingRules = new ArrayList<MatchingRule>();
    matchingRules.addAll(equalityMatchingRules.values());
    matchingRules.addAll(orderingMatchingRules.values());
    matchingRules.addAll(substringMatchingRules.values());
    return Collections.unmodifiableCollection(matchingRules);
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<MatchingRuleIndexProvider> getIndexProvider()
  {
    return Collections.unmodifiableCollection(indexProviders.values());
  }



  /**
   * Clears the Map containing matching Rules.
   */
  private void resetRules()
  {
    equalityMatchingRules.clear();
    orderingMatchingRules.clear();
    substringMatchingRules.clear();
    indexProviders.clear();
  }



  /**
   * Reads the configuration and initializes matching rule types.
   *
   * @param ruleTypes
   *          The Set containing allowed matching rule types.
   */
  private void initializeMatchingRuleTypes(
      SortedSet<MatchingRuleType> ruleTypes)
  {
    for (MatchingRuleType type : ruleTypes)
    {
      switch (type)
      {
      case EQUALITY:
        equalityMatchingRuleType = true;
        break;
      case LESS_THAN:
        lessThanMatchingRuleType = true;
        break;
      case LESS_THAN_OR_EQUAL_TO:
        lessThanEqualToMatchingRuleType = true;
        break;
      case GREATER_THAN:
        greaterThanMatchingRuleType = true;
        break;
      case GREATER_THAN_OR_EQUAL_TO:
        greaterThanEqualToMatchingRuleType = true;
        break;
      case SUBSTRING:
        substringMatchingRuleType = true;
        break;
      default:
        // No default values allowed.
      }
    }
  }



  /**
   * Creates a new Collator instance.
   *
   * @param locale
   *          Locale for the collator
   * @return Returns a new Collator instance
   */
  private Collator createCollator(Locale locale)
  {
    Collator collator = Collator.getInstance(locale);
    collator.setStrength(Collator.PRIMARY);
    collator.setDecomposition(Collator.FULL_DECOMPOSITION);
    return collator;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public void initializeMatchingRule(
      CollationMatchingRuleCfg configuration) throws ConfigException,
      InitializationException
  {
    initializeMatchingRuleTypes(configuration.getMatchingRuleType());
    for (String collation : configuration.getCollation())
    {
      CollationMapper mapper = new CollationMapper(collation);

      String nOID = mapper.getNumericOID();
      String languageTag = mapper.getLanguageTag();
      if (nOID == null || languageTag == null)
      {
        Message msg =
            WARN_ATTR_INVALID_COLLATION_MATCHING_RULE_FORMAT
                .get(collation);
        logError(msg);
        continue;
      }

      Locale locale = getLocale(languageTag);
      if (locale != null)
      {
        createLessThanMatchingRule(mapper, locale);
        createLessThanOrEqualToMatchingRule(mapper, locale);
        createEqualityMatchingRule(mapper, locale);
        createGreaterThanOrEqualToMatchingRule(mapper, locale);
        createGreaterThanMatchingRule(mapper, locale);
        createSubstringMatchingRule(mapper, locale);
      }
      else
      {
        // This locale is not supported by JVM.
        Message msg =
            WARN_ATTR_INVALID_COLLATION_MATCHING_RULE_LOCALE.get(
                collation, configuration.dn().toNormalizedString(),
                languageTag);

        logError(msg);
      }
    }

    // Save this configuration.
    currentConfig = configuration;

    // Register for change events.
    currentConfig.addCollationChangeListener(this);
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public void finalizeMatchingRule()
  {
    // De-register the listener.
    currentConfig.removeCollationChangeListener(this);
  }



  /**
   * {@inheritDoc}
   */
  public ConfigChangeResult applyConfigurationChange(
      CollationMatchingRuleCfg configuration)
  {
    ResultCode resultCode = ResultCode.SUCCESS;
    boolean adminActionRequired = false;
    ArrayList<Message> messages = new ArrayList<Message>();

    if (!configuration.isEnabled()
        || currentConfig.isEnabled() != configuration.isEnabled())
    {
      // Don't do anything if:
      // 1. The configuration is disabled.
      // 2. There is a change in the enable status
      // i.e. (disable->enable or enable->disable). In this case, the
      // ConfigManager will have already created the new Factory object.
      return new ConfigChangeResult(resultCode, adminActionRequired,
          messages);
    }

    // Since we have come here it means that this Factory is enabled and
    // there is a change in the CollationMatchingRuleFactory's
    // configuration.
    // Deregister all the Matching Rule corresponding to this factory.
    for (MatchingRule rule : getMatchingRules())
    {
      DirectoryServer.deregisterMatchingRule(rule);
    }
    //deregister all the index providers.
    for(MatchingRuleIndexProvider provider : getIndexProvider())
    {
      DirectoryServer.deregisterIndexProvider(provider);
    }

    // Clear the associated matching rules.
    resetRules();

    initializeMatchingRuleTypes(configuration.getMatchingRuleType());
    for (String collation : configuration.getCollation())
    {
      CollationMapper mapper = new CollationMapper(collation);
      String languageTag = mapper.getLanguageTag();
      Locale locale = getLocale(languageTag);
      createLessThanMatchingRule(mapper, locale);
      createLessThanOrEqualToMatchingRule(mapper, locale);
      createEqualityMatchingRule(mapper, locale);
      createGreaterThanOrEqualToMatchingRule(mapper, locale);
      createGreaterThanMatchingRule(mapper, locale);
      createSubstringMatchingRule(mapper, locale);
    }

    try
    {
      for (MatchingRule matchingRule : getMatchingRules())
      {
        DirectoryServer.registerMatchingRule(matchingRule, false);
      }
      for(MatchingRuleIndexProvider provider : getIndexProvider())
      {
        DirectoryServer.registerIndexProvider(provider);
      }
    }
    catch (DirectoryException de)
    {
      Message message =
          WARN_CONFIG_SCHEMA_MR_CONFLICTING_MR.get(String
              .valueOf(configuration.dn()), de.getMessageObject());
      adminActionRequired = true;
      messages.add(message);
    }
    currentConfig = configuration;
    return new ConfigChangeResult(resultCode, adminActionRequired,
        messages);
  }



  /**
   * {@inheritDoc}
   */
  public boolean isConfigurationChangeAcceptable(
      CollationMatchingRuleCfg configuration,
      List<Message> unacceptableReasons)
  {
    boolean configAcceptable = true;

    // If the new configuration disables this factory, don't do
    // anything.
    if (!configuration.isEnabled())
    {
      return configAcceptable;
    }

    // If it comes here we don't need to verify MatchingRuleType; it
    // should be okay as its syntax is verified by the admin framework.
    // Iterate over the collations and verify if the format is okay.
    // Also,
    // verify if the locale is allowed by the JVM.
    for (String collation : configuration.getCollation())
    {
      CollationMapper mapper = new CollationMapper(collation);

      String nOID = mapper.getNumericOID();
      String languageTag = mapper.getLanguageTag();
      if (nOID == null || languageTag == null)
      {
        configAcceptable = false;
        Message msg =
            WARN_ATTR_INVALID_COLLATION_MATCHING_RULE_FORMAT
                .get(collation);
        unacceptableReasons.add(msg);
        continue;
      }

      Locale locale = getLocale(languageTag);
      if (locale == null)
      {
        Message msg =
            WARN_ATTR_INVALID_COLLATION_MATCHING_RULE_LOCALE.get(
                collation, configuration.dn().toNormalizedString(),
                languageTag);
        unacceptableReasons.add(msg);
        configAcceptable = false;
        continue;
      }
    }
    return configAcceptable;
  }



  /**
   * Creates Less-than Matching Rule.
   *
   * @param mapper
   *          CollationMapper containing OID and the language Tag.
   * @param locale
   *          Locale value
   */
  private void createLessThanMatchingRule(CollationMapper mapper,
      Locale locale)
  {
    if (!lessThanMatchingRuleType) return;

    String oid = mapper.getNumericOID() + ".1";
    String lTag = mapper.getLanguageTag();

    Collection<String> names = new HashSet<String>();
    CollationOrderingMatchingRule matchingRule =
            orderingMatchingRules.get(oid);
    if (matchingRule != null)
    {
      for (String name : matchingRule.getAllNames())
      {
        names.add(name);
      }
    }

    names.add(lTag + ".lt");
    names.add(lTag + ".1");

    matchingRule =
        new CollationLessThanMatchingRule(oid, names, locale);
    orderingMatchingRules.put(oid,matchingRule);
    indexProviders.put(oid,new CollationLessThanMatchingRuleIndexProvider(
            matchingRule,matchingRule.getIndexName()+"."+SHARED_INDEX_ID));
  }



  /**
   * Creates Less-Than-Equal-To Matching Rule.
   *
   * @param mapper
   *          CollationMapper containing OID and the language Tag.
   * @param locale
   *          Locale value
   */
  private void createLessThanOrEqualToMatchingRule(
      CollationMapper mapper, Locale locale)
  {
    if (!lessThanEqualToMatchingRuleType) return;

    String oid = mapper.getNumericOID() + ".2";
    String lTag = mapper.getLanguageTag();

    Collection<String> names = new HashSet<String>();
    CollationOrderingMatchingRule matchingRule =
            orderingMatchingRules.get(oid);
    if (matchingRule != null)
    {
      for (String name : matchingRule.getAllNames())
      {
        names.add(name);
      }
    }

    names.add(lTag + ".lte");
    names.add(lTag + ".2");

    matchingRule =
        new CollationLessThanOrEqualToMatchingRule(oid, names, locale);
    orderingMatchingRules.put(oid, matchingRule);
    indexProviders.put(oid,new CollationLessThanEqualToMatchingRuleIndexProvider(
            matchingRule,matchingRule.getIndexName()+"."+SHARED_INDEX_ID));
  }



  /**
   * Creates Equality Matching Rule.
   *
   * @param mapper
   *          CollationMapper containing OID and the language Tag.
   * @param locale
   *          Locale value
   */
  private void createEqualityMatchingRule(CollationMapper mapper,
      Locale locale)
  {
    if (!equalityMatchingRuleType)
    {
      return;
    }

    // Register the default OID as equality matching rule.
    String lTag = mapper.getLanguageTag();
    String nOID = mapper.getNumericOID();

    CollationEqualityMatchingRule matchingRule =
            equalityMatchingRules.get(nOID);
    Collection<String> defaultNames = new HashSet<String>();
    if (matchingRule != null)
    {
      for (String name : matchingRule.getAllNames())
      {
        defaultNames.add(name);
      }
    }

    defaultNames.add(lTag);
    matchingRule =
        new CollationEqualityMatchingRule(nOID,
                                      defaultNames, locale);
    equalityMatchingRules.put(nOID, matchingRule);
    indexProviders.put(nOID,new DefaultEqualityIndexProvider(
            matchingRule, matchingRule.getIndexName()+"."+SHARED_INDEX_ID));
    Collection<String> names = new HashSet<String>();
    // Register OID.3 as the equality matching rule.
    String OID = mapper.getNumericOID() + ".3";
    CollationEqualityMatchingRule equalityMatchingRule = equalityMatchingRules.get(OID);
    if (equalityMatchingRule != null)
    {
      for (String name : equalityMatchingRule.getAllNames())
      {
        names.add(name);
      }
    }

    names.add(lTag + ".eq");
    names.add(lTag + ".3");

    equalityMatchingRule =
        new CollationEqualityMatchingRule(OID, names, locale);
    equalityMatchingRules.put(OID, equalityMatchingRule);
    indexProviders.put(OID,new DefaultEqualityIndexProvider(
            equalityMatchingRule,equalityMatchingRule.getIndexName()+"."+
            SHARED_INDEX_ID));
  }



  /**
   * Creates Greater-than-equal-to Matching Rule.
   *
   * @param mapper
   *          CollationMapper containing OID and the language Tag.
   * @param locale
   *          Locale value
   */
  private void createGreaterThanOrEqualToMatchingRule(
      CollationMapper mapper, Locale locale)
  {
    if (!greaterThanEqualToMatchingRuleType) return;

    String oid = mapper.getNumericOID() + ".4";
    String lTag = mapper.getLanguageTag();

    Collection<String> names = new HashSet<String>();
    CollationOrderingMatchingRule matchingRule =
            orderingMatchingRules.get(oid);
    if (matchingRule != null)
    {
      for (String name : matchingRule.getAllNames())
      {
        names.add(name);
      }
    }

    names.add(lTag + ".gte");
    names.add(lTag + ".4");
    matchingRule =
        new CollationGreaterThanOrEqualToMatchingRule(oid, names,
            locale);
    orderingMatchingRules.put(oid, matchingRule);
    indexProviders.put(oid,
            new CollationGreaterThanOrEqualToMatchingRuleIndexProvider(
            matchingRule,matchingRule.getIndexName()+"."+SHARED_INDEX_ID));
  }



  /**
   * Creates Greater-than Matching Rule.
   *
   * @param mapper
   *          CollationMapper containing OID and the language Tag.
   * @param locale
   *          Locale value
   */
  private void createGreaterThanMatchingRule(CollationMapper mapper,
      Locale locale)
  {
    if (!greaterThanMatchingRuleType) return;

    String oid = mapper.getNumericOID() + ".5";
    String lTag = mapper.getLanguageTag();

    Collection<String> names = new HashSet<String>();
    CollationOrderingMatchingRule matchingRule =
            orderingMatchingRules.get(oid);
    if (matchingRule != null)
    {
      for (String name : matchingRule.getAllNames())
      {
        names.add(name);
      }
    }

    names.add(lTag + ".gt");
    names.add(lTag + ".5");
    matchingRule =
        new CollationGreaterThanMatchingRule(oid, names, locale);
    orderingMatchingRules.put(oid, matchingRule);
    indexProviders.put(oid,new CollationGreaterThanMatchingRuleIndexProvider(
            matchingRule,matchingRule.getIndexName()+"."+SHARED_INDEX_ID));
  }



  /**
   * Creates substring Matching Rule.
   *
   * @param mapper
   *          CollationMapper containing OID and the language Tag.
   * @param locale
   *          Locale value
   */
  private void createSubstringMatchingRule(CollationMapper mapper,
      Locale locale)
  {
    if (!substringMatchingRuleType) return;

    String oid = mapper.getNumericOID() + ".6";
    String lTag = mapper.getLanguageTag();

    Collection<String> names = new HashSet<String>();
    CollationSubstringMatchingRule matchingRule =
            substringMatchingRules.get(oid);
    if (matchingRule != null)
    {
      for (String name : matchingRule.getAllNames())
      {
        names.add(name);
      }
    }
    names.add(lTag + ".sub");
    names.add(lTag + ".6");
    matchingRule =
        new CollationSubstringMatchingRule(oid, names, locale);
    substringMatchingRules.put(oid, matchingRule);
    indexProviders.put(oid,new CollationSubstringRuleIndexProvider(
            matchingRule, equalityMatchingRules.get(mapper.getNumericOID())));
  }



  /**
   * Verifies if the locale is supported by the JVM.
   *
   * @param lTag
   *          The language tag specified in the configuration.
   * @return Locale The locale correspoding to the languageTag.
   */
  private Locale getLocale(String lTag)
  {
    // Separates the language and the country from the locale.
    Locale locale;
    String lang = null;
    String country = null;
    String variant = null;

    int countryIndex = lTag.indexOf("-");
    int variantIndex = lTag.lastIndexOf("-");

    if (countryIndex > 0)
    {
      lang = lTag.substring(0, countryIndex);

      if (variantIndex > countryIndex)
      {
        country = lTag.substring(countryIndex + 1, variantIndex);
        variant = lTag.substring(variantIndex + 1, lTag.length());
        locale = new Locale(lang, country, variant);
      }
      else
      {
        country = lTag.substring(countryIndex + 1, lTag.length());
        locale = new Locale(lang, country);
      }
    }
    else
    {
      lang = lTag;
      locale = new Locale(lTag);
    }

    if (!supportedLocales.contains(locale))
    {
      // This locale is not supported by this JVM.
      locale = null;
    }
    return locale;
  }



  /**
   * Creates a generalized name from the locale. The generated name is used
   * by the matching rules to create an index using that name.
   */
  private String getLocaleAsString(Locale locale)
  {
    String language = locale.getLanguage();
    String country = locale.getCountry();
    String variant = locale.getVariant();
    StringBuilder builder = new StringBuilder(language);
    if (country != null && country.length() > 0)
    {
      builder.append("_");
      builder.append(locale.getCountry());
    }
    if (variant != null && variant.length() > 0)
    {
      builder.append("_");
      builder.append(locale.getVariant());
    }
    return builder.toString();
  }



  /**
   * Collation Extensible matching rule.
   */
  private final class CollationEqualityMatchingRule
          extends EqualityMatchingRule
  {
    // Names for this class.
    private final Collection<String> names;

    // Collator for performing equality match.
    private final Collator collator;

    // Numeric OID of the rule.
    private final String nOID;

    // Locale associated with this rule.
    private final Locale locale;



    /**
     * Constructs a new CollationEqualityMatchingRule.
     *
     * @param nOID
     *          OID of the collation matching rule
     * @param names
     *          names of this matching rule
     * @param locale
     *          Locale of the collation matching rule
     */
    private CollationEqualityMatchingRule(String nOID,
        Collection<String> names, Locale locale)
    {
      super();
      this.names = names;
      this.collator = createCollator(locale);
      this.locale = locale;
      this.nOID = nOID;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
      //This is called when there is only 1 name.
      return names.iterator().next();
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getAllNames()
    {
      return Collections.unmodifiableCollection(names);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getOID()
    {
      return nOID;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription()
    {
      // There is no standard description for this matching rule.
      return null;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getSyntaxOID()
    {
      return SYNTAX_DIRECTORY_STRING_OID;
    }



    /**
     * Returns the name of the index database for this matching rule. An
     * index name for this rule will be based upon the Locale. This will
     * ensure that multiple collation matching rules corresponding to
     * the same Locale can share the same index database.
     *
     * @return The name of the index for this matching rule.
     */
    public String getIndexName()
    {
      return getLocaleAsString(locale);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ByteString normalizeValue(ByteSequence value)
        throws DirectoryException
    {
      CollationKey key = collator.getCollationKey(value.toString());
      return ByteString.wrap(key.toByteArray());
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ConditionResult valuesMatch(ByteSequence attributeValue,
        ByteSequence assertionValue)
    {
      if (assertionValue.equals(attributeValue))
      {
        return ConditionResult.TRUE;
      }
      else
      {
        return ConditionResult.FALSE;
      }
    }
  }

  
  /**
   * Collation rule for Substring matching rule.
   */
  private final class CollationSubstringMatchingRule extends
      SubstringMatchingRule
  {
        // Names for this class.
    private final Collection<String> names;

    // Collator for performing equality match.
    protected final Collator collator;

    // Numeric OID of the rule.
    private final String nOID;

    // Locale associated with this rule.
    private final Locale locale;



    /**
     * Constructs a new CollationSubstringMatchingRule.
     *
     * @param nOID
     *          OID of the collation substring matching rule
     * @param names
     *          names of this matching rule
     * @param locale
     *          Locale of the collation matching rule
     */
    private CollationSubstringMatchingRule(String nOID,
        Collection<String> names, Locale locale)
    {
      super();
      this.names = names;
      this.collator = createCollator(locale);
      this.locale = locale;
      this.nOID = nOID;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
      //This is called when there is only 1 name.
      return names.iterator().next();
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getAllNames()
    {
      return Collections.unmodifiableCollection(names);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getOID()
    {
      return nOID;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription()
    {
      // There is no standard description for this matching rule.
      return null;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getSyntaxOID()
    {
      return SYNTAX_DIRECTORY_STRING_OID;
    }



    /**
     * Returns the name of the index database for this matching rule. An
     * index name for this rule will be based upon the Locale. This will
     * ensure that multiple collation matching rules corresponding to
     * the same Locale can share the same index database.
     *
     * @return The name of the index for this matching rule.
     */
    public String getIndexName()
    {
      return getLocaleAsString(locale);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ByteString normalizeValue(ByteSequence value)
        throws DirectoryException
    {
      CollationKey key = collator.getCollationKey(value.toString());
      return ByteString.wrap(key.toByteArray());
    }



    /**
     * Utility class which abstracts a substring assertion value.
     */
    private final class Assertion
    {
      // Initial part of the substring filter.
      private String subInitial;

      // any parts of the substring filter.
      private List<String> subAny;

      // Final part of the substring filter.
      private String subFinal;



      /**
       * Creates a new instance of Assertion.
       *
       * @param subInitial
       *          Initial part of the filter.
       * @param subAny
       *          Any part of the filter.
       * @param subFinal
       *          Final part of the filter.
       */
      private Assertion(String subInitial, List<String> subAny,
          String subFinal)
      {
        this.subInitial = subInitial;
        this.subAny = subAny;
        this.subFinal = subFinal;
      }



      /**
       * Returns the Initial part of the assertion.
       *
       * @return Initial part of assertion.
       */
      private String getInitial()
      {
        return subInitial;
      }



      /**
       * Returns the any part of the assertion.
       *
       * @return Any part of the assertion.
       */
      private List<String> getAny()
      {
        return subAny;
      }



      /**
       * Returns the final part of the assertion.
       *
       * @return Final part of the assertion.
       */
      private String getFinal()
      {
        return subFinal;
      }
    }



    /**
     * Parses the assertion from a given value.
     *
     * @param value
     *          The value that needs to be parsed.
     * @return The parsed Assertion object containing the
     * @throws org.opends.server.types.DirectoryException
     */
    public Assertion parseAssertion(ByteSequence value)
        throws DirectoryException
    {
      // Get a string representation of the value.
      String filterString = value.toString();
      int endPos = filterString.length();

      // Find the locations of all the asterisks in the value. Also,
      // check to see if there are any escaped values, since they will
      // need special treatment.
      boolean hasEscape = false;
      LinkedList<Integer> asteriskPositions = new LinkedList<Integer>();
      for (int i = 0; i < endPos; i++)
      {
        if (filterString.charAt(i) == 0x2A) // The asterisk.
        {
          asteriskPositions.add(i);
        }
        else if (filterString.charAt(i) == 0x5C) // The backslash.
        {
          hasEscape = true;
        }
      }

      // If there were no asterisks, then this isn't a substring filter.
      if (asteriskPositions.isEmpty())
      {
        Message message =
            ERR_SEARCH_FILTER_SUBSTRING_NO_ASTERISKS.get(filterString,
                0, endPos);
        throw new DirectoryException(ResultCode.PROTOCOL_ERROR, message);
      }

      // If the value starts with an asterisk, then there is no
      // subInitial component. Otherwise, parse out the subInitial.
      String subInitial;
      int firstPos = asteriskPositions.removeFirst();
      if (firstPos == 0)
      {
        subInitial = null;
      }
      else
      {
        if (hasEscape)
        {
          CharBuffer buffer = CharBuffer.allocate(firstPos);
          for (int i = 0; i < firstPos; i++)
          {
            if (filterString.charAt(i) == 0x5C)
            {
              char escapeValue = hexToEscapedChar(filterString, i + 1);
              i += 2; // Move to the next sequence.
              buffer.put(escapeValue);
            }
            else
            {
              buffer.put(filterString.charAt(i));
            }
          }

          char[] subInitialChars = new char[buffer.position()];
          buffer.flip();
          buffer.get(subInitialChars);
          subInitial = new String(subInitialChars);
        }
        else
        {
          subInitial = filterString.substring(0, firstPos);
        }
      }

      // Next, process through the rest of the asterisks to get the
      // subAny values.
      List<String> subAny = new ArrayList<String>();
      for (int asteriskPos : asteriskPositions)
      {
        int length = asteriskPos - firstPos - 1;

        if (hasEscape)
        {
          CharBuffer buffer = CharBuffer.allocate(length);
          for (int i = firstPos + 1; i < asteriskPos; i++)
          {
            if (filterString.charAt(i) == 0x5C)
            {
              char escapeValue = hexToEscapedChar(filterString, i + 1);
              i += 2; // Move to the next sequence.
              buffer.put(escapeValue);
            }
            else
            {
              buffer.put(filterString.charAt(i));
            }
          }

          char[] subAnyChars = new char[buffer.position()];
          buffer.flip();
          buffer.get(subAnyChars);
          subAny.add(new String(subAnyChars));
        }
        else
        {
          subAny.add(filterString.substring(firstPos + 1, firstPos
              + length + 1));
        }

        firstPos = asteriskPos;
      }

      // Finally, see if there is anything after the last asterisk,
      // which would be the subFinal value.
      String subFinal;
      if (firstPos == (endPos - 1))
      {
        subFinal = null;
      }
      else
      {
        int length = endPos - firstPos - 1;

        if (hasEscape)
        {
          CharBuffer buffer = CharBuffer.allocate(length);
          for (int i = firstPos + 1; i < endPos; i++)
          {
            if (filterString.charAt(i) == 0x5C)
            {
              char escapeValue = hexToEscapedChar(filterString, i + 1);
              i += 2; // Move to the next sequence.
              buffer.put(escapeValue);
            }
            else
            {
              buffer.put(filterString.charAt(i));
            }
          }

          char[] subFinalChars = new char[buffer.position()];
          buffer.flip();
          buffer.get(subFinalChars);
          subFinal = new String(subFinalChars);
        }
        else
        {
          subFinal =
              filterString.substring(firstPos + 1, length + firstPos
                  + 1);
        }
      }

      return new Assertion(subInitial, subAny, subFinal);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ByteString normalizeAssertionValue(ByteSequence value)
        throws DirectoryException
    {
      Assertion assertion = parseAssertion(value);
      String subInitial = assertion.getInitial();

      // Normalize the Values in the following format:
      // initialLength, initial, numberofany, anyLength1, any1,
      // anyLength2, any2, ..., anyLengthn, anyn, finalLength,
      // final
      CollationKey key = null;
      List<Integer> normalizedList = new ArrayList<Integer>();

      if (subInitial == null)
      {
        normalizedList.add(0);
      }
      else
      {
        key = collator.getCollationKey(subInitial);
        byte[] initialBytes = key.toByteArray();

        // Last 4 bytes are 0s with PRIMARY strength.
        int length = initialBytes.length - 4;
        normalizedList.add(length);
        for (int i = 0; i < length; i++)
        {
          normalizedList.add((int) initialBytes[i]);
        }
      }

      List<String> subAny = assertion.getAny();
      if (subAny.size() == 0)
      {
        normalizedList.add(0);
      }
      else
      {
        normalizedList.add(subAny.size());
        for (String any : subAny)
        {
          key = collator.getCollationKey(any);
          byte[] anyBytes = key.toByteArray();
          int length = anyBytes.length - 4;
          normalizedList.add(length);
          for (int i = 0; i < length; i++)
          {
            normalizedList.add((int) anyBytes[i]);
          }
        }
      }

      String subFinal = assertion.getFinal();
      if (subFinal == null)
      {
        normalizedList.add(0);
      }
      else
      {
        key = collator.getCollationKey(subFinal);
        byte[] subFinalBytes = key.toByteArray();
        int length = subFinalBytes.length - 4;
        normalizedList.add(length);
        for (int i = 0; i < length; i++)
        {
          normalizedList.add((int) subFinalBytes[i]);
        }
      }

      byte[] normalizedBytes = new byte[normalizedList.size()];
      for (int i = 0; i < normalizedList.size(); i++)
      {
        normalizedBytes[i] = normalizedList.get(i).byteValue();
      }

      return ByteString.wrap(normalizedBytes);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ConditionResult valuesMatch(ByteSequence attributeValue,
        ByteSequence assertionValue)
    {
      int valueLength = attributeValue.length() - 4;
      int valuePos = 0; // position in the value bytes array.
      int assertPos = 0; // position in the assertion bytes array.

      // First byte is the length of subInitial.
      int subInitialLength = 0xFF & assertionValue.byteAt(0);

      if (subInitialLength != 0)
      {
        if (subInitialLength > valueLength)
        {
          return ConditionResult.FALSE;
        }

        for (; valuePos < subInitialLength; valuePos++)
        {
          if (attributeValue.byteAt(valuePos) != assertionValue
              .byteAt(valuePos + 1))
          {
            return ConditionResult.FALSE;
          }
        }
      }

      assertPos = subInitialLength + 1;
      int anySize = 0xFF & assertionValue.byteAt(assertPos++);
      if (anySize != 0)
      {
        while (anySize-- > 0)
        {
          int anyLength = 0xFF & assertionValue.byteAt(assertPos++);
          int end = valueLength - anyLength;
          boolean match = false;

          for (; valuePos <= end; valuePos++)
          {
            if (assertionValue.byteAt(assertPos) == attributeValue
                .byteAt(valuePos))
            {
              boolean subMatch = true;
              for (int i = 1; i < anyLength; i++)
              {
                if (assertionValue.byteAt(assertPos + i) != attributeValue
                    .byteAt(valuePos + i))
                {
                  subMatch = false;
                  break;
                }
              }

              if (subMatch)
              {
                match = subMatch;
                break;
              }
            }
          }

          if (match)
          {
            valuePos += anyLength;
          }
          else
          {
            return ConditionResult.FALSE;
          }

          assertPos = assertPos + anyLength;
        }
      }

      int finalLength = 0xFF & assertionValue.byteAt(assertPos++);
      if (finalLength != 0)
      {
        if ((valueLength - finalLength) < valuePos)
        {
          return ConditionResult.FALSE;
        }

        valuePos = valueLength - finalLength;

        if (finalLength != assertionValue.length() - assertPos)
        {
          // Some issue with the encoding.
          return ConditionResult.FALSE;
        }

        valuePos = valueLength - finalLength;
        for (int i = 0; i < finalLength; i++, valuePos++)
        {
          if (assertionValue.byteAt(assertPos + i) != attributeValue
              .byteAt(valuePos))
          {
            return ConditionResult.FALSE;
          }
        }
      }

      return ConditionResult.TRUE;
    }



    /**
     * Returns the collator associated with this matching rule.
     * @return
     */
    Collator getCollator()
    {
      return collator;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ByteString normalizeSubstring(ByteSequence substring)
            throws DirectoryException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }


  /**
   * Index key factory for collation substring indexes.
   */
  private final class CollationSubstringIndexKeyFactory extends IndexKeyFactory
  {
    //The substring matching rule.
    private final CollationSubstringMatchingRule rule;

    //The collator instance used by the corresponding substring matching rule.
    private final Collator collator;

    //The substring length to be used by this key factory.
    private int substrLength;

    //The comparator to be used by this key factory.
    private final Comparator<byte[]> comparator;


    /**
     * Creates a collation substring index key factory instance.
     * 
     * @param rule Collation substring matching rule.
     * 
     * @param substrLength The length of the substring.
     */
    private CollationSubstringIndexKeyFactory(
            CollationSubstringMatchingRule rule, int substrLength)
    {
      this.rule = rule;
      this.substrLength = substrLength;
      this.collator = rule.getCollator();
      this.comparator = new DefaultByteKeyComparator();
    }


    /**
    * {@inheritDoc}
    */
    @Override
    public String getIndexID()
    {
      return rule.getIndexName() + "." + SUBSTRING_INDEX_ID;
    }


    /**
     * Makes a byte array representing a substring index key for one
     * substring of a value.
     *
     * @param value
     *          The String containing the value.
     * @param pos
     *          The starting position of the substring.
     * @param len
     *          The length of the substring.
     * @return A byte array containing a substring key.
     */
    private byte[] makeSubstringKey(String value, int pos, int len)
    {
      String sub = value.substring(pos, pos + len);
      CollationKey col = collator.getCollationKey(sub);
      byte[] origKey = col.toByteArray();
      byte[] newKey = new byte[origKey.length - 4];
      System.arraycopy(origKey, 0, newKey, 0, newKey.length);
      return newKey;
    }


    /**
     * Decomposes an attribute value into a set of substring index keys.
     *
     * @param attValue
     *          The normalized attribute value
     * @param set
     *          A set into which the keys will be inserted.
     */
    private void substringKeys(ByteString attValue, KeySet keySet)
    {
      String value = attValue.toString();
      for (int i = 0, remain = value.length(); remain > 0; i++, remain--)
      {
        int len = Math.min(substrLength, remain);
        byte[] keyBytes = makeSubstringKey(value, i, len);
        keySet.addKey(keyBytes);
      }
    }


    /**
     * {@inheirtDoc}
     */
    @Override
    public void getKeys(List<Attribute> attrList, KeySet keySet)
    {
      for (Attribute attr : attrList)
      {
        for(AttributeValue value : attr)
        {
          try
          {
            ByteString key = rule.normalizeValue(value.getValue());
            substringKeys(key,keySet);
          }
          catch (DirectoryException de)
          {
          }
        }
      }
    }


    /**
     * {@inheirtDoc}
     */
    @Override
    public Comparator<byte[]> getComparator()
    {
      return comparator;
    }


    /**
     * Returns the length of the substring.
     */
    private int getSubstringLength()
    {
      return substrLength;
    }


    /**
     * Sets the length of the substring.
     */
    private void setSubstringLength(int substrLength)
    {
      this.substrLength = substrLength;
    }
  }


 /**
  * This class implements the index provider for a collation substring
  * matching rule.
  */
  private final class CollationSubstringRuleIndexProvider
          extends MatchingRuleIndexProvider
  {
    //The collation substring matching rule.
    private final CollationSubstringMatchingRule subRule;

    //The collation equality matching rule.
    private final CollationEqualityMatchingRule eqRule;

    //The index key factory for creating substring index keys.
    private CollationSubstringIndexKeyFactory subKeyFactory;

    //The shared/equality key factory for creating equality index keys.
    private IndexKeyFactory eqKeyFactory;


    //Creates a new index provider.
    private CollationSubstringRuleIndexProvider(
            CollationSubstringMatchingRule subRule,
            CollationEqualityMatchingRule eqRule)
    {
      this.subRule = subRule;
      this.eqRule = eqRule;
    }


    /**
     * Retrieves the Index Records that might contain a given substring.
     *
     * @param value
     *          A String representing the attribute value.
     * @param factory
     *          An IndexQueryFactory which issues calls to the backend.
     * @param substrLength
     *          The length of the substring.
     * @return The candidate entry IDs.
     */
    private <T> T matchSubstring(String value,
        IndexQueryFactory<T> factory)
    {
      T intersectionQuery = null;
      int substrLength = subKeyFactory.getSubstringLength();

      if (value.length() < substrLength)
      {
        byte[] lower = subKeyFactory.makeSubstringKey(value, 0, value.length());
        byte[] upper = subKeyFactory.makeSubstringKey(value, 0, value.length());
        for (int i = upper.length - 1; i >= 0; i--)
        {
          if (upper[i] == 0xFF)
          {
            // We have to carry the overflow to the more significant
            // byte.
            upper[i] = 0;
          }
          else
          {
            // No overflow, we can stop.
            upper[i] = (byte) (upper[i] + 1);
            break;
          }
        }
        // Read the range: lower <= keys < upper.
        intersectionQuery =
            factory.createRangeMatchQuery(subKeyFactory
                .getIndexID(), ByteString.wrap(lower),
                ByteString.wrap(upper), true, false);
      }
      else
      {
        List<T> queryList = new ArrayList<T>();
        Set<byte[]> set =
            new TreeSet<byte[]>(new IndexKeyFactory.DefaultByteKeyComparator());
        for (int first = 0, last = substrLength;
             last <= value.length();
             first++, last++)
        {
          byte[] keyBytes;
          keyBytes = subKeyFactory.makeSubstringKey(value, first, substrLength);
          set.add(keyBytes);
        }

        for (byte[] keyBytes : set)
        {
          T single =
              factory.createExactMatchQuery(subKeyFactory
                  .getIndexID(), ByteString.wrap(keyBytes));
          queryList.add(single);
        }
        intersectionQuery = factory.createIntersectionQuery(queryList);
      }
      return intersectionQuery;
    }



    /**
     * Uses an equality index to retrieve the entry IDs that might
     * contain a given initial substring.
     *
     * @param bytes
     *          A normalized initial substring of an attribute value.
     * @return The candidate entry IDs.
     */
    private <T> T matchInitialSubstring(String value,
        IndexQueryFactory<T> factory)
    {
      byte[] lower = subKeyFactory.makeSubstringKey(value, 0, value.length());
      byte[] upper = new byte[lower.length];
      System.arraycopy(lower, 0, upper, 0, lower.length);

      for (int i = upper.length - 1; i >= 0; i--)
      {
        if (upper[i] == 0xFF)
        {
          // We have to carry the overflow to the more significant byte.
          upper[i] = 0;
        }
        else
        {
          // No overflow, we can stop.
          upper[i] = (byte) (upper[i] + 1);
          break;
        }
      }
      // Use the shared equality indexer.
      return factory.createRangeMatchQuery(eqKeyFactory.
          getIndexID(), ByteString.wrap(lower), ByteString
          .wrap(upper), true, false);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createIndexQuery(ByteSequence assertionValue,
        IndexQueryFactory<T> factory) throws DirectoryException
    {
      CollationSubstringMatchingRule.Assertion assertion =
              subRule.parseAssertion(assertionValue);
      String subInitial = assertion.getInitial();
      List<String> subAny = assertion.getAny();
      String subFinal = assertion.getFinal();
      List<T> queries = new ArrayList<T>();

      if (subInitial == null && subAny.size() == 0 && subFinal == null)
      {
        // Can happen with a filter like "cn:en.6:=*".
        // Just return an empty record.
        return factory.createMatchAllQuery();
      }
      List<String> elements = new ArrayList<String>();
      if (subInitial != null)
      {
        // Always use the shared indexer for initial match.
        T query = matchInitialSubstring(subInitial, factory);
        queries.add(query);
      }

      if (subAny != null && subAny.size() > 0)
      {
        elements.addAll(subAny);
      }

      if (subFinal != null)
      {
        elements.add(subFinal);
      }

      for (String element : elements)
      {
        queries.add(matchSubstring(element, factory));
      }
      return factory.createIntersectionQuery(queries);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IndexKeyFactory> getIndexKeyFactory(IndexConfig config)
    {
      Collection<IndexKeyFactory> factories =
          new ArrayList<IndexKeyFactory>();
      int substrLength = 6; // Default substring length;
      if(subKeyFactory == null)
      {
        if (config != null)
        {
          substrLength = config.getSubstringLength();
        }
        subKeyFactory = new CollationSubstringIndexKeyFactory(subRule,
                substrLength);
      }
      else
      {
        if (config != null)
        {
          if (config.getSubstringLength() !=
                  subKeyFactory.getSubstringLength())
          {
            subKeyFactory.setSubstringLength(substrLength);
          }
        }
      }

      if(eqKeyFactory == null)
      {
        MatchingRuleIndexProvider provider =
                DirectoryServer.getIndexProvider(eqRule);
        if(provider !=null)
        {
          eqKeyFactory = provider.getIndexKeyFactory(config).iterator().next();
        }
      }

      if(eqKeyFactory != null)
      {
        factories.add(eqKeyFactory);
      }

      factories.add(subKeyFactory);
      return factories;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MatchingRule getMatchingRule()
    {
      return subRule;
    }
  }

  /**
   * An abstract Collation rule for Ordering matching rule.
   */
  private abstract class CollationOrderingMatchingRule
          implements OrderingMatchingRule
  {
        // Names for this class.
    private final Collection<String> names;

    // Collator for performing equality match.
    protected final Collator collator;

    // Numeric OID of the rule.
    private final String nOID;

    // Locale associated with this rule.
    private final Locale locale;



    /**
     * Constructs a new CollationMatchingRule.
     *
     * @param nOID
     *          OID of the collation matching rule
     * @param names
     *          names of this matching rule
     * @param locale
     *          Locale of the collation matching rule
     */
    private CollationOrderingMatchingRule(String nOID,
        Collection<String> names, Locale locale)
    {
      this.names = names;
      this.collator = createCollator(locale);
      this.locale = locale;
      this.nOID = nOID;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
      //This is called when there is only 1 name.
      return names.iterator().next();
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getAllNames()
    {
      return Collections.unmodifiableCollection(names);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getOID()
    {
      return nOID;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription()
    {
      // There is no standard description for this matching rule.
      return null;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getSyntaxOID()
    {
      return SYNTAX_DIRECTORY_STRING_OID;
    }



    /**
     * Returns the name of the index database for this matching rule. An
     * index name for this rule will be based upon the Locale. This will
     * ensure that multiple collation matching rules corresponding to
     * the same Locale can share the same index database.
     *
     * @return The name of the index for this matching rule.
     */
    public String getIndexName()
    {
      return getLocaleAsString(locale);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ByteString normalizeValue(ByteSequence value)
        throws DirectoryException
    {
      CollationKey key = collator.getCollationKey(value.toString());
      return ByteString.wrap(key.toByteArray());
    }



    /**
     * {@inheritDoc}
     */
    public int compare(byte[] arg0, byte[] arg1)
    {
      return StaticUtils.compare(arg0, arg1);
    }



    /**
     * {@inheritDoc}
     */
    public int compareValues(ByteSequence value1, ByteSequence value2)
    {
      return value1.compareTo(value2);
    }


   /**
    * {@inheritDoc}
    */
    public void toString(StringBuilder buffer)
    {
     //TODO
    }



    /**
     * {@inheritDoc}
     */
    public boolean isObsolete()
    {
      return false;
    }



    /**
     * {@inheritDoc}
     */
    public String getNameOrOID()
    {
      return nOID;
    }



    /**
     * {@inheritDoc}
     */
    public ByteString normalizeAssertionValue(ByteSequence seq)
            throws DirectoryException
    {
      return normalizeValue(seq);
    }
  }

  /**
   * Collation matching rule for Less-than matching rule.
   */
  private final class CollationLessThanMatchingRule extends
      CollationOrderingMatchingRule
  {
    /**
     * Constructs a new CollationLessThanMatchingRule.
     *
     * @param nOID
     *          OID of the collation matching rule
     * @param names
     *          names of this matching rule
     * @param locale
     *          Locale of the collation matching rule
     */
    private CollationLessThanMatchingRule(String nOID,
        Collection<String> names, Locale locale)
    {
      super(nOID, names, locale);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ConditionResult valuesMatch(ByteSequence attributeValue,
        ByteSequence assertionValue)
    {
      int ret = attributeValue.compareTo(assertionValue);

      if (ret < 0)
      {
        return ConditionResult.TRUE;
      }
      else
      {
        return ConditionResult.FALSE;
      }
    }
  }

  
 /**
  * This class implements the index provider for a collation less-than
  * matching rule.
  */
  private final class CollationLessThanMatchingRuleIndexProvider
          extends DefaultOrderingIndexProvider
  {
    /**
     * Creates an instance of index provider.
     *
     * @param rule The ordering matching rule.
     *
     * @param indexID The index id to be used by the index key factory.
     */
    private CollationLessThanMatchingRuleIndexProvider(
            OrderingMatchingRule rule,String indexID)
    {
      super(rule,indexID);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createIndexQuery(ByteSequence assertionValue,
        IndexQueryFactory<T> queryFactory) throws DirectoryException
    {
      return queryFactory.createRangeMatchQuery(factory.
          getIndexID(), ByteString.empty(),
          matchingRule.normalizeValue(assertionValue), false, false);
    }
  }


  /**
   * Collation rule for less-than-equal-to matching rule.
   */
  private final class CollationLessThanOrEqualToMatchingRule extends
      CollationOrderingMatchingRule
  {
    /**
     * Constructs a new CollationLessThanOrEqualToMatchingRule.
     *
     * @param nOID
     *          OID of the collation matching rule
     * @param names
     *          names of this matching rule
     * @param locale
     *          Locale of the collation matching rule
     */
    private CollationLessThanOrEqualToMatchingRule(String nOID,
        Collection<String> names, Locale locale)
    {
      super(nOID, names, locale);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ConditionResult valuesMatch(ByteSequence attributeValue,
        ByteSequence assertionValue)
    {
      int ret = attributeValue.compareTo(assertionValue);

      if (ret <= 0)
      {
        return ConditionResult.TRUE;
      }
      else
      {
        return ConditionResult.FALSE;
      }
    }
  }


 /**
  * This class implements the index provider for a collation
  * less-than-equal-to matching rule.
  */
  private final class CollationLessThanEqualToMatchingRuleIndexProvider
          extends DefaultOrderingIndexProvider
  {
    /**
     * Creates a new instance of index provider.
     *
     * @param rule The ordering matching rule.
     *
     * @param indexID The index id to be used by the index key factory.
     */
    private CollationLessThanEqualToMatchingRuleIndexProvider(
            OrderingMatchingRule rule,
            String indexID)
    {
      super(rule,indexID);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createIndexQuery(ByteSequence assertionValue,
        IndexQueryFactory<T> queryFactory) throws DirectoryException
    {
      return queryFactory.createRangeMatchQuery(factory.
          getIndexID(), ByteString.empty(),
          matchingRule.normalizeValue(assertionValue), false, true);
    }
  }

  /**
   * Collation rule for greater-than matching rule.
   */
  private final class CollationGreaterThanMatchingRule extends
      CollationOrderingMatchingRule
  {
    /**
     * Constructs a new CollationGreaterThanMatchingRule.
     *
     * @param nOID
     *          OID of the collation matching rule
     * @param names
     *          names of this matching rule
     * @param locale
     *          Locale of the collation matching rule
     */
    private CollationGreaterThanMatchingRule(String nOID,
        Collection<String> names, Locale locale)
    {
      super(nOID, names, locale);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ConditionResult valuesMatch(ByteSequence attributeValue,
        ByteSequence assertionValue)
    {
      int ret = attributeValue.compareTo(assertionValue);

      if (ret > 0)
      {
        return ConditionResult.TRUE;
      }
      else
      {
        return ConditionResult.FALSE;
      }
    }
  }


 /**
  * This class implements the index provider for a collation greater-than
  * matching rule.
  */
  private final class CollationGreaterThanMatchingRuleIndexProvider
             extends DefaultOrderingIndexProvider
  {
    /**
     * Creates a new instance of index provider.
     *
     * @param rule Ordering matching rule.
     *
     * @param indexID The index id to be used by the index key factory.
     */
    private CollationGreaterThanMatchingRuleIndexProvider(
            OrderingMatchingRule rule,
            String indexID)
    {
      super(rule,indexID);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createIndexQuery(ByteSequence assertionValue,
        IndexQueryFactory<T> queryFactory) throws DirectoryException
    {
      return queryFactory.createRangeMatchQuery(factory.
          getIndexID(),
          matchingRule.normalizeValue(assertionValue),
          ByteString.empty(),false, false);
    }
  }


  /**
   * Collation rule for greater-than-equal-to matching rule.
   */
  private final class CollationGreaterThanOrEqualToMatchingRule extends
      CollationOrderingMatchingRule
  {
    /**
     * Constructs a new CollationGreaterThanOrEqualToMatchingRule.
     *
     * @param nOID
     *          OID of the collation matching rule
     * @param names
     *          names of this matching rule
     * @param locale
     *          Locale of the collation matching rule
     */
    private CollationGreaterThanOrEqualToMatchingRule(String nOID,
        Collection<String> names, Locale locale)
    {
      super(nOID, names, locale);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ConditionResult valuesMatch(ByteSequence attributeValue,
        ByteSequence assertionValue)
    {
      int ret = attributeValue.compareTo(assertionValue);

      if (ret >= 0)
      {
        return ConditionResult.TRUE;
      }
      else
      {
        return ConditionResult.FALSE;
      }
    }
  }


 /**
  * This class implements the index provider for a collation
  * greater-than-equal-to matching rule.
  */
  private final class CollationGreaterThanOrEqualToMatchingRuleIndexProvider
          extends DefaultOrderingIndexProvider
  {
    /**
     * Creates a new instance of index provider.
     *
     * @param rule The ordering matching rule.
     *
     * @param indexID The index id to be used by the embedded key factory.
     */
    private CollationGreaterThanOrEqualToMatchingRuleIndexProvider(
            OrderingMatchingRule rule,
            String indexID)
    {
      super(rule,indexID);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createIndexQuery(ByteSequence assertionValue,
        IndexQueryFactory<T> queryFactory) throws DirectoryException
    {
      return queryFactory.createRangeMatchQuery(factory.
          getIndexID(),
          matchingRule.normalizeValue(assertionValue), ByteString.empty(),
          true, false);
    }
  }


  /**
   * A utility class for extracting the OID and Language Tag from the
   * configuration entry.
   */
  private final class CollationMapper
  {
    // OID of the collation rule.
    private String oid;

    // Language Tag.
    private String lTag;



    /**
     * Creates a new instance of CollationMapper.
     *
     * @param collation
     *          The collation text in the LOCALE:OID format.
     */
    private CollationMapper(String collation)
    {
      int index = collation.indexOf(":");
      if (index > 0)
      {
        oid = collation.substring(index + 1, collation.length());
        lTag = collation.substring(0, index);
      }
    }



    /**
     * Returns the OID part of the collation text.
     *
     * @return OID part of the collation text.
     */
    private String getNumericOID()
    {
      return oid;
    }



    /**
     * Returns the language Tag of collation text.
     *
     * @return Language Tag part of the collation text.
     */
    private String getLanguageTag()
    {
      return lTag;
    }
  }
}
