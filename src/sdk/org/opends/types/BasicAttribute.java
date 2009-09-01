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



import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.opends.schema.EqualityMatchingRule;
import org.opends.schema.Schema;
import org.opends.server.types.ByteString;
import org.opends.util.Validator;



/**
 * Attribute implementation.
 */
final class BasicAttribute implements Attribute
{
  private static abstract class Impl
  {

    abstract boolean add(BasicAttribute attribute, ByteString value);



    boolean addAll(BasicAttribute attribute,
        Collection<? extends ByteString> values)
    {
      // TODO: could optimize if values is a BasicAttribute.
      ensureCapacity(attribute, values.size());
      boolean modified = false;
      for (ByteString value : values)
      {
        modified |= add(attribute, value);
      }
      resize(attribute);
      return modified;
    }



    boolean addAllObjects(BasicAttribute attribute,
        Collection<?> objects)
    {
      // TODO: could optimize if objects is a BasicAttribute.
      ensureCapacity(attribute, objects.size());
      boolean modified = false;
      for (Object object : objects)
      {
        modified |= add(attribute, ByteString.valueOf(object));
      }
      resize(attribute);
      return modified;
    }



    abstract void clear(BasicAttribute attribute);



    abstract boolean contains(BasicAttribute attribute, ByteString value);



    boolean containsAll(BasicAttribute attribute, Collection<?> objects)
    {
      // TODO: could optimize if objects is a BasicAttribute.
      for (Object object : objects)
      {
        if (!contains(attribute, ByteString.valueOf(object)))
        {
          return false;
        }
      }
      return true;
    }



    abstract void ensureCapacity(BasicAttribute attribute, int size);



    abstract ByteString firstValue(BasicAttribute attribute)
        throws NoSuchElementException;



    abstract int hashCode(BasicAttribute attribute);



    abstract boolean isEmpty(BasicAttribute attribute);



    abstract Iterator<ByteString> iterator(BasicAttribute attribute);



    abstract boolean remove(BasicAttribute attribute, ByteString value);



    boolean removeAll(BasicAttribute attribute, Collection<?> objects)
    {
      // TODO: could optimize if objects is a BasicAttribute.
      boolean modified = false;
      for (Object object : objects)
      {
        modified |= remove(attribute, ByteString.valueOf(object));
      }
      return modified;
    }



    abstract void resize(BasicAttribute attribute);



    abstract boolean retainAll(BasicAttribute attribute,
        Collection<?> objects);



    abstract int size(BasicAttribute attribute);
  }



  private static final class MultiValueImpl extends Impl
  {

    boolean add(BasicAttribute attribute, ByteString value)
    {
      ByteString normalizedValue = normalizeValue(attribute, value);
      if (attribute.multipleValues.put(normalizedValue, value) == null)
      {
        return true;
      }
      else
      {
        return false;
      }
    }



    void clear(BasicAttribute attribute)
    {
      attribute.multipleValues = null;
      attribute.pimpl = ZERO_VALUE_IMPL;
    }



    boolean contains(BasicAttribute attribute, ByteString value)
    {
      return attribute.multipleValues.containsKey(normalizeValue(
          attribute, value));
    }



    void ensureCapacity(BasicAttribute attribute, int size)
    {
      // Nothing to do.
    }



    ByteString firstValue(BasicAttribute attribute)
        throws NoSuchElementException
    {
      return attribute.multipleValues.values().iterator().next();
    }



    int hashCode(BasicAttribute attribute)
    {
      // Only compute the hash code over the normalized values.
      return attribute.multipleValues.keySet().hashCode();
    }



    boolean isEmpty(BasicAttribute attribute)
    {
      return false;
    }



