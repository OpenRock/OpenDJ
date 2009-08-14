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
 *      Copyright 2006-2009 Sun Microsystems, Inc.
 */
package org.opends.server.backends.jeb;
import org.opends.messages.Message;

import java.util.*;

import com.sleepycat.je.*;


import static org.opends.server.loggers.debug.DebugLogger.*;
import org.opends.server.loggers.debug.DebugTracer;
import static org.opends.server.loggers.ErrorLogger.*;
import org.opends.server.types.*;
import org.opends.server.admin.std.server.LocalDBIndexCfg;
import org.opends.server.admin.std.meta.LocalDBIndexCfgDefn;
import org.opends.server.admin.server.ConfigurationChangeListener;
import org.opends.server.backends.index.IndexKeyFactory;
import org.opends.server.backends.index.IndexConfig;
import org.opends.server.api.MatchingRule;
import org.opends.server.backends.index.IndexQueryFactory;
import org.opends.server.config.ConfigException;
import org.opends.server.backends.index.Indexer;
import org.opends.server.backends.index.MatchingRuleIndexProvider;
import org.opends.server.backends.index.PresenceIndexKeyFactory;
import static org.opends.messages.JebMessages.*;
import static org.opends.server.util.ServerConstants.*;
import static org.opends.server.core.DirectoryServer.*;

import org.opends.server.core.DirectoryServer;
import org.opends.server.util.StaticUtils;

/**
 * Class representing an attribute index.
 * We have a separate database for each type of indexing, which makes it easy
 * to tell which attribute indexes are configured.  The different types of
 * indexing are equality, presence, substrings and ordering.  The keys in the
 * ordering index are ordered by setting the btree comparator to the ordering
 * matching rule comparator.
 * Note that the values in the equality index are normalized by the equality
 * matching rule, whereas the values in the ordering index are normalized
 * by the ordering matching rule.  If these could be guaranteed to be identical
 * then we would not need a separate ordering index.
 */
