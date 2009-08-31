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



import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.opends.schema.AttributeType;
import org.opends.schema.Schema;
import org.opends.server.types.ByteString;
import org.opends.util.Iterators;
import org.opends.util.Validator;



/**
 * This class contains methods for creating and manipulating schema
 * aware objects.
 */
public final class Types
{

  /**
   * Empty attribute.
   */
  private static final class EmptyAttribute implements Attribute
  {

    private final AttributeDescription attributeDescription;



    private EmptyAttribute(AttributeDescription attributeDescription)
    {
      this.attributeDescription = attributeDescription;
    }



    public boolean add(ByteString value)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public boolean addAll(Collection<? extends ByteString> values)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public boolean addAllObjects(Collection<?> objects)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public boolean addObject(Object object)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public void clear() throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException();
    }



    public boolean contains(ByteString value)
        throws NullPointerException
    {
      return false;
    }



    public boolean contains(Object object) throws NullPointerException
    {
      return false;
    }



    public boolean containsAll(Collection<?> objects)
        throws NullPointerException
    {
      return objects.isEmpty() ? true : false;
    }



    public AttributeDescription getAttributeDescription()
    {
      return attributeDescription;
    }



    public String getAttributeDescriptionAsString()
    {
      return attributeDescription.toString();
    }



    public boolean isEmpty()
    {
      return true;
    }



    public Iterator<ByteString> iterator()
    {
      return Iterators.empty();
    }



    public boolean remove(ByteString value)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public boolean remove(Object object)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public boolean removeAll(Collection<?> objects)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public boolean retainAll(Collection<?> objects)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public int size()
    {
      return 0;
    }



    public ByteString[] toArray()
    {
      return new ByteString[0];
    }