    Iterator<ByteString> iterator(final BasicAttribute attribute)
    {
      return new Iterator<ByteString>()
      {
        private Impl expectedImpl = MULTI_VALUE_IMPL;
        private Iterator<ByteString> iterator =
            attribute.multipleValues.values().iterator();



        public boolean hasNext()
        {
          return iterator.hasNext();
        }



        public ByteString next()
        {
          if (attribute.pimpl != expectedImpl)
          {
            throw new ConcurrentModificationException();
          }
          else
          {
            return iterator.next();
          }
        }



        public void remove()
        {
          if (attribute.pimpl != expectedImpl)
          {
            throw new ConcurrentModificationException();
          }
          else
          {
            iterator.remove();

            // Resize if we have removed the second to last value.
            if (attribute.multipleValues != null
                && attribute.multipleValues.size() == 1)
            {
              resize(attribute);
              iterator = attribute.pimpl.iterator(attribute);
            }

            // Always update since we may change to single or zero value
            // impl.
            expectedImpl = attribute.pimpl;
          }
        }

      };
    }



    boolean remove(BasicAttribute attribute, ByteString value)
    {
      ByteString normalizedValue =
          normalizeValue(attribute, ByteString.valueOf(value));
      if (attribute.multipleValues.remove(normalizedValue) != null)
      {
        resize(attribute);
        return true;
      }
      else
      {
        return false;
      }
    }



    void resize(BasicAttribute attribute)
    {
      // May need to resize if initial size estimate was wrong (e.g. all
      // values in added collection were the same).
      switch (attribute.multipleValues.size())
      {
      case 0:
        attribute.multipleValues = null;
        attribute.pimpl = ZERO_VALUE_IMPL;
        break;
      case 1:
        Map.Entry<ByteString, ByteString> e =
            attribute.multipleValues.entrySet().iterator().next();
        attribute.singleValue = e.getValue();
        attribute.normalizedSingleValue = e.getKey();
        attribute.multipleValues = null;
        attribute.pimpl = SINGLE_VALUE_IMPL;
        break;
      default:
        // Nothing to do.
        break;
      }
    }



    boolean retainAll(BasicAttribute attribute, Collection<?> objects)
    {
      // TODO: could optimize if objects is a BasicAttribute.
      if (objects.isEmpty())
      {
        clear(attribute);
        return true;
      }

      Set<ByteString> normalizedValues =
          new HashSet<ByteString>(objects.size());
      for (Object object : objects)
      {
        normalizedValues.add(normalizeValue(attribute, ByteString
            .valueOf(object)));
      }

      boolean modified = false;

      Iterator<ByteString> iterator =
          attribute.multipleValues.keySet().iterator();
      while (iterator.hasNext())
      {
        ByteString key = iterator.next();
        if (!normalizedValues.contains(key))
        {
          iterator.remove();
          modified = true;
        }
      }

      resize(attribute);

      return modified;
    }



    int size(BasicAttribute attribute)
    {
      return attribute.multipleValues.size();
    }
  }



  private static final class SingleValueImpl extends Impl
  {

    boolean add(BasicAttribute attribute, ByteString value)
    {
      ByteString normalizedValue = normalizeValue(attribute, value);
      if (normalizedSingleValue(attribute).equals(normalizedValue))
      {
        return false;
      }

      attribute.multipleValues =
          new LinkedHashMap<ByteString, ByteString>(2);
      attribute.multipleValues.put(attribute.normalizedSingleValue,
          attribute.singleValue);
      attribute.multipleValues.put(normalizedValue, value);
      attribute.singleValue = null;
      attribute.normalizedSingleValue = null;
      attribute.pimpl = MULTI_VALUE_IMPL;

      return true;
    }



    void clear(BasicAttribute attribute)
    {
      attribute.singleValue = null;
      attribute.normalizedSingleValue = null;
      attribute.pimpl = ZERO_VALUE_IMPL;
    }



    boolean contains(BasicAttribute attribute, ByteString value)
    {
      if (attribute.singleValue.equals(value))
      {
        return true;
      }

      ByteString normalizedValue = normalizeValue(attribute, value);
      return normalizedSingleValue(attribute).equals(normalizedValue);
    }



    void ensureCapacity(BasicAttribute attribute, int size)
    {
      if (size < 1)
      {
        return;
      }

      attribute.multipleValues =
          new LinkedHashMap<ByteString, ByteString>(1 + size);
      attribute.multipleValues.put(attribute.normalizedSingleValue,
          attribute.singleValue);
      attribute.singleValue = null;
      attribute.normalizedSingleValue = null;
      attribute.pimpl = MULTI_VALUE_IMPL;
    }



