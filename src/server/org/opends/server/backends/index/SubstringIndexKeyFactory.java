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
import org.opends.server.api.SubstringMatchingRule;
import org.opends.server.types.AttributeValue;
import org.opends.server.types.ByteString;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.Attribute;
import static org.opends.server.util.ServerConstants.*;

/**
 * This class provides an implementation for generating index keys
 * for a substring matching rule. This default implementation should
 * work for most of the substring matching rules.
 */
public class SubstringIndexKeyFactory extends IndexKeyFactory
{
  //The substring matching rule to be used for indexing.
  private SubstringMatchingRule matchingRule;


  //The byte-by-byte comparator for comparing the keys.
  private Comparator<byte[]> comparator;


  //The substring length.
  private int substrLength;



  /**
  * Create a new substring index key factory for the given substring
  * matching rule.
  *
  * @param matchingRule The substring matching rule which will be used
  *                                    to create keys.
   * @param substringLength The decomposed substring length.
  */
  public SubstringIndexKeyFactory(SubstringMatchingRule matchingRule, int substringLength)
  {
    this.matchingRule = matchingRule;
    this.substrLength = substringLength;
    this.comparator = new DefaultByteKeyComparator();
  }



  /**
  * {@inheritDoc}
  */
  @Override
  public String getIndexID()
  {
    return SUBSTRING_INDEX_ID;
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
          ByteString key = matchingRule.normalizeValue(value.getValue());
          substringKeys(key.toByteArray(),keySet);
        }
        catch (DirectoryException de)
        {
        }
      }
    }
  }



  /**
   * Decompose an attribute value into a set of substring index keys.
   * The ID of the entry containing this value should be inserted
   * into the list of each of these keys.
   *
   * @param value A byte array containing the normalized attribute value
   * @param set A set into which the keys will be inserted.
   */
  public void substringKeys(byte[] value, KeySet keySet)
  {
    byte[] keyBytes;

    // Example: The value is ABCDE and the substring length is 3.
    // We produce the keys ABC BCD CDE DE E
    // To find values containing a short substring such as DE,
    // iterate through keys with prefix DE. To find values
    // containing a longer substring such as BCDE, read keys
    // BCD and CDE.
    for (int i = 0, remain = value.length; remain > 0; i++, remain--)
    {
      int len = Math.min(substrLength, remain);
      keyBytes = makeSubstringKey(value, i, len);
      keySet.addKey(keyBytes);
    }
  }



  /**
   * Makes a byte array representing a substring index key for
   * one substring of a value.
   *
   * @param bytes The byte array containing the value
   * @param pos The starting position of the substring
   * @param len The length of the substring
   * @return A byte array containing a substring key
   */
  public byte[] makeSubstringKey(byte[] bytes, int pos, int len)
  {
    byte[] keyBytes = new byte[len];
    System.arraycopy(bytes, pos, keyBytes, 0, len);
    return keyBytes;
  }



  public int getSubstringLength()
  {
    return substrLength;
  }



  /**
   * Sets the substring length.
   *
   * @param substringLen
   *          The substring length.
   */
  public void setSubstringLength(int substrLength)
  {
    this.substrLength = substrLength;
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
