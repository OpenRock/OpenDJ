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

package org.opends.sdk.responses;



import java.util.Collection;

import org.opends.sdk.AttributeSequence;
import org.opends.sdk.AttributeValueSequence;
import org.opends.sdk.controls.Control;
import org.opends.sdk.util.ByteString;



/**
 * A Search Result Entry represents an entry found during a Search
 * operation.
 * <p>
 * Each entry returned in a Search Result Entry will contain all
 * appropriate attributes as specified in the Search request, subject to
 * access control and other administrative policy.
 * <p>
 * Note that a Search Result Entry may hold zero attributes. This may
 * happen when none of the attributes of an entry were requested or
 * could be returned.
 * <p>
 * Note also that each returned attribute may hold zero attribute
 * values. This may happen when only attribute types are requested,
 * access controls prevent the return of values, or other reasons.
 */
public interface SearchResultEntry extends Response, AttributeSequence
{

  /**
   * {@inheritDoc}
   */
  SearchResultEntry addControl(Control control)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  SearchResultEntry clearControls()
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  Control getControl(String oid) throws NullPointerException;



  /**
   * {@inheritDoc}
   */
  Iterable<Control> getControls();



  /**
   * {@inheritDoc}
   */
  boolean hasControls();



  /**
   * {@inheritDoc}
   */
  Control removeControl(String oid)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Ensures that this search result entry contains the provided
   * attribute values. Any existing values for the attribute will be
   * retained.
   *
   * @param attribute
   *          The attribute to be added, which may be empty.
   * @return This search result entry.
   * @throws UnsupportedOperationException
   *           If this search result entry does not permit attributes to
   *           be added.
   * @throws NullPointerException
   *           If {@code attribute} was {@code null}.
   */
  SearchResultEntry addAttribute(AttributeValueSequence attribute)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Ensures that this search result entry contains the provided
   * attribute. Any existing values for the attribute will be retained.
   *
   * @param attributeDescription
   *          The name of the attribute to be added.
   * @return This search result entry.
   * @throws UnsupportedOperationException
   *           If this search result entry does not permit attributes to
   *           be added.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  SearchResultEntry addAttribute(String attributeDescription)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Ensures that this search result entry contains the provided
   * attribute values. Any existing values for the attribute will be
   * retained.
   * <p>
   * Any attribute values which are not instances of {@code ByteString}
   * will be converted using the {@link ByteString#valueOf(Object)}
   * method.
   *
   * @param attributeDescription
   *          The name of the attribute to be added.
   * @param values
   *          The values of the attribute to be added, which may be
   *          empty.
   * @return This search result entry.
   * @throws UnsupportedOperationException
   *           If this search result entry does not permit attributes to
   *           be added.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code values} was
   *           {@code null}.
   */
  SearchResultEntry addAttribute(String attributeDescription,
      Collection<?> values) throws UnsupportedOperationException,
      NullPointerException;



  /**
   * Ensures that this search result entry contains the provided
   * attribute value. Any existing values for the attribute will be
   * retained.
   * <p>
   * If the attribute value is not an instance of {@code ByteString}
   * then it will be converted using the
   * {@link ByteString#valueOf(Object)} method.
   *
   * @param attributeDescription
   *          The name of the attribute to be added.
   * @param value
   *          The value of the attribute to be added.
   * @return This search result entry.
   * @throws UnsupportedOperationException
   *           If this search result entry does not permit attributes to
   *           be added.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code value} was
   *           {@code null}.
   */
  SearchResultEntry addAttribute(String attributeDescription,
      Object value) throws UnsupportedOperationException,
      NullPointerException;



  /**
   * Ensures that this search result entry contains the provided
   * attribute values. Any existing values for the attribute will be
   * retained.
   * <p>
   * Any attribute values which are not instances of {@code ByteString}
   * will be converted using the {@link ByteString#valueOf(Object)}
   * method.
   *
   * @param attributeDescription
   *          The name of the attribute to be added.
   * @param values
   *          The values of the attribute to be added.
   * @return This search result entry.
   * @throws UnsupportedOperationException
   *           If this search result entry does not permit attributes to
   *           be added.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code values} was
   *           {@code null}.
   */
  SearchResultEntry addAttribute(String attributeDescription,
      Object... values) throws UnsupportedOperationException,
      NullPointerException;



  /**
   * Removes all the attributes from this search result entry.
   *
   * @return This search result entry.
   * @throws UnsupportedOperationException
   *           If this search result entry does not permit attributes to
   *           be removed.
   */
  SearchResultEntry clearAttributes()
      throws UnsupportedOperationException;



  /**
   * Gets the named attribute from this search result entry.
   *
   * @param attributeDescription
   *          The name of the attribute to be returned.
   * @return The named attribute, or {@code null} if it is not included
   *         with this search result entry.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  AttributeValueSequence getAttribute(String attributeDescription)
      throws NullPointerException;



  /**
   * Returns the number of attribute in this search result entry.
   *
   * @return The number of attributes.
   */
  int getAttributeCount();



  /**
   * Returns an {@code Iterable} containing the attributes included in
   * this search result entry. The returned {@code Iterable} may be used
   * to remove attributes if permitted by this search result entry.
   *
   * @return An {@code Iterable} containing the attributes.
   */
  Iterable<? extends AttributeValueSequence> getAttributes();



  /**
   * Returns the distinguished name of this search result entry.
   *
   * @return The distinguished name of this search result entry.
   */
  String getName();



  /**
   * Indicates whether or not this search result entry has any
   * attributes.
   *
   * @return {@code true} if this search result entry has any
   *         attributes, otherwise {@code false}.
   */
  boolean hasAttributes();



  /**
   * Removes the named attribute from this search result entry.
   *
   * @param attributeDescription
   *          The name of the attribute to be removed.
   * @return This search result entry.
   * @throws UnsupportedOperationException
   *           If this search result entry does not permit attributes to
   *           be removed.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  SearchResultEntry removeAttribute(String attributeDescription)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Sets the distinguished name of this search result entry.
   *
   * @param dn
   *          The distinguished name of this search result entry.
   * @return This search result entry.
   * @throws UnsupportedOperationException
   *           If this search result entry does not permit the
   *           distinguished name to be set.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  SearchResultEntry setName(String dn)
      throws UnsupportedOperationException, NullPointerException;

}
