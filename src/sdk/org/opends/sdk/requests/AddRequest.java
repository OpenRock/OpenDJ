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

package org.opends.sdk.requests;



import java.util.Collection;

import org.opends.sdk.AttributeSequence;
import org.opends.sdk.AttributeValueSequence;
import org.opends.sdk.controls.Control;
import org.opends.sdk.ldif.ChangeRecord;
import org.opends.sdk.ldif.ChangeRecordVisitor;
import org.opends.sdk.util.ByteString;



/**
 * The Add operation allows a client to request the addition of an entry
 * into the Directory.
 * <p>
 * The RDN attribute(s) may or may not be included in the Add request.
 * NO-USER-MODIFICATION attributes such as the {@code createTimestamp}
 * or {@code creatorsName} attributes must not be included, since the
 * server maintains these automatically.
 */
public interface AddRequest extends Request, AttributeSequence, ChangeRecord
{

  /**
   * {@inheritDoc}
   */
  <R, P> R accept(ChangeRecordVisitor<R, P> v, P p);



  /**
   * {@inheritDoc}
   */
  AddRequest addControl(Control control)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  AddRequest clearControls() throws UnsupportedOperationException;



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
   * Ensures that the entry to be added by this add request contains the
   * provided attribute values. Any existing values for the attribute
   * will be retained.
   *
   * @param attribute
   *          The attribute to be added, which must not be empty.
   * @return This add request.
   * @throws UnsupportedOperationException
   *           If this add request does not permit attributes to be
   *           added.
   * @throws IllegalArgumentException
   *           If {@code attribute} was empty.
   * @throws NullPointerException
   *           If {@code attribute} was {@code null}.
   */
  AddRequest addAttribute(AttributeValueSequence attribute)
      throws UnsupportedOperationException, IllegalArgumentException,
      NullPointerException;



  /**
   * Ensures that the entry to be added by this add request contains the
   * provided attribute value. Any existing values for the attribute
   * will be retained.
   * <p>
   * If the attribute value is not an instance of {@code ByteString}
   * then it will be converted using the
   * {@link ByteString#valueOf(Object)} method.
   *
   * @param attributeDescription
   *          The name of the attribute to be added.
   * @param value
   *          The value of the attribute to be added.
   * @return This add request.
   * @throws UnsupportedOperationException
   *           If this add request does not permit attributes to be
   *           added.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code value} was
   *           {@code null}.
   */
  AddRequest addAttribute(String attributeDescription, Object value)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Ensures that the entry to be added by this add request contains the
   * provided attribute values. Any existing values for the attribute
   * will be retained.
   * <p>
   * Any attribute values which are not instances of {@code ByteString}
   * will be converted using the {@link ByteString#valueOf(Object)}
   * method.
   *
   * @param attributeDescription
   *          The name of the attribute to be added.
   * @param values
   *          The values of the attribute to be added, which must not be
   *          empty.
   * @return This add request.
   * @throws UnsupportedOperationException
   *           If this add request does not permit attributes to be
   *           added.
   * @throws IllegalArgumentException
   *           If {@code values} was empty.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code values} was
   *           {@code null}.
   */
  AddRequest addAttribute(String attributeDescription,
      Collection<?> values) throws UnsupportedOperationException,
      IllegalArgumentException, NullPointerException;



  /**
   * Ensures that the entry to be added by this add request contains the
   * provided attribute values. Any existing values for the attribute
   * will be retained.
   * <p>
   * Any attribute values which are not instances of {@code ByteString}
   * will be converted using the {@link ByteString#valueOf(Object)}
   * method.
   *
   * @param attributeDescription
   *          The name of the attribute to be added.
   * @param firstValue
   *          The first value of the attribute to be added.
   * @param remainingValues
   *          The remaining values of the attribute to be added.
   * @return This add request.
   * @throws UnsupportedOperationException
   *           If this add request does not permit attributes to be
   *           added.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code firstValue} was
   *           {@code null}, or if {@code remainingValues} contains a
   *           {@code null} element.
   */
  AddRequest addAttribute(String attributeDescription,
      Object firstValue, Object... remainingValues)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Removes all the attributes from the entry to be added by this add
   * request.
   *
   * @return This add request.
   * @throws UnsupportedOperationException
   *           If this add request does not permit attributes to be
   *           removed.
   */
  AddRequest clearAttributes() throws UnsupportedOperationException;



  /**
   * Gets the named attribute from the entry to be added by this add
   * request.
   *
   * @param attributeDescription
   *          The name of the attribute to be returned.
   * @return The named attribute, or {@code null} if it is not included
   *         with this add request.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  AttributeValueSequence getAttribute(String attributeDescription)
      throws NullPointerException;



  /**
   * Returns the number of attributes in the entry to be added by this
   * add request.
   *
   * @return The number of attributes.
   */
  int getAttributeCount();



  /**
   * Returns an {@code Iterable} containing the attributes in the entry
   * to be added by this add request. The returned {@code Iterable} may
   * be used to remove attributes if permitted by this add request.
   *
   * @return An {@code Iterable} containing the attributes.
   */
  Iterable<? extends AttributeValueSequence> getAttributes();



  /**
   * Returns the distinguished name of the entry to be added by this add
   * request. The server shall not dereference any aliases in locating
   * the entry to be added.
   *
   * @return The distinguished name of the entry.
   */
  String getName();



  /**
   * Indicates whether or not the entry to be added by this add request
   * has any attributes.
   *
   * @return {@code true} if the entry has any attributes, otherwise
   *         {@code false}.
   */
  boolean hasAttributes();



  /**
   * Removes the named attribute from the entry to be added by this add
   * request.
   *
   * @param attributeDescription
   *          The name of the attribute to be removed.
   * @return This add request.
   * @throws UnsupportedOperationException
   *           If this add request does not permit attributes to be
   *           removed.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  AddRequest removeAttribute(String attributeDescription)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Sets the distinguished name of the entry to be added by this add
   * request. The server shall not dereference any aliases in locating
   * the entry to be added.
   *
   * @param dn
   *          The distinguished name of the entry to be added.
   * @return This add request.
   * @throws UnsupportedOperationException
   *           If this add request does not permit the distinguished
   *           name to be set.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  AddRequest setName(String dn) throws UnsupportedOperationException,
      NullPointerException;
}
