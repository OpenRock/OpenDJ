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

import java.util.Comparator;
import java.util.List;
import org.opends.server.api.EqualityMatchingRule;
import org.opends.server.types.AttributeValue;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.Attribute;

/**
 * This class provides an implementation for generating index keys
 * for an equality matching rule. This default implementation should
 * work for most of the equality matching rules.
 */
public class EqualityIndexKeyFactory extends IndexKeyFactory
{
  //The equality matching rule to be used for indexing.
  private EqualityMatchingRule matchingRule;


  //The byte-by-byte comparator for comparing the keys.
  private Comparator<byte[]> comparator;
  
  
  //The Index ID of the equality index key factory.
  private String indexID;



  /**
  * Create a new equality index key factory for the given equality
  * matching rule. An indexID can be used to share this index with
  * an ordering index which uses the same indexID. 
   * 
  * @param matchingRule The equality matching rule which will be used
  *                                    to create keys.
   * @param indexID The indexID that will be used for this key factory.
  */
  public EqualityIndexKeyFactory(EqualityMatchingRule matchingRule,
          String indexID)
  {
    this.matchingRule = matchingRule;
    this.indexID = indexID;
    this.comparator = new DefaultByteKeyComparator();
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
}
