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



import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.opends.schema.ObjectClass;
import org.opends.schema.Schema;
import org.opends.server.types.ByteString;
import org.opends.util.Function;
import org.opends.util.Iterables;
import org.opends.util.Predicate;
import org.opends.util.Validator;



/**
 * An implementation of the {@code Entry} interface which uses a {@code
 * SortedMap} for storing attributes. Attributes are returned in
 * ascending order of attribute description, with {@code objectClass}
 * first, then all user attributes, and finally any operational
 * attributes. All operations are supported by this implementation.
 */
public final class SortedEntry implements Entry
{
  private final SortedMap<AttributeDescription, Attribute> attributes =
      new TreeMap<AttributeDescription, Attribute>();

  private DN name;

  private Attribute objectClassAttribute;

  // Predicate used for findAttributes.
  private static final Predicate<Attribute, AttributeDescription> FIND_ATTRIBUTES_PREDICATE =
      new Predicate<Attribute, AttributeDescription>()
        {

    public boolean matches(Attribute value, AttributeDescription p)
    {
      return value.getAttributeDescription().isSubTypeOf(p);
    }

  };

  // Function used for getObjectClasses
  private static final Function<ByteString, String, Void> BYTE_STRING_TO_STRING_FUNCTION =
      new Function<ByteString, String, Void>()
        {

    public String apply(ByteString value, Void p)
    {
      return value.toString();
    }

  };



  /**
   * Creates an empty sorted entry with no attributes, object classes,
   * and a default name (root DN).
   */
  public SortedEntry()
  {
    this.name = DN.rootDN();
    this.objectClassAttribute = null;
  }



  /**
   * Creates a sorted entry having the same name as the provided entry
   * and containing all of its attributes and object classes.
   *
   * @param entry
   *          The entry to be copied.
   * @throws NullPointerException
   *           If {@code entry} was {@code null}.
   */
  public SortedEntry(Entry entry)
  {
    Validator.ensureNotNull(entry);

    this.name = entry.getNameDN();
    for (Attribute attribute : entry.getAttributes())
    {
      addAttribute(attribute);
    }
  }



  /**
   * Creates a sorted entry having the same name as the provided
   * attribute sequence and containing all of its attributes and object
   * classes.
   *
   * @param entry
   *          The attribute sequence to be copied.
   * @param schema
   *          The schema to use for decoding the name and attributes.
   * @throws IllegalArgumentException
   *           If {@code entry} could not be decoded successfully. For
   *           example, if its name is not a well-formed distinguised
   *           name, or if its attributes could not be decoded
   *           successfully using the provided schema.
   * @throws NullPointerException
   *           If {@code entry} or {@code schema} was {@code null}.
   */
  public SortedEntry(AttributeSequence entry, Schema schema)
      throws IllegalArgumentException
  {
    Validator.ensureNotNull(entry, schema);

    if (entry instanceof Entry)
    {
      this.name = ((Entry) entry).getNameDN();
      for (Attribute attribute : ((Entry) entry).getAttributes())
      {
        addAttribute(attribute);
      }
    }
    else
    {
      this.name = DN.valueOf(entry.getName(), schema);
      for (AttributeValueSequence attribute : entry.getAttributes())
      {
        addAttribute(Types.newAttribute(attribute, schema));
      }
    }
  }



  /**
   * Adds the provided attribute to this entry, replacing any existing
   * attribute having the same attribute description.
   *
   * @param attribute
   *          The attribute to be added.
   * @return The previous attribute having the same attribute
   *         description, or {@code null} if there was no existing
   *         attribute with the same attribute description.
   * @throws NullPointerException
   *           If {@code attribute} was {@code null}.
   */
  public Attribute addAttribute(Attribute attribute)
      throws NullPointerException
  {
    Validator.ensureNotNull(attribute);

    if (attribute.getAttributeDescription().isObjectClass())
    {
      objectClassAttribute = attribute;
    }

    return attributes.put(attribute.getAttributeDescription(),
        attribute);
  }



