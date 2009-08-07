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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.opends.server.types.ByteString;
import org.opends.util.Validator;



/**
 * A modification to be performed on an entry during a Modify operation.
 */
public final class Change implements AttributeValueSequence
{
  private final String attributeDescription;
  private final ModificationType type;
  private final List<ByteString> values;



  /**
   * Creates a new change using the provided attribute value sequence.
   *
   * @param type
   *          The type of change to be performed.
   * @param attribute
   *          The attribute name and values to be modified.
   * @throws NullPointerException
   *           If {@code type} or {@code attribute} was {@code null}.
   */
  public Change(ModificationType type, AttributeValueSequence attribute)
      throws NullPointerException
  {
    Validator.ensureNotNull(type, attribute);

    this.type = type;
    this.attributeDescription =
        attribute.getAttributeDescriptionString();

    List<ByteString> values =
        new ArrayList<ByteString>(attribute.size());
    for (ByteString value : attribute)
    {
      values.add(value);
    }
    this.values = Collections.unmodifiableList(values);
  }



  /**
   * Creates a new change using the provided attribute description and
   * no values.
   *
   * @param type
   *          The type of change to be performed.
   * @param attributeDescription
   *          The name of the attribute to be modified.
   * @throws NullPointerException
   *           If {@code type} or {@code attributeDescription} was
   *           {@code null}.
   */
  public Change(ModificationType type, String attributeDescription)
      throws NullPointerException
  {
    Validator.ensureNotNull(type, attributeDescription);

    this.type = type;
    this.attributeDescription = attributeDescription;
    this.values = Collections.emptyList();
  }



  /**
   * Creates a new change using the provided attribute description and
   * single value.
   *
   * @param type
   *          The type of change to be performed.
   * @param attributeDescription
   *          The name of the attribute to be modified.
   * @param value
   *          The attribute value to be modified.
   * @throws NullPointerException
   *           If {@code type}, {@code attributeDescription}, or {@code
   *           value} was {@code null}.
   */
  public Change(ModificationType type, String attributeDescription,
      ByteString value) throws NullPointerException
  {
    Validator.ensureNotNull(type, attributeDescription, value);

    this.type = type;
    this.attributeDescription = attributeDescription;
    this.values = Collections.singletonList(value);
  }



  /**
   * Creates a new change using the provided attribute description and
   * values.
   *
   * @param type
   *          The type of change to be performed.
   * @param attributeDescription
   *          The name of the attribute to be modified.
   * @param firstValue
   *          The first attribute value to be modified.
   * @param remainingValues
   *          The remaining attribute values to be modified.
   * @throws NullPointerException
   *           If {@code type}, {@code attributeDescription}, or {@code
   *           firstValue} was {@code null}, or if {@code
   *           remainingValues} contains a {@code null} element.
   */
  public Change(ModificationType type, String attributeDescription,
      ByteString firstValue, ByteString... remainingValues)
      throws NullPointerException
  {
    Validator.ensureNotNull(type, attributeDescription, firstValue,
        remainingValues);

    this.type = type;
    this.attributeDescription = attributeDescription;

    int sz = 1 + remainingValues.length;
    List<ByteString> values = new ArrayList<ByteString>(sz);
    values.add(firstValue);
    for (ByteString value : remainingValues)
    {
      values.add(value);
    }
    this.values = Collections.unmodifiableList(values);
  }



  /**
   * Creates a new change using the provided attribute description and
   * values.
   *
   * @param type
   *          The type of change to be performed.
   * @param attributeDescription
   *          The name of the attribute to be modified.
   * @param values
   *          The attribute values to be modified.
   * @throws NullPointerException
   *           If {@code type}, {@code attributeDescription}, or {@code
   *           values} was {@code null}.
   */
  public Change(ModificationType type, String attributeDescription,
      Collection<ByteString> values) throws NullPointerException
  {
    Validator.ensureNotNull(type, attributeDescription, values);

    this.type = type;
    this.attributeDescription = attributeDescription;

    int sz = values.size();
    if (sz == 0)
    {
      this.values = Collections.emptyList();
    }
    else if (sz == 1)
    {
      this.values = Collections.singletonList(values.iterator().next());
    }
    else
    {
      List<ByteString> tmp = new ArrayList<ByteString>(sz);
      for (ByteString value : values)
      {
        tmp.add(value);
      }
      this.values = Collections.unmodifiableList(tmp);
    }
  }



  /**
   * Creates a new change using the provided attribute description and
   * single value.
   *
   * @param type
   *          The type of change to be performed.
   * @param attributeDescription
   *          The name of the attribute to be modified.
   * @param value
   *          The attribute value to be modified.
   * @throws NullPointerException
   *           If {@code type}, {@code attributeDescription}, or {@code
   *           value} was {@code null}.
   */
  public Change(ModificationType type, String attributeDescription,
      String value) throws NullPointerException
  {
    Validator.ensureNotNull(type, attributeDescription, value);

    this.type = type;
    this.attributeDescription = attributeDescription;
    this.values = Collections.singletonList(ByteString.valueOf(value));
  }



  /**
   * Creates a new change using the provided attribute description and
   * values.
   *
   * @param type
   *          The type of change to be performed.
   * @param attributeDescription
   *          The name of the attribute to be modified.
   * @param firstValue
   *          The first attribute value to be modified.
   * @param remainingValues
   *          The remaining attribute values to be modified.
   * @throws NullPointerException
   *           If {@code type}, {@code attributeDescription}, or {@code
   *           firstValue} was {@code null}, or if {@code
   *           remainingValues} contains a {@code null} element.
   */
  public Change(ModificationType type, String attributeDescription,
      String firstValue, String... remainingValues)
      throws NullPointerException
  {
    Validator.ensureNotNull(type, attributeDescription, firstValue,
        remainingValues);

    this.type = type;
    this.attributeDescription = attributeDescription;

    int sz = 1 + remainingValues.length;
    List<ByteString> values = new ArrayList<ByteString>(sz);
    values.add(ByteString.valueOf(firstValue));
    for (String value : remainingValues)
    {
      values.add(ByteString.valueOf(value));
    }
    this.values = Collections.unmodifiableList(values);
  }



  /**
   * {@inheritDoc}
   */
  public String getAttributeDescriptionString()
  {
    return attributeDescription;
  }



  /**
   * Returns the type of change to be performed.
   *
   * @return The type of change to be performed.
   */
  public ModificationType getModificationType()
  {
    return type;
  }



  /**
   * {@inheritDoc}
   */
  public boolean isEmpty()
  {
    return values.isEmpty();
  }



  /**
   * {@inheritDoc}
   */
  public Iterator<ByteString> iterator()
  {
    return values.iterator();
  }



  /**
   * {@inheritDoc}
   */
  public int size()
  {
    return values.size();
  }



  /**
   * {@inheritDoc}
   */
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    return toString(builder).toString();
  }



  /**
   * {@inheritDoc}
   */
  public StringBuilder toString(StringBuilder builder)
  {
    builder.append("Change(modificationType=");
    builder.append(type);
    builder.append(", attributeDescription=");
    builder.append(attributeDescription);
    builder.append(", attributeValues=");
    builder.append(values);
    builder.append(")");
    return builder;
  }

}