public class AttributeIndex
    implements ConfigurationChangeListener<LocalDBIndexCfg>
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();



  /**
   * A database key for the presence index.
   */
  public static final DatabaseEntry presenceKey =
       new DatabaseEntry("+".getBytes());



  /**
   * The entryContainer in which this attribute index resides.
   */
  private EntryContainer entryContainer;



  /**
   * The JE environment.
   */
  private Environment env;



  /**
   * The attribute index configuration.
   */
  private LocalDBIndexCfg indexConfig;



  /**
   * The Index manager which manages the index.
   */
   private MatchingRuleBasedIndexManager indexManager;



  /**
   * The index database for attribute presence.
   */
  private Index presenceIndex;
  
  
  
  /**
   * The List of all the matching rules used in extensible indexes.
   */
  private Collection<String> extensibleRules;



  /**
   * The state.
   */
  private State state;



  /**
   * Default cursor entry limit.
   */
  private int cursorEntryLimit = 100000;



  /**
   * Create a new attribute index object.
   * @param entryContainer The entryContainer of this attribute index.
   * @param state The state database to persist index state info.
   * @param env The JE environment handle.
   * @param indexConfig The attribute index configuration.
   * @throws DatabaseException if a JE database error occurs.
   * @throws ConfigException if a configuration related error occurs.
   */
  public AttributeIndex(LocalDBIndexCfg indexConfig, State state,
                        Environment env,
                        EntryContainer entryContainer)
      throws DatabaseException, ConfigException
  {
    this.entryContainer = entryContainer;
    this.env = env;
    this.indexConfig = indexConfig;
    this.state = state;
    this.indexManager = new MatchingRuleBasedIndexManager();

    AttributeType attrType = indexConfig.getAttribute();
    String name =
        entryContainer.getDatabasePrefix() + "_" + attrType.getNameOrOID();
    int indexEntryLimit = indexConfig.getIndexEntryLimit();

    if (indexConfig.getIndexType().contains(
            LocalDBIndexCfgDefn.IndexType.EQUALITY))
    {
      MatchingRule matchingRule = attrType.getEqualityMatchingRule();
      if (matchingRule == null)
      {
        Message message = ERR_CONFIG_INDEX_TYPE_NEEDS_MATCHING_RULE.get(
            String.valueOf(attrType), "equality");
        throw new ConfigException(message);
      }
      indexManager.registerNewIndex(indexConfig, matchingRule);
    }

    if (indexConfig.getIndexType().contains(
            LocalDBIndexCfgDefn.IndexType.PRESENCE))
    {
      IndexKeyFactory factory = new PresenceIndexKeyFactory();
      this.presenceIndex = new Index(name + "." + factory.getIndexID(),
                                     new Indexer(attrType,factory),
                                     state,
                                     indexEntryLimit,
                                     cursorEntryLimit,
                                     false,
                                     env,
                                     entryContainer);
    }

    if (indexConfig.getIndexType().contains(
            LocalDBIndexCfgDefn.IndexType.SUBSTRING))
    {
      MatchingRule matchingRule = attrType.getSubstringMatchingRule();
      if (matchingRule == null)
      {
        Message message = ERR_CONFIG_INDEX_TYPE_NEEDS_MATCHING_RULE.get(
            String.valueOf(attrType), "substring");
        throw new ConfigException(message);
      }
      indexManager.registerNewIndex(indexConfig, matchingRule);
    }

    if (indexConfig.getIndexType().contains(
            LocalDBIndexCfgDefn.IndexType.ORDERING))
    {
      MatchingRule matchingRule = attrType.getOrderingMatchingRule();

      if (matchingRule == null)
      {
        Message message = ERR_CONFIG_INDEX_TYPE_NEEDS_MATCHING_RULE.get(
            String.valueOf(attrType), "ordering");
        throw new ConfigException(message);
      }
      indexManager.registerNewIndex(indexConfig, matchingRule);
    }

    if (indexConfig.getIndexType().contains(
        LocalDBIndexCfgDefn.IndexType.APPROXIMATE))
    {
      MatchingRule matchingRule = attrType.getApproximateMatchingRule();
      if (matchingRule == null)
      {
        Message message = ERR_CONFIG_INDEX_TYPE_NEEDS_MATCHING_RULE.get(
            String.valueOf(attrType), "approximate");
        throw new ConfigException(message);
      }
     indexManager.registerNewIndex(indexConfig, matchingRule);
    }
    if (indexConfig.getIndexType().contains(
        LocalDBIndexCfgDefn.IndexType.EXTENSIBLE))
    {
      extensibleRules =
              indexConfig.getIndexExtensibleMatchingRule();
      if(extensibleRules == null || extensibleRules.size() == 0)
      {
        Message message = ERR_CONFIG_INDEX_TYPE_NEEDS_MATCHING_RULE.get(
            String.valueOf(attrType), "extensible");
        throw new ConfigException(message);
      }
      for(String ruleName:extensibleRules)
      {
        MatchingRule rule =
                DirectoryServer.getMatchingRule(StaticUtils.toLowerCase(ruleName));
        if(rule == null)
        {
          Message message =
                  ERR_CONFIG_INDEX_TYPE_NEEDS_VALID_MATCHING_RULE.get(
                  String.valueOf(attrType),ruleName);
          logError(message);
          continue;
        }
        indexManager.registerNewIndex(indexConfig, rule);
      }
    }
    this.indexConfig.addChangeListener(this);
  }

  /**
   * Open the attribute index.
   *
   * @throws DatabaseException if a JE database error occurs while
   * openning the index.
   */
  public void open() throws DatabaseException
  {
    if(presenceIndex !=null)
    {
      presenceIndex.open();
    }

    for(Index index : indexManager.getIndexes())
    {
      index.open();
    }
  }

  /**
   * Close the attribute index.
   *
   * @throws DatabaseException if a JE database error occurs while
   * closing the index.
   */
  public void close() throws DatabaseException
  {
    //Close the presence index if present.
    if(presenceIndex != null)
    {
      presenceIndex.close();
    }
    //Close all the matching-rule based indexes.
    for(Index index : indexManager.getIndexes())
    {
      index.close();
    }

    indexConfig.removeChangeListener(this);
    // The entryContainer is responsible for closing the JE databases.
  }

  /**
   * Get the attribute type of this attribute index.
   * @return The attribute type of this attribute index.
   */
  public AttributeType getAttributeType()
  {
    return indexConfig.getAttribute();
  }

  /**
   * Get the JE index configuration used by this index.
   * @return The configuration in effect.
   */
  public LocalDBIndexCfg getConfiguration()
  {
    return indexConfig;
  }

  /**
   * Update the attribute index for a new entry.
   *
   * @param buffer The index buffer to use to store the added keys
   * @param entryID     The entry ID.
   * @param entry       The contents of the new entry.
   * @return True if all the index keys for the entry are added. False if the
   *         entry ID already exists for some keys.
   * @throws DatabaseException If an error occurs in the JE database.
   * @throws DirectoryException If a Directory Server error occurs.
   */
  public boolean addEntry(IndexBuffer buffer, EntryID entryID,
                          Entry entry)
       throws DatabaseException, DirectoryException
  {
    boolean success = true;

    if(presenceIndex !=null)
    {
      presenceIndex.addEntry(buffer, entryID, entry);
    }

    for(Index index : indexManager.getIndexes())
    {
      if(!index.addEntry(buffer, entryID, entry))
      {
        success = false;
      }
    }

    return success;
  }


  /**
   * Update the attribute index for a new entry.
   *
   * @param txn         The database transaction to be used for the insertions.
   * @param entryID     The entry ID.
   * @param entry       The contents of the new entry.
   * @return True if all the index keys for the entry are added. False if the
   *         entry ID already exists for some keys.
   * @throws DatabaseException If an error occurs in the JE database.
   * @throws DirectoryException If a Directory Server error occurs.
   */
  public boolean addEntry(Transaction txn, EntryID entryID, Entry entry)
       throws DatabaseException, DirectoryException
  {
    boolean success = true;

    if(presenceIndex !=null)
    {
      presenceIndex.addEntry(txn, entryID, entry);
    }

    for(Index index : indexManager.getIndexes())
    {
      if(!index.addEntry(txn, entryID, entry))
      {
        success = false;
      }
    }

    return success;
  }

  /**
   * Update the attribute index for a deleted entry.
   *
   * @param buffer The index buffer to use to store the deleted keys
   * @param entryID     The entry ID
   * @param entry       The contents of the deleted entry.
   * @throws DatabaseException If an error occurs in the JE database.
   * @throws DirectoryException If a Directory Server error occurs.
   */
  public void removeEntry(IndexBuffer buffer, EntryID entryID,
                          Entry entry)
       throws DatabaseException, DirectoryException
  {
    if(presenceIndex !=null)
    {
      presenceIndex.removeEntry(buffer, entryID, entry);
    }

    for(Index index : indexManager.getIndexes())
    {
      index.removeEntry(buffer, entryID, entry);
    }
  }

  /**
   * Update the attribute index for a deleted entry.
   *
   * @param txn         The database transaction to be used for the deletions
   * @param entryID     The entry ID
   * @param entry       The contents of the deleted entry.
   * @throws DatabaseException If an error occurs in the JE database.
   * @throws DirectoryException If a Directory Server error occurs.
   */
  public void removeEntry(Transaction txn, EntryID entryID, Entry entry)
       throws DatabaseException, DirectoryException
  {
    //Remove this entry from the presence index.
    if(presenceIndex != null)
    {
      presenceIndex.removeEntry(txn, entryID, entry);
    }
    //Remove this entry from all the rule-based indexes.
    for(Index index : indexManager.getIndexes())
    {
      index.removeEntry(txn, entryID, entry);
    }
  }

  /**
   * Update the index to reflect a sequence of modifications in a Modify
   * operation.
   *
   * @param txn The JE transaction to use for database updates.
   * @param entryID The ID of the entry that was modified.
   * @param oldEntry The entry before the modifications were applied.
   * @param newEntry The entry after the modifications were applied.
   * @param mods The sequence of modifications in the Modify operation.
   * @throws DatabaseException If an error occurs during an operation on a
   * JE database.
   */
  public void modifyEntry(Transaction txn,
                          EntryID entryID,
                          Entry oldEntry,
                          Entry newEntry,
                          List<Modification> mods)
       throws DatabaseException
  {
    if(presenceIndex != null)
    {
      presenceIndex.modifyEntry(txn, entryID, oldEntry, newEntry, mods);
    }

    for(Index index : indexManager.getIndexes())
    {
      index.modifyEntry(txn, entryID, oldEntry, newEntry, mods);
    }
  }

  /**
   * Update the index to reflect a sequence of modifications in a Modify
   * operation.
   *
   * @param buffer The index buffer used to buffer up the index changes.
   * @param entryID The ID of the entry that was modified.
   * @param oldEntry The entry before the modifications were applied.
   * @param newEntry The entry after the modifications were applied.
   * @param mods The sequence of modifications in the Modify operation.
   * @throws DatabaseException If an error occurs during an operation on a
   * JE database.
   */
  public void modifyEntry(IndexBuffer buffer,
                          EntryID entryID,
                          Entry oldEntry,
                          Entry newEntry,
                          List<Modification> mods)
       throws DatabaseException
  {
    if(presenceIndex != null)
    {
      presenceIndex.modifyEntry(buffer, entryID, oldEntry, newEntry, mods);
    }
    for(Index index : indexManager.getIndexes())
    {
      index.modifyEntry(buffer, entryID, oldEntry, newEntry, mods);
    }
  }



  /**
   * Retrieve the entry IDs that might match an equality filter.
   *
   * @param equalityFilter The equality filter.
   * @param debugBuffer If not null, a diagnostic string will be written
   *                     which will help determine how the indexes contributed
   *                     to this search.
   * @return The candidate entry IDs that might contain the filter
   *         assertion value.
   */
  public EntryIDSet evaluateEqualityFilter(SearchFilter equalityFilter,
                                           StringBuilder debugBuffer)
  {
    AttributeType attrType = indexConfig.getAttribute();
    MatchingRule rule = attrType.getEqualityMatchingRule();
    IndexQueryFactory<IndexQuery> factory = null;

    if(indexManager == null
            || (factory = indexManager.getQueryFactory(rule))==null)
    {
      // There is no index on this matching rule.
      return IndexQuery.createNullIndexQuery().evaluate();
    }

    try
    {
      if(debugBuffer != null)
      {
        debugBuffer.append("[INDEX:");
       IndexConfig config =
                new JEIndexConfig(indexConfig.getSubstringLength());
       MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(rule);
        for(IndexKeyFactory keyFactory :  provider.getIndexKeyFactory(config))
        {
          String longID = attrType.getNameOrOID() + "."  + keyFactory.getIndexID();
          debugBuffer.append(longID);
        }
        debugBuffer.append("]");
      }
      MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(rule);
      IndexQuery expression = provider.createIndexQuery(
              equalityFilter.getAssertionValue().getValue(), factory);
      return expression.evaluate();
    }
    catch (DirectoryException e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }
      return IndexQuery.createNullIndexQuery().evaluate();
    }
  }

  /**
   * Retrieve the entry IDs that might match a presence filter.
   *
   * @param filter The presence filter.
   * @param debugBuffer If not null, a diagnostic string will be written
   *                     which will help determine how the indexes contributed
   *                     to this search.
   * @return The candidate entry IDs that might contain one or more
   *         values of the attribute type in the filter.
   */
  public EntryIDSet evaluatePresenceFilter(SearchFilter filter,
                                           StringBuilder debugBuffer)
  {
    if (presenceIndex == null)
    {
      return new EntryIDSet();
    }

    if(debugBuffer != null)
    {
      debugBuffer.append("[INDEX:");
      debugBuffer.append(indexConfig.getAttribute().getNameOrOID());
      debugBuffer.append(".");
      debugBuffer.append("presence]");
    }

    // Read the presence key
    return presenceIndex.readKey(presenceKey, null, LockMode.DEFAULT);
  }

  /**
   * Retrieve the entry IDs that might match a greater-or-equal filter.
   *
   * @param filter The greater-or-equal filter.
   * @param debugBuffer If not null, a diagnostic string will be written
   *                     which will help determine how the indexes contributed
   *                     to this search.
   * @return The candidate entry IDs that might contain a value
   *         greater than or equal to the filter assertion value.
   */
  public EntryIDSet evaluateGreaterOrEqualFilter(SearchFilter filter,
                                                 StringBuilder debugBuffer)
  {
    AttributeType attrType = indexConfig.getAttribute();
    MatchingRule rule = attrType.getOrderingMatchingRule();
    IndexQueryFactory<IndexQuery> factory = null;

    if(indexManager == null
            || (factory = indexManager.getQueryFactory(rule))==null)
    {
      // There is no index on this matching rule.
      return IndexQuery.createNullIndexQuery().evaluate();
    }

    try
    {
      if(debugBuffer != null)
      {
        debugBuffer.append("[INDEX:");
       IndexConfig config =
                new JEIndexConfig(indexConfig.getSubstringLength());
       MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(rule);
        for(IndexKeyFactory keyFactory :  provider.getIndexKeyFactory(config))
        {
          String longID = attrType.getNameOrOID() + "."  + keyFactory.getIndexID();
          debugBuffer.append(longID);
        }
        debugBuffer.append("]");
      }
      MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(rule);
      IndexQuery expression = provider.createGreaterThanOrEqualIndexQuery(
              filter.getAssertionValue().getValue(), factory);
      return expression.evaluate();
    }
    catch (DirectoryException e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }
      return IndexQuery.createNullIndexQuery().evaluate();
    }
  }

  /**
   * Retrieve the entry IDs that might match a less-or-equal filter.
   *
   * @param filter The less-or-equal filter.
   * @param debugBuffer If not null, a diagnostic string will be written
   *                     which will help determine how the indexes contributed
   *                     to this search.
   * @return The candidate entry IDs that might contain a value
   *         less than or equal to the filter assertion value.
   */
  public EntryIDSet evaluateLessOrEqualFilter(SearchFilter filter,
                                              StringBuilder debugBuffer)
  {
    AttributeType attrType = indexConfig.getAttribute();
    MatchingRule rule = attrType.getOrderingMatchingRule();
    IndexQueryFactory<IndexQuery> factory = null;

    if(indexManager == null
            || (factory = indexManager.getQueryFactory(rule))==null)
    {
      // There is no index on this matching rule.
      return IndexQuery.createNullIndexQuery().evaluate();
    }

    try
    {
      if(debugBuffer != null)
      {
        debugBuffer.append("[INDEX:");
       IndexConfig config =
                new JEIndexConfig(indexConfig.getSubstringLength());
       MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(rule);
        for(IndexKeyFactory keyFactory :  provider.getIndexKeyFactory(config))
        {
          String longID = attrType.getNameOrOID() + "."  + keyFactory.getIndexID();
          debugBuffer.append(longID);
        }
        debugBuffer.append("]");
      }
      MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(rule);
      IndexQuery expression = provider.createLessThanOrEqualIndexQuery(
              filter.getAssertionValue().getValue(), factory);
      return expression.evaluate();
    }
    catch (DirectoryException e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }
      return IndexQuery.createNullIndexQuery().evaluate();
    }
  }

  /**
   * Retrieve the entry IDs that might match a substring filter.
   *
   * @param filter The substring filter.
   * @param debugBuffer If not null, a diagnostic string will be written
   *                     which will help determine how the indexes contributed
   *                     to this search.
   * @return The candidate entry IDs that might contain a value
   *         that matches the filter substrings.
   */
  public EntryIDSet evaluateSubstringFilter(SearchFilter filter,
                                            StringBuilder debugBuffer)
  {
    AttributeType attrType = indexConfig.getAttribute();
    MatchingRule rule = attrType.getSubstringMatchingRule();
    IndexQueryFactory<IndexQuery> factory = null;

    if(indexManager == null
            || (factory = indexManager.getQueryFactory(rule))==null)
    {
      // There is no index on this matching rule.
      return IndexQuery.createNullIndexQuery().evaluate();
    }

    try
    {
      if(debugBuffer != null)
      {
        debugBuffer.append("[INDEX:");
       IndexConfig config =
                new JEIndexConfig(indexConfig.getSubstringLength());
       MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(rule);
        for(IndexKeyFactory keyFactory :  provider.getIndexKeyFactory(config))
        {
          String longID = attrType.getNameOrOID() + "."  + keyFactory.getIndexID();
          debugBuffer.append(longID);
        }
        debugBuffer.append("]");
      }
      MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(rule);
      ByteSequence assertion = normalizeSubFilter(filter);
      IndexQuery expression = provider.createIndexQuery(assertion, factory);
      return expression.evaluate();
    }
    catch (DirectoryException e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }
      return IndexQuery.createNullIndexQuery().evaluate();
    }
  }

  /**
   * Retrieve the entry IDs that might have a value greater than or
   * equal to the lower bound value, and less than or equal to the
   * upper bound value.
   *
   * @param lowerValue The lower bound value
   * @param upperValue The upper bound value
   * @return The candidate entry IDs.
   */
  public EntryIDSet evaluateBoundedRange(AttributeValue lowerValue,
                                          AttributeValue upperValue)
  {
    //Create intersection query.
    AttributeType attrType = indexConfig.getAttribute();
    MatchingRule rule = attrType.getOrderingMatchingRule();
    IndexQueryFactory<IndexQuery> factory = null;

    if(indexManager == null
            || (factory = indexManager.getQueryFactory(rule))==null)
    {
      // There is no index on this matching rule.
      return IndexQuery.createNullIndexQuery().evaluate();
    }

    try
    {
      MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(rule);
      List<IndexQuery> queries = new ArrayList<IndexQuery>();
      queries.add(provider.createLessThanOrEqualIndexQuery(
              upperValue.getValue(), factory));
      queries.add(provider.createGreaterThanOrEqualIndexQuery(
              lowerValue.getValue(), factory));
      IndexQuery expression = factory.createIntersectionQuery(queries);
      return expression.evaluate();
    }
    catch (DirectoryException e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }
      return IndexQuery.createNullIndexQuery().evaluate();
    }
  }


  /**
   * Retrieve the entry IDs that might match an approximate filter.
   *
   * @param approximateFilter The approximate filter.
   * @param debugBuffer If not null, a diagnostic string will be written
   *                     which will help determine how the indexes contributed
   *                     to this search.
   * @return The candidate entry IDs that might contain the filter
   *         assertion value.
   */
  public EntryIDSet evaluateApproximateFilter(SearchFilter approximateFilter,
                                              StringBuilder debugBuffer)
  {
    AttributeType attrType = indexConfig.getAttribute();
    MatchingRule rule = attrType.getApproximateMatchingRule();
    IndexQueryFactory<IndexQuery> factory = null;

    if(indexManager == null
            || (factory = indexManager.getQueryFactory(rule))==null)
    {
      // There is no index on this matching rule.
      return IndexQuery.createNullIndexQuery().evaluate();
    }
    try
    {
      if(debugBuffer != null)
      {
        debugBuffer.append("[INDEX:");
       IndexConfig config =
                new JEIndexConfig(indexConfig.getSubstringLength());
       MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(rule);
        for(IndexKeyFactory keyFactory :  provider.getIndexKeyFactory(config))
        {
          String longID = attrType.getNameOrOID() + "."  + keyFactory.getIndexID();
          debugBuffer.append(longID);
        }
        debugBuffer.append("]");
      }
      MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(rule);
      IndexQuery expression = provider.createIndexQuery(
              approximateFilter.getAssertionValue().getValue(), factory);
      return expression.evaluate();
    }
    catch (DirectoryException e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }
      return new EntryIDSet();
    }
  }


  /**
   * Retrieve the entry IDs that might match an extensible filter.
   *
   * @param extensibleFilter The extensible filter.
   * @param debugBuffer If not null, a diagnostic string will be written
   *                     which will help determine how the indexes contributed
   *                     to this search.
   * @return The candidate entry IDs that might contain the filter
   *         assertion value.
   */
  public EntryIDSet evaluateExtensibleFilter(SearchFilter extensibleFilter,
                                              StringBuilder debugBuffer)
  {
    //Get the Matching Rule OID of the filter.
    String nOID  = extensibleFilter.getMatchingRuleID();
    MatchingRule rule =
            DirectoryServer.getMatchingRule(nOID);
    IndexQueryFactory<IndexQuery> factory = null;
    if(indexManager == null
            || (factory = indexManager.getQueryFactory(rule))==null)
    {
      // There is no index on this matching rule.
      return IndexQuery.createNullIndexQuery().evaluate();
    }

    try
    {

      if(debugBuffer != null)
      {
        debugBuffer.append("[INDEX:");
        IndexConfig config =
                new JEIndexConfig(indexConfig.getSubstringLength());
        MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(rule);
        for(IndexKeyFactory keyFactory :  provider.getIndexKeyFactory(config))
        {
          String longID = getAttributeType().getNameOrOID()
                  + "."  + keyFactory.getIndexID();
          debugBuffer.append(longID);
        }
        debugBuffer.append("]");
      }
      MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(rule);
      IndexQuery expression = provider.createIndexQuery(
              extensibleFilter.getAssertionValue().getValue(), factory);
      return expression.evaluate();
    }
    catch (DirectoryException e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }
      return IndexQuery.createNullIndexQuery().evaluate();
    }
  }



  /**
   * Close cursors related to the attribute indexes.
   *
   * @throws DatabaseException If a database error occurs.
   */
  public void closeCursors() throws DatabaseException
  {

    if (presenceIndex != null)
    {
      presenceIndex.closeCursor();
    }

    for(Index index : indexManager.getIndexes())
    {
      index.closeCursor();
    }
  }

  /**
   * Return the number of values that have exceeded the entry limit since this
   * object was created.
   *
   * @return The number of values that have exceeded the entry limit.
   */
  public long getEntryLimitExceededCount()
  {
    long entryLimitExceededCount = 0;

    if (presenceIndex != null)
    {
      entryLimitExceededCount += presenceIndex.getEntryLimitExceededCount();
    }

    for(Index index : indexManager.getIndexes())
    {
      entryLimitExceededCount += index.getEntryLimitExceededCount();
    }

    return entryLimitExceededCount;
  }

  /**
   * Get a list of the databases opened by this attribute index.
   * @param dbList A list of database containers.
   */
  public void listDatabases(List<DatabaseContainer> dbList)
  {
    if (presenceIndex != null)
    {
      dbList.add(presenceIndex);
    }

    for(Index index : indexManager.getIndexes())
    {
      dbList.add(index);
    }
  }

  /**
   * Get a string representation of this object.
   * @return return A string representation of this object.
   */
  @Override
  public String toString()
  {
    return getName();
  }

  /**
   * {@inheritDoc}
   */
  public synchronized boolean isConfigurationChangeAcceptable(
      LocalDBIndexCfg cfg,
      List<Message> unacceptableReasons)
  {
    AttributeType attrType = cfg.getAttribute();
    MatchingRule rule = null;
    if (cfg.getIndexType().contains(LocalDBIndexCfgDefn.IndexType.EQUALITY))
    {
      rule = attrType.getEqualityMatchingRule();
      if (rule == null && indexManager.getQueryFactory(rule)==null)
      {
        Message message = ERR_CONFIG_INDEX_TYPE_NEEDS_MATCHING_RULE.get(
                String.valueOf(String.valueOf(attrType)), "equality");
        unacceptableReasons.add(message);
        return false;
      }
    }

    if (cfg.getIndexType().contains(LocalDBIndexCfgDefn.IndexType.SUBSTRING))
    {
      rule = attrType.getSubstringMatchingRule();
      if (rule == null && indexManager.getQueryFactory(rule) == null)
      {
        Message message = ERR_CONFIG_INDEX_TYPE_NEEDS_MATCHING_RULE.get(
                String.valueOf(attrType), "substring");
        unacceptableReasons.add(message);
        return false;
      }

    }

    if (cfg.getIndexType().contains(LocalDBIndexCfgDefn.IndexType.ORDERING))
    {
      rule = attrType.getOrderingMatchingRule();
      if (rule == null && indexManager.getQueryFactory(rule) == null)
      {
        Message message = ERR_CONFIG_INDEX_TYPE_NEEDS_MATCHING_RULE.get(
                String.valueOf(attrType), "ordering");
        unacceptableReasons.add(message);
        return false;
      }
    }
    if (cfg.getIndexType().contains(LocalDBIndexCfgDefn.IndexType.APPROXIMATE))
    {
      rule = attrType.getApproximateMatchingRule();
      if (rule == null && indexManager.getQueryFactory(rule) == null)
      {
        Message message = ERR_CONFIG_INDEX_TYPE_NEEDS_MATCHING_RULE.get(
                String.valueOf(attrType), "approximate");
        unacceptableReasons.add(message);
        return false;
      }
    }
    if (cfg.getIndexType().contains(LocalDBIndexCfgDefn.IndexType.EXTENSIBLE))
    {
      Set<String> newRules =
              cfg.getIndexExtensibleMatchingRule();
      if (newRules == null || newRules.size() == 0)
      {
        Message message = ERR_CONFIG_INDEX_TYPE_NEEDS_MATCHING_RULE.get(
                String.valueOf(attrType), "extensible");
        unacceptableReasons.add(message);
        return false;
      }
    }

    return true;
  }

  /**
   * {@inheritDoc}
   */
  public synchronized ConfigChangeResult applyConfigurationChange(
      LocalDBIndexCfg cfg)
  {
    ConfigChangeResult ccr;
    boolean adminActionRequired = false;
    ArrayList<Message> messages = new ArrayList<Message>();
    ArrayList<String> ids = new ArrayList<String>();
    try
    {
      AttributeType attrType = cfg.getAttribute();
      String name =
        entryContainer.getDatabasePrefix() + "_" + attrType.getNameOrOID();
      int indexEntryLimit = cfg.getIndexEntryLimit();
      IndexConfig config = new JEIndexConfig(cfg.getSubstringLength());
      if (cfg.getIndexType().contains(LocalDBIndexCfgDefn.IndexType.EQUALITY))
      {
        MatchingRule matchingRule = attrType.getEqualityMatchingRule();
        if (matchingRule == null)
        {
          Message message = ERR_CONFIG_INDEX_TYPE_NEEDS_MATCHING_RULE.get(
              String.valueOf(attrType), "equality");
          throw new ConfigException(message);
        }
          indexManager.applyNewIndexConfiguration(cfg, matchingRule,messages);
      }
      else
      {
        MatchingRule matchingRule = attrType.getEqualityMatchingRule();
        MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(matchingRule);
        for(IndexKeyFactory factory : provider.getIndexKeyFactory(config))
        {
          ids.add(factory.getIndexID());
        }
      }
      if (cfg.getIndexType().contains(LocalDBIndexCfgDefn.IndexType.PRESENCE))
      {
        if(presenceIndex == null)
        {
          IndexKeyFactory factory = new PresenceIndexKeyFactory();
           this.presenceIndex = new Index(name + "." + factory.getIndexID(),
                                     new Indexer(attrType,factory),
                                     state,
                                     indexEntryLimit,
                                     cursorEntryLimit,
                                     false,
                                     env,
                                     entryContainer);
          presenceIndex.open();

          if(!presenceIndex.isTrusted())
          {
            adminActionRequired = true;
            messages.add(NOTE_JEB_INDEX_ADD_REQUIRES_REBUILD.get(
                presenceIndex.getName()));
          }
        }
        else
        {
          // already exists. Just update index entry limit.
          if(this.presenceIndex.setIndexEntryLimit(indexEntryLimit))
          {
            adminActionRequired = true;

            Message message =
                    NOTE_JEB_CONFIG_INDEX_ENTRY_LIMIT_REQUIRES_REBUILD.get(
                            presenceIndex.getName());
            messages.add(message);
          }
        }
      }
      else
      {
        if (presenceIndex != null)
        {
          entryContainer.exclusiveLock.lock();
          try
          {
            entryContainer.deleteDatabase(presenceIndex);
            presenceIndex = null;
          }
          catch(DatabaseException de)
          {
            messages.add(Message.raw(
                    StaticUtils.stackTraceToSingleLineString(de)));
            ccr = new ConfigChangeResult(
                DirectoryServer.getServerErrorResultCode(), false, messages);
            return ccr;
          }
          finally
          {
            entryContainer.exclusiveLock.unlock();
          }
        }
      }

      if (cfg.getIndexType().contains(LocalDBIndexCfgDefn.IndexType.SUBSTRING))
      {
        MatchingRule matchingRule = attrType.getSubstringMatchingRule();
        if (matchingRule == null)
        {
          Message message = ERR_CONFIG_INDEX_TYPE_NEEDS_MATCHING_RULE.get(
              String.valueOf(attrType), "substring");
          throw new ConfigException(message);
        }
          indexManager.applyNewIndexConfiguration(cfg, matchingRule,messages);
      }
      else
      {
        MatchingRule matchingRule = attrType.getEqualityMatchingRule();
        MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(matchingRule);
        for(IndexKeyFactory factory : provider.getIndexKeyFactory(config))
        {
          ids.add(factory.getIndexID());
        }
      }

      if (cfg.getIndexType().contains(LocalDBIndexCfgDefn.IndexType.ORDERING))
      {
        MatchingRule matchingRule = attrType.getOrderingMatchingRule();
        if (matchingRule == null)
        {
          Message message = ERR_CONFIG_INDEX_TYPE_NEEDS_MATCHING_RULE.get(
              String.valueOf(attrType), "ordering");
          throw new ConfigException(message);
        }
          indexManager.applyNewIndexConfiguration(cfg, matchingRule,messages);
      }
      else
      {
        MatchingRule matchingRule = attrType.getOrderingMatchingRule();
        MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(matchingRule);
        for(IndexKeyFactory factory : provider.getIndexKeyFactory(config))
        {
          ids.add(factory.getIndexID());
        }
      }

      if (cfg.getIndexType().contains(
              LocalDBIndexCfgDefn.IndexType.APPROXIMATE))
      {
        MatchingRule matchingRule = attrType.getApproximateMatchingRule();
        if (matchingRule == null)
        {
          Message message = ERR_CONFIG_INDEX_TYPE_NEEDS_MATCHING_RULE.get(
              String.valueOf(attrType), "equality");
          throw new ConfigException(message);
        }
          indexManager.applyNewIndexConfiguration(cfg, matchingRule,messages);
      }
      else
      {
        MatchingRule matchingRule = attrType.getApproximateMatchingRule();
        MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(matchingRule);
        for(IndexKeyFactory factory : provider.getIndexKeyFactory(config))
        {
          ids.add(factory.getIndexID());
        }
      }

      if (cfg.getIndexType().contains(
              LocalDBIndexCfgDefn.IndexType.EXTENSIBLE))
      {
        Set<String> extensibleMatchingRules =
            cfg.getIndexExtensibleMatchingRule();
        Set<MatchingRule> validRules = new HashSet<MatchingRule>();
        for(String ruleName:extensibleMatchingRules)
        {
          MatchingRule rule =DirectoryServer.getMatchingRule(
                                            StaticUtils.toLowerCase(ruleName));
           if(rule == null)
          {
            Message message =
                    ERR_CONFIG_INDEX_TYPE_NEEDS_VALID_MATCHING_RULE.get(
                    String.valueOf(attrType),ruleName);
            logError(message);
            continue;
          }
          validRules.add(rule);
          adminActionRequired = 
                  indexManager.applyNewIndexConfiguration(cfg, rule,messages);
        //Some rules might have been removed from the configuration.
        Set<MatchingRule> deletedRules =
                new HashSet<MatchingRule>();
        for(MatchingRule r:indexManager.getAllIndexedRules())
        {
          if(!validRules.contains(r))
          {
            deletedRules.add(r);
          }
        }
        if(deletedRules.size() > 0)
        {
          entryContainer.exclusiveLock.lock();
          try
          {
            for(MatchingRule mRule:deletedRules)
            {
              Set<MatchingRule> rules =
                      new HashSet<MatchingRule>();
              MatchingRuleIndexProvider provider = DirectoryServer.getIndexProvider(mRule);
              for(IndexKeyFactory factory: provider.getIndexKeyFactory(config))
              {
                String id = attrType.getNameOrOID()  + "."
                 + factory.getIndexID();
                rules.addAll(indexManager.getMatchingRuleByIndexID(id));
                ids.add(id);
              }
              if(rules.isEmpty())
              {
                //Rule has been already deleted.
                continue;
              }
              //If all the rules are part of the deletedRules, delete
              //this index.
              if(deletedRules.containsAll(rules))
              {
                //it is safe to delete this index as it is not shared.
                for(String indexID : ids)
                {
                  Index extensibleIndex = indexManager.getIndex(indexID);
                  entryContainer.deleteDatabase(extensibleIndex);
                  extensibleIndex = null;
                  indexManager.deleteIndex(indexID);
                  indexManager.deleteRule(indexID);
                }
              }
              else
              {
                for(String indexID : ids)
                {
                  indexManager.deleteRule(rule, indexID);
                }
              }
            }
          }
          catch(DatabaseException de)
          {
            messages.add(
                  Message.raw(StaticUtils.stackTraceToSingleLineString(de)));
            ccr = new ConfigChangeResult(
              DirectoryServer.getServerErrorResultCode(), false, messages);
            return ccr;
          }
          finally
          {
            entryContainer.exclusiveLock.unlock();
          }
        }
      }
      }
      else
      {
        //Delete all the extensible indexes.
        
      }

      indexConfig = cfg;

      return new ConfigChangeResult(ResultCode.SUCCESS, adminActionRequired,
                                    messages);
    }
    catch(Exception e)
    {
      messages.add(Message.raw(StaticUtils.stackTraceToSingleLineString(e)));
      ccr = new ConfigChangeResult(DirectoryServer.getServerErrorResultCode(),
                                   adminActionRequired,
                                   messages);
      return ccr;
    }
  }

  /**
   * Set the index truststate.
   * @param txn A database transaction, or null if none is required.
   * @param trusted True if this index should be trusted or false
   *                otherwise.
   * @throws DatabaseException If an error occurs in the JE database.
   */
  public synchronized void setTrusted(Transaction txn, boolean trusted)
      throws DatabaseException
  {
    if (presenceIndex != null)
    {
      presenceIndex.setTrusted(txn, trusted);
    }

    for(Index index : indexManager.getIndexes())
    {
      index.setTrusted(txn, trusted);
    }
  }

  /**
   * Return true iff this index is trusted.
   * @return the trusted state of this index
   */
  public boolean isTrusted()
  {
    for(Index index : indexManager.getIndexes())
    {
      if (!index.isTrusted())
      {
        return false;
      }
    }


    if (presenceIndex != null && !presenceIndex.isTrusted())
    {
      return false;
    }

    return true;
  }

  /**
   * Set the rebuild status of this index.
   * @param rebuildRunning True if a rebuild process on this index
   *                       is running or False otherwise.
   */
  public synchronized void setRebuildStatus(boolean rebuildRunning)
  {
    for(Index index : indexManager.getIndexes())
    {
      index.setRebuildStatus(rebuildRunning);
    }

    if (presenceIndex != null)
    {
      presenceIndex.setRebuildStatus(rebuildRunning);
    }
  }

  /**
   * Get the JE database name prefix for indexes in this attribute
   * index.
   *
   * @return JE database name for this database container.
   */
  public String getName()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(entryContainer.getDatabasePrefix());
    builder.append("_");
    builder.append(indexConfig.getAttribute().getNameOrOID());
    return builder.toString();
  }

  /**
   * Return the presence index.
   *
   * @return The presence index.
   */
  public Index getPresenceIndex() {
    return presenceIndex;
  }



  /**
   * Retrieves all the indexes used by this attribute index.
   *
   * @return A collection of all indexes in use by this attribute
   * index.
   */
  public Collection<Index> getAllIndexes() {
    LinkedHashSet<Index> indexes = new LinkedHashSet<Index>();

    if (presenceIndex != null)
    {
      indexes.add(presenceIndex);
    }

    indexes.addAll(indexManager.getIndexes());
    return indexes;
  }




  //Converts the different components of the substring filter to a ByteString.
  private ByteString normalizeSubFilter(SearchFilter filter)
  {
    // Organizes the filter Values in the following format:
    // initialLength, initial, numberofany, anyLength1, any1,
    // anyLength2, any2, ..., anyLengthn, anyn, finalLength,
    // final
    List<Integer> normalizedList = new ArrayList<Integer>();

    if (filter.getSubInitialElement() == null)
    {
      normalizedList.add(0);
    }
    else
    {
      byte[] initialBytes = filter.getSubInitialElement().toByteArray();
      int length = initialBytes.length;
      normalizedList.add(length);
      for (int i = 0; i < length; i++)
      {
        normalizedList.add((int) initialBytes[i]);
      }
    }

    List<ByteString> subAny = filter.getSubAnyElements();
    if (subAny.size() == 0)
    {
      normalizedList.add(0);
    }
    else
    {
      normalizedList.add(subAny.size());
      for (ByteString any : subAny)
      {
        byte[] anyBytes = any.toByteArray();
        int length = anyBytes.length;
        normalizedList.add(length);
        for (int i = 0; i < length; i++)
        {
          normalizedList.add((int) anyBytes[i]);
        }
      }
    }

    ByteString subFinal = filter.getSubFinalElement();
    if (subFinal == null)
    {
      normalizedList.add(0);
    }
    else
    {
      byte[] subFinalBytes = subFinal.toByteArray();
      int length = subFinalBytes.length;
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
   * This class extends the IndexConfig for JE Backend.
   */
  public static class JEIndexConfig extends IndexConfig
  {
    //The length of the substring index.
    private int substringLength;


    /**
     * Creates a new JEIndexConfig instance.
     * @param substringLength The length of the substring.
     */
    private JEIndexConfig(int substringLength)
    {
      this.substringLength = substringLength;
    }


    /**
     * Returns the length of the substring.
     * @return the length of the substring.
     */
   public int getSubstringLength()
   {
     return substringLength;
   }
  }



   /**
   * This class manages all the configured matching rules and their
   * their corresponding indexes.
   */
  private class MatchingRuleBasedIndexManager
  {
    /**
      * The mapping of index ID and Index database.
      */
    private final Map<String,Index> indexByID;


    /**
     * The mapping of Index ID and Set the matching rules.
     */
    private final Map<String,Set<MatchingRule>> rulesByID;


    /**
     * The Map of configured MatchingRule and the corresponding
     * IndexQueryFactory.
     */
    private final Map<MatchingRule,
            IndexQueryFactory<IndexQuery>> factoryByRule;



    /**
     * Creates a new instance of MatchingRuleBasedIndexManager.
     */
    private MatchingRuleBasedIndexManager()
    {
      indexByID = new HashMap<String,Index>();
      rulesByID = new HashMap<String,Set<MatchingRule>>();
      factoryByRule = new HashMap<MatchingRule,
              IndexQueryFactory<IndexQuery>>();
    }


    /**
     * Reads the configuration object and creates an index if the matching rule
     * doesn't have a corresponding index. If the configuration doesn't contain
     * an index corresponding to the matching rule, it is deleted.
     *
     * @param cfg The index configuration object.
     * @param rule The matching rule that needs to be indexed.
     * @param messages List of messages.
     * @return Whether the admin action is required.
     * @throws com.sleepycat.je.DatabaseException
     */
     public boolean applyNewIndexConfiguration(LocalDBIndexCfg cfg,
             MatchingRule rule,
             List<Message> messages) throws DatabaseException
    {
      IndexConfig config = new JEIndexConfig(cfg.getSubstringLength());
      int indexEntryLimit = cfg.getIndexEntryLimit();
      AttributeType attrType = getAttributeType();
      boolean adminActionRequired = false;

      Map<String,Index> indexMap = new HashMap<String,Index>();
      //Get the IndexProvider for this matching rule.
      MatchingRuleIndexProvider provider = getIndexProvider(rule);
      for(IndexKeyFactory keyFactory : provider.getIndexKeyFactory(config))
      {
        String indexID = attrType.getNameOrOID() + "." + keyFactory.getIndexID();
        if(!hasIndexWithID(indexID))
        {
          String indexName = entryContainer.getDatabasePrefix() + "_"  + indexID;
          Index index = new Index(indexName,
                                 new Indexer(attrType,keyFactory),
                                 state,
                                 indexEntryLimit,
                                 cursorEntryLimit,
                                 false,
                                 env,
                                 entryContainer);
          addIndex(index, indexID);
          index.open();
          if(!index.isTrusted())
          {
            adminActionRequired = true;
            messages.add(NOTE_JEB_INDEX_ADD_REQUIRES_REBUILD.get(
                  index.getName()));
          }
        }
        else
        {
          Index index = indexManager.getIndex(indexID);
          if(index.setIndexEntryLimit(indexEntryLimit))
          {
            adminActionRequired = true;
            Message message =
                  NOTE_JEB_CONFIG_INDEX_ENTRY_LIMIT_REQUIRES_REBUILD.get(
                          index.getName());
            messages.add(message);
          }
          if(indexID.equals(SUBSTRING_INDEX_ID) && indexConfig.getSubstringLength() !=
            cfg.getSubstringLength())
          {
            index.setIndexer(new Indexer(getAttributeType(),keyFactory));
          }
        }
        addRule(indexID, rule);
        indexMap.put(attrType.getNameOrOID() , getIndex(indexID));
      }
      IndexQueryFactory<IndexQuery> queryFactory =
            new IndexQueryFactoryImpl(indexMap);
      addQueryFactory(rule, queryFactory);
      return adminActionRequired;
    }



    /**
     * Registers a new index for the given matching rule. This must be called
     * when registering a new index while reading the configuration for the
     * first time.
     * 
     * @param cfg The index configuration object.
     * @param rule The matching rule that needs to be indexed.
     * @throws DatabaseException
     */
    public void registerNewIndex(LocalDBIndexCfg cfg,MatchingRule rule)
            throws DatabaseException
    {
      IndexConfig config = new JEIndexConfig(cfg.getSubstringLength());
      int indexEntryLimit = cfg.getIndexEntryLimit();
      AttributeType attrType = getAttributeType();

      Map<String,Index> indexMap = new HashMap<String,Index>();
      //Get the IndexProvider for this matching rule.
      MatchingRuleIndexProvider provider = getIndexProvider(rule);
      for(IndexKeyFactory keyFactory : provider.getIndexKeyFactory(config))
      {
        String shortIndexID = keyFactory.getIndexID();
        String longIndexID = attrType.getNameOrOID() + "." + shortIndexID;
        if(!hasIndexWithID(longIndexID))
        {
          String indexName = entryContainer.getDatabasePrefix() + "_"  
                  + longIndexID;
          Index index = new Index(indexName,
                                 new Indexer(attrType,keyFactory),
                                 state,
                                 indexEntryLimit,
                                 cursorEntryLimit,
                                 false,
                                 env,
                                 entryContainer);
          addIndex(index, longIndexID);
        }
        addRule(longIndexID, rule);
        indexMap.put(shortIndexID, getIndex(longIndexID));
      }
      IndexQueryFactory<IndexQuery> queryFactory =
            new IndexQueryFactoryImpl(indexMap);
      addQueryFactory(rule, queryFactory);
    }



    /**
     * Deregisters a collection of index Ids.
     * @param ids The collection of index ids.
     * @param messages A list of messages.
     * @throws DatabaseException
     */
    public void deregisterIndex(Collection<String> ids,
            ArrayList<Message> messages)
            throws DatabaseException
    {
      entryContainer.exclusiveLock.lock();
      try
      {
        for(String indexID : ids)
        {
          Index extensibleIndex = getIndex(indexID);
          entryContainer.deleteDatabase(extensibleIndex);
          extensibleIndex = null;
          deleteIndex(indexID);
          deleteRule(indexID);
        }
      }
      finally
      {
        entryContainer.exclusiveLock.unlock();
      }
    }

    
    
    /**
     * Returns all configured matching rule  instances.
     * @return A Set  of  matching rules.
     */
    public Set<MatchingRule> getAllIndexedRules()
    {
      return factoryByRule.keySet();
    }
    
    
    
    /**
     * Returns the substring index
     */
    public Index getSubstringIndex()
    {
      MatchingRule rule = getAttributeType().getSubstringMatchingRule();
      if(rule == null)
      {
        return null;
      }
      MatchingRuleIndexProvider provider = getIndexProvider(rule);
      JEIndexConfig config = new JEIndexConfig(
              indexConfig.getSubstringLength());
      IndexKeyFactory factory = 
              provider.getIndexKeyFactory(config).iterator().next();
      return getIndex(factory.getIndexID());
    }
    
    

    /**
     * Returns  MatchingRule instances for an index.
     * @param indexID The index ID of a matching rule index.
     * @return A Set of matching rules corresponding to an index ID.
     */
    public Set<MatchingRule>
            getMatchingRuleByIndexID(String indexID)
    {
      Set<MatchingRule> rules = rulesByID.get(indexID);
      if(rules == null)
      {
        return Collections.emptySet();
      }
      else
      {
        return Collections.unmodifiableSet(rulesByID.get(indexID));
      }
    }
    
    
    
    public boolean isIndexSubstringType(Index index)
    {
      //String id = indexByID.entrySet().
      return true;
    }

    
    /**
     * Returns whether an index is present or not.
     * @param indexID The index ID of a matching rule index.
     * @return True if an index is present. False if there is no matching index.
     */
    private boolean hasIndexWithID(String indexID)
    {
      return indexByID.containsKey(indexID);
    }


    /**
     * Returns the index corresponding to an index ID.
     * @param indexID The ID of an index.
     * @return The matching rule index corresponding to the index ID.
     */
    private Index getIndex(String indexID)
    {
      return indexByID.get(indexID);
    }


    /**
     * Adds a new matching Rule and the name of the associated index.
     * @indexName Name of the index.
     * @rule A MatchingRule instance that needs to be indexed.
     */
    private void addRule(String indexName,MatchingRule rule)
    {
      Set<MatchingRule> rules = rulesByID.get(indexName);
      if(rules == null)
      {
        rules = new HashSet<MatchingRule>();
        rulesByID.put(indexName, rules);
      }
      rules.add(rule);
    }

    
    /**
     * Adds a new Index and its name.
     * @param index The matching rule index.
     * @indexName The name of the index.
     */
    private void addIndex(Index index,String indexName)
    {
      indexByID.put(indexName, index);
    }

    
    /**
     * Returns all the configured indexes.
     * @return All the available matching rule indexes.
     */
    private Collection<Index> getIndexes()
    {
      return Collections.unmodifiableCollection(indexByID.values());
    }


    /**
     * Returns a map of all the configured indexes and their types.
     * @return A map of all the available matching rule indexes
     *             and their types.
     */
    public Map<String,Collection<Index>> getIndexMap()
    {
      if(indexByID.isEmpty())
      {
        return Collections.emptyMap();
      }
      Collection<Index> substring = new ArrayList<Index>();
      Collection<Index> equality = new ArrayList<Index>();
      Collection<Index> ordering = new ArrayList<Index>();
      Collection<Index> approximate = new ArrayList<Index>();      
      Collection<Index> shared = new ArrayList<Index>();
      for(Map.Entry<String,Index> entry :  indexByID.entrySet())
      {
        String indexID = entry.getKey();
        if(indexID.endsWith(SUBSTRING_INDEX_ID))
        {
          substring.add(entry.getValue());
        }
        else if(indexID.endsWith(EQUALITY_INDEX_ID))
        {
          equality.add(entry.getValue());
        }
        else if(indexID.endsWith(ORDERING_INDEX_ID))
        {
          ordering.add(entry.getValue());
        }
        else if(indexID.endsWith(SHARED_INDEX_ID))
        {
          shared.add(entry.getValue());
        }
        else if(indexID.endsWith(APPROXIMATE_INDEX_ID))
        {
          approximate.add(entry.getValue());
        }
      }
      Map<String,Collection<Index>> indexMap =
              new HashMap<String,Collection<Index>>();
      indexMap.put(SUBSTRING_INDEX_ID, substring);
      indexMap.put(SHARED_INDEX_ID, shared);
      indexMap.put(EQUALITY_INDEX_ID,equality);
      indexMap.put(ORDERING_INDEX_ID,ordering);
      indexMap.put(APPROXIMATE_INDEX_ID,approximate);
      return Collections.unmodifiableMap(indexMap);
    }


    /**
     * Deletes an index corresponding to the index ID.
     * @param indexID Name of the index.
     */
    private void deleteIndex(String indexID)
    {
      indexByID.remove(indexID);
    }


    /**
     * Deletes a matching rule from the list of available rules.
     * @param rule The MatchingRule that needs to be removed.
     * @param indexID The name of the index corresponding to the rule.
     */
    private void deleteRule(MatchingRule rule,String indexID)
    {
      Set<MatchingRule> rules = rulesByID.get(indexID);
      rules.remove(rule);
      if(rules.size() == 0)
      {
        rulesByID.remove(indexID);
      }
      factoryByRule.remove(rule);
    }


    /**
     * Adds a MatchingRule and its corresponding IndexQueryFactory.
     * @param rule A MatchingRule that needs to be added.
     * @param query A query factory matching the rule.
     */
    private void addQueryFactory(MatchingRule rule,
            IndexQueryFactory<IndexQuery> query)
    {
      factoryByRule.put(rule, query);
    }


    /**
     * Returns the query factory associated with the rule.
     * @param rule A MatchingRule that needs to be searched.
     * @return An IndexQueryFactory corresponding to the matching rule.
     */
    private IndexQueryFactory<IndexQuery> getQueryFactory(
            MatchingRule rule)
    {
      return factoryByRule.get(rule);
    }


    /**
     * Deletes   matching rules from the list of available rules.
     * @param indexID The name of the index corresponding to the rules.
     */
    private void deleteRule(String indexID)
    {
      Set<MatchingRule> rules  = rulesByID.get(indexID);
      factoryByRule.remove(rules);
      rules.clear();
      rulesByID.remove(indexID);
    }


    /**
     * Deletes all references to matching rules and the indexes.
     */
    private void deleteAll()
    {
      indexByID.clear();
      rulesByID.clear();
      factoryByRule.clear();
    }
  }
  
 
   public Map<String,Collection<Index>> getIndexMap()
   {
     return indexManager.getIndexMap();
   }
   
   
   public Set<MatchingRule> getAllIndexMatchingRules()
   {
     return indexManager.getAllIndexedRules();
   }
   
   
   public IndexConfig getIndexConfig()
   {
     return new JEIndexConfig(indexConfig.getSubstringLength());
   }
   
   public Index getIndexById(String id)
   {
     return indexManager.getIndex(id);
   }
}