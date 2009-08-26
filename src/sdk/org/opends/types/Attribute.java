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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.opends.schema.EqualityMatchingRule;
import org.opends.schema.Schema;
import org.opends.server.types.ByteString;
import org.opends.util.Validator;



/**
 * An attribute, comprising of an attribute description and zero or more
 * values. Attributes are immutable and can therefore be used safely in
 * multi-threaded applications. Various factory methods are provided for
 * creating new attributes and modifying existing attributes.
 * <p>
 * TODO: rename attribute factory (suitable for search results).
 * <p>
 * TODO: matching against attribute value assertions.
 * <p>
 * TODO: extension mechanism for implementing virtual attributes and
 * other special cases (e.g. attributes whose values are lazily
 * retrieved from persistent storage).
 * <p>
 * TODO: add / subtract / contains should have equivalent methods to
 * create.
 * <p>
 * TODO: methods for retrieving a single value as a specific type (e.g.
 * boolean, integer, etc).
 */
public final class Attribute implements AttributeValueSequence
{
  private static abstract class Impl implements Iterable<ByteString>
  {
    protected Impl()
    {
      // Nothing to do.
    }



    public abstract boolean contains(AttributeDescription name,
        ByteString value);



    public abstract boolean containsAll(AttributeDescription name,
        AttributeValueSequence values);



    public abstract boolean containsAll(AttributeDescription name,
        Collection<ByteString> values);



    public abstract boolean equals(Impl other);



    public abstract ByteString firstValue();



    @Override
    public abstract int hashCode();



    public abstract boolean isEmpty();



    public abstract int size();

  }



  private static final class MultiValueImpl extends Impl
  {
    private final Map<ByteString, ByteString> values;



    private MultiValueImpl(AttributeDescription name,
        Map<ByteString, ByteString> values)
    {
      this.values = Collections.unmodifiableMap(values);
    }



    @Override
    public boolean contains(AttributeDescription name, ByteString value)
    {
      ByteString normalizedValue =
          name.getAttributeType().getEqualityMatchingRule()
              .normalizeAttributeValue(value).toByteString();
      return values.containsKey(normalizedValue);
    }



    @Override
    public boolean containsAll(AttributeDescription name,
        AttributeValueSequence values)
    {
      if (values instanceof Attribute)
      {
        // Avoid normalization if possible.
        Attribute other = (Attribute) values;

        EqualityMatchingRule thisMrule =
            name.getAttributeType().getEqualityMatchingRule();
        EqualityMatchingRule otherMrule =
            other.getAttributeDescription().getAttributeType()
                .getEqualityMatchingRule();

        if (thisMrule.equals(otherMrule))
        {
          switch (other.size())
          {
          case 0:
            return true;
          case 1:
            SingleValueImpl otherSinglePimpl =
                (SingleValueImpl) other.pimpl;
            return this.values
                .containsKey(otherSinglePimpl.normalizedValue);
          default:
            MultiValueImpl otherMultiPimpl =
                (MultiValueImpl) other.pimpl;

            if (otherMultiPimpl.values.size() > this.values.size())
            {
              return false;
            }

            for (ByteString normalizedValue : otherMultiPimpl.values
                .keySet())
            {
              if (!this.values.containsKey(normalizedValue))
              {
                return false;
              }
            }

            return true;
          }
        }
      }

      // Brute force.
      for (ByteString value : values)
      {
        if (!contains(name, value))
        {
          return false;
        }
      }
      return true;
    }



    @Override
    public boolean containsAll(AttributeDescription name,
        Collection<ByteString> values)
    {
      for (ByteString value : values)
      {
        if (!contains(name, value))
        {
          return false;
        }
      }
      return true;
    }



    @Override
    public boolean equals(Impl other)
    {
      // We have already tested for identity.
      if (other instanceof MultiValueImpl)
      {
        MultiValueImpl otherPimpl = (MultiValueImpl) other;
        return values.keySet().equals(otherPimpl.values.keySet());
      }
      else
      {
        return false;
      }
    }



    @Override
    public ByteString firstValue()
    {
      return iterator().next();
    }



    @Override
    public int hashCode()
    {
      return values.keySet().hashCode();
    }



    @Override
    public boolean isEmpty()
    {
      return false;
    }