    ByteString firstValue(BasicAttribute attribute)
        throws NoSuchElementException
    {
      if (attribute.singleValue != null)
      {
        return attribute.singleValue;
      }
      else
      {
        throw new NoSuchElementException();
      }
    }



    int hashCode(BasicAttribute attribute)
    {
      // Only compute the hash code over the normalized value.
      return normalizedSingleValue(attribute).hashCode();
    }



    boolean isEmpty(BasicAttribute attribute)
    {
      return false;
    }



    Iterator<ByteString> iterator(final BasicAttribute attribute)
    {
      return new Iterator<ByteString>()
      {
        private Impl expectedImpl = SINGLE_VALUE_IMPL;
        private boolean hasNext = true;



        public boolean hasNext()
        {
          return hasNext;
        }



        public ByteString next()
        {
          if (attribute.pimpl != expectedImpl)
          {
            throw new ConcurrentModificationException();
          }
          else if (hasNext)
          {
            hasNext = false;
            return attribute.singleValue;
          }
          else
          {
            throw new NoSuchElementException();
          }
        }



        public void remove()
        {
          if (attribute.pimpl != expectedImpl)
          {
            throw new ConcurrentModificationException();
          }
          else if (hasNext || attribute.singleValue == null)
          {
            throw new IllegalStateException();
          }
          else
          {
            clear(attribute);
            expectedImpl = attribute.pimpl;
          }
        }

      };
    }



    boolean remove(BasicAttribute attribute, ByteString value)
    {
      if (contains(attribute, value))
      {
        clear(attribute);
        return true;
      }
      else
      {
        return false;
      }
    }



    void resize(BasicAttribute attribute)
    {
      // Nothing to do.
    }



    boolean retainAll(BasicAttribute attribute, Collection<?> objects)
    {
      // TODO: could optimize if objects is a BasicAttribute.
      if (objects.isEmpty())
      {
        clear(attribute);
        return true;
      }

      ByteString normalizedSingleValue =
          normalizedSingleValue(attribute);
      for (Object object : objects)
      {
        ByteString normalizedValue =
            normalizeValue(attribute, ByteString.valueOf(object));
        if (normalizedSingleValue.equals(normalizedValue))
        {
          return false;
        }
      }

      clear(attribute);
      return true;
    }



    int size(BasicAttribute attribute)
    {
      return 1;
    }
  }



  private static final class ZeroValueImpl extends Impl
  {

    boolean add(BasicAttribute attribute, ByteString value)
    {
      attribute.singleValue = value;
      attribute.pimpl = SINGLE_VALUE_IMPL;
      return true;
    }



    void clear(BasicAttribute attribute)
    {
      // Nothing to do.
    }



    boolean contains(BasicAttribute attribute, ByteString value)
    {
      return false;
    }



    boolean containsAll(BasicAttribute attribute, Collection<?> objects)
    {
      return objects.isEmpty();
    }



    void ensureCapacity(BasicAttribute attribute, int size)
    {
      if (size < 2)
      {
        return;
      }

      attribute.multipleValues =
          new LinkedHashMap<ByteString, ByteString>(size);
      attribute.pimpl = MULTI_VALUE_IMPL;
    }



    ByteString firstValue(BasicAttribute attribute)
        throws NoSuchElementException
    {
      throw new NoSuchElementException();
    }



    int hashCode(BasicAttribute attribute)
    {
      return 0;
    }



    boolean isEmpty(BasicAttribute attribute)
    {
      return true;
    }



    Iterator<ByteString> iterator(final BasicAttribute attribute)
    {
      return new Iterator<ByteString>()
      {
        public boolean hasNext()
        {
          return false;
        }



        public ByteString next()
        {
          if (attribute.pimpl != ZERO_VALUE_IMPL)
          {
            throw new ConcurrentModificationException();
          }
          else
          {
            throw new NoSuchElementException();
          }
        }



        public void remove()
        {
          if (attribute.pimpl != ZERO_VALUE_IMPL)
          {
            throw new ConcurrentModificationException();
          }
          else
          {
            throw new IllegalStateException();
          }
        }

      };
    }



    boolean remove(BasicAttribute attribute, ByteString value)
    {
      return false;
    }



