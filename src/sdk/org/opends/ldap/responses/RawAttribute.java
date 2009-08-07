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



import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.opends.server.types.ByteString;
import org.opends.types.AttributeValueSequence;



/**
 * An immutable attribute implementation. The methods in this class do
 * not perform any argument validation.
 * <p>
 * TODO: remove when requests and responses are merged into same
 * package.
 */
final class RawAttribute implements AttributeValueSequence
{
  // Iterator implementation.
  private final class IteratorImpl implements Iterator<ByteString>
  {
    private int index = 0;



    /**
     * {@inheritDoc}
     */
    public boolean hasNext()
    {
      if (singleValue != null)
      {
        if (index == 0)
        {
          return true;
        }
      }
      else if (multipleValues != null)
      {
        if (index < multipleValues.length)
        {
          return true;
        }
      }
      return false;
    }



    /**
     * {@inheritDoc}
     */
    public ByteString next() throws NoSuchElementException
    {
      if (singleValue != null)
      {
        if (index == 0)
        {
          index++;
          return singleValue;
        }
      }
      else if (multipleValues != null)
      {
        if (index < multipleValues.length)
        {
          final int oldIndex = index;
          index++;
          return multipleValues[oldIndex];
        }
      }

      throw new NoSuchElementException();
    }



    /**
     * {@inheritDoc}
     */
    public void remove() throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException();
    }

  }



  /**
   * Creates a new attribute using the provided attribute value
   * sequence.
   *
   * @param a
   *          The attribute value sequence to be copied.
   * @return The new attribute.
   */
  static RawAttribute copyOf(AttributeValueSequence a)
  {
    if (a instanceof RawAttribute)
    {
      return (RawAttribute) a;
    }

    final String name = a.getAttributeDescriptionString();
    final int sz = a.size();
    if (sz == 0)
    {
      return new RawAttribute(name, null, null);
    }
    else if (sz == 1)
    {
      return new RawAttribute(name, a.iterator().next(), null);
    }
    else
    {
      final ByteString[] values = new ByteString[sz];
      int i = 0;
      for (final ByteString value : a)
      {
        values[i++] = value;
      }
      return new RawAttribute(name, null, values);
    }
  }



  /**
   * Creates a new attribute using the provided attribute value
   * sequences.
   *
   * @param a1
   *          The first attribute value sequence to be copied.
   * @param a2
   *          The second attribute value sequence to be copied.
   * @return The new attribute.
   */
  static RawAttribute copyOf(AttributeValueSequence a1,
      AttributeValueSequence a2)
  {
    final int sz1 = a1.size();
    final int sz2 = a2.size();

    // If one attribute is empty then no need to merge.
    if (sz1 == 0)
    {
      return copyOf(a2);
    }
    else if (sz2 == 0)
    {
      return copyOf(a1);
    }
    else
    {
      // Both contain values.
      final String name = a1.getAttributeDescriptionString();
      final int sz = sz1 + sz2;
      final ByteString[] values = new ByteString[sz];

      int i = 0;

      for (final ByteString value : a1)
      {
        values[i++] = value;
      }

      for (final ByteString value : a2)
      {
        values[i++] = value;
      }

      return new RawAttribute(name, null, values);
    }
  }



  /**
   * Creates a new empty attribute.
   *
   * @param attributeDescription
   *          The attribute name.
   * @return The new empty attribute.
   */
  static RawAttribute create(String attributeDescription)
  {
    return new RawAttribute(attributeDescription, null, null);
  }



  /**
   * Creates a new single-valued attribute.
   *
   * @param attributeDescription
   *          The attribute name.
   * @param value
   *          The attribute value.
   * @return The new single-valued attribute.
   */
  static RawAttribute create(String attributeDescription,
      ByteString value)
  {
    return new RawAttribute(attributeDescription, value, null);
  }



  /**
   * Creates a new multi-valued attribute using the provided values.
   *
   * @param attributeDescription
   *          The attribute name.
   * @param firstValue
   *          The first attribute value.
   * @param remainingValues
   *          The remaining attribute values.
   * @return The new multi-valued attribute.
   */
  static RawAttribute create(String attributeDescription,
      ByteString firstValue, ByteString... remainingValues)
  {
    final int sz = remainingValues.length;
    if (sz == 0)
    {
      return new RawAttribute(attributeDescription, firstValue, null);
    }
    else
    {
      final ByteString[] values = new ByteString[sz + 1];
      values[0] = firstValue;
      int i = 0;
      while (i < sz)
      {
        final ByteString value = remainingValues[i++];
        values[i] = value;
      }
      return new RawAttribute(attributeDescription, null, values);
    }
  }



  /**
   * Creates a new attribute using the provided collection.
   *
   * @param attributeDescription
   *          The attribute name.
   * @param values
   *          The attribute values.
   * @return The new attribute.
   */
  static RawAttribute create(String attributeDescription,
      Collection<ByteString> values)
  {
    final int sz = values.size();
    if (sz == 0)
    {
      return new RawAttribute(attributeDescription, null, null);
    }
    else if (sz == 1)
    {
      return new RawAttribute(attributeDescription, values.iterator()
          .next(), null);
    }
    else
    {
      return new RawAttribute(attributeDescription, null, values
          .toArray(new ByteString[sz]));
    }
  }



  /**
   * Creates a new single-valued attribute.
   *
   * @param attributeDescription
   *          The attribute name.
   * @param value
   *          The attribute value.
   * @return The new single-valued attribute.
   */
  static RawAttribute create(String attributeDescription, String value)
  {
    return new RawAttribute(attributeDescription, ByteString
        .valueOf(value), null);
  }



  /**
   * Creates a new multi-valued attribute using the provided values.
   *
   * @param attributeDescription
   *          The attribute name.
   * @param firstValue
   *          The first attribute value.
   * @param remainingValues
   *          The remaining attribute values.
   * @return The new multi-valued attribute.
   */
  static RawAttribute create(String attributeDescription,
      String firstValue, String... remainingValues)
  {
    final int sz = remainingValues.length;
    if (sz == 0)
    {
      final ByteString value = ByteString.valueOf(firstValue);
      return new RawAttribute(attributeDescription, value, null);
    }
    else
    {
      final ByteString[] values = new ByteString[sz + 1];
      values[0] = ByteString.valueOf(firstValue);
      int i = 0;
      while (i < sz)
      {
        final ByteString value =
            ByteString.valueOf(remainingValues[i++]);
        values[i] = value;
      }
      return new RawAttribute(attributeDescription, null, values);
    }
  }



  /**
   * Wraps the provided array of values as an attribute.
   *
   * @param attributeDescription
   *          The attribute name.
   * @param values
   *          The attribute values.
   * @return The new attribute.
   */
  static RawAttribute wrap(String attributeDescription,
      ByteString[] values)
  {
    final int sz = values.length;
    if (sz == 0)
    {
      // No need to wrap.
      return new RawAttribute(attributeDescription, null, null);
    }
    else if (sz == 1)
    {
      // No need to wrap.
      return new RawAttribute(attributeDescription, values[0], null);
    }
    else
    {
      return new RawAttribute(attributeDescription, null, values);
    }
  }



  // The user-provided attribute description, including options.
  private final String attributeDescription;

  // Null when this attribute is empty or single-valued.
  private final ByteString[] multipleValues;

  // Null when this attribute is empty or multi-valued.
  private final ByteString singleValue;



  // Private constructor.
  private RawAttribute(String attributeDescription,
      ByteString singleValue, ByteString[] multipleValues)
  {
    this.attributeDescription = attributeDescription;
    this.singleValue = singleValue;
    this.multipleValues = multipleValues;
  }



  /**
   * {@inheritDoc}
   */
  public String getAttributeDescriptionString()
  {
    return attributeDescription;
  }



  /**
   * {@inheritDoc}
   */
  public boolean isEmpty()
  {
    return (size() == 0);
  }



  /**
   * {@inheritDoc}
   */
  public Iterator<ByteString> iterator()
  {
    return new IteratorImpl();
  }



  /**
   * {@inheritDoc}
   */
  public int size()
  {
    if (singleValue != null)
    {
      return 1;
    }
    else if (multipleValues != null)
    {
      return multipleValues.length;
    }
    else
    {
      return 0;
    }
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    return toString(builder).toString();
  }



  /**
   * {@inheritDoc}
   */
  public StringBuilder toString(StringBuilder builder)
  {
    builder.append("Attribute(attributeDescription=");
    builder.append(attributeDescription);
    builder.append(", attributeValues=[");
    if (singleValue != null)
    {
      builder.append(singleValue);
    }
    else if (multipleValues != null)
    {
      builder.append(multipleValues[0]);
      for (int i = 1; i < multipleValues.length; i++)
      {
        builder.append(", ");
        builder.append(multipleValues[i]);
      }
    }
    builder.append("])");
    return builder;
  }
}
