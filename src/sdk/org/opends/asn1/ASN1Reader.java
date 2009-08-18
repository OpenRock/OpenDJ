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
 *      Copyright 2006-2008 Sun Microsystems, Inc.
 */

package org.opends.asn1;



import java.io.Closeable;
import java.io.IOException;

import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;



/**
 * An interface for decoding ASN.1 elements from a data source.
 * <p>
 * Methods for creating {@link ASN1Reader}s are provided in the
 * {@link ASN1} class.
 */
public interface ASN1Reader extends Closeable
{

  /**
   * Closes this ASN.1 reader.
   *
   * @throws IOException
   *           If an error occurs while closing.
   */
  void close() throws IOException;



  /**
   * Indicates whether or not the next element can be read without
   * blocking.
   *
   * @return {@code true} if a complete element is available or {@code
   *         false} otherwise.
   * @throws IOException
   *           If an error occurs while trying to make the
   *           determination.
   */
  boolean elementAvailable() throws IOException;



  /**
   * Indicates whether or not the current stream, sequence, or set
   * contains another element. Note that this method may return {@code
   * true} even if a previous call to {@link #elementAvailable} returned
   * {@code false}, indicating that the current set or sequence contains
   * another element but an attempt to read that element may block. This
   * method will block if there is not enough data available to make the
   * determination (typically only the next element's type is required).
   *
   * @return {@code true} if the current stream, sequence, or set
   *         contains another element, or {@code false} if the end of
   *         the stream, sequence, set has been reached.
   * @throws IOException
   *           If an error occurs while trying to make the
   *           determination.
   */
  boolean hasNextElement() throws IOException;



  /**
   * Returns the data length of the next element without actually
   * reading it.
   *
   * @return The data length of the next element, or {@code -1} if the
   *         end of the stream, sequence, set has been reached.
   * @throws IOException
   *           If an error occurs while determining the length.
   */
  int peekLength() throws IOException;



  /**
   * Returns the type of the next element without actually reading it.
   *
   * @return The type of the next element, or {@code -1} if the end of
   *         the stream, sequence, set has been reached.
   * @throws IOException
   *           If an error occurs while determining the type.
   */
  byte peekType() throws IOException;



  /**
   * Reads the next element as a boolean having the Universal Boolean
   * ASN.1 type tag.
   *
   * @return The decoded boolean value.
   * @throws IOException
   *           If the element cannot be decoded as a boolean.
   */
  boolean readBoolean() throws IOException;



  /**
   * Reads the next element as a boolean having the provided type tag.
   *
   * @param type
   *          The expected type tag of the element.
   * @return The decoded boolean value.
   * @throws IOException
   *           If the element cannot be decoded as a boolean.
   */
  boolean readBoolean(byte type) throws IOException;



  /**
   * Finishes reading a sequence and discards any unread elements.
   *
   * @throws IOException
   *           If an error occurs while advancing to the end of the
   *           sequence.
   * @throws IllegalStateException
   *           If there is no sequence being read.
   */
  void readEndSequence() throws IOException, IllegalStateException;



  /**
   * Finishes reading a set and discards any unread elements.
   *
   * @throws IOException
   *           If an error occurs while advancing to the end of the set.
   * @throws IllegalStateException
   *           If there is no set being read.
   */
  void readEndSet() throws IOException, IllegalStateException;



  /**
   * Reads the next element as an enumerated having the Universal
   * Enumerated ASN.1 type tag.
   *
   * @return The decoded enumerated value.
   * @throws IOException
   *           If the element cannot be decoded as an enumerated value.
   */
  int readEnumerated() throws IOException;



  /**
   * Reads the next element as an enumerated having the provided type
   * tag.
   *
   * @param type
   *          The expected type tag of the element.
   * @return The decoded enumerated value.
   * @throws IOException
   *           If the element cannot be decoded as an enumerated value.
   */
  int readEnumerated(byte type) throws IOException;



  /**
   * Reads the next element as an integer having the Universal Integer
   * ASN.1 type tag.
   *
   * @return The decoded integer value.
   * @throws IOException
   *           If the element cannot be decoded as an integer.
   */
  long readInteger() throws IOException;



  /**
   * Reads the next element as an integer having the provided type tag.
   *
   * @param type
   *          The expected type tag of the element.
   * @return The decoded integer value.
   * @throws IOException
   *           If the element cannot be decoded as an integer.
   */
  long readInteger(byte type) throws IOException;