    boolean removeAll(BasicAttribute attribute, Collection<?> objects)
    {
      return false;
    }



    void resize(BasicAttribute attribute)
    {
      // Nothing to do.
    }



    boolean retainAll(BasicAttribute attribute, Collection<?> objects)
    {
      return false;
    }



    int size(BasicAttribute attribute)
    {
      return 0;
    }

  }

  private static final MultiValueImpl MULTI_VALUE_IMPL =
      new MultiValueImpl();

  private static final SingleValueImpl SINGLE_VALUE_IMPL =
      new SingleValueImpl();
  private static final ZeroValueImpl ZERO_VALUE_IMPL =
      new ZeroValueImpl();



  // Lazily computes the normalized single value.
  private static ByteString normalizedSingleValue(
      BasicAttribute attribute)
  {
    if (attribute.normalizedSingleValue == null)
    {
      attribute.normalizedSingleValue =
          normalizeValue(attribute, attribute.singleValue);
    }
    return attribute.normalizedSingleValue;
  }



  // Normalizes the provided value using the attribute's equality
  // matching rule.
  private static ByteString normalizeValue(BasicAttribute attribute,
      ByteString value)
  {
    EqualityMatchingRule mrule =
        attribute.name.getAttributeType().getEqualityMatchingRule();

    ByteString normalizedValue;
    try
    {
      normalizedValue =
          mrule.normalizeAttributeValue(value).toByteString();
    }
    catch (Exception e)
    {
      // Fall back to provided value.
      normalizedValue = value;
    }
    return normalizedValue;
  }

  private Map<ByteString, ByteString> multipleValues = null;

  private final AttributeDescription name;

  private ByteString normalizedSingleValue = null;

  private Impl pimpl = ZERO_VALUE_IMPL;

  private ByteString singleValue = null;



  /**
   * Creates a new basic attribute which is a copy of the provided
   * attribute.
   *
   * @param attribute
   *          The attribute to be copied.
   */
  BasicAttribute(Attribute attribute)
  {
    this.name = attribute.getAttributeDescription();

    if (attribute instanceof BasicAttribute)
    {
      BasicAttribute other = (BasicAttribute) attribute;
      this.pimpl = other.pimpl;
      this.singleValue = other.singleValue;
      this.normalizedSingleValue = other.normalizedSingleValue;
      if (other.multipleValues != null)
      {
        this.multipleValues =
            new LinkedHashMap<ByteString, ByteString>(
                other.multipleValues);
      }
    }
    else
    {
      addAll(attribute);
    }
  }



  /**
   * Creates a new basic attribute having the specified attribute
   * description.
   *
   * @param name
   *          The attribute description.
   */
  BasicAttribute(AttributeDescription name)
  {
    Validator.ensureNotNull(name);
    this.name = name;
  }



  /**
   * Creates a new basic attribute which is a copy of the provided
   * attribute value sequence.
   *
   * @param attribute
   *          The attribute value sequence to be copied.
   */
  BasicAttribute(AttributeValueSequence attribute, Schema schema)
  {
    this.name =
        AttributeDescription.valueOf(attribute
            .getAttributeDescriptionAsString(), schema);

    this.pimpl.ensureCapacity(this, attribute.size());
    for (ByteString value : attribute)
    {
      this.pimpl.add(this, value);
    }
    this.pimpl.resize(this);
  }



  /**
   * {@inheritDoc}
   */
  public boolean add(ByteString value) throws NullPointerException
  {
    Validator.ensureNotNull(value);
    return pimpl.add(this, value);
  }



  /**
   * {@inheritDoc}
   */
  public boolean addAll(Collection<? extends ByteString> values)
      throws NullPointerException
  {
    return pimpl.addAll(this, values);
  }



  /**
   * {@inheritDoc}
   */
  public boolean addAllObjects(Collection<?> objects)
      throws NullPointerException
  {
    return pimpl.addAllObjects(this, objects);
  }



  /**
   * {@inheritDoc}
   */
  public boolean addObject(Object object) throws NullPointerException
  {
    Validator.ensureNotNull(object);
    return pimpl.add(this, ByteString.valueOf(object));
  }



  /**
   * {@inheritDoc}
   */
  public void clear()
  {
    pimpl.clear(this);
  }



