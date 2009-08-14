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
 * A readable named sequence of attributes.
 * <p>
 * TODO: Does this need to be read-only? Why not expose the update
 * methods?
 */
public interface NameAndAttributeSequence
{

  /**
   * Returns the distinguished name of this attribute sequence.
   *
   * @return The distinguished name.
   */
  String getName();



  /**
   * Gets the named attribute from this attribute sequence.
   *
   * @param attributeDescription
   *          The name of the attribute to be returned.
   * @return The named attribute, or {@code null} if it is not included
   *         with this attribute sequence.
   * @throws NullPointerException
   *           If {@code attributeDescription} was {@code null}.
   */
  AttributeValueSequence getAttribute(String attributeDescription)
      throws NullPointerException;



  /**
   * Returns the number of attributes in this attribute sequence.
   *
   * @return The number of attributes.
   */
  int getAttributeCount();



  /**
   * Returns an {@code Iterable} containing the attributes in this
   * attribute sequence. The returned {@code Iterable} may be used to
   * remove attributes if permitted by this attribute sequence.
   *
   * @return An {@code Iterable} containing the attributes.
   */
  Iterable<AttributeValueSequence> getAttributes();



  /**
   * Indicates whether or not this attribute sequence has any
   * attributes.
   *
   * @return {@code true} if this attribute sequence has any attributes,
   *         otherwise {@code false}.
   */
  boolean hasAttributes();

}
