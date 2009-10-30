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

package org.opends.sdk.requests;



import static org.opends.sdk.util.StaticUtils.toLowerCase;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.opends.sdk.AttributeValueSequence;
import org.opends.sdk.ldif.ChangeRecordVisitor;
import org.opends.sdk.util.Validator;



/**
 * Add request implementation.
 */
final class AddRequestImpl extends AbstractMessage<AddRequest>
    implements AddRequest
{
  private final Map<String, AttributeValueSequence> attributes =
      new LinkedHashMap<String, AttributeValueSequence>();
  private String name;



  /**
   * Creates a new add request using the provided distinguished name.
   *
   * @param name
   *          The distinguished name of this add request.
   * @throws NullPointerException
   *           If {@code name} was {@code null}.
   */
  AddRequestImpl(String name) throws NullPointerException
  {
    Validator.ensureNotNull(name);

    this.name = name;
  }



  /**
   * {@inheritDoc}
   */
  public <R, P> R accept(ChangeRecordVisitor<R, P> v, P p)
  {
    return v.visitChangeRecord(p, this);
  }



  /**
   * {@inheritDoc}
   */
  public AddRequest addAttribute(AttributeValueSequence attribute)
      throws IllegalArgumentException, NullPointerException
  {
    Validator.ensureNotNull(attribute);
    Validator.ensureTrue(!attribute.isEmpty(), "attribute is empty");

    addAttribute0(attribute);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public AddRequest addAttribute(String attributeDescription,
      Collection<?> values) throws IllegalArgumentException,
      NullPointerException
  {
    Validator.ensureNotNull(attributeDescription, values);
    Validator.ensureTrue(!values.isEmpty(), "attribute is empty");

    addAttribute0(Attributes.create(attributeDescription, values));
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public AddRequest addAttribute(String attributeDescription,
      Object value) throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescription, value);

    addAttribute0(Attributes.create(attributeDescription, value));
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public AddRequest addAttribute(String attributeDescription,
      Object firstValue, Object... remainingValues)
      throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescription, firstValue,
        remainingValues);

    addAttribute0(Attributes.create(attributeDescription, firstValue,
        remainingValues));
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public AddRequest clearAttributes()

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
  public AddRequest removeAttribute(String attributeDescription)
      throws NullPointerException
  {
    final String key = toLowerCase(attributeDescription);
    attributes.remove(key);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public AddRequest setName(String dn) throws NullPointerException
  {
    Validator.ensureNotNull(dn);

    this.name = dn;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("AddRequest(name=");
    builder.append(name);
    builder.append(", attributes=");
    builder.append(attributes);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder.toString();
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
}