    @Override
    public Iterator<ByteString> iterator()
    {
      return values.values().iterator();
    }



    @Override
    public int size()
    {
      return values.size();
    }
  }



  private static final class SingleValueImpl extends Impl
  {
    private final ByteString normalizedValue;
    private final ByteString value;



    private SingleValueImpl(AttributeDescription name,
        ByteString value, ByteString normalizedValue)
    {
      this.value = value;
      this.normalizedValue = normalizedValue;
    }



    @Override
    public boolean contains(AttributeDescription name, ByteString value)
    {
      ByteString normalizedValue =
          name.getAttributeType().getEqualityMatchingRule()
              .normalizeAttributeValue(value).toByteString();
      return this.normalizedValue.equals(normalizedValue);
    }



    @Override
    public boolean containsAll(AttributeDescription name,
        AttributeValueSequence values)
    {
      if (values instanceof Attribute)
      {
        // Avoid normalization if possible.
        Attribute other = (Attribute) values;

        EqualityMatchingRule thisMrule =
            name.getAttributeType().getEqualityMatchingRule();
        EqualityMatchingRule otherMrule =
            other.getAttributeDescription().getAttributeType()
                .getEqualityMatchingRule();

        if (thisMrule.equals(otherMrule))
        {
          switch (other.size())
          {
          case 0:
            return true;
          case 1:
            SingleValueImpl otherPimpl = (SingleValueImpl) other.pimpl;
            return normalizedValue.equals(otherPimpl.normalizedValue);
          default:
            return false;
          }
        }
      }

      // Brute force.
      for (ByteString value : values)
      {
        if (!contains(name, value))
        {
          return false;
        }
      }
      return true;
    }



    @Override
    public boolean containsAll(AttributeDescription name,
        Collection<ByteString> values)
    {
      for (ByteString value : values)
      {
        if (!contains(name, value))
        {
          return false;
        }
      }
      return true;
    }



    @Override
    public boolean equals(Impl other)
    {
      // We have already tested for identity.
      if (other instanceof SingleValueImpl)
      {
        SingleValueImpl otherPimpl = (SingleValueImpl) other;
        return normalizedValue.equals(otherPimpl.normalizedValue);
      }
      else
      {
        return false;
      }
    }



    @Override
    public ByteString firstValue()
    {
      return value;
    }



    @Override
    public int hashCode()
    {
      return normalizedValue.hashCode();
    }



    @Override
    public boolean isEmpty()
    {
      return false;
    }



    @Override
    public Iterator<ByteString> iterator()
    {
      return new Iterator<ByteString>()
      {
        private final boolean hasNext = true;



        public boolean hasNext()
        {
          return hasNext;
        }



        public ByteString next()
        {
          if (hasNext)
          {
            return value;
          }
          else
          {
            throw new NoSuchElementException();
          }
        }



        public void remove()
        {
          throw new UnsupportedOperationException();
        }

      };
    }



    @Override
    public int size()
    {
      return 1;
    }
  }



  private static final class ZeroValueImpl extends Impl
  {
    private ZeroValueImpl()
    {
      // Nothing to do.
    }



    @Override
    public boolean contains(AttributeDescription name, ByteString value)
    {
      return false;
    }



    @Override
    public boolean containsAll(AttributeDescription name,
        AttributeValueSequence values)
    {
      return values.isEmpty() ? true : false;
    }



    @Override
    public boolean containsAll(AttributeDescription name,
        Collection<ByteString> values)
    {
      return values.isEmpty() ? true : false;
    }



    @Override
    public boolean equals(Impl other)
    {
      return (this == other);
    }



    @Override
    public ByteString firstValue()
    {
      return null;
    }



    @Override
    public int hashCode()
    {
      return 0;
    }



    @Override
    public boolean isEmpty()
    {
      return true;
    }



    @Override
    public Iterator<ByteString> iterator()
    {
      return Collections.<ByteString> emptyList().iterator();
    }



    @Override
    public int size()
    {
      return 0;
    }
  }

  private static final ZeroValueImpl ZERO_VALUE_IMPL =
      new ZeroValueImpl();



