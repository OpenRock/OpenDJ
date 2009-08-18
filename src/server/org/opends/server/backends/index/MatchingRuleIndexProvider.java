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


package org.opends.server.backends.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.opends.server.api.ApproximateMatchingRule;
import org.opends.server.api.EqualityMatchingRule;
import org.opends.server.api.MatchingRule;
import org.opends.server.api.OrderingMatchingRule;
import org.opends.server.api.SubstringMatchingRule;
import org.opends.server.core.DirectoryServer;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.types.DirectoryException;

/**
 * This class acts as an index provider for a particular matching rule.
 * All matching rules that need to be indexed, must have an index provider
 * that extends this class and makes a matching rule indexable.
 */
public abstract class MatchingRuleIndexProvider
{
  /**
   * Returns a collection of index key factories  associated with a particular
   * matching rule.
   *
   * @param config
   *          The index configuration to be used by this matching
   *          rule.
   * @return The collection of index key factories  associated with a particular
   *         matching rule.
   */
  public abstract Collection<IndexKeyFactory> getIndexKeyFactory(
      IndexConfig config);



  /**
   * Returns the matching rule this index provider corresponds to.
   *
   * @return The matching rule this index provider corresponds to.
   */
  public abstract MatchingRule getMatchingRule();



  /**
   * Returns an index query appropriate for the provided search filter.
   *
   * @param <T>
   *          The type of index query created by the {@code factory}.
   * @param assertion The assertion value that is used for creating
   *          this index query.
   * @param factory
   *          The index query factory which should be used to
   *          construct the index query.
   * @return The index query appropriate for the provided assertion.
   * @throws DirectoryException
   *           If an error occurs while generating the index query.
   */
  public abstract <T> T createIndexQuery(ByteSequence assertion,
      IndexQueryFactory<T> factory) throws DirectoryException;



  /**
   * Returns an index query appropriate for the provided assertion
   * value. It is used only for those ordering matching rules
   * that need the ordering mechanism to be specified during
   * index search. For example, CaseIgnoreOrderingMatchingRule
   * must use this method while searching the index for values greater
   * than or equal to the assertion value.
   *
   * <P> For all but an Ordering matching rule, this method defaults to
   * {@link #createIndexQuery()}</P>
   *
   *
   * @param <T>
   *          The type of index query created by the {@code factory}.
   * @param  assertion
   *          The assertion value that is used for creating
   *          this index query.
   * @param factory
   *          The index query factory which should be used to
   *          construct the index query.
   * @return The index query appropriate for the provided assertion.
   * @throws DirectoryException
   *           If an error occurs while generating the index query.
   */
  public <T> T createGreaterThanOrEqualIndexQuery(ByteSequence assertion,
      IndexQueryFactory<T> factory) throws DirectoryException
  {
    //Default implementation.
    return createIndexQuery(assertion,factory);
  }



  /**
   * Returns an index query appropriate for the provided assertion value.
   * It is used only for those ordering matching rules that need the ordering
   * mechanism to be specified during index search.
   * For example, CaseIgnoreOrderingMatchingRule must use this method
   * while searching the index for values less than or equal to the assertion
   * value.
   *
   * <P> For all but an Ordering matching rule, this method defaults to
   * {@link #createIndexQuery()}</P>
   *
   * @param <T>
   *          The type of index query created by the {@code factory}.
   * @param  assertion
   *          The assertion value that is used for creating
   *          this index query.
   * @param factory
   *          The index query factory which should be used to
   *          construct the index query.
   * @return The index query appropriate for the provided assertion value.
   * @throws DirectoryException
   *           If an error occurs while generating the index query.
   */
  public <T> T createLessThanOrEqualIndexQuery(ByteSequence assertion,
      IndexQueryFactory<T> factory) throws DirectoryException
  {
    //Default implementation.
    return createIndexQuery(assertion,factory);
  }



  /**
   * Returns the default matching rule index provider for a given equality
   * matching rule.
   *
   * @param rule
   *           The equality matching rule.
   * @param indexID
   *           The index id to be used by the provider.
   * @return The default equality matching rule index provider.
   */
  public static MatchingRuleIndexProvider
          getDefaultEqualityIndexProvider(EqualityMatchingRule rule,
          String indexID)
  {
    return new DefaultEqualityIndexProvider(rule,indexID);
  }



  /**
   * Returns the default matching rule index provider for a given ordering
   * matching rule.
   *
   * @param rule
   *           The ordering matching rule.
   * @param indexID
   *            The index id to be used by the provider.
   * @return The default ordering matching rule index provider.
   */
  public static MatchingRuleIndexProvider
          getDefaultOrderingIndexProvider(OrderingMatchingRule rule,
          String indexID)
  {
    return new DefaultOrderingIndexProvider(rule,indexID);
  }



