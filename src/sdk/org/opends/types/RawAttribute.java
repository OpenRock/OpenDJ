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



import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.opends.asn1.ASN1Reader;
import org.opends.asn1.ASN1Writer;
import org.opends.server.types.ByteString;



/**
 * An immutable raw LDAP attribute.
 */
public final class RawAttribute implements Iterable<ByteString>
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
          int oldIndex = index;
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
   * Reads the next ASN.1 element from the provided {@code ASN1Reader}
   * as a {@code RawAttribute}. The element must contain at least one
   * attribute value.
   *
   * @param reader
   *          The {@code ASN1Reader} from which the ASN.1 encoded
   *          {@code RawAttribute} should be read.
   * @return The decoded {@code RawAttribute}.
   * @throws IOException
   *           If an error occurs while reading from {@code reader}.
   */
  public static RawAttribute decodeAttribute(ASN1Reader reader)
      throws IOException
  {
    reader.readStartSequence();
    String attributeDescription = reader.readOctetStringAsString();
    reader.readStartSet();

    ByteString singleValue = reader.readOctetString();
    ByteString[] multipleValues = null;

    if (reader.hasNextElement())
    {
      List<ByteString> vlist = new LinkedList<ByteString>();
      vlist.add(singleValue);
      singleValue = null;
      do
      {
        vlist.add(reader.readOctetString());
      }
      while (reader.hasNextElement());
      multipleValues = vlist.toArray(new ByteString[0]);
    }

    reader.readEndSet();
    reader.readEndSequence();

    return new RawAttribute(attributeDescription, singleValue,
        multipleValues);
  }



  /**
   * Reads the next ASN.1 element from the provided {@code ASN1Reader}
   * as a {@code RawAttribute}. The element may not contain any
   * attribute values.
   *
   * @param reader
   *          The {@code ASN1Reader} from which the ASN.1 encoded
   *          {@code RawAttribute} should be read.
   * @return The decoded {@code RawAttribute}.
   * @throws IOException
   *           If an error occurs while reading from {@code reader}.
   */
  public static RawAttribute decodePartialAttribute(ASN1Reader reader)
      throws IOException
  {
    reader.readStartSequence();
    String attributeDescription = reader.readOctetStringAsString();
    reader.readStartSet();

    ByteString singleValue = null;
    ByteString[] multipleValues = null;

    if (reader.hasNextElement())
    {
      singleValue = reader.readOctetString();

      if (reader.hasNextElement())
      {
        List<ByteString> vlist = new LinkedList<ByteString>();
        vlist.add(singleValue);
        singleValue = null;
        do
        {
          vlist.add(reader.readOctetString());
        }
        while (reader.hasNextElement());
        multipleValues = vlist.toArray(new ByteString[0]);
      }
    }

    reader.readEndSet();
    reader.readEndSequence();

    return new RawAttribute(attributeDescription, singleValue,
        multipleValues);
  }



  /**
   * Creates a {@code RawAttribute} having the provided attribute
   * description and list of values.
   *
   * @param attributeDescription
   *          The attribute description.
   * @param values
   *          The list of attribute values, which may be empty or
   *          {@code null}.
   * @return The {@code RawAttribute}.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  public static RawAttribute newRawAttribute(
      String attributeDescription, ByteString... values)
      throws NullPointerException
  {
    if (attributeDescription == null)
    {
      throw new NullPointerException();
    }

    if (values == null)
    {
      return new RawAttribute(attributeDescription, null, null);
    }

    int sz = values.length;
    switch (sz)
    {
    case 0:
      return new RawAttribute(attributeDescription, null, null);
    case 1:
      return new RawAttribute(attributeDescription, values[0], null);
    default:
      return new RawAttribute(attributeDescription, null, Arrays
          .copyOf(values, sz));
    }
  }



  /**
   * Creates a {@code RawAttribute} having the provided attribute
   * description and list of values.
   *
   * @param attributeDescription
   *          The attribute description.
   * @param values
   *          The list of attribute values, which may be empty or
   *          {@code null}.
   * @return The {@code RawAttribute}.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  public static RawAttribute newRawAttribute(
      String attributeDescription, String... values)
      throws NullPointerException
  {
    if (attributeDescription == null)
    {
      throw new NullPointerException();
    }

    if (values == null)
    {
      return new RawAttribute(attributeDescription, null, null);
    }

    int sz = values.length;
    switch (sz)
    {
    case 0:
      return new RawAttribute(attributeDescription, null, null);
    case 1:
      return new RawAttribute(attributeDescription, ByteString
          .valueOf(values[0]), null);
    default:
      ByteString[] tmp = new ByteString[sz];
      for (int i = 0; i < sz; i++)
      {
        tmp[i] = ByteString.valueOf(values[i]);
      }
      return new RawAttribute(attributeDescription, null, tmp);
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
      ByteString singlevalue, ByteString[] multipleValues)
  {
    this.attributeDescription = attributeDescription;
    this.singleValue = singlevalue;
    this.multipleValues = multipleValues;
  }



  /**
   * Writes the ASN.1 encoding of this {@code RawAttribute} to the
   * provided {@code ASN1Writer}.
   *
   * @param writer
   *          The {@code ASN1Writer} to which the ASN.1 encoding of this
   *          {@code RawAttribute} should be written.
   * @return The updated {@code ASN1Writer}.
   * @throws IOException
   *           If an error occurs while writing to the {@code writer}.
   */
  public ASN1Writer encode(ASN1Writer writer) throws IOException
  {
    writer.writeStartSequence();
    writer.writeOctetString(attributeDescription);

    writer.writeStartSet();
    if (singleValue != null)
    {
      writer.writeOctetString(singleValue);
    }
    else if (multipleValues != null)
    {
      for (ByteString value : multipleValues)
      {
        writer.writeOctetString(value);
      }
    }
    writer.writeEndSet();
    writer.writeEndSequence();
    return writer;
  }



  /**
   * Returns the attribute description associated with this {@code
   * RawAttribute}. This includes the attribute name and any attribute
   * options.
   *
   * @return The attribute description associated with this {@code
   *         RawAttribute}.
   */
  public String getAttributeDescription()
  {
    return attributeDescription;
  }



  /**
   * Indicates whether or not this {@code RawAttribute} has any values.
   *
   * @return {@code true} if this {@code RawAttribute} does not contain
   *         any values, otherwise {@code false}.
   */
  public boolean isEmpty()
  {
    return (size() == 0);
  }



  /**
   * Returns an {@code Iterator} over the values in this {@code
   * RawAttribute}. The values are returned in the order in which they
   * were added to this {@code RawAttribute}. Attempts to use the
   * iterator's {@code remove()} method will fail throwing an {@code
   * UnsupportedOperationException}.
   *
   * @return An {@code Iterator} over the attribute values in this
   *         {@code RawAttribute}.
   */
  public Iterator<ByteString> iterator()
  {
    return new IteratorImpl();
  }



  /**
   * Returns the number of attribute values in this {@code RawAttribute}
   * .
   *
   * @return The number of attribute values in this {@code RawAttribute}
   *         .
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
   * Returns a string representation of this {@code RawAttribute}.
   *
   * @return A string representation of this {@code RawAttribute}.
   */
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    return toString(builder).toString();
  }



  /**
   * Appends a string representation of this {@code RawAttribute} to the
   * provided {@code StringBuilder}.
   *
   * @param builder
   *          The {@code StringBuilder} into which a string
   *          representation of this {@code RawAttribute} should be
   *          appended.
   * @return The updated {@code StringBuilder}.
   * @throws NullPointerException
   *           If {@code builder} was {@code null}.
   */
  public StringBuilder toString(StringBuilder builder)
      throws NullPointerException
  {
    builder.append("RawAttribute(attributeDescription=");
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
