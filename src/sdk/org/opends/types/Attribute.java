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



import java.util.Iterator;

import org.opends.server.types.ByteString;



/**
 * An LDAP attribute.
 */
public interface Attribute extends Iterable<ByteString>
{

  /**
   * Adds the provided value to this attribute.
   *
   * @param value
   *          The value to be added to this attribute.
   * @return This attribute.
   * @throws UnsupportedOperationException
   *           If this attribute does not permit values to be added.
   * @throws NullPointerException
   *           If {@code value} was {@code null}.
   */
  Attribute add(ByteString value) throws UnsupportedOperationException,
      NullPointerException;



  /**
   * Adds the provided value to this attribute.
   *
   * @param value
   *          The value to be added to this attribute.
   * @return This attribute.
   * @throws UnsupportedOperationException
   *           If this attribute does not permit values to be added.
   * @throws NullPointerException
   *           If {@code value} was {@code null}.
   */
  Attribute add(String value) throws UnsupportedOperationException,
      NullPointerException;



  /**
   * Removes all the values included with this attribute.
   *
   * @return This attribute.
   * @throws UnsupportedOperationException
   *           If this attribute does not permit values to be removed.
   */
  Attribute clear() throws UnsupportedOperationException;



  /**
   * Removes the specified value from this attribute if it is present.
   *
   * @param value
   *          The value to be removed.
   * @return {@code true} if the value was removed.
   * @throws UnsupportedOperationException
   *           If this attribute does not permit values to be removed.
   * @throws NullPointerException
   *           If {@code value} was {@code null}.
   */
  boolean remove(ByteString value)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Returns the attribute description associated with this attribute.
   * This includes the attribute name and any attribute options.
   *
   * @return The attribute description associated with this attribute.
   */
  String getAttributeDescription();



  /**
   * Indicates whether or not this attribute has any values.
   *
   * @return {@code true} if this attribute does not contain any values,
   *         otherwise {@code false}.
   */
  boolean isEmpty();



  /**
   * Returns an {@code Iterator} over the attribute values in this
   * attribute. The attribute values are returned in the order in which
   * they were added to this attribute. The returned {@code Iterator}
   * may be used to remove values if permitted by this attribute.
   *
   * @return An {@code Iterator} over the attribute values in this
   *         attribute.
   */
  Iterator<ByteString> iterator();



  /**
   * Returns the number of attribute values in this attribute.
   *
   * @return The number of attribute values in this attribute.
   */
  int size();



  /**
   * Returns a string representation of this attribute.
   *
   * @return A string representation of this attribute.
   */
  String toString();



  /**
   * Appends a string representation of this attribute to the provided
   * buffer.
   *
   * @param builder
   *          The builder into which a string representation of this
   *          attribute should be appended.
   * @throws NullPointerException
   *           If {@code builder} was {@code null}.
   */
  void toString(StringBuilder builder) throws NullPointerException;
}
