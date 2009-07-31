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

package org.opends.ldap.responses;



import java.util.LinkedHashMap;
import java.util.Map;

import org.opends.server.types.ByteString;
import org.opends.types.DN;
import org.opends.types.RawAttribute;



/**
 * LDAP search result entry response message implementation.
 */
final class SearchResultEntryImpl extends
    ResponseImpl<SearchResultEntry> implements SearchResultEntry
{

  private String dn;
  private final Map<String, RawAttribute> attributes =
      new LinkedHashMap<String, RawAttribute>();



  /**
   * Creates a new search result entry using the provided DN.
   *
   * @param dn
   *          The DN of this search result entry.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  SearchResultEntryImpl(String dn)
  {
    if (dn == null)
    {
      throw new NullPointerException();
    }

    this.dn = dn;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(RawAttribute attribute)
      throws NullPointerException
  {
    attributes.put(attribute.getAttributeDescription(), attribute);
    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(String attributeDescription,
      ByteString... attributeValues) throws NullPointerException
  {
    addAttribute(RawAttribute.newRawAttribute(attributeDescription,
        attributeValues));
    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(String attributeDescription,
      String... attributeValues) throws NullPointerException
  {
    addAttribute(RawAttribute.newRawAttribute(attributeDescription,
        attributeValues));
    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry clearAttributes()
  {
    attributes.clear();
    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public RawAttribute getAttribute(String attributeDescription)
      throws NullPointerException
  {
    if (attributeDescription == null)
    {
      throw new NullPointerException();
    }

    return attributes.get(attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public int getAttributeCount()
  {
    return attributes.size();
  }



  /**
   * {@inheritDoc}
   */
  public Iterable<RawAttribute> getAttributes()
  {
    return attributes.values();
  }



  /**
   * {@inheritDoc}
   */
  public String getDN()
  {
    return dn;
  }



  /**
   * {@inheritDoc}
   */
  public boolean hasAttribute(String attributeDescription)
      throws NullPointerException
  {
    if (attributeDescription == null)
    {
      throw new NullPointerException();
    }

    return attributes.containsKey(attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public boolean hasAttributes()
  {
    return !attributes.isEmpty();
  }



  /**
   * {@inheritDoc}
   */
  public RawAttribute removeAttribute(String attributeDescription)
      throws NullPointerException
  {
    if (attributeDescription == null)
    {
      throw new NullPointerException();
    }

    return attributes.remove(attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry setDN(DN dn) throws NullPointerException
  {
    if (dn == null)
    {
      throw new NullPointerException();
    }

    this.dn = dn.toString();
    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry setDN(String dn) throws NullPointerException
  {
    if (dn == null)
    {
      throw new NullPointerException();
    }

    this.dn = dn;
    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public void toString(StringBuilder buffer)
  {
    buffer.append("SearchResultEntry(dn=");
    buffer.append(dn);
    buffer.append(", attributes=");
    buffer.append(attributes);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