  /**
   * Returns the default matching rule index provider for a given substring
   * matching rule.
   *
   * @param subRule
   *           The substring matching rule.
   * @param eqRule
   *           The equality matching rule.
   * @return The default substring matching rule index provider.
   */
  public static MatchingRuleIndexProvider
          getDefaultSubstringIndexProvider(SubstringMatchingRule subRule,
                                                EqualityMatchingRule eqRule)
  {
    return new DefaultSubstringIndexProvider(subRule,eqRule);
  }



  /**
   * Returns the default matching rule index provider for a given approximate
   * matching rule.
   *
   * @param rule
   *           The approximate matching rule.
   * @return The default approximate matching rule index provider.
   */
  public static MatchingRuleIndexProvider
          getDefaultApproximateRuleIndexProvider(ApproximateMatchingRule rule)
  {
    return new DefaultApproximateIndexProvider(rule);
  }



 /**
  * This class provides a default implementation for equality-based matching
  * rule index providers. This should work well for indexing most of the equality
  * matching rules.
  */
  public static class DefaultEqualityIndexProvider
          extends MatchingRuleIndexProvider
  {
    //Matching rule.
    protected EqualityMatchingRule matchingRule;



    //The index key factory.
    protected IndexKeyFactory factory;



    /**
     * Creates a new instance of this class.
     *
     * @param matchingRule The equality matching rule.
     */
    public DefaultEqualityIndexProvider(
            EqualityMatchingRule matchingRule,
            String indexID)
    {
      this.matchingRule = matchingRule;
      this.factory = new EqualityIndexKeyFactory(matchingRule,indexID);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IndexKeyFactory> getIndexKeyFactory(IndexConfig config)
    {
      //No config is required for this index key factory.
      return Collections.singleton(factory);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public MatchingRule getMatchingRule()
    {
      return matchingRule;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createIndexQuery(ByteSequence assertionValue,
            IndexQueryFactory<T> queryFactory) throws DirectoryException
    {
      return queryFactory.createExactMatchQuery(factory.getIndexID(),
              matchingRule.normalizeAssertionValue(assertionValue));
    }
  }



 /**
  * This class provides a default implementation for ordering-based matching
  * rule index providers. This should work well for indexing most of the
  * ordering matching rules.
  */
  public static class DefaultOrderingIndexProvider
          extends MatchingRuleIndexProvider
  {
    //Matching rule.
    protected OrderingMatchingRule matchingRule;



    //The index key factory.
    protected IndexKeyFactory factory;



    /**
     * Creates a new instance of this class.
     *
     * @param matchingRule The ordering matching rule.
     */
    public DefaultOrderingIndexProvider(
            OrderingMatchingRule matchingRule,
            String indexID)
    {
      this.matchingRule = matchingRule;
      this.factory = new OrderingIndexKeyFactory(matchingRule,indexID);
    }



    /**
     * Creates a new instance of this class.
     *
     * @param matchingRule The ordering matching rule.
     * @param factory The ordering index key factory.
     */
    public DefaultOrderingIndexProvider(
            OrderingMatchingRule matchingRule,
            OrderingIndexKeyFactory factory)
    {
      this.matchingRule = matchingRule;
      this.factory = factory;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IndexKeyFactory>
            getIndexKeyFactory(IndexConfig config)
    {
      //No config is required for this index key factory.
      return Collections.singleton(factory);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MatchingRule getMatchingRule()
    {
      return matchingRule;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createIndexQuery(ByteSequence assertionValue,
            IndexQueryFactory<T> queryFactory) throws DirectoryException
    {
      return queryFactory.createExactMatchQuery(factory.getIndexID(),
             matchingRule.normalizeAssertionValue(assertionValue));
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createGreaterThanOrEqualIndexQuery(ByteSequence assertionValue,
        IndexQueryFactory<T> queryFactory) throws DirectoryException
    {
      return queryFactory.createRangeMatchQuery(factory.getIndexID(),
            matchingRule.normalizeAssertionValue(assertionValue),
            ByteString.empty(),
            true, false);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createLessThanOrEqualIndexQuery(ByteSequence assertionValue,
        IndexQueryFactory<T> queryFactory) throws DirectoryException
    {
      // Read the range: lower < keys <= upper.
      return queryFactory.createRangeMatchQuery(factory.getIndexID(),
              ByteString.empty(),
              matchingRule.normalizeAssertionValue(assertionValue),
              false, true);
    }
  }


 /**
  * This class provides a default implementation for substring-based matching
  * rule index providers. This should work well for indexing most of the
  * substring matching rules.
  */
  public static class DefaultSubstringIndexProvider extends MatchingRuleIndexProvider
  {
    //The index key factory for creating index keys.
    private SubstringIndexKeyFactory subKeyFactory;


    //The shared/equality key factory for creating index key.s
    private IndexKeyFactory eqKeyFactory;


    //The substring matching rule.
    private SubstringMatchingRule subMatchingRule;



    //The corresponding equality matching rule.
    private EqualityMatchingRule eqMatchingRule;



    /**
     * Creates a new instance of this class.
     *
     * @param subMatchingRule The substring matching rule.
     * @param eqMatchingRule  The equality matching rule.
     */
    public DefaultSubstringIndexProvider(
            SubstringMatchingRule subMatchingRule,
            EqualityMatchingRule  eqMatchingRule)
    {
      super();
      this.subMatchingRule = subMatchingRule;
      this.eqMatchingRule = eqMatchingRule;
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
        subKeyFactory = new SubstringIndexKeyFactory(subMatchingRule,
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
                DirectoryServer.getIndexProvider(eqMatchingRule);
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
      return subMatchingRule;
    }



    /**
     * Uses an equality index to retrieve the entry IDs that might
     * contain a given initial substring.
     *
     * @param bytes
     *          A normalized initial substring of an attribute value.
     * @param factory
     *          The index query factory
     * @return The candidate entry IDs.
     */
    public <T> T matchInitialSubstring(byte[] bytes,
        IndexQueryFactory<T> factory)
    {
      byte[] lower = bytes;
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
      return factory.createRangeMatchQuery(eqKeyFactory.getIndexID(),
           ByteString.wrap(lower), ByteString
          .wrap(upper), true, false);
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
    private <T> T matchSubstring(byte[] bytes,
        IndexQueryFactory<T> factory)
    {
      T intersectionQuery = null;
      int substrLength = subKeyFactory.getSubstringLength();

      if (bytes.length < substrLength)
      {
        byte[] lower = subKeyFactory.makeSubstringKey(bytes, 0, bytes.length);
        byte[] upper = subKeyFactory.makeSubstringKey(bytes, 0, bytes.length);
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
            new TreeSet<byte[]>(subKeyFactory.getComparator());
        for (int first = 0, last = substrLength;
             last <= bytes.length;
             first++, last++)
        {
          byte[] keyBytes;
          keyBytes = subKeyFactory.makeSubstringKey(bytes, first, substrLength);
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
     *<P> The default implementation assumes the assertion values in the
     * following format: initialLength, initial, numberofany, anyLength1,
     * any1,anyLength2, any2, ..., anyLengthn, anyn, finalLength, final
     * </P>
     * {@inheritDoc}
     */
    @Override
    public <T> T createIndexQuery(ByteSequence assertion,
            IndexQueryFactory<T> queryFactory) throws DirectoryException
    {
      List<T> queries = new ArrayList<T>();
      List<ByteSequence> elements = new ArrayList<ByteSequence>();
      int assertPos = 0;

      // First byte is the length of subInitial.
      int subInitialLength = 0xFF & assertion.byteAt(0);
      if (subInitialLength > 0)
      {
        queries.add(matchInitialSubstring(assertion.subSequence(1,
                subInitialLength+1).toByteArray(),
                queryFactory));
      }
      assertPos = subInitialLength+1;
      //Find subAny portion.
      int anySize = 0xFF & assertion.byteAt(assertPos++);
      while (anySize-- > 0)
      {
        int anyLength = 0xFF & assertion.byteAt(assertPos++);
        elements.add(assertion.subSequence(assertPos, assertPos+anyLength));
        assertPos +=anyLength;
      }

      //Find subFinal.
      int finalLength = 0xFF & assertion.byteAt(assertPos++);
      if (finalLength > 0)
      {
        elements.add(assertion.subSequence(assertPos,assertPos+finalLength));
      }

      for (ByteSequence element : elements)
      {
        queries.add(matchSubstring(element.toByteArray(), queryFactory));
      }
      return queryFactory.createIntersectionQuery(queries);
    }
  }



  /**
  * This class provides a default implementation for approximate-based matching
  * rule index providers. This should work well for indexing most of the
  * approximate matching rules.
  */
  public static class DefaultApproximateIndexProvider
          extends MatchingRuleIndexProvider
  {
    //The approximate matching rule.
    private ApproximateMatchingRule matchingRule;



    //The index key factory.
    private IndexKeyFactory factory;



    /**
     * Creates a new instance of this class.
     *
     * @param matchingRule The approximate matching rule.
     */
    public DefaultApproximateIndexProvider(
            ApproximateMatchingRule matchingRule)
    {
      this.matchingRule = matchingRule;
      factory = new ApproximateIndexKeyFactory(matchingRule);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IndexKeyFactory> getIndexKeyFactory(IndexConfig config)
    {
      return Collections.singleton(factory);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public MatchingRule getMatchingRule()
    {
      return matchingRule;
    }




    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createIndexQuery(ByteSequence assertionValue,
            IndexQueryFactory<T> queryFactory) throws DirectoryException
    {
      return queryFactory.createExactMatchQuery(factory.getIndexID(),
              matchingRule.normalizeAssertionValue(assertionValue));
    }
  }
}