    public <T> T[] toArray(T[] array) throws ArrayStoreException,
        NullPointerException
    {
      return Collections.emptyList().toArray(array);
    }
  }



  /**
   * Renamed attribute.
   */
  private static final class RenamedAttribute implements Attribute
  {

    private final Attribute attribute;
    private final AttributeDescription attributeDescription;



    private RenamedAttribute(Attribute attribute,
        AttributeDescription attributeDescription)
    {
      this.attribute = attribute;
      this.attributeDescription = attributeDescription;
    }



    public boolean add(ByteString value)
        throws UnsupportedOperationException, NullPointerException
    {
      return attribute.add(value);
    }



    public boolean addAll(Collection<? extends ByteString> values)
        throws UnsupportedOperationException, NullPointerException
    {
      return attribute.addAll(values);
    }



    public boolean addAllObjects(Collection<?> objects)
        throws UnsupportedOperationException, NullPointerException
    {
      return attribute.addAllObjects(objects);
    }



    public boolean addObject(Object object)
        throws UnsupportedOperationException, NullPointerException
    {
      return attribute.addObject(object);
    }



    public void clear() throws UnsupportedOperationException
    {
      attribute.clear();
    }



    public boolean contains(ByteString value)
        throws NullPointerException
    {
      return attribute.contains(value);
    }



    public boolean contains(Object object) throws NullPointerException
    {
      return attribute.contains(object);
    }



    public boolean containsAll(Collection<?> objects)
        throws NullPointerException
    {
      return attribute.containsAll(objects);
    }



    public AttributeDescription getAttributeDescription()
    {
      return attributeDescription;
    }



    public String getAttributeDescriptionAsString()
    {
      return attributeDescription.toString();
    }



    public boolean isEmpty()
    {
      return attribute.isEmpty();
    }



    public Iterator<ByteString> iterator()
    {
      return attribute.iterator();
    }



    public boolean remove(ByteString value)
        throws UnsupportedOperationException, NullPointerException
    {
      return attribute.remove(value);
    }



    public boolean remove(Object object)
        throws UnsupportedOperationException, NullPointerException
    {
      return attribute.remove(object);
    }



    public boolean removeAll(Collection<?> objects)
        throws UnsupportedOperationException, NullPointerException
    {
      return attribute.removeAll(objects);
    }



    public boolean retainAll(Collection<?> objects)
        throws UnsupportedOperationException, NullPointerException
    {
      return attribute.retainAll(objects);
    }



    public int size()
    {
      return attribute.size();
    }



    public ByteString[] toArray()
    {
      return attribute.toArray();
    }



    public <T> T[] toArray(T[] array) throws ArrayStoreException,
        NullPointerException
    {
      return attribute.toArray(array);
    }
  }



  /**
   * Unmodifiable attribute.
   */
  private static final class UnmodifiableAttribute implements Attribute
  {

    private final Attribute attribute;



    private UnmodifiableAttribute(Attribute attribute)
    {
      this.attribute = attribute;
    }



    public boolean add(ByteString value)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public boolean addAll(Collection<? extends ByteString> values)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public boolean addAllObjects(Collection<?> objects)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public boolean addObject(Object object)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public void clear() throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException();
    }



    public boolean contains(ByteString value)
        throws NullPointerException
    {
      return attribute.contains(value);
    }



    public boolean contains(Object object) throws NullPointerException
    {
      return attribute.contains(object);
    }



    public boolean containsAll(Collection<?> objects)
        throws NullPointerException
    {
      return attribute.containsAll(objects);
    }



    public AttributeDescription getAttributeDescription()
    {
      return attribute.getAttributeDescription();
    }



    public String getAttributeDescriptionAsString()
    {
      return attribute.getAttributeDescriptionAsString();
    }



    public boolean isEmpty()
    {
      return attribute.isEmpty();
    }



    public Iterator<ByteString> iterator()
    {
      return Iterators.unmodifiable(attribute.iterator());
    }



    public boolean remove(ByteString value)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public boolean remove(Object object)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public boolean removeAll(Collection<?> objects)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public boolean retainAll(Collection<?> objects)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    public int size()
    {
      return attribute.size();
    }



    public ByteString[] toArray()
    {
      return attribute.toArray();
    }



    public <T> T[] toArray(T[] array) throws ArrayStoreException,
        NullPointerException
    {
      return attribute.toArray(array);
    }
  }



  /**
   * Returns a read-only empty attribute having the specified attribute
   * description.
   *
   * @param attributeDescription
   *          The attribute description.
   * @return The empty attribute.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  public static final Attribute emptyAttribute(
      AttributeDescription attributeDescription)
      throws NullPointerException
  {
    return new EmptyAttribute(attributeDescription);
  }



  /**
   * Creates a new attribute having the same attribute description and
   * attribute values as {@code attribute}.
   *
   * @param attribute
   *          The attribute to be copied.
   * @return The new attribute.
   * @throws NullPointerException
   *           If {@code attribute} was {@code null}.
   */
  public static final Attribute newAttribute(Attribute attribute)
      throws NullPointerException
  {
    return new BasicAttribute(attribute);
  }



  /**
   * Creates a new attribute having the specified attribute description
   * and no attribute values.
   *
   * @param attributeDescription
   *          The attribute description.
   * @return The new attribute.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  public static final Attribute newAttribute(
      AttributeDescription attributeDescription)
      throws NullPointerException
  {
    return new BasicAttribute(attributeDescription);
  }



  /**
   * Creates a new attribute having the specified attribute description
   * and single attribute value.
   *
   * @param attributeDescription
   *          The attribute description.
   * @param value
   *          The single attribute value.
   * @return The new attribute.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code value} was
   *           {@code null}.
   */
  public static final Attribute newAttribute(
      AttributeDescription attributeDescription, ByteString value)
      throws NullPointerException
  {
    Attribute attribute = newAttribute(attributeDescription);
    attribute.add(value);
    return attribute;
  }



  /**
   * Creates a new attribute having the specified attribute description
   * and attribute values.
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
  public static final Attribute newAttribute(
      AttributeDescription attributeDescription, ByteString... values)
      throws NullPointerException
  {
    Attribute attribute = newAttribute(attributeDescription);
    attribute.addAll(Arrays.asList(values));
    return attribute;
  }



  /**
   * Creates a new attribute having the specified attribute description
   * and attribute values.
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
  public static final Attribute newAttribute(
      AttributeDescription attributeDescription,
      Collection<ByteString> values) throws NullPointerException
  {
    Attribute attribute = newAttribute(attributeDescription);
    attribute.addAll(values);
    return attribute;
  }



  /**
   * Creates a new attribute having the specified attribute description
   * and single attribute value.
   * <p>
   * If {@code object} is not an instance of {@code ByteString} then it
   * will be converted to one using its string representation.
   *
   * @param attributeDescription
   *          The attribute description.
   * @param object
   *          The single attribute value.
   * @return The new attribute.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code object} was
   *           {@code null}.
   */
  public static final Attribute newAttribute(
      AttributeDescription attributeDescription, Object object)
      throws NullPointerException
  {
    Attribute attribute = newAttribute(attributeDescription);
    attribute.addObject(object);
    return attribute;
  }



  /**
   * Creates a new attribute having the specified attribute description
   * and attribute values.
   * <p>
   * Any attribute value contained in {@code objects} which is not an
   * instances of {@code ByteString} will be converted to one using its
   * string representation.
   *
   * @param attributeDescription
   *          The attribute description.
   * @param objects
   *          The attribute values.
   * @return The new attribute.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code objects} was
   *           {@code null}.
   */
  public static final Attribute newAttribute(
      AttributeDescription attributeDescription, Object... objects)
      throws NullPointerException
  {
    Attribute attribute = newAttribute(attributeDescription);
    attribute.addAllObjects(Arrays.asList(objects));
    return attribute;
  }



  /**
   * Creates a new attribute having the same attribute description and
   * attribute values as {@code attribute} decoded using the provided
   * schema.
   *
   * @param attribute
   *          The attribute to be copied.
   * @param schema
   *          The schema to use for decoding {@code attribute}.
   * @return The new attribute.
   * @throws NullPointerException
   *           If {@code attribute} or {@code schema} was {@code null}.
   */
  public static final Attribute newAttribute(
      AttributeValueSequence attribute, Schema schema)
      throws NullPointerException
  {
    return new BasicAttribute(attribute, schema);
  }



  /**
   * Returns a view of {@code attribute} having a different attribute
   * description. All operations on the returned attribute
   * "pass-through" to the underlying attribute.
   *
   * @param attribute
   *          The attribute to be renamed.
   * @param attributeDescription
   *          The new attribute description for {@code attribute}, which
   *          must be compatible with {@code attribute}'s attribute
   *          description.
   * @return A renamed view of {@code attribute}.
   * @throws IllegalArgumentException
   *           If {@code attributeDescription} does not have the same
   *           attribute type as {@code attribute}'s attribute
   *           description.
   * @throws NullPointerException
   *           If {@code attribute} or {@code attributeDescription} was
   *           {@code null}.
   */
  public static final Attribute renameAttribute(Attribute attribute,
      AttributeDescription attributeDescription)
      throws IllegalArgumentException, NullPointerException
  {
    AttributeType oldType =
        attribute.getAttributeDescription().getAttributeType();
    AttributeType newType = attributeDescription.getAttributeType();

    // We could relax a bit by ensuring that they are both compatible
    // (e.g. one sub-type of another, or same equality matching rule,
    // etc).
    Validator.ensureTrue(oldType.equals(newType),
        "Old and new attribute type are not the same");

    return new RenamedAttribute(attribute, attributeDescription);
  }



  /**
   * Returns a read-only view of {@code attribute}. Query operations on
   * the returned attribute "read-through" to the underlying attribute,
   * and attempts to modify the returned attribute either directly or
   * indirectly via an iterator result in an {@code
   * UnsupportedOperationException}.
   *
   * @param attribute
   *          The attribute for which a read-only view is to be
   *          returned.
   * @return A read-only view of {@code attribute}.
   * @throws NullPointerException
   *           If {@code attribute} was {@code null}.
   */
  public static final Attribute unmodifiableAttribute(
      Attribute attribute) throws NullPointerException
  {
    return new UnmodifiableAttribute(attribute);
  }



  // Prevent instantiation.
  private Types()
  {
    // Nothing to do.
  }
}