  /**
   * Removes all the attributes from this entry, including the {@code
   * objectClass} attribute if present.
   *
   * @return This entry.
   */
  public Entry clearAttributes()
  {
    attributes.clear();
    objectClassAttribute = null;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public boolean containsAttribute(
      AttributeDescription attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);

    return attributes.containsKey(attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public boolean containsObjectClass(ObjectClass objectClass)
  {
    Validator.ensureNotNull(objectClass);

    return containsObjectClass(objectClass.getOID());
  }



  /**
   * {@inheritDoc}
   */
  public boolean containsObjectClass(String objectClass)
  {
    Validator.ensureNotNull(objectClass);

    if (objectClassAttribute == null)
    {
      return false;
    }
    else
    {
      return objectClassAttribute.contains(objectClass);
    }
  }



  /**
   * Returns an {@code Iterable} containing all the attributes in this
   * entry having an attribute description which is a sub-type of the
   * provided attribute description. The returned iterable supports the
   * iterator {@code remove()} operation.
   *
   * @param attributeDescription
   *          The name of the attributes to be returned.
   * @return An {@code Iterable} containing the matching attributes.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  public Iterable<Attribute> findAttributes(
      AttributeDescription attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);

    return Iterables.filter(
        attributes.values(), FIND_ATTRIBUTES_PREDICATE,
        attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public Attribute getAttribute(
      AttributeDescription attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);

    return attributes.get(attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public int getAttributeCount()
  {
    return attributes.size();
  }



  /**
   * Returns an {@code Iterable} containing the attributes in this
   * entry. The returned iterable supports the iterator {@code remove()}
   * operation.
   *
   * @return An {@code Iterable} containing the attributes.
   */
  public Iterable<Attribute> getAttributes()
  {
    return attributes.values();
  }



  /**
   * {@inheritDoc}
   */
  public DN getNameDN()
  {
    return name;
  }



  /**
   * {@inheritDoc}
   */
  public int getObjectClassCount()
  {
    return objectClassAttribute != null ? objectClassAttribute.size()
        : 0;
  }



  /**
   * Returns an unmodifiable {@code Iterable} containing the names of
   * the object classes in this entry. The returned iterable does not
   * support the iterator {@code remove()} operation.
   *
   * @return An {@code Iterable} containing the object classes.
   */
  public Iterable<String> getObjectClasses()
  {
    if (objectClassAttribute == null)
    {
      return Collections.emptyList();
    }
    else
    {
      // FIXME: support remove().
      return Iterables.transform(objectClassAttribute,
          BYTE_STRING_TO_STRING_FUNCTION);
    }
  }



  /**
   * {@inheritDoc}
   */
  public boolean hasAttributes()
  {
    return !attributes.isEmpty();
  }



  /**
   * {@inheritDoc}
   */
  public boolean hasObjectClasses()
  {
    return objectClassAttribute != null ? !objectClassAttribute
        .isEmpty() : false;
  }



  /**
   * Removes the named attribute from this entry.
   *
   * @param attributeDescription
   *          The name of the attribute to be removed.
   * @return The removed attribute, or {@code null} if the attribute is
   *         not included with this entry.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  public Attribute removeAttribute(
      AttributeDescription attributeDescription)
      throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescription);

    if (attributeDescription.isObjectClass())
    {
      objectClassAttribute = null;
    }

    return attributes.remove(attributeDescription);
  }



  /**
   * Sets the distinguished name of this entry.
   *
   * @param dn
   *          The distinguished name.
   * @return This entry.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  public Entry setNameDN(DN dn) throws
      NullPointerException
  {
    Validator.ensureNotNull(dn);

    name = dn;
    return this;
  }



  /**
   * Returns a string representation of this entry.
   *
   * @return The string representation of this entry.
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("Entry(");
    builder.append(name);
    builder.append(", {");

    boolean firstValue = true;
    for (Attribute attribute : attributes.values())
    {
      if (!firstValue)
      {
        builder.append(", ");
      }

      builder.append(attribute);
      firstValue = false;
    }

    builder.append("})");
    return builder.toString();
  }

}