  /**
   * Creates a new attribute containing all the values from the provided
   * attribute and attribute value sequence. The returned attribute will
   * have the same attribute description as the provided attribute.
   * <p>
   * This method is equivalent to:
   *
   * <pre>
   * add(attribute, values, null);
   * </pre>
   *
   * @param attribute
   *          The attribute.
   * @param values
   *          The attribute value sequence containing the values to be
   *          added to {@code attribute}.
   * @return A new attribute containing all the values from the provided
   *         attribute and attribute value sequence.
   */
  public static Attribute add(Attribute attribute,
      AttributeValueSequence values)
  {
    return add(attribute, values, null);
  }



  /**
   * Creates a new attribute containing all the values from the provided
   * attribute and attribute value sequence, putting any duplicate
   * values in the provided collection. The returned attribute will have
   * the same attribute description as the provided attribute.
   *
   * @param attribute
   *          The attribute.
   * @param values
   *          The attribute value sequence containing the values to be
   *          added to {@code attribute}.
   * @param duplicateValues
   *          A collection which will be used to store any duplicate
   *          values, or {@code null} if duplicate values should not be
   *          stored.
   * @return A new attribute containing all the values from the provided
   *         attribute and attribute value sequence.
   */
  public static Attribute add(Attribute attribute,
      AttributeValueSequence values,
      Collection<ByteString> duplicateValues)
  {
    Validator.ensureNotNull(attribute, values);

    int valuesSize = values.size();
    if (valuesSize == 0)
    {
      return attribute;
    }

    AttributeDescription attributeDescription =
        attribute.attributeDescription;

    int attributeSize = attribute.size();
    if (attributeSize == 0)
    {
      return create(attributeDescription, values);
    }

    // Create a multi-valued attribute.
    LinkedHashMap<ByteString, ByteString> map =
        new LinkedHashMap<ByteString, ByteString>(attributeSize
            + valuesSize);

    // Add attribute's values.
    if (attributeSize == 1)
    {
      SingleValueImpl pimpl = (SingleValueImpl) attribute.pimpl;
      map.put(pimpl.normalizedValue, pimpl.value);
    }
    else
    {
      MultiValueImpl pimpl = (MultiValueImpl) attribute.pimpl;
      map.putAll(pimpl.values);
    }

    // Add sequence's values.

    // If the sequence is an attribute having the same matching rule
    // then we can avoid normalization.
    if (values instanceof Attribute)
    {
      Attribute otherAttribute = (Attribute) values;

      EqualityMatchingRule thisMrule =
          attributeDescription.getAttributeType()
              .getEqualityMatchingRule();
      EqualityMatchingRule otherMrule =
          otherAttribute.getAttributeDescription().getAttributeType()
              .getEqualityMatchingRule();

      if (thisMrule.equals(otherMrule))
      {
        if (valuesSize == 1)
        {
          SingleValueImpl pimpl =
              (SingleValueImpl) otherAttribute.pimpl;
          if (duplicateValues != null
              && map.containsKey(pimpl.normalizedValue))
          {
            duplicateValues.add(pimpl.value);
          }
          else
          {
            map.put(pimpl.normalizedValue, pimpl.value);
          }
        }
        else
        {
          MultiValueImpl pimpl = (MultiValueImpl) otherAttribute.pimpl;
          for (Map.Entry<ByteString, ByteString> e : pimpl.values
              .entrySet())
          {
            if (duplicateValues != null && map.containsKey(e.getKey()))
            {
              duplicateValues.add(e.getValue());
            }
            else
            {
              map.put(e.getKey(), e.getValue());
            }
          }
        }

        Impl pimpl = new MultiValueImpl(attributeDescription, map);
        return new Attribute(attributeDescription, pimpl);
      }
    }

    // Brute force.
    for (ByteString value : values)
    {
      ByteString normalizedValue =
          attributeDescription.getAttributeType()
              .getEqualityMatchingRule().normalizeAttributeValue(value)
              .toByteString();
      if (duplicateValues != null && map.containsKey(normalizedValue))
      {
        duplicateValues.add(value);
      }
      else
      {
        map.put(normalizedValue, value);
      }
    }

    Impl pimpl = new MultiValueImpl(attributeDescription, map);
    return new Attribute(attributeDescription, pimpl);
  }



