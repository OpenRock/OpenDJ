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



import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.opends.sdk.AttributeValueSequence;
import org.opends.sdk.util.ByteString;



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
      return builder.toString();
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



  private static final class UnmodifiableAttributeValueSequence
      implements AttributeValueSequence
  {
    private final AttributeValueSequence attribute;



    private UnmodifiableAttributeValueSequence(
        AttributeValueSequence attribute)
    {
      this.attribute = attribute;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString()
    {
      return attribute.toString();
    }



    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj)
    {
      return (obj == this || attribute.equals(obj));
    }



    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
      return attribute.hashCode();
    }



    /**
     * {@inheritDoc}
     */
    public String getAttributeDescriptionAsString()
    {
      return attribute.getAttributeDescriptionAsString();
    }



    /**
     * {@inheritDoc}
     */
    public boolean isEmpty()
    {
      return attribute.isEmpty();
    }



    /**
     * {@inheritDoc}
     */
    public Iterator<ByteString> iterator()
    {
      return new Iterator<ByteString>()
      {

        public boolean hasNext()
        {
          return iterator.hasNext();
        }



        public ByteString next()
        {
          return iterator.next();
        }



        public void remove()
        {
          // Prevent modifications.
          throw new UnsupportedOperationException();
        }

        private final Iterator<ByteString> iterator =
            attribute.iterator();

      };
    }



    /**
     * {@inheritDoc}
     */
    public int size()
    {
      return attribute.size();
    }

  }



  /**
   * Returns an unmodifiable view of the provided attribute value
   * sequence. Attempts to modify the return attribute value sequence
   * (i.e. through an {@code Iterator} will result in an {@code
   * UnsupportedOperationException} being thrown. Queries against the
   * returned attribute value sequence read-through to the underlying
   * attribute value sequence.
   *
   * @param attribute
   *          The attribute value sequence for which an unmodifiable
   *          view is to be returned.
   * @return An unmodifiable view of the attribute value sequence.
   */
  static AttributeValueSequence unmodifiable(
      AttributeValueSequence attribute)
  {
    return new UnmodifiableAttributeValueSequence(attribute);
  }



  /**
   * Copies the provided attribute value sequence. Modifications made to
   * the returned sequence will not be reflected in the provided
   * sequence.
   *
   * @param attribute
   *          The attribute value sequence to be copied.
   * @return A copy of the attribute value sequence.
   */
  static AttributeValueSequence copyOf(AttributeValueSequence attribute)
  {
    String attributeDescription =
        attribute.getAttributeDescriptionAsString();
    final int sz = attribute.size();
    if (sz == 0)
    {
      return new EmptyAttributeValueSequence(attributeDescription);
    }
    else if (sz == 1)
    {
      return new SingleAttributeValueSequence(attributeDescription,
          attribute.iterator().next());
    }
    else
    {
      ByteString[] values = new ByteString[sz];
      int i = 0;
      for (ByteString value : attribute)
      {
        values[i++] = value;
      }
      return new MultiAttributeValueSequence(attributeDescription,
          values);
    }
  }



  /**
   * Merges the provided attribute value sequences.
   *
   * @param attribute1
   *          The first attribute value sequence to be merged.
   * @param attribute2
   *          The second attribute value sequence to be merged.
   * @return The merged attribute.
   */
  static AttributeValueSequence merge(
      AttributeValueSequence attribute1,
      AttributeValueSequence attribute2)
  {
    final int sz1 = attribute1.size();
    final int sz2 = attribute2.size();

    // If one attribute is empty then no need to merge.
    if (sz1 == 0)
    {
      return attribute2;
    }
    else if (sz2 == 0)
    {
      return attribute1;
    }
    else
    {
      // Both contain values.
      final String name = attribute1.getAttributeDescriptionAsString();
      final int sz = sz1 + sz2;
      final ByteString[] values = new ByteString[sz];

      int i = 0;

      for (final ByteString value : attribute1)
      {
        values[i++] = value;
      }

      for (final ByteString value : attribute2)
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
   * Creates a new attribute using the provided collection.
   *
   * @param attributeDescription
   *          The attribute name.
   * @param values
   *          The attribute values.
   * @return The new attribute.
   */
  static AttributeValueSequence create(String attributeDescription,
      Collection<?> values)
  {
    final int sz = values.size();
    if (sz == 0)
    {
      return new EmptyAttributeValueSequence(attributeDescription);
    }
    else if (sz == 1)
    {
      return new SingleAttributeValueSequence(attributeDescription,
          ByteString.valueOf(values.iterator().next()));
    }
    else
    {
      ByteString[] array = new ByteString[sz];
      int i = 0;
      for (Object object : values)
      {
        array[i++] = ByteString.valueOf(object);
      }
      return new MultiAttributeValueSequence(attributeDescription,
          array);
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
      Object value)
  {
    return new SingleAttributeValueSequence(attributeDescription,
        ByteString.valueOf(value));
  }



  /**
   * Creates a new multi-valued attribute using the provided values.
   *
   * @param attributeDescription
   *          The attribute name.
   * @param values
   *          The attribute values.
   * @return The new multi-valued attribute.
   */
  static AttributeValueSequence create(String attributeDescription,
      Object... values)
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
          ByteString.valueOf(values[0]));
    }
    else
    {
      ByteString[] tmp = new ByteString[sz];
      for (int i = 0; i < sz; i++)
      {
        tmp[i] = ByteString.valueOf(values[i]);
      }
      return new MultiAttributeValueSequence(attributeDescription, tmp);
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
