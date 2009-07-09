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

package org.opends.ldap.requests;



import java.util.ArrayList;
import java.util.List;

import org.opends.ldap.Message;
import org.opends.ldap.responses.SearchResultEntry;
import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;
import org.opends.types.Attribute;
import org.opends.types.DN;
import org.opends.types.Entry;



/**
 * A raw add request.
 */
public final class AddRequest extends Message implements Request
{
  // The list of attributes associated with this request.
  private final List<Attribute> attributes;

  // The DN of the entry to be added.
  private String dn;



  public AddRequest(DN dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();
    this.attributes = new ArrayList<Attribute>();
  }



  public AddRequest(DN dn,
      org.opends.server.types.Attribute... attributes)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();

    if (attributes != null)
    {
      this.attributes = new ArrayList<Attribute>(attributes.length);
      addAttribute(attributes);
    }
    else
    {
      this.attributes = new ArrayList<Attribute>();
    }
  }



  public AddRequest(Entry entry)
  {
    Validator.ensureNotNull(entry);
    this.dn = entry.getDN().toString();
    this.attributes = new ArrayList<Attribute>(entry.attributeCount());
    for (org.opends.server.types.Attribute attribute : entry
        .getAttributes())
    {
      addAttribute(attribute);
    }
  }



  public AddRequest(SearchResultEntry entry)
  {
    Validator.ensureNotNull(entry);
    this.dn = entry.getDn();
    this.attributes = new ArrayList<Attribute>(entry.attributeCount());
    for (Attribute attribute : entry.getAttributes())
    {
      attributes.add(attribute);
    }
  }



  /**
   * Creates a new raw add request using the provided entry DN.
   * <p>
   * The new raw add request will contain an empty list of controls, and
   * an empty list of attributes.
   * 
   * @param dn
   *          The raw, unprocessed entry DN for this add request.
   */
  public AddRequest(String dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn;
    this.attributes = new ArrayList<Attribute>();
  }



  /**
   * Adds the provided attribute to the set of raw attributes for this
   * add request.
   * 
   * @param attributes
   *          The attributes to add.
   * @return This raw add request.
   */
  public AddRequest addAttribute(Attribute... attributes)
  {
    if (attributes == null)
    {
      return this;
    }

    for (Attribute attribute : attributes)
    {
      Validator.ensureNotNull(attribute);
      this.attributes.add(attribute);
    }
    return this;
  }



  /**
   * Adds the provided attribute to the set of raw attributes for this
   * add request.
   * 
   * @param attributes
   *          The attributes to add.
   * @return This raw add request.
   */
  public AddRequest addAttribute(
      org.opends.server.types.Attribute... attributes)
  {
    if (attributes == null)
    {
      return this;
    }

    for (org.opends.server.types.Attribute attribute : attributes)
    {
      Validator.ensureNotNull(attribute);
      this.attributes.add(new Attribute(attribute));
    }
    return this;
  }



  /**
   * Adds the provided attribute to the set of raw attributes for this
   * add request.
   * 
   * @param attributeDescription
   * @param attributeValue
   *          The first attribute value.
   * @param attributeValues
   *          Any additional attribute values.
   * @return This raw add request.
   */
  public AddRequest addAttribute(String attributeDescription,
      ByteString attributeValue, ByteString... attributeValues)
  {
    Validator.ensureNotNull(attributeDescription, attributeValue);
    attributes.add(new Attribute(attributeDescription, attributeValue,
        attributeValues));
    return this;
  }



  public int attributeCount()
  {
    return attributes.size();
  }



  public Iterable<ByteString> getAttribute(String attributeDescription)
  {
    for (Attribute attribute : attributes)
    {
      if (attribute.getAttributeDescription().equals(
          attributeDescription))
      {
        return attribute.getAttributeValues();
      }
    }

    return null;
  }



  public Iterable<Attribute> getAttributes()
  {
    return attributes;
  }



  /**
   * Returns the raw, unprocessed entry DN as included in the request
   * from the client.
   * <p>
   * This may or may not contain a valid DN, as no validation will have
   * been performed.
   * 
   * @return The raw, unprocessed entry DN as included in the request
   *         from the client.
   */
  public String getDN()
  {
    return dn;
  }



  /**
   * Sets the raw, unprocessed entry DN for this add request.
   * <p>
   * This may or may not contain a valid DN.
   * 
   * @param dn
   *          The raw, unprocessed entry DN for this add request.
   * @return This raw add request.
   */
  public AddRequest setDN(DN dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();
    return this;
  }



  /**
   * Sets the raw, unprocessed entry DN for this add request.
   * <p>
   * This may or may not contain a valid DN.
   * 
   * @param dn
   *          The raw, unprocessed entry DN for this add request.
   * @return This raw add request.
   */
  public AddRequest setDN(String dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("AddRequest(entry=");
    buffer.append(dn);
    buffer.append(", attributes=");
    buffer.append(attributes);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