  /**
   * Creates a new attribute containing all the values in the provided
   * attribute as well as the provided attribute value. The returned
   * attribute will have the same attribute description as the provided
   * attribute.
   *
   * @param attribute
   *          The attribute.
   * @param value
   *          The attribute value to be included in {@code attribute}.
   * @return A new attribute containing all the values in the provided
   *         attribute as well as the provided attribute value.
   */
  public static Attribute add(Attribute attribute, ByteString value)
  {
    Validator.ensureNotNull(attribute, value);

    if (attribute.contains(value))
    {
      return attribute;
    }

    AttributeDescription attributeDescription =
        attribute.getAttributeDescription();

    if (attribute.isEmpty())
    {
      return create(attributeDescription, value);
    }

    // We're going to have a multi-valued attribute.
    MultiValueImpl pimpl = (MultiValueImpl) attribute.pimpl;
    LinkedHashMap<ByteString, ByteString> map =
        new LinkedHashMap<ByteString, ByteString>(pimpl.values);

    ByteString normalizedValue =
        attributeDescription.getAttributeType()
            .getEqualityMatchingRule().normalizeAttributeValue(value)
            .toByteString();
    map.put(normalizedValue, value);

    Impl newPimpl = new MultiValueImpl(attributeDescription, map);
    return new Attribute(attributeDescription, newPimpl);
  }



  /**
   * Creates a new attribute containing all the values in the provided
   * attribute as well as the provided attribute value. The returned
   * attribute will have the same attribute description as the provided
   * attribute.
   *
   * @param attribute
   *          The attribute.
   * @param value
   *          The attribute value to be included in {@code attribute}.
   * @return A new attribute containing all the values in the provided
   *         attribute as well as the provided attribute value.
   */
  public static Attribute add(Attribute attribute, String value)
  {
    Validator.ensureNotNull(attribute, value);

    return add(attribute, ByteString.valueOf(value));
  }



  /**
   * Creates a new attribute having the provided attribute description
   * and no values.
   *
   * @param attributeDescription
   *          The attribute description.
   * @return The new attribute.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  public static Attribute create(
      AttributeDescription attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);
    return new Attribute(attributeDescription, ZERO_VALUE_IMPL);
  }



  /**
   * Creates a new attribute having the provided attribute description
   * and single value.
   *
   * @param attributeDescription
   *          The attribute description.
   * @param value
   *          The attribute value.
   * @return The new attribute.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code value} was
   *           {@code null}.
   */
  public static Attribute create(
      AttributeDescription attributeDescription, ByteString value)
  {
    Validator.ensureNotNull(attributeDescription, value);

    ByteString normalizedValue =
        attributeDescription.getAttributeType()
            .getEqualityMatchingRule().normalizeAttributeValue(value)
            .toByteString();
    Impl pimpl =
        new SingleValueImpl(attributeDescription, value,
            normalizedValue);
    return new Attribute(attributeDescription, pimpl);
  }



  /**
   * Creates a new attribute having the provided attribute description
   * and values.
   *
   * @param attributeDescription
   *          The attribute description.
   * @param values
   *          The attribute values.
   * @return The new attribute.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code values} was
   *           {@code null}.
   */
  public static Attribute create(
      AttributeDescription attributeDescription, ByteString... values)
  {
    Validator.ensureNotNull(attributeDescription, values);

    switch (values.length)
    {
    case 0:
      return create(attributeDescription);
    case 1:
      return create(attributeDescription, values[0]);
    default:
      LinkedHashMap<ByteString, ByteString> map =
          new LinkedHashMap<ByteString, ByteString>(values.length);
      for (ByteString value : values)
      {
        ByteString normalizedValue =
            attributeDescription.getAttributeType()
                .getEqualityMatchingRule().normalizeAttributeValue(
                    value).toByteString();
        map.put(normalizedValue, value);
      }
      Impl pimpl = new MultiValueImpl(attributeDescription, map);
      return new Attribute(attributeDescription, pimpl);
    }
  }



