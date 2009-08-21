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
 *      Copyright 2008 Sun Microsystems, Inc.
 */


package org.opends.server.backends.index;

import java.io.Serializable;
import java.util.*;
import org.opends.server.api.OrderingMatchingRule;
import org.opends.server.types.AttributeValue;
import org.opends.server.types.ByteString;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.Attribute;
import org.opends.server.core.DirectoryServer;

/**
 * This class provides an implementation for generating index keys
 * for an ordering matching rule. This default implementation should
 * work for most of the ordering matching rules.
 */
public class OrderingIndexKeyFactory extends IndexKeyFactory
{
  //The ordering matching rule to be used for indexing.
  private OrderingMatchingRule matchingRule;


  //The byte-by-byte comparator for comparing the keys.
  private Comparator<byte[]> comparator;
  
  
  //The index id for this key factory.
  private String indexID;



  /**
  * Create a new ordering index key factory for the given equality
  * matching rule.
  *
  * @param matchingRule The ordering matching rule which will be used
  *                                    to create keys.
   * @param indexID The index id to be used for this key factory.
  */
  public OrderingIndexKeyFactory(OrderingMatchingRule matchingRule,
          String indexID)
  {
    this.matchingRule = matchingRule;
    this.indexID = indexID;
    comparator = new OrderingKeyComparator(
            matchingRule.getOID());
  }



  /**
  * {@inheritDoc}
  */
  @Override
  public String getIndexID()
  {
    return indexID;
  }



  /**
   * {@inheritDoc}
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
          keySet.addKey(matchingRule.normalizeValue(value.getValue()));          
        }
        catch (DirectoryException de)
        {
        }
      }
    }
  }




  /**
   * {@inheritDoc}
   */
  @Override
  public Comparator<byte[]> getComparator()
  {
    return comparator;
  }



  /**
   * The Ordering matching rule requires a serializable key comparator.
  */
  public static class OrderingKeyComparator
          implements Comparator<byte[]>, Serializable
  {
    /**
     * The serial version identifier required to satisfy the compiler because
     * this class implements the <CODE>java.io.Serializable</CODE> interface.
     * This value was generated using the <CODE>serialver</CODE> command-line
     * utility included with the Java SDK.
     */
    private static final long serialVersionUID = 6259219052120666010L;

    //The oid of the ordering matching rule.
    private String oid;


    //Creates an instance of the ordering key comparator using 
    //the oid of the Ordering matching rule.
    public OrderingKeyComparator(String oid)
    {
      this.oid = oid;
    }


    public OrderingKeyComparator()
    {
      //no implementation required. Used by the backend.
    }


    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.
     *
     * @param a the first object to be compared.
     * @param b the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second.
     */
    public int compare(byte[] a, byte[] b)
    {
      //Get the MatchingRule.
      OrderingMatchingRule rule = DirectoryServer.getOrderingMatchingRule(oid);
      return rule.compareValues(ByteString.wrap(a), ByteString.wrap(b));
    }
  }

}
