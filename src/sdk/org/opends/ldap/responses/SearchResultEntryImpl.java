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



import static org.opends.util.StaticUtils.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.opends.server.types.ByteString;
import org.opends.spi.AbstractMessage;
import org.opends.types.AttributeValueSequence;
import org.opends.util.Validator;



/**
 * Search result entry implementation.
 */
final class SearchResultEntryImpl extends
    AbstractMessage<SearchResultEntry> implements SearchResultEntry
{

  private String dn;
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

    this.dn = dn;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(AttributeValueSequence attribute)
      throws IllegalArgumentException, NullPointerException
  {
    Validator.ensureNotNull(attribute);
    Validator.ensureTrue(!attribute.isEmpty(), "attribute is empty");

    addAttribute0(RawAttribute.copyOf(attribute));
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(String attributeDescription)
      throws UnsupportedOperationException, NullPointerException
  {
    Validator.ensureNotNull(attributeDescription);

    addAttribute0(RawAttribute.create(attributeDescription));
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(String attributeDescription,
      ByteString value) throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescription, value);

    addAttribute0(RawAttribute.create(attributeDescription, value));
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(String attributeDescription,
      ByteString firstValue, ByteString... remainingValues)
      throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescription, firstValue,
        remainingValues);

    addAttribute0(RawAttribute.create(attributeDescription, firstValue,
        remainingValues));
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(String attributeDescription,
      Collection<ByteString> values) throws IllegalArgumentException,
      NullPointerException
  {
    Validator.ensureNotNull(attributeDescription, values);
    Validator.ensureTrue(!values.isEmpty(), "attribute is empty");

    addAttribute0(RawAttribute.create(attributeDescription, values));
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(String attributeDescription,
      String value) throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescription, value);

    addAttribute0(RawAttribute.create(attributeDescription, value));
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry addAttribute(String attributeDescription,
      String firstValue, String... remainingValues)
      throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescription, firstValue,
        remainingValues);

    addAttribute0(RawAttribute.create(attributeDescription, firstValue,
        remainingValues));
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
  public String getDN()
  {
    return dn;
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
  public AttributeValueSequence removeAttribute(
      String attributeDescription) throws NullPointerException
  {
    final String key = toLowerCase(attributeDescription);
    return attributes.remove(key);
  }



  /**
   * {@inheritDoc}
   */
  public SearchResultEntry setDN(String dn) throws NullPointerException
  {
    Validator.ensureNotNull(dn);

    this.dn = dn;
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
      attributes.put(key, RawAttribute.copyOf(oldAttribute, attribute));
    }
  }



  /**
   * {@inheritDoc}
   */
  public StringBuilder toString(StringBuilder builder)
  {
    builder.append("SearchResultEntry(dn=");
    builder.append(dn);
    builder.append(", attributes=");
    builder.append(attributes);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder;
  }
}