  /**
   * Creates a new attribute having the provided attribute description
   * and values.
   *
   * @param attributeDescription
   *          The attribute description.
   * @param values
   *          The attribute values.
   * @return The new attribute.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code values} was
   *           {@code null}.
   */
  public static Attribute create(
      AttributeDescription attributeDescription,
      Collection<ByteString> values)
  {
    Validator.ensureNotNull(attributeDescription, values);

    switch (values.size())
    {
    case 0:
      return create(attributeDescription);
    case 1:
      return create(attributeDescription, values.iterator().next());
    default:
      LinkedHashMap<ByteString, ByteString> map =
          new LinkedHashMap<ByteString, ByteString>(values.size());
      for (ByteString value : values)
      {
        ByteString normalizedValue =
            attributeDescription.getAttributeType()
                .getEqualityMatchingRule().normalizeAttributeValue(
                    value).toByteString();
        map.put(normalizedValue, value);
      }
      Impl pimpl = new MultiValueImpl(attributeDescription, map);
      return new Attribute(attributeDescription, pimpl);
    }
  }



  /**
   * Creates a new attribute having the provided attribute description
   * and single value.
   *
   * @param attributeDescription
   *          The attribute description.
   * @param value
   *          The attribute value.
   * @return The new attribute.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code value} was
   *           {@code null}.
   */
  public static Attribute create(
      AttributeDescription attributeDescription, String value)
  {
    return create(attributeDescription, ByteString.valueOf(value));
  }



  /**
   * Creates a new attribute having the provided attribute description
   * and values.
   *
   * @param attributeDescription
   *          The attribute description.
   * @param values
   *          The attribute values.
   * @return The new attribute.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code values} was
   *           {@code null}.
   */
  public static Attribute create(
      AttributeDescription attributeDescription, String... values)
  {
    Validator.ensureNotNull(attributeDescription, values);

    switch (values.length)
    {
    case 0:
      return create(attributeDescription);
    case 1:
      return create(attributeDescription, values[0]);
    default:
      LinkedHashMap<ByteString, ByteString> map =
          new LinkedHashMap<ByteString, ByteString>(values.length);
      for (String stringValue : values)
      {
        ByteString value = ByteString.valueOf(stringValue);
        ByteString normalizedValue =
            attributeDescription.getAttributeType()
                .getEqualityMatchingRule().normalizeAttributeValue(
                    value).toByteString();
        map.put(normalizedValue, value);
      }
      Impl pimpl = new MultiValueImpl(attributeDescription, map);
      return new Attribute(attributeDescription, pimpl);
    }
  }



  /**
   * Creates a new attribute using the provided attribute value
   * sequence.
   *
   * @param attribute
   *          The attribute value sequence.
   * @param schema
   *          The schema to use when parsing the attribute description.
   * @return The new attribute.
   * @throws NullPointerException
   *           If {@code attribute} or {@code schema} was {@code null}.
   */
  public static Attribute create(AttributeValueSequence attribute,
      Schema schema)
  {
    Validator.ensureNotNull(attribute, schema);

    // The attribute value sequence may already be an attribute.
    // However, it may have been created using a different schema, so
    // rebuild it regardless.
    AttributeDescription attributeDescription =
        AttributeDescription.valueOf(attribute
            .getAttributeDescriptionAsString(), schema);

    return create(attributeDescription, attribute);
  }



  /**
   * Creates a new attribute having the same attribute description as
   * the provided attribute, but no values.
   *
   * @param attribute
   *          The attribute description.
   * @return The new attribute.
   * @throws NullPointerException
   *           If {@code attribute} was {@code null}.
   */
  public static Attribute empty(Attribute attribute)
  {
    Validator.ensureNotNull(attribute);

    if (!attribute.isEmpty())
    {
      return new Attribute(attribute.getAttributeDescription(),
          ZERO_VALUE_IMPL);
    }
    else
    {
      return attribute;
    }
  }



  /**
   * Creates a new attribute containing all the values in the provided
   * attribute which are not contained in the provided attribute value
   * sequence. The returned attribute will have the same attribute
   * description as the provided attribute.
   * <p>
   * This method is equivalent to:
   *
   * <pre>
   * subtract(attribute, values, null);
   * </pre>
   *
   * @param attribute
   *          The attribute.
   * @param values
   *          The attribute value sequence containing the values to be
   *          removed from {@code attribute}.
   * @return A new attribute containing all the values in the provided
   *         attribute which are not contained in the provided attribute
   *         value sequence.
   */
  public static Attribute subtract(Attribute attribute,
      AttributeValueSequence values)
  {
    return subtract(attribute, values, null);
  }



