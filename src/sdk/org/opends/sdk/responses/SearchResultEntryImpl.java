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

package org.opends.sdk.responses;



import static org.opends.sdk.util.StaticUtils.toLowerCase;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.opends.sdk.AttributeValueSequence;
import org.opends.sdk.util.Validator;



/**
 * Search result entry implementation.
 */
final class SearchResultEntryImpl extends
    AbstractMessage<SearchResultEntry> implements SearchResultEntry
{

  private String name;
  private final Map<String, AttributeValueSequence> attributes =
      new LinkedHashMap<String, AttributeValueSequence>();



  /**
   * Creates a new search result entry using the provided DN.
   *
   * @param dn
   *          The DN of this search result entry.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  SearchResultEntryImpl(String dn) throws NullPointerException
  {
    Validator.ensureNotNull(dn);

    this.name = dn;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(AttributeValueSequence attribute)
      throws IllegalArgumentException, NullPointerException
  {
    Validator.ensureNotNull(attribute);

    addAttribute0(attribute);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(String attributeDescription)
      throws UnsupportedOperationException, NullPointerException
  {
    Validator.ensureNotNull(attributeDescription);

    addAttribute0(Attributes.create(attributeDescription));
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(String attributeDescription,
      Collection<?> values) throws IllegalArgumentException,
      NullPointerException
  {
    Validator.ensureNotNull(attributeDescription, values);

    addAttribute0(Attributes.create(attributeDescription, values));
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(String attributeDescription,
      Object value) throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescription, value);

    addAttribute0(Attributes.create(attributeDescription, value));
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(String attributeDescription,
      Object... values) throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescription, values);

    addAttribute0(Attributes.create(attributeDescription, values));
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry clearAttributes()

  {
    attributes.clear();
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public AttributeValueSequence getAttribute(String attributeDescription)
      throws NullPointerException
  {
    final String key = toLowerCase(attributeDescription);
    return attributes.get(key);
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
  public Iterable<AttributeValueSequence> getAttributes()
  {
    return attributes.values();
  }



  /**
   * {@inheritDoc}
   */
  public String getName()
  {
    return name;
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
  public SearchResultEntry removeAttribute(String attributeDescription)
      throws NullPointerException
  {
    final String key = toLowerCase(attributeDescription);
    attributes.remove(key);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry setName(String dn)
      throws NullPointerException
  {
    Validator.ensureNotNull(dn);

    this.name = dn;
    return this;
  }



  // Add the provided attribute, merging if required.
  private void addAttribute0(AttributeValueSequence attribute)
  {
    final String name = attribute.getAttributeDescriptionAsString();
    final String key = toLowerCase(name);
    final AttributeValueSequence oldAttribute =
        attributes.put(key, attribute);

    if (oldAttribute != null)
    {
      // Need to merge the values.
      attributes.put(key, Attributes.merge(oldAttribute, attribute));
    }
  }



  /**
   * {@inheritDoc}
   */
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("SearchResultEntry(name=");
    builder.append(name);
    builder.append(", attributes=");
    builder.append(attributes);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder.toString();
  }
}
