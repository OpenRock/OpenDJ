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



import java.util.LinkedHashMap;
import java.util.Map;

import org.opends.server.types.ByteString;
import org.opends.types.DN;
import org.opends.types.RawAttribute;



/**
 * An add request.
 */
public final class AddRequest extends Request
{
  private String dn;
  private final Map<String, RawAttribute> attributes =
      new LinkedHashMap<String, RawAttribute>();



  /**
   * Creates a new add request using the provided DN.
   *
   * @param dn
   *          The DN of this add request.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  public AddRequest(DN dn)
  {
    if (dn == null)
    {
      throw new NullPointerException();
    }

    this.dn = dn.toString();
  }



  /**
   * Creates a new add request using the provided DN.
   *
   * @param dn
   *          The DN of this add request.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  public AddRequest(String dn)
  {
    if (dn == null)
    {
      throw new NullPointerException();
    }

    this.dn = dn;
  }



  public AddRequest addAttribute(RawAttribute attribute)
      throws NullPointerException
  {
    attributes.put(attribute.getAttributeDescription(), attribute);
    return this;
  }



  public AddRequest addAttribute(String attributeDescription,
      ByteString... attributeValues) throws NullPointerException
  {
    addAttribute(RawAttribute.newRawAttribute(attributeDescription,
        attributeValues));
    return this;
  }



  public AddRequest addAttribute(String attributeDescription,
      String... attributeValues) throws NullPointerException
  {
    addAttribute(RawAttribute.newRawAttribute(attributeDescription,
        attributeValues));
    return this;
  }



  public AddRequest clearAttributes()
  {
    attributes.clear();
    return this;
  }



  public RawAttribute getAttribute(String attributeDescription)
      throws NullPointerException
  {
    if (attributeDescription == null)
    {
      throw new NullPointerException();
    }

    return attributes.get(attributeDescription);
  }



  public int getAttributeCount()
  {
    return attributes.size();
  }



  public Iterable<RawAttribute> getAttributes()
  {
    return attributes.values();
  }



  public String getDN()
  {
    return dn;
  }



  public boolean hasAttribute(String attributeDescription)
      throws NullPointerException
  {
    if (attributeDescription == null)
    {
      throw new NullPointerException();
    }

    return attributes.containsKey(attributeDescription);
  }



  public boolean hasAttributes()
  {
    return !attributes.isEmpty();
  }



  public RawAttribute removeAttribute(String attributeDescription)
      throws NullPointerException
  {
    if (attributeDescription == null)
    {
      throw new NullPointerException();
    }

    return attributes.remove(attributeDescription);
  }



  public AddRequest setDN(DN dn) throws NullPointerException
  {
    if (dn == null)
    {
      throw new NullPointerException();
    }

    this.dn = dn.toString();
    return this;
  }



  public AddRequest setDN(String dn) throws NullPointerException
  {
    if (dn == null)
    {
      throw new NullPointerException();
    }

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