  /**
   * Creates a new attribute containing all the values in the provided
   * attribute which are not contained in the provided attribute value
   * sequence, putting any missing values in the provided collection.
   * The returned attribute will have the same attribute description as
   * the provided attribute.
   *
   * @param attribute
   *          The attribute.
   * @param values
   *          The attribute value sequence containing the values to be
   *          removed from {@code attribute}.
   * @param missingValues
   *          A collection which will be used to store any missing
   *          values, or {@code null} if missing values should not be
   *          stored.
   * @return A new attribute containing all the values in the provided
   *         attribute which are not contained in the provided attribute
   *         value sequence.
   */
  public static Attribute subtract(Attribute attribute,
      AttributeValueSequence values,
      Collection<ByteString> missingValues)
  {
    Validator.ensureNotNull(attribute, values);

    int attributeSize = attribute.size();
    int valuesSize = values.size();
    if (valuesSize == 0 || attributeSize == 0)
    {
      if (missingValues != null && valuesSize != 0)
      {
        for (ByteString value : values)
        {
          missingValues.add(value);
        }
      }
      return attribute;
    }

    AttributeDescription attributeDescription =
        attribute.attributeDescription;

    // Subtract sequence's values.

    // If the sequence is an attribute having the same matching rule
    // then we can avoid normalization.
    if (values instanceof Attribute)
    {
      Attribute otherAttribute = (Attribute) values;

      EqualityMatchingRule thisMrule =
          attributeDescription.getAttributeType()
              .getEqualityMatchingRule();
      EqualityMatchingRule otherMrule =
          otherAttribute.getAttributeDescription().getAttributeType()
              .getEqualityMatchingRule();

      if (thisMrule.equals(otherMrule))
      {
        if (attributeSize == 1)
        {
          SingleValueImpl pimpl = (SingleValueImpl) attribute.pimpl;
          if (valuesSize == 1)
          {
            SingleValueImpl otherPimpl =
                (SingleValueImpl) otherAttribute.pimpl;
            if (pimpl.normalizedValue
                .equals(otherPimpl.normalizedValue))
            {
              return create(attributeDescription);
            }
            else
            {
              if (missingValues != null)
              {
                missingValues.add(otherPimpl.value);
              }
              return attribute;
            }
          }
          else
          {
            MultiValueImpl otherPimpl =
                (MultiValueImpl) otherAttribute.pimpl;

            if (missingValues != null)
            {
              missingValues.addAll(otherPimpl.values.values());
            }

            if (otherPimpl.values.containsKey(pimpl.normalizedValue))
            {
              if (missingValues != null)
              {
                missingValues.remove(pimpl.value);
              }
              return create(attributeDescription);
            }
            else
            {
              return attribute;
            }
          }
        }
        else
        {
          MultiValueImpl pimpl = (MultiValueImpl) attribute.pimpl;
          if (valuesSize == 1)
          {
            SingleValueImpl otherPimpl =
                (SingleValueImpl) otherAttribute.pimpl;
            if (pimpl.values.containsKey(otherPimpl.normalizedValue))
            {
              LinkedHashMap<ByteString, ByteString> map =
                  new LinkedHashMap<ByteString, ByteString>(
                      pimpl.values);
              map.remove(otherPimpl.normalizedValue);
              Impl newPimpl =
                  new MultiValueImpl(attributeDescription, map);
              return new Attribute(attributeDescription, newPimpl);
            }
            else
            {
              if (missingValues != null)
              {
                missingValues.add(otherPimpl.value);
              }
              return attribute;
            }
          }
          else
          {
            MultiValueImpl otherPimpl =
                (MultiValueImpl) otherAttribute.pimpl;
            LinkedHashMap<ByteString, ByteString> map = null;
            for (Map.Entry<ByteString, ByteString> e : otherPimpl.values
                .entrySet())
            {
              if (map == null)
              {
                if (pimpl.values.containsKey(e.getKey()))
                {
                  map =
                      new LinkedHashMap<ByteString, ByteString>(
                          pimpl.values);
                  map.remove(e.getKey());
                }
                else if (missingValues != null)
                {
                  missingValues.add(e.getValue());
                }
              }
              else
              {
                if (map.remove(e.getKey()) == null
                    && missingValues != null)
                {
                  missingValues.add(e.getValue());
                }
              }
            }
            if (map != null)
            {
              Impl newPimpl =
                  new MultiValueImpl(attributeDescription, map);
              return new Attribute(attributeDescription, newPimpl);
            }
            else
            {
              return attribute;
            }
          }
        }
      }
    }

    // Brute force.
    if (attributeSize == 1)
    {
      SingleValueImpl pimpl = (SingleValueImpl) attribute.pimpl;
      boolean valueFound = false;

      for (ByteString value : values)
      {
        ByteString normalizedValue =
            attributeDescription.getAttributeType()
                .getEqualityMatchingRule().normalizeAttributeValue(
                    value).toByteString();
        if (!pimpl.normalizedValue.equals(normalizedValue))
        {
          if (missingValues != null)
          {
            missingValues.add(value);
          }
        }
        else
        {
          valueFound = true;
        }
      }

      if (valueFound)
      {
        return create(attributeDescription);
      }
      else
      {
        return attribute;
      }
    }
    else
    {
      MultiValueImpl pimpl = (MultiValueImpl) attribute.pimpl;
      LinkedHashMap<ByteString, ByteString> map = null;
      for (ByteString value : values)
      {
        ByteString normalizedValue =
            attributeDescription.getAttributeType()
                .getEqualityMatchingRule().normalizeAttributeValue(
                    value).toByteString();
        if (map == null)
        {
          if (pimpl.values.containsKey(normalizedValue))
          {
            map =
                new LinkedHashMap<ByteString, ByteString>(pimpl.values);
            map.remove(normalizedValue);
          }
          else if (missingValues != null)
          {
            missingValues.add(value);
          }
        }
        else
        {
          if (map.remove(normalizedValue) == null
              && missingValues != null)
          {
            missingValues.add(value);
          }
        }
      }

      if (map != null)
      {
        Impl newPimpl = new MultiValueImpl(attributeDescription, map);
        return new Attribute(attributeDescription, newPimpl);
      }
      else
      {
        return attribute;
      }
    }
  }



