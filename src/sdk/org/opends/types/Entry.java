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






/**
 * An entry.
 * <p>
 * Need object class methods.
 */
public interface Entry extends AttributeSequence
{
  /**
   * Ensures that this entry contains the provided attribute values. Any
   * existing values for the attribute will be retained.
   *
   * @param attribute
   *          The attribute to be added.
   * @return This entry.
   * @throws UnsupportedOperationException
   *           If this entry does not permit attributes to be added.
   * @throws NullPointerException
   *           If {@code attribute} was {@code null}.
   */
  Entry addAttribute(Attribute attribute)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Ensures that this entry contains the provided attribute values. Any
   * existing values for the attribute will be retained.
   *
   * @param attribute
   *          The attribute to be added.
   * @return This entry.
   * @throws UnsupportedOperationException
   *           If this entry does not permit attributes to be added.
   * @throws NullPointerException
   *           If {@code attribute} was {@code null}.
   */
  Entry addAttribute(AttributeValueSequence attribute)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Removes all the attributes from this entry.
   *
   * @return This entry.
   * @throws UnsupportedOperationException
   *           If this entry does not permit attributes to be removed.
   */
  Entry clearAttributes() throws UnsupportedOperationException;



  /**
   * Indicates whether or not this entry contains an attribute which has
   * exactly the same attribute type and options as those provided.
   *
   * @param attributeDescription
   *          The name of the attribute.
   * @return {@code true} if this entry contains the named attribute,
   *         otherwise {@code false}.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  boolean containsAttribute(AttributeDescription attributeDescription);



  /**
   * Indicates whether or not this entry contains an attribute which has
   * exactly the same attribute type and options as those provided.
   *
   * @param attributeDescription
   *          The name of the attribute.
   * @return {@code true} if this entry contains the named attribute,
   *         otherwise {@code false}.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  boolean containsAttribute(String attributeDescription);



  /**
   * Returns an {@code Iterable} containing all the attributes in this
   * entry having an attribute description which is a sub-type of the
   * provided attribute type and options. The returned {@code Iterable}
   * may be used to remove attributes if permitted by this entry.
   *
   * @param attributeDescription
   *          The name of the attributes to be returned.
   * @return An {@code Iterable} containing the matching attributes.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  Iterable<Attribute> findAttributes(
      AttributeDescription attributeDescription);



  /**
   * Returns an {@code Iterable} containing all the attributes in this
   * entry having an attribute description which is a sub-type of the
   * provided attribute type and options. The returned {@code Iterable}
   * may be used to remove attributes if permitted by this entry.
   *
   * @param attributeDescription
   *          The name of the attributes to be returned.
   * @return An {@code Iterable} containing the matching attributes.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  Iterable<Attribute> findAttributes(String attributeDescription);



  /**
   * Gets the attribute from this entry which has exactly the same
   * attribute type and options as those provided.
   *
   * @param attributeDescription
   *          The name of the attribute to be returned.
   * @return The named attribute, or {@code null} if it is not included
   *         with this entry.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  Attribute getAttribute(AttributeDescription attributeDescription);



  /**
   * Gets the named attribute from this entry which has exactly the same
   * attribute type and options as those provided.
   *
   * @param attributeDescription
   *          The name of the attribute to be returned.
   * @return The named attribute, or {@code null} if it is not included
   *         with this entry.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  Attribute getAttribute(String attributeDescription)
      throws NullPointerException;



  /**
   * Returns the number of attributes in this entry.
   *
   * @return The number of attributes.
   */
  int getAttributeCount();



  /**
   * Returns an {@code Iterable} containing the attributes in this
   * entry. The returned {@code Iterable} may be used to remove
   * attributes if permitted by this entry.
   *
   * @return An {@code Iterable} containing the attributes.
   */
  Iterable<Attribute> getAttributes();



  /**
   * Returns the distinguished name of this entry.
   *
   * @return The distinguished name.
   */
  String getName();



  /**
   * Returns the distinguished name of this entry.
   *
   * @return The distinguished name.
   */
  DN getNameDN();



  /**
   * Indicates whether or not this entry has any attributes.
   *
   * @return {@code true} if this entry has any attributes, otherwise
   *         {@code false}.
   */
  boolean hasAttributes();



  /**
   * Removes the named attribute from this entry.
   *
   * @param attributeDescription
   *          The name of the attribute to be removed.
   * @return The removed attribute, or {@code null} if the attribute is
   *         not included with this entry.
   * @throws UnsupportedOperationException
   *           If this entry does not permit attributes to be removed.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  Attribute removeAttribute(String attributeDescription)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Removes the named attribute from this entry.
   *
   * @param attributeDescription
   *          The name of the attribute to be removed.
   * @return The removed attribute, or {@code null} if the attribute is
   *         not included with this entry.
   * @throws UnsupportedOperationException
   *           If this entry does not permit attributes to be removed.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  Attribute removeAttribute(AttributeDescription attributeDescription)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Sets the distinguished name of this entry.
   *
   * @param dn
   *          The distinguished name.
   * @return This entry.
   * @throws UnsupportedOperationException
   *           If this entry does not permit the distinguished name to
   *           be set.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  Entry setName(String dn) throws UnsupportedOperationException,
      NullPointerException;



  /**
   * Sets the distinguished name of this entry.
   *
   * @param dn
   *          The distinguished name.
   * @return This entry.
   * @throws UnsupportedOperationException
   *           If this entry does not permit the distinguished name to
   *           be set.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  Entry setNameDN(DN dn) throws UnsupportedOperationException,
      NullPointerException;
}
