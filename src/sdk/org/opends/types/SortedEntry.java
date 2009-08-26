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



import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.opends.schema.ObjectClass;
import org.opends.schema.Schema;
import org.opends.server.types.ByteString;
import org.opends.util.FilteredIterable;
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
  private final Schema schema;

  private final SortedMap<AttributeDescription, Attribute> attributes =
      new TreeMap<AttributeDescription, Attribute>();

  private DN name = DN.rootDN();

  private final List<ObjectClass> objectClasses =
      new LinkedList<ObjectClass>();

  // Predicate used for findAttributes.
  private static final Predicate<Attribute, AttributeDescription> FIND_ATTRIBUTES_PREDICATE =
      new Predicate<Attribute, AttributeDescription>()
      {

        public boolean matches(AttributeDescription p, Attribute value)
        {
          return value.getAttributeDescription().isSubTypeOf(p);
        }

      };



  /**
   * Creates an empty entry with the provided schema and default name
   * (root DN).
   *
   * @param schema
   *          The schema to use when looking up attribute types and
   *          object classes.
   * @throws NullPointerException
   *           If {@code schema} was {@code null}.
   */
  public SortedEntry(Schema schema) throws NullPointerException
  {
    Validator.ensureNotNull(schema);

    this.schema = schema;
  }



  /**
   * {@inheritDoc}
   */
  public Attribute addAttribute(Attribute attribute)
      throws UnsupportedOperationException, NullPointerException
  {
    Validator.ensureNotNull(attribute);

    if (attribute.getAttributeDescription().isObjectClass())
    {
      // Need to update object classes.
      objectClasses.clear();
      for (ByteString value : attribute)
      {
        String oid = value.toString();

        // FIXME: the schema should create a default object class on
        // demand. Right now this could yield an NPE.
        objectClasses.add(schema.getObjectClass(oid));
      }
    }

    return attributes.put(attribute.getAttributeDescription(),
        attribute);
  }



  /**
   * {@inheritDoc}
   */
  public boolean addObjectClass(ObjectClass objectClass)
      throws UnsupportedOperationException, NullPointerException
  {
    Validator.ensureNotNull(objectClass);

    if (objectClasses.contains(objectClass))
    {
      return false;
    }
    else
    {
      objectClasses.add(objectClass);

      AttributeDescription attributeDescription =
          AttributeDescription.objectClass();
      String value = objectClass.getNameOrOID();

      Attribute attribute = attributes.get(attributeDescription);
      if (attribute == null)
      {
        attribute = Attribute.create(attributeDescription, value);
      }
      else
      {
        attribute = Attribute.add(attribute, value);
      }
      attributes.put(attribute.getAttributeDescription(), attribute);

      return true;
    }
  }



  /**
   * {@inheritDoc}
   */
  public Entry clearAttributes() throws UnsupportedOperationException
  {
    attributes.clear();
    objectClasses.clear();
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

    return objectClasses.contains(objectClass);
  }



  /**
   * {@inheritDoc}
   */
  public Iterable<Attribute> findAttributes(
      AttributeDescription attributeDescription)
  {
    return new FilteredIterable<Attribute, AttributeDescription>(
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
   * {@inheritDoc}
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
    return objectClasses.size();
  }



  /**
   * {@inheritDoc}
   */
  public Iterable<ObjectClass> getObjectClasses()
  {
    // TODO Auto-generated method stub
    return null;
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
    return !objectClasses.isEmpty();
  }



  /**
   * {@inheritDoc}
   */
  public Attribute removeAttribute(
      AttributeDescription attributeDescription)
      throws UnsupportedOperationException, NullPointerException
  {
    Validator.ensureNotNull(attributeDescription);

    if (attributeDescription.isObjectClass())
    {
      // Need to update object classes.
      objectClasses.clear();
    }

    return attributes.remove(attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public boolean removeObjectClass(ObjectClass objectClass)
      throws UnsupportedOperationException, NullPointerException
  {
    Validator.ensureNotNull(objectClass);

    if (objectClasses.remove(objectClass))
    {
      Attribute attribute =
          attributes.get(AttributeDescription.objectClass());
      if (attribute != null)
      {
        attribute =
            Attribute.subtract(attribute, objectClass.getNameOrOID());
        attributes.put(attribute.getAttributeDescription(), attribute);
      }
      return true;
    }
    else
    {
      return false;
    }
  }



  /**
   * {@inheritDoc}
   */
  public Entry setNameDN(DN dn) throws UnsupportedOperationException,
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
