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



import org.opends.schema.ObjectClass;



/**
 * An entry.
 * <p>
 * TODO: need to figure out how this should interact with
 * AttributeSequence. In particular AttributeSequence methods require a
 * schema in order to decode parameters.
 * <p>
 * TODO: note add semantics which may confuse users. They are aligned
 * with the Collections APIs, but not aligned with AttributeSequence and
 * LDAP modify add semantics (which do a merge).
 * <p>
 * TODO: need to define attribute ordering, e.g. object class first,
 * FIFO, implementation dependent, etc.
 */
public interface Entry
{
  /**
   * Adds the provided attribute to this entry, replacing any existing
   * attribute having the same attribute description.
   *
   * @param attribute
   *          The attribute to be added.
   * @return The previous attribute having the same attribute
   *         description, or {@code null} if there was no existing
   *         attribute with the same attribute description.
   * @throws UnsupportedOperationException
   *           If this entry does not permit attributes to be added.
   * @throws NullPointerException
   *           If {@code attribute} was {@code null}.
   */
  Attribute addAttribute(Attribute attribute)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Ensures that this entry contains the provided object class.
   *
   * @param objectClass
   *          The object class to be added.
   * @return {@code true} if this entry did not already contain {@code
   *         objectClass}, otherwise {@code false}.
   * @throws UnsupportedOperationException
   *           If this entry does not permit object classes to be added.
   * @throws NullPointerException
   *           If {@code objectClass} was {@code null}.
   */
  boolean addObjectClass(ObjectClass objectClass)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Removes all the attributes from this entry, including the {@code
   * objectClass} attribute if present.
   *
   * @return This entry.
   * @throws UnsupportedOperationException
   *           If this entry does not permit attributes to be removed.
   */
  Entry clearAttributes() throws UnsupportedOperationException;



  /**
   * Indicates whether or not this entry contains the named attribute.
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
   * Indicates whether or not this entry contains the provided object
   * class.
   *
   * @param objectClass
   *          The object class.
   * @return {@code true} if this entry contains the object class,
   *         otherwise {@code false}.
   * @throws NullPointerException
   *           If {@code objectClass} was {@code null}.
   */
  boolean containsObjectClass(ObjectClass objectClass);



  /**
   * Returns an {@code Iterable} containing all the attributes in this
   * entry having an attribute description which is a sub-type of the
   * provided attribute description. The returned {@code Iterable} may
   * be used to remove attributes if permitted by this entry.
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
   * Gets the named attribute from this entry, or {@code null} if it is
   * not included with this entry.
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
  DN getNameDN();



  /**
   * Returns the number of object classes in this entry.
   *
   * @return The number of object classes.
   */
  int getObjectClassCount();



  /**
   * Returns an {@code Iterable} containing the object classes in this
   * entry. The returned {@code Iterable} may be used to remove object
   * classes if permitted by this entry.
   *
   * @return An {@code Iterable} containing the object classes.
   */
  Iterable<ObjectClass> getObjectClasses();



  /**
   * Indicates whether or not this entry has any attributes.
   *
   * @return {@code true} if this entry has any attributes, otherwise
   *         {@code false}.
   */
  boolean hasAttributes();



  /**
   * Indicates whether or not this entry has any object classes.
   *
   * @return {@code true} if this entry has any object classes,
   *         otherwise {@code false}.
   */
  boolean hasObjectClasses();



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
   * Removes the provided object class from this entry if it is present.
   *
   * @param objectClass
   *          The object class to be remove.
   * @return {@code true} if this entry contained {@code objectClass},
   *         otherwise {@code false}.
   * @throws UnsupportedOperationException
   *           If this entry does not permit object classes to be
   *           removed.
   * @throws NullPointerException
   *           If {@code objectClass} was {@code null}.
   */
  boolean removeObjectClass(ObjectClass objectClass)
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
  Entry setNameDN(DN dn) throws UnsupportedOperationException,
      NullPointerException;
}