  /**
   * Creates a new attribute containing all the values in the provided
   * attribute excluding the provided attribute value. The returned
   * attribute will have the same attribute description as the provided
   * attribute.
   *
   * @param attribute
   *          The attribute.
   * @param value
   *          The attribute value to be excluded from {@code attribute}.
   * @return A new attribute containing all the values in the provided
   *         attribute which are not contained in the provided attribute
   *         value sequence.
   */
  public static Attribute subtract(Attribute attribute, ByteString value)
  {
    Validator.ensureNotNull(attribute, value);

    if (!attribute.contains(value))
    {
      return attribute;
    }

    if (attribute.size() == 1)
    {
      return empty(attribute);
    }

    // We're going to be left with a multi-valued attribute.
    AttributeDescription attributeDescription =
        attribute.attributeDescription;

    MultiValueImpl pimpl = (MultiValueImpl) attribute.pimpl;
    LinkedHashMap<ByteString, ByteString> map =
        new LinkedHashMap<ByteString, ByteString>(pimpl.values);

    ByteString normalizedValue =
        attributeDescription.getAttributeType()
            .getEqualityMatchingRule().normalizeAttributeValue(value)
            .toByteString();
    map.remove(normalizedValue);

    Impl newPimpl = new MultiValueImpl(attributeDescription, map);
    return new Attribute(attributeDescription, newPimpl);
  }



  /**
   * Creates a new attribute containing all the values in the provided
   * attribute excluding the provided attribute value. The returned
   * attribute will have the same attribute description as the provided
   * attribute.
   *
   * @param attribute
   *          The attribute.
   * @param value
   *          The attribute value to be excluded from {@code attribute}.
   * @return A new attribute containing all the values in the provided
   *         attribute which are not contained in the provided attribute
   *         value sequence.
   */
  public static Attribute subtract(Attribute attribute, String value)
  {
    Validator.ensureNotNull(attribute, value);

    return subtract(attribute, ByteString.valueOf(value));
  }



  private static Attribute create(
      AttributeDescription attributeDescription,
      AttributeValueSequence values)
  {
    switch (values.size())
    {
    case 0:
      return create(attributeDescription);
    case 1:
      return create(attributeDescription, values.iterator().next());
    default:
      LinkedHashMap<ByteString, ByteString> map =
          new LinkedHashMap<ByteString, ByteString>(values.size());
      for (ByteString value : values)
      {
        ByteString normalizedValue =
            attributeDescription.getAttributeType()
                .getEqualityMatchingRule().normalizeAttributeValue(
                    value).toByteString();
        map.put(normalizedValue, value);
      }
      Impl pimpl = new MultiValueImpl(attributeDescription, map);
      return new Attribute(attributeDescription, pimpl);
    }
  }

