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

package org.opends.ldap.requests;



import java.util.Collection;

import org.opends.server.types.ByteString;
import org.opends.types.AttributeValueSequence;



/**
 * An Add request. The Add operation allows a client to request the
 * addition of an entry into the Directory.
 * <p>
 * The RDN attribute(s) may or may not be included in the Add request.
 * NO-USER-MODIFICATION attributes such as the {@code createTimestamp}
 * or {@code creatorsName} attributes must not be included, since the
 * server maintains these automatically.
 */
public interface AddRequest extends Request<AddRequest>
{

  /**
   * Ensures that the entry to be added by this add request contains the
   * provided attribute values. Any existing values for the attribute
   * will be retained.
   * 
   * @param attribute
   *          The attribute to be added.
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
  AddRequest addAttribute(String attributeDescription, ByteString value)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Ensures that the entry to be added by this add request contains the
   * provided attribute values. Any existing values for the attribute
   * will be retained.
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
      ByteString firstValue, ByteString... remainingValues)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Ensures that the entry to be added by this add request contains the
   * provided attribute values. Any existing values for the attribute
   * will be retained.
   * 
   * @param attributeDescription
   *          The name of the attribute to be added.
   * @param values
   *          The values of the attribute to be added.
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
      Collection<ByteString> values)
      throws UnsupportedOperationException, IllegalArgumentException,
      NullPointerException;



  /**
   * Ensures that the entry to be added by this add request contains the
   * provided attribute value. Any existing values for the attribute
   * will be retained.
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
  AddRequest addAttribute(String attributeDescription, String value)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Ensures that the entry to be added by this add request contains the
   * provided attribute values. Any existing values for the attribute
   * will be retained.
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
      String firstValue, String... remainingValues)
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
   * Returns the number of attribute in the entry to be added by this
   * add request.
   * 
   * @return The number of attribute in the entry to be added by this
   *         add request.
   */
  int getAttributeCount();



  /**
   * Returns an {@code Iterable} containing the attributes in the entry
   * to be added by this add request. The returned {@code Iterable} may
   * be used to remove attributes if permitted by this add request.
   * 
   * @return An {@code Iterable} containing the attribute included with
   *         this add request.
   */
  Iterable<AttributeValueSequence> getAttributes();



  /**
   * Returns the name of the entry to be added. The server shall not
   * dereference any aliases in locating the entry to be added.
   * 
   * @return The name of the entry to be added.
   */
  String getDN();



  /**
   * Indicates whether or not the entry to be added by this add request
   * has any attributes.
   * 
   * @return {@code true} if the entry to be added by this add request
   *         has any attributes, otherwise {@code false}.
   */
  boolean hasAttributes();



  /**
   * Removes the named attribute from the entry to be added by this add
   * request.
   * 
   * @param attributeDescription
   *          The name of the attribute to be removed.
   * @return The removed attribute, or {@code null} if the attribute is
   *         not included with this add request.
   * @throws UnsupportedOperationException
   *           If this add request does not permit attributes to be
   *           removed.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  AttributeValueSequence removeAttribute(String attributeDescription)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Sets the name of the entry to be added. The server shall not
   * dereference any aliases in locating the entry to be added.
   * 
   * @param dn
   *          The name of the entry to be added.
   * @return This add request.
   * @throws UnsupportedOperationException
   *           If this add request does not permit the DN to be set.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  AddRequest setDN(String dn) throws UnsupportedOperationException,
      NullPointerException;
}