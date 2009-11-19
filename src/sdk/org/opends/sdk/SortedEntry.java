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

package org.opends.sdk;



import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import org.opends.sdk.schema.Schema;
import org.opends.sdk.util.ByteString;
import org.opends.sdk.util.Validator;



/**
 * An implementation of the {@code Entry} interface which uses a {@code
 * SortedMap} for storing attributes. Attributes are returned in
 * ascending order of attribute description, with {@code objectClass}
 * first, then all user attributes, and finally any operational
 * attributes. All operations are supported by this implementation.
 */
public final class SortedEntry extends AbstractEntry
{
  private final SortedMap<AttributeDescription, Attribute> attributes =
      new TreeMap<AttributeDescription, Attribute>();

  private DN name;

  private final Schema schema;



  /**
   * Creates an empty sorted entry using the default schema and root
   * distinguished name.
   */
  public SortedEntry()
  {
    this(Schema.getDefaultSchema());
  }



  /**
   * Creates a sorted entry having the same distinguished name as the
   * provided attribute sequence and containing all of its attributes
   * and object classes.
   *
   * @param entry
   *          The attribute sequence to be copied.
   * @param schema
   *          The schema to use for decoding the name and attributes.
   * @throws IllegalArgumentException
   *           If {@code entry} could not be decoded successfully. For
   *           example, if its name is not a well-formed distinguished
   *           name, or if its attributes could not be decoded
   *           successfully using the provided schema.
   * @throws NullPointerException
   *           If {@code entry} or {@code schema} was {@code null}.
   */
  public SortedEntry(Entry entry, Schema schema)
      throws IllegalArgumentException
  {
    Validator.ensureNotNull(entry, schema);

    this.name = entry.getName();
    this.schema = schema;

    for (Attribute attribute : entry.getAttributes())
    {
      addAttribute(attribute);
    }
  }



  /**
   * Creates a sorted entry having the same schema, distinguished name,
   * attributes, and object classes of the provided entry.
   *
   * @param entry
   *          The entry to be copied.
   * @throws NullPointerException
   *           If {@code entry} was {@code null}.
   */
  public SortedEntry(Entry entry)
  {
    this(entry, entry.getSchema());
  }



  /**
   * Creates an empty sorted entry using the provided schema and root
   * distinguished name.
   *
   * @param schema
   *          The schema which this entry should use for decoding
   *          attribute types and distinguished names.
   * @throws NullPointerException
   *           If {@code schema} was {@code null}.
   */
  public SortedEntry(Schema schema) throws NullPointerException
  {
    Validator.ensureNotNull(schema);

    this.name = DN.rootDN();
    this.schema = schema;
  }



  /**
   * {@inheritDoc}
   */
  public boolean addAttribute(Attribute attribute,
      Collection<ByteString> duplicateValues)
      throws NullPointerException
  {
    Validator.ensureNotNull(attribute);

    if (!attribute.isEmpty())
    {
      AttributeDescription attributeDescription =
          attribute.getAttributeDescription();
      Attribute oldAttribute = attributes.get(attributeDescription);
      if (oldAttribute != null)
      {
        return oldAttribute.addAll(attribute, duplicateValues);
      }
      else
      {
        attributes.put(attributeDescription, attribute);
        return true;
      }
    }
    return false;
  }



  /**
   * {@inheritDoc}
   */
  public Entry clearAttributes()
  {
    attributes.clear();
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public boolean containsAttribute(
      AttributeDescription attributeDescription)
      throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescription);

    return attributes.containsKey(attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public Attribute getAttribute(
      AttributeDescription attributeDescription)
      throws NullPointerException
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
  public DN getName()
  {
    return name;
  }



  /**
   * {@inheritDoc}
   */
  public Schema getSchema()
  {
    return schema;
  }



  /**
   * {@inheritDoc}
   */
  public boolean removeAttribute(Attribute attribute,
      Collection<ByteString> missingValues) throws NullPointerException
  {
    Validator.ensureNotNull(attribute);

    AttributeDescription attributeDescription =
        attribute.getAttributeDescription();

    if (attribute.isEmpty())
    {
      return attributes.remove(attributeDescription) != null;
    }
    else
    {
      Attribute oldAttribute = attributes.get(attributeDescription);
      if (oldAttribute != null)
      {
        boolean modified =
            oldAttribute.removeAll(attribute, missingValues);
        if (oldAttribute.isEmpty())
        {
          attributes.remove(attributeDescription);
          return true;
        }
        return modified;
      }
      else
      {
        return false;
      }
    }
  }



  /**
   * {@inheritDoc}
   */
  public Entry setName(DN dn) throws NullPointerException
  {
    Validator.ensureNotNull(dn);
    this.name = dn;
    return this;
  }

}