  /**
   * {@inheritDoc}
   */
  public boolean contains(ByteString value) throws NullPointerException
  {
    Validator.ensureNotNull(value);
    return pimpl.contains(this, value);
  }



  /**
   * {@inheritDoc}
   */
  public boolean contains(Object object) throws NullPointerException
  {
    Validator.ensureNotNull(object);
    return pimpl.contains(this, ByteString.valueOf(object));
  }



  /**
   * {@inheritDoc}
   */
  public boolean containsAll(Collection<?> objects)
      throws NullPointerException
  {
    return pimpl.containsAll(this, objects);
  }



  /**
   * {@inheritDoc}
   */
  public boolean equals(Object object)
  {
    if (this == object)
    {
      return true;
    }

    if (!(object instanceof Attribute))
    {
      // TODO: do we want to compare against AttributeValueSequences?
      return false;
    }

    Attribute other = (Attribute) object;
    if (!name.equals(other.getAttributeDescription()))
    {
      return false;
    }

    // Attribute description is the same, compare values.
    if (size() != other.size())
    {
      return false;
    }

    return containsAll(other);
  }



  /**
   * {@inheritDoc}
   */
  public ByteString firstValue() throws NoSuchElementException
  {
    return pimpl.firstValue(this);
  }



  /**
   * {@inheritDoc}
   */
  public String firstValueAsString()
  {
    return firstValue().toString();
  }



  /**
   * {@inheritDoc}
   */
  public AttributeDescription getAttributeDescription()
  {
    return name;
  }



  /**
   * {@inheritDoc}
   */
  public String getAttributeDescriptionAsString()
  {
    return name.toString();
  }



  /**
   * {@inheritDoc}
   */
  public int hashCode()
  {
    return name.hashCode() + pimpl.hashCode(this);
  }



  /**
   * {@inheritDoc}
   */
  public boolean isEmpty()
  {
    return pimpl.isEmpty(this);
  }



  /**
   * {@inheritDoc}
   */
  public Iterator<ByteString> iterator()
  {
    return pimpl.iterator(this);
  }



  /**
   * {@inheritDoc}
   */
  public boolean remove(ByteString value) throws NullPointerException
  {
    Validator.ensureNotNull(value);
    return pimpl.remove(this, value);
  }



  /**
   * {@inheritDoc}
   */
  public boolean remove(Object object) throws NullPointerException
  {
    Validator.ensureNotNull(object);
    return pimpl.remove(this, ByteString.valueOf(object));
  }



  /**
   * {@inheritDoc}
   */
  public boolean removeAll(Collection<?> objects)
      throws NullPointerException
  {
    return pimpl.removeAll(this, objects);
  }



  /**
   * {@inheritDoc}
   */
  public boolean retainAll(Collection<?> objects)
      throws NullPointerException
  {
    return pimpl.retainAll(this, objects);
  }



  /**
   * {@inheritDoc}
   */
  public int size()
  {
    return pimpl.size(this);
  }



  /**
   * {@inheritDoc}
   */
  public ByteString[] toArray()
  {
    int sz = size();
    ByteString[] array = new ByteString[sz];
    switch (sz)
    {
    case 0:
      break;
    case 1:
      array[0] = singleValue;
      break;
    default:
      int i = 0;
      for (ByteString value : multipleValues.values())
      {
        array[i++] = value;
      }
      break;
    }
    return array;
  }



  /**
   * {@inheritDoc}
   */
  public <T> T[] toArray(T[] array) throws ArrayStoreException,
      NullPointerException
  {
    Validator.ensureNotNull(array);
    switch (size())
    {
    case 0:
      return Collections.emptySet().toArray(array);
    case 1:
      return Collections.singleton(singleValue).toArray(array);
    default:
      return multipleValues.values().toArray(array);
    }
  }



  /**
   * {@inheritDoc}
   */
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("Attribute(");
    builder.append(name);
    builder.append(", {");

    boolean firstValue = true;
    for (ByteString value : this)
    {
      if (!firstValue)
      {
        builder.append(", ");
      }

      builder.append(value);
      firstValue = false;
    }

    builder.append("})");
    return builder.toString();
  }

}
