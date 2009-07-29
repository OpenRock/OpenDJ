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

package org.opends.types;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.opends.server.types.ByteString;



/**
 *
 */
public final class Types
{
  private static final class AttributeImpl implements Attribute
  {
    private final String attributeDescription;
    private final List<ByteString> attributeValues;



    private AttributeImpl(String attributeDescription,
        List<ByteString> attributeValues)
    {
      this.attributeDescription = attributeDescription;
      this.attributeValues = attributeValues;
    }



    /**
     * {@inheritDoc}
     */
    public String getAttributeDescription()
    {
      return attributeDescription;
    }



    /**
     * {@inheritDoc}
     */
    public boolean isEmpty()
    {
      return attributeValues.isEmpty();
    }



    /**
     * {@inheritDoc}
     */
    public Iterator<ByteString> iterator()
    {
      return attributeValues.iterator();
    }



    /**
     * {@inheritDoc}
     */
    public int size()
    {
      return attributeValues.size();
    }



    /**
     * {@inheritDoc}
     */
    public String toString()
    {
      StringBuilder builder = new StringBuilder();
      toString(builder);
      return builder.toString();
    }



    /**
     * {@inheritDoc}
     */
    public void toString(StringBuilder builder)
        throws NullPointerException
    {
      builder.append("Attribute(attributeDescription=");
      builder.append(attributeDescription);
      builder.append(", attributeValues=");
      builder.append(attributeValues);
      builder.append(")");
    }



    /**
     * {@inheritDoc}
     */
    public Attribute add(ByteString value)
        throws UnsupportedOperationException, NullPointerException
    {
      attributeValues.add(value);
      return this;
    }



    /**
     * {@inheritDoc}
     */
    public Attribute add(String value)
        throws UnsupportedOperationException, NullPointerException
    {
      attributeValues.add(ByteString.valueOf(value));
      return this;
    }



    /**
     * {@inheritDoc}
     */
    public Attribute clear() throws UnsupportedOperationException
    {
      attributeValues.clear();
      return this;
    }



    /**
     * {@inheritDoc}
     */
    public boolean remove(ByteString value)
        throws UnsupportedOperationException, NullPointerException
    {
      return attributeValues.remove(value);
    }

  }



  public static final Attribute newAttribute(
      String attributeDescription,
      Collection<ByteString> attributeValues)
      throws NullPointerException
  {
    List<ByteString> valueList =
        new ArrayList<ByteString>(attributeValues);
    return new AttributeImpl(attributeDescription, valueList);
  }



  public static final Attribute newAttribute(
      String attributeDescription, ByteString... attributeValues)
      throws NullPointerException
  {
    if (attributeDescription == null)
    {
      throw new NullPointerException();
    }

    List<ByteString> valueList;
    if (attributeValues == null)
    {
      valueList = new ArrayList<ByteString>(0);
    }
    else
    {
      valueList = new ArrayList<ByteString>(attributeValues.length);
    }

    for (ByteString value : attributeValues)
    {
      if (value == null)
      {
        throw new NullPointerException();
      }
      valueList.add(value);
    }

    return new AttributeImpl(attributeDescription, valueList);
  }



  public static final Attribute newAttribute(
      String attributeDescription, String... attributeValues)
      throws NullPointerException
  {
    if (attributeDescription == null)
    {
      throw new NullPointerException();
    }

    List<ByteString> valueList;
    if (attributeValues == null)
    {
      valueList = new ArrayList<ByteString>(0);
    }
    else
    {
      valueList = new ArrayList<ByteString>(attributeValues.length);
    }

    for (String value : attributeValues)
    {
      if (value == null)
      {
        throw new NullPointerException();
      }
      valueList.add(ByteString.valueOf(value));
    }

    return new AttributeImpl(attributeDescription, valueList);
  }

}