  private final AttributeDescription attributeDescription;

  private final Impl pimpl;



  // Private constructor.
  private Attribute(AttributeDescription attributeDescription,
      Impl pimpl)
  {
    this.attributeDescription = attributeDescription;
    this.pimpl = pimpl;
  }



  /**
   * Indicates whether this attribute contains the specified value.
   *
   * @param value
   *          The value for which to make the determination.
   * @return {@code true} if this attribute has the specified value, or
   *         {@code false} if not.
   */
  public boolean contains(ByteString value)
  {
    return pimpl.contains(attributeDescription, value);
  }



  /**
   * Indicates whether this attribute contains all the values in the
   * provided attribute value sequence.
   *
   * @param values
   *          The set of values for which to make the determination.
   * @return {@code true} if this attribute contains all the values in
   *         the provided collection, or {@code false} if it does not
   *         contain one or more of them.
   */
  public boolean containsAll(AttributeValueSequence values)
  {
    return pimpl.containsAll(attributeDescription, values);
  }



  /**
   * Indicates whether this attribute contains all the values in the
   * provided collection.
   *
   * @param values
   *          The set of values for which to make the determination.
   * @return {@code true} if this attribute contains all the values in
   *         the provided collection, or {@code false} if it does not
   *         contain one or more of them.
   */
  public boolean containsAll(Collection<ByteString> values)
  {
    return pimpl.containsAll(attributeDescription, values);
  }



  /**
   * Indicates whether the provided object is an attribute that is equal
   * to this attribute. It will be considered equal if the object is an
   * attribute having the same attribute description and set of values.
   *
   * @param o
   *          The object for which to make the determination.
   * @return {@code true} if the provided object is an attribute that is
   *         equal to this attribute, or {@code false} if not.
   */
  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }

    if (!(o instanceof Attribute))
    {
      // TODO: do we want to compare against AttributeValueSequences?
      return false;
    }

    Attribute other = (Attribute) o;
    if (!attributeDescription.equals(other.attributeDescription))
    {
      return false;
    }

    // Attribute description is the same, compare values.
    return pimpl.equals(other.pimpl);
  }



  /**
   * Returns the attribute description associated with this attribute.
   * This includes the attribute type and options.
   *
   * @return The attribute description.
   */
  public AttributeDescription getAttributeDescription()
  {
    return attributeDescription;
  }



  /**
   * Returns the string representation of the attribute description
   * associated with this attribute. This includes the attribute type
   * and options.
   *
   * @return The string representation of the attribute description.
   */
  public String getAttributeDescriptionAsString()
  {
    return attributeDescription.toString();
  }



  /**
   * Returns the hash code for this attribute. It will be calculated as
   * the sum of the hash codes of the attribute description and all the
   * values.
   *
   * @return The hash code for this attribute.
   */
  @Override
  public int hashCode()
  {
    return attributeDescription.hashCode() + pimpl.hashCode();
  }



  /**
   * Returns {@code true} if this attribute contains no attribute
   * values.
   *
   * @return {@code true} if this attribute contains no attribute
   *         values.
   */
  public boolean isEmpty()
  {
    return pimpl.isEmpty();
  }



  /**
   * Returns an iterator over the attribute values in this attribute.
   * The attribute values are returned in the order in which they were
   * added this attribute. The returned iterator does not support
   * attribute value removals via its {@code remove} method.
   *
   * @return An iterator over the attribute values in this attribute.
   */
  public Iterator<ByteString> iterator()
  {
    return pimpl.iterator();
  }



  /**
   * Returns the number of attribute values in this attribute.
   *
   * @return The number of attribute values in this attribute.
   */
  public int size()
  {
    return pimpl.size();
  }



  /**
   * Returns a string representation of this attribute.
   *
   * @return The string representation of this attribute.
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("Attribute(");
    builder.append(attributeDescription);
    builder.append(", {");

    boolean firstValue = true;
    for (ByteString value : pimpl)
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
