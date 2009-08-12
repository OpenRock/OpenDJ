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
 * Factory methods for creating mutable attribute value sequences.
 */
final class Attributes
{
  private Attributes()
  {
  }



  private static abstract class AbstractAttributeValueSequence
      implements AttributeValueSequence
  {
    private final String attributeDescription;



    private AbstractAttributeValueSequence(String attributeDescription)
    {
      this.attributeDescription = attributeDescription;
    }



    /**
     * {@inheritDoc}
     */
    public final String getAttributeDescriptionAsString()
    {
      return attributeDescription;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString()
    {
      final StringBuilder builder = new StringBuilder();
      return toString(builder).toString();
    }



    /**
     * {@inheritDoc}
     */
    public final StringBuilder toString(StringBuilder builder)
    {
      builder.append("Attribute(attributeDescription=");
      builder.append(attributeDescription);
      builder.append(", attributeValues=[");
      Iterator<ByteString> iterator = iterator();
      if (iterator.hasNext())
      {
        builder.append(iterator.next());
        while (iterator.hasNext())
        {
          builder.append(", ");
          builder.append(iterator.next());
        }
      }
      builder.append("])");
      return builder;
    }
  }

  private static final class EmptyAttributeValueSequence extends
      AbstractAttributeValueSequence
  {
    private static final Iterator<ByteString> ITERATOR =
        new Iterator<ByteString>()
        {

          public boolean hasNext()
          {
            return false;
          }



          public ByteString next()
          {
            throw new NoSuchElementException();
          }



          public void remove()
          {
            throw new IllegalStateException();
          }

        };



    private EmptyAttributeValueSequence(String attributeDescription)
    {
      super(attributeDescription);
    }



    /**
     * {@inheritDoc}
     */
    public boolean isEmpty()
    {
      return true;
    }



    /**
     * {@inheritDoc}
     */
    public Iterator<ByteString> iterator()
    {
      return ITERATOR;
    }



    /**
     * {@inheritDoc}
     */
    public int size()
    {
      return 0;
    }

  }

  private static final class SingleAttributeValueSequence extends
      AbstractAttributeValueSequence
  {

    private ByteString value;



    private SingleAttributeValueSequence(String attributeDescription,
        ByteString value)
    {
      super(attributeDescription);
      this.value = value;
    }



    /**
     * {@inheritDoc}
     */
    public boolean isEmpty()
    {
      return value == null;
    }



    /**
     * {@inheritDoc}
     */
    public Iterator<ByteString> iterator()
    {
      return new Iterator<ByteString>()
      {
        private boolean hasNext = (value != null);



        public boolean hasNext()
        {
          return hasNext;
        }



        public ByteString next()
        {
          if (!hasNext)
          {
            throw new NoSuchElementException();
          }

          hasNext = false;
          return value;
        }



        public void remove()
        {
          if (hasNext)
          {
            // Not yet iterated or empty.
            throw new IllegalStateException();
          }

          if (value == null)
          {
            // Value already removed.
            throw new IllegalStateException();
          }

          value = null;
        }
      };
    }



    /**
     * {@inheritDoc}
     */
    public int size()
    {
      return value == null ? 0 : 1;
    }

  }

  private static final class MultiAttributeValueSequence extends
      AbstractAttributeValueSequence
  {

    private ByteString[] values;
    private int size;



    private MultiAttributeValueSequence(String attributeDescription,
        ByteString[] values)
    {
      super(attributeDescription);
      this.values = values;
      this.size = values.length;
    }



    /**
     * {@inheritDoc}
     */
    public boolean isEmpty()
    {
      return size == 0;
    }



    /**
     * {@inheritDoc}
     */
    public Iterator<ByteString> iterator()
    {

      return new Iterator<ByteString>()
      {
        private int findNext(int start)
        {
          int end = values.length;

          if (start >= end)
          {
            return end;
          }

          for (int i = start; i < end; i++)
          {
            if (values[i] != null)
            {
              return i;
            }
          }

          // No more values.
          return end;
        }



        private int nextIndex = findNext(0);
        private int lastIndex = -1;



        public boolean hasNext()
        {
          return nextIndex < values.length;
        }



        public ByteString next()
        {
          if (!hasNext())
          {
            throw new NoSuchElementException();
          }

          ByteString value = values[nextIndex];
          nextIndex = findNext(nextIndex + 1);
          return value;
        }



        public void remove()
        {
          if (lastIndex < 0)
          {
            // Not yet iterated or value already removed.
            throw new IllegalStateException();
          }

          values[lastIndex] = null;
          lastIndex = -1;
        }
      };
    }



    /**
     * {@inheritDoc}
     */
    public int size()
    {
      return size;
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
  static AttributeValueSequence merge(AttributeValueSequence a1,
      AttributeValueSequence a2)
  {
    final int sz1 = a1.size();
    final int sz2 = a2.size();

    // If one attribute is empty then no need to merge.
    if (sz1 == 0)
    {
      return a2;
    }
    else if (sz2 == 0)
    {
      return a1;
    }
    else
    {
      // Both contain values.
      final String name = a1.getAttributeDescriptionAsString();
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

      return new MultiAttributeValueSequence(name, values);
    }
  }



  /**
   * Creates a new empty attribute.
   *
   * @param attributeDescription
   *          The attribute name.
   * @return The new empty attribute.
   */
  static AttributeValueSequence create(String attributeDescription)
  {
    return new EmptyAttributeValueSequence(attributeDescription);
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
  static AttributeValueSequence create(String attributeDescription,
      ByteString value)
  {
    return new SingleAttributeValueSequence(attributeDescription, value);
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
  static AttributeValueSequence create(String attributeDescription,
      ByteString firstValue, ByteString... remainingValues)
  {
    final int sz = remainingValues.length;
    if (sz == 0)
    {
      return new SingleAttributeValueSequence(attributeDescription,
          firstValue);
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
      return new MultiAttributeValueSequence(attributeDescription,
          values);
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
  static AttributeValueSequence create(String attributeDescription,
      Collection<ByteString> values)
  {
    final int sz = values.size();
    if (sz == 0)
    {
      return new EmptyAttributeValueSequence(attributeDescription);
    }
    else if (sz == 1)
    {
      return new SingleAttributeValueSequence(attributeDescription,
          values.iterator().next());
    }
    else
    {
      return new MultiAttributeValueSequence(attributeDescription,
          values.toArray(new ByteString[sz]));
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
  static AttributeValueSequence create(String attributeDescription,
      String value)
  {
    return new SingleAttributeValueSequence(attributeDescription,
        ByteString.valueOf(value));
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
  static AttributeValueSequence create(String attributeDescription,
      String firstValue, String... remainingValues)
  {
    final int sz = remainingValues.length;
    if (sz == 0)
    {
      final ByteString value = ByteString.valueOf(firstValue);
      return new SingleAttributeValueSequence(attributeDescription,
          value);
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
      return new MultiAttributeValueSequence(attributeDescription,
          values);
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
  static AttributeValueSequence wrap(String attributeDescription,
      ByteString[] values)
  {
    final int sz = values.length;
    if (sz == 0)
    {
      // No need to wrap.
      return new EmptyAttributeValueSequence(attributeDescription);
    }
    else if (sz == 1)
    {
      // No need to wrap.
      return new SingleAttributeValueSequence(attributeDescription,
          values[0]);
    }
    else
    {
      return new MultiAttributeValueSequence(attributeDescription,
          values);
    }
  }
}
