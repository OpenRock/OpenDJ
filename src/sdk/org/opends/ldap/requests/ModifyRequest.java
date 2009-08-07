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
import org.opends.types.Change;
import org.opends.types.ModificationType;



/**
 * A Modify request. The Modify operation allows a client to request
 * that a modification of an entry be performed on its behalf by a
 * server.
 */
public interface ModifyRequest extends Request<ModifyRequest>
{
  /**
   * Appends the provided change to the list of changes included with
   * this modify request.
   * 
   * @param change
   *          The change to be performed.
   * @return This modify request.
   * @throws UnsupportedOperationException
   *           If this modify request does not permit changes to be
   *           added.
   * @throws NullPointerException
   *           If {@code change} was {@code null}.
   */
  ModifyRequest addChange(Change change)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Appends the provided change to the list of changes included with
   * this modify request.
   * 
   * @param type
   *          The type of change to be performed.
   * @param attribute
   *          The attribute name and values to be modified.
   * @return This modify request.
   * @throws UnsupportedOperationException
   *           If this modify request does not permit changes to be
   *           added.
   * @throws NullPointerException
   *           If {@code type} or {@code attribute} was {@code null}.
   */
  ModifyRequest addChange(ModificationType type,
      AttributeValueSequence attribute)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Appends the provided change to the list of changes included with
   * this modify request.
   * 
   * @param type
   *          The type of change to be performed.
   * @param attributeDescription
   *          The name of the attribute to be modified.
   * @return This modify request.
   * @throws UnsupportedOperationException
   *           If this modify request does not permit changes to be
   *           added.
   * @throws NullPointerException
   *           If {@code type} or {@code attributeDescription} was
   *           {@code null}.
   */
  ModifyRequest addChange(ModificationType type,
      String attributeDescription)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Appends the provided change to the list of changes included with
   * this modify request.
   * 
   * @param type
   *          The type of change to be performed.
   * @param attributeDescription
   *          The name of the attribute to be modified.
   * @param value
   *          The attribute value to be modified.
   * @return This modify request.
   * @throws UnsupportedOperationException
   *           If this modify request does not permit changes to be
   *           added.
   * @throws NullPointerException
   *           If {@code type}, {@code attributeDescription}, or {@code
   *           value} was {@code null}.
   */
  ModifyRequest addChange(ModificationType type,
      String attributeDescription, ByteString value)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Appends the provided change to the list of changes included with
   * this modify request.
   * 
   * @param type
   *          The type of change to be performed.
   * @param attributeDescription
   *          The name of the attribute to be modified.
   * @param firstValue
   *          The first attribute value to be modified.
   * @param remainingValues
   *          The remaining attribute values to be modified.
   * @return This modify request.
   * @throws UnsupportedOperationException
   *           If this modify request does not permit changes to be
   *           added.
   * @throws NullPointerException
   *           If {@code type}, {@code attributeDescription}, or {@code
   *           firstValue} was {@code null}, or if {@code
   *           remainingValues} contains a {@code null} element.
   */
  ModifyRequest addChange(ModificationType type,
      String attributeDescription, ByteString firstValue,
      ByteString... remainingValues)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Appends the provided change to the list of changes included with
   * this modify request.
   * 
   * @param type
   *          The type of change to be performed.
   * @param attributeDescription
   *          The name of the attribute to be modified.
   * @param values
   *          The attribute values to be modified.
   * @return This modify request.
   * @throws UnsupportedOperationException
   *           If this modify request does not permit changes to be
   *           added.
   * @throws NullPointerException
   *           If {@code type}, {@code attributeDescription}, or {@code
   *           values} was {@code null}.
   */
  ModifyRequest addChange(ModificationType type,
      String attributeDescription, Collection<ByteString> values)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Appends the provided change to the list of changes included with
   * this modify request.
   * 
   * @param type
   *          The type of change to be performed.
   * @param attributeDescription
   *          The name of the attribute to be modified.
   * @param value
   *          The attribute value to be modified.
   * @return This modify request.
   * @throws UnsupportedOperationException
   *           If this modify request does not permit changes to be
   *           added.
   * @throws NullPointerException
   *           If {@code type}, {@code attributeDescription}, or {@code
   *           value} was {@code null}.
   */
  ModifyRequest addChange(ModificationType type,
      String attributeDescription, String value)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Appends the provided change to the list of changes included with
   * this modify request.
   * 
   * @param type
   *          The type of change to be performed.
   * @param attributeDescription
   *          The name of the attribute to be modified.
   * @param firstValue
   *          The first attribute value to be modified.
   * @param remainingValues
   *          The remaining attribute values to be modified.
   * @return This modify request.
   * @throws UnsupportedOperationException
   *           If this modify request does not permit changes to be
   *           added.
   * @throws NullPointerException
   *           If {@code type}, {@code attributeDescription}, or {@code
   *           firstValue} was {@code null}, or if {@code
   *           remainingValues} contains a {@code null} element.
   */
  ModifyRequest addChange(ModificationType type,
      String attributeDescription, String firstValue,
      String... remainingValues) throws UnsupportedOperationException,
      NullPointerException;



  /**
   * Removes all the changes included with this modify request.
   * 
   * @return This modify request.
   * @throws UnsupportedOperationException
   *           If this modify request does not permit changes to be
   *           removed.
   */
  ModifyRequest clearChanges() throws UnsupportedOperationException;



  /**
   * Returns the number of changes included with this modify request.
   * 
   * @return The number of changes included with this modify request.
   */
  int getChangeCount();



  /**
   * Returns an {@code Iterable} containing the changes included with
   * this modify request. The returned {@code Iterable} may be used to
   * remove changes if permitted by this modify request.
   * 
   * @return An {@code Iterable} containing the changes included with
   *         this modify request.
   */
  Iterable<Change> getChanges();



  /**
   * Returns the name of the entry to be modified. The server shall not
   * perform any alias dereferencing in determining the object to be
   * modified.
   * 
   * @return The name of the entry to be modified.
   */
  String getDN();



  /**
   * Indicates whether or not this modify request has any changes.
   * 
   * @return {@code true} if this modify request has any changes,
   *         otherwise {@code false}.
   */
  boolean hasChanges();



  /**
   * Sets the name of the entry to be modified. The server shall not
   * perform any alias dereferencing in determining the object to be
   * modified.
   * 
   * @param dn
   *          The the name of the entry to be modified.
   * @return This modify request.
   * @throws UnsupportedOperationException
   *           If this modify request does not permit the DN to be set.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  ModifyRequest setDN(String dn) throws UnsupportedOperationException,
      NullPointerException;
}