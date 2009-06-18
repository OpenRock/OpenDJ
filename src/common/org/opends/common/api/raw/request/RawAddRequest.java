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

package org.opends.common.api.raw.request;


import org.opends.common.api.raw.RawMessage;
import org.opends.common.api.raw.response.RawSearchResultEntry;
import org.opends.common.api.DN;
import org.opends.common.api.Entry;
import org.opends.server.types.ByteString;
import org.opends.server.types.Attribute;
import org.opends.server.types.AttributeValue;
import org.opends.server.util.Validator;

import java.util.*;


/**
 * A raw add request.
 */
public final class RawAddRequest extends RawMessage implements RawRequest
{
  // The list of attributes associated with this request.
  private final Map<String, List<ByteString>> attributes =
      new HashMap<String, List<ByteString>>();

  // The DN of the entry to be added.
  private String dn;



  /**
   * Creates a new raw add request using the provided entry DN.
   * <p>
   * The new raw add request will contain an empty list of controls, and
   * an empty list of attributes.
   *
   * @param dn
   *          The raw, unprocessed entry DN for this add request.
   */
  public RawAddRequest(String dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn;
  }

  public RawAddRequest(DN dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();
  }

  public RawAddRequest(DN dn, Attribute... attributes)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();
    addAttribute(attributes);
  }

  public RawAddRequest(Entry entry)
  {
    Validator.ensureNotNull(entry);
    this.dn = entry.getDN().toString();
    for(Attribute attribute : entry.getAttributes())
    {
      addAttribute(attribute);
    }
  }

  public RawAddRequest(RawSearchResultEntry entry)
  {
    Validator.ensureNotNull(entry);
    this.dn = entry.getDn();
    for(Map.Entry<String, List<ByteString>> attribute : entry.getAttributes())
    {
      attributes.put(attribute.getKey(),
                     new ArrayList<ByteString>(attribute.getValue()));
    }
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
  public RawAddRequest addAttribute(String attributeDescription,
                                    ByteString attributeValue,
                                    ByteString... attributeValues)
  {
    Validator.ensureNotNull(attributeDescription, attributeValue);
    List<ByteString> values = attributes.get(attributeDescription);
    if(values == null)
    {
      values = new ArrayList<ByteString>(1);
    }
    values.add(attributeValue);

    if(attributeValues != null)
    {
      for(ByteString value : attributeValues)
      {
        Validator.ensureNotNull(attributeValue);
        values.add(value);
      }
    }

    return this;
  }

  /**
   * Adds the provided attribute to the set of raw attributes for this
   * add request.
   *
   * @param attributes The attributes to add.
   * @return This raw add request.
   */
  public RawAddRequest addAttribute(Attribute... attributes)
  {
    if(attributes == null)
    {
      return this;
    }

    for(Attribute attribute : attributes)
    {
      Validator.ensureNotNull(attribute);
      List<ByteString> values =
          this.attributes.get(attribute.getNameWithOptions());
      if(values == null)
      {
        if(attribute.size() > 0)
        {
          values = new ArrayList<ByteString>(attribute.size());
        }
        else
        {
          values = Collections.emptyList();
        }
      }

      for(AttributeValue attributeValue : attribute)
      {
        values.add(attributeValue.getValue());
      }
    }
    return this;
  }



  public Set<Map.Entry<String, List<ByteString>>> getAttributes()
  {
    return attributes.entrySet();
  }

  public Iterable<ByteString> getAttribute(String attributeDescription)
  {
    return attributes.get(attributeDescription);
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
  public RawAddRequest setDN(String dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn;
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
  public RawAddRequest setDN(DN dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();
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