  /**
   * Reads the next element as a null element having the Universal Null
   * ASN.1 type tag.
   *
   * @throws IOException
   *           If the element cannot be decoded as a null element.
   */
  void readNull() throws IOException;



  /**
   * Reads the next element as a null element having the provided type
   * tag.
   *
   * @param type
   *          The expected type tag of the element.
   * @throws IOException
   *           If the element cannot be decoded as a null element.
   */
  void readNull(byte type) throws IOException;



  /**
   * Reads the next element as an octet string having the Universal
   * Octet String ASN.1 type tag and appends it to the provided
   * {@link ByteStringBuilder}.
   *
   * @return A reference to {@code builder}.
   * @throws IOException
   *           If the element cannot be decoded as an octet string.
   */
  ByteString readOctetString() throws IOException;



  /**
   * Reads the next element as an octet string having the provided type
   * tag.
   *
   * @param type
   *          The expected type tag of the element.
   * @return The decoded octet string represented using a
   *         {@link ByteString}.
   * @throws IOException
   *           If the element cannot be decoded as an octet string.
   */
  ByteString readOctetString(byte type) throws IOException;



  /**
   * Reads the next element as an octet string having the provided type
   * tag and appends it to the provided {@link ByteStringBuilder}.
   *
   * @param type
   *          The expected type tag of the element.
   * @param builder
   *          The {@link ByteStringBuilder} to append the octet string
   *          to.
   * @return A reference to {@code builder}.
   * @throws IOException
   *           If the element cannot be decoded as an octet string.
   */
  ByteStringBuilder readOctetString(byte type, ByteStringBuilder builder)
      throws IOException;



  /**
   * Reads the next element as an octet string having the Universal
   * Octet String ASN.1 type tag and appends it to the provided
   * {@link ByteStringBuilder}.
   *
   * @param builder
   *          The {@link ByteStringBuilder} to append the octet string
   *          to.
   * @return A reference to {@code builder}.
   * @throws IOException
   *           If the element cannot be decoded as an octet string.
   */
  ByteStringBuilder readOctetString(ByteStringBuilder builder)
      throws IOException;



  /**
   * Reads the next element as an octet string having the Universal
   * Octet String ASN.1 type tag and decodes the value as a UTF-8
   * encoded string.
   *
   * @return The decoded octet string as a UTF-8 encoded string.
   * @throws IOException
   *           If the element cannot be decoded as an octet string.
   */
  String readOctetStringAsString() throws IOException;



  /**
   * Reads the next element as an octet string having the provided type
   * tag and decodes the value as a UTF-8 encoded string.
   *
   * @param type
   *          The expected type tag of the element.
   * @return The decoded octet string as a UTF-8 encoded string.
   * @throws IOException
   *           If the element cannot be decoded as an octet string.
   */
  String readOctetStringAsString(byte type) throws IOException;



  /**
   * Reads the next element as a sequence having the Universal Sequence
   * ASN.1 type tag. All further reads will read the elements in the
   * sequence until {@link #readEndSequence()} is called.
   *
   * @throws IOException
   *           If the element cannot be decoded as a sequence.
   */
  void readStartSequence() throws IOException;



  /**
   * Reads the next element as a sequence having the provided type tag.
   * All further reads will read the elements in the sequence until
   * {@link #readEndSequence()} is called.
   *
   * @param type
   *          The expected type tag of the element.
   * @throws IOException
   *           If the element cannot be decoded as a sequence.
   */
  void readStartSequence(byte type) throws IOException;



  /**
   * Reads the next element as a set having the Universal Set ASN.1 type
   * tag. All further reads will read the elements in the set until
   * {@link #readEndSet()} is called.
   *
   * @throws IOException
   *           If the element cannot be decoded as a set.
   */
  void readStartSet() throws IOException;



  /**
   * Reads the next element as a set having the provided type tag. All
   * further reads will read the elements in the set until
   * {@link #readEndSet()} is called.
   *
   * @param type
   *          The expected type tag of the element.
   * @throws IOException
   *           If the element cannot be decoded as a set.
   */
  void readStartSet(byte type) throws IOException;



  /**
   * Skips the next element without decoding it.
   *
   * @return A reference to this ASN.1 reader.
   * @throws IOException
   *           If the next element could not be skipped.
   */
  ASN1Reader skipElement() throws IOException;
}
