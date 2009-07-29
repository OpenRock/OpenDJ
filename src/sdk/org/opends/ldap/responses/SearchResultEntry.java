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

package org.opends.ldap.responses;



import org.opends.ldap.controls.Control;
import org.opends.server.types.ByteString;
import org.opends.types.Attribute;
import org.opends.types.DN;



/**
 * An LDAP search result entry response message.
 */
public interface SearchResultEntry extends Response
{

  /**
   * Adds the provided attribute to this search result entry. If this
   * search result entry already contains an attribute having the same
   * attribute description, then it will be replaced with the new
   * attribute.
   *
   * @param attributeDescription
   *          The name of the attribute to be added to this search
   *          result entry.
   * @param attributeValues
   *          The values of the attribute to be added to this search
   *          result entry.
   * @return This search result entry.
   * @throws UnsupportedOperationException
   *           If this search result entry does not permit attributes to
   *           be added.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  SearchResultEntry addAttribute(String attributeDescription,
      ByteString... attributeValues)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Adds the provided attribute to this search result entry. If this
   * search result entry already contains an attribute having the same
   * attribute description, then it will be replaced with the new
   * attribute.
   *
   * @param attributeDescription
   *          The name of the attribute to be added to this search
   *          result entry.
   * @param attributeValues
   *          The values of the attribute to be added to this search
   *          result entry.
   * @return This search result entry.
   * @throws UnsupportedOperationException
   *           If this search result entry does not permit attributes to
   *           be added.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  SearchResultEntry addAttribute(String attributeDescription,
      String... attributeValues) throws UnsupportedOperationException,
      NullPointerException;



  /**
   * Adds the provided attribute to this search result entry. If this
   * search result entry already contains an attribute having the same
   * attribute description, then it will be replaced with the new
   * attribute.
   *
   * @param attribute
   *          The attribute to be added to this search result entry.
   * @return This search result entry.
   * @throws UnsupportedOperationException
   *           If this search result entry does not permit attributes to
   *           be added.
   * @throws NullPointerException
   *           If {@code attribute} was {@code null}.
   */
  SearchResultEntry addAttribute(Attribute attribute)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  SearchResultEntry addControl(Control control)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Returns the number of attributes included with this search result
   * entry.
   *
   * @return The number of attributes included with this search result
   *         entry.
   */
  int getAttributeCount();



  /**
   * Removes all the attributes included with this search result entry.
   *
   * @return This search result entry.
   * @throws UnsupportedOperationException
   *           If this search result entry does not permit attributes to
   *           be removed.
   */
  SearchResultEntry clearAttributes()
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  SearchResultEntry clearControls()
      throws UnsupportedOperationException;



  /**
   * Returns the named attribute from this search result entry, if it
   * exists.
   *
   * @param attributeDescription
   *          The name of the attribute to be returned.
   * @return The named attribute, or {@code null} if it is not included
   *         with this search result entry.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  Attribute getAttribute(String attributeDescription)
      throws NullPointerException;



  /**
   * Returns an {@code Iterable} containing the attributes included with
   * this search result entry. The returned {@code Iterable} may be used
   * to remove attributes if permitted by this search result entry.
   *
   * @return An {@code Iterable} containing the attribute included with
   *         this search result entry.
   */
  Iterable<Attribute> getAttributes();



  /**
   * Returns the DN associated with this search result entry.
   *
   * @return The DN associated with this search result entry.
   */
  String getDN();



  /**
   * Indicates whether or not this search result entry contains the
   * specified attribute.
   *
   * @param attributeDescription
   *          The name of the attribute to be returned.
   * @return {@code true} if this search result entry contains the
   *         specified attribute, otherwise {@code false}.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  boolean hasAttribute(String attributeDescription)
      throws NullPointerException;



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
   *          The name of the attribute to be removed from this search
   *          result entry.
   * @return The removed attribute, or {@code null} if the attribute is
   *         not included with this search result entry.
   * @throws UnsupportedOperationException
   *           If this search result entry does not permit attributes to
   *           be removed.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  Attribute removeAttribute(String attributeDescription)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Sets the DN associated with this search result entry.
   *
   * @param dn
   *          The DN associated with this result.
   * @return This search result entry.
   * @throws UnsupportedOperationException
   *           If this search result entry does not permit the DN to be
   *           set.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  SearchResultEntry setDN(DN dn) throws UnsupportedOperationException,
      NullPointerException;



  /**
   * Sets the DN associated with this search result entry.
   *
   * @param dn
   *          The DN associated with this result.
   * @return This search result entry.
   * @throws UnsupportedOperationException
   *           If this search result entry does not permit the DN to be
   *           set.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  SearchResultEntry setDN(String dn)
      throws UnsupportedOperationException, NullPointerException;
}