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



import java.util.Iterator;

import org.opends.sdk.AttributeValueSequence;
import org.opends.sdk.Change;
import org.opends.sdk.ModificationType;
import org.opends.sdk.util.ByteString;



/**
 * A modification to be performed on an entry during a Modify operation.
 */
final class ChangeImpl implements Change
{
  private final ModificationType modificationType;
  private final AttributeValueSequence attribute;



  /**
   * Creates a new change using the provided attribute value sequence.
   *
   * @param modificationType
   *          The type of change to be performed.
   * @param attribute
   *          The attribute name and values to be modified.
   */
  ChangeImpl(ModificationType modificationType,
      AttributeValueSequence attribute) throws NullPointerException
  {
    this.modificationType = modificationType;
    this.attribute = attribute;
  }



  /**
   * {@inheritDoc}
   */
  public String getAttributeDescriptionAsString()
  {
    return attribute.getAttributeDescriptionAsString();
  }



  public ModificationType getModificationType()
  {
    return modificationType;
  }



  /**
   * {@inheritDoc}
   */
  public boolean isEmpty()
  {
    return attribute.isEmpty();
  }



  /**
   * {@inheritDoc}
   */
  public Iterator<ByteString> iterator()
  {
    return attribute.iterator();
  }



  /**
   * {@inheritDoc}
   */
  public int size()
  {
    return attribute.size();
  }



  /**
   * {@inheritDoc}
   */
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("Change(modificationType=");
    builder.append(modificationType);
    builder.append(", attributeDescription=");
    builder.append(attribute.getAttributeDescriptionAsString());
    builder.append(", attributeValues={");
    boolean firstValue = true;
    for (ByteString value : attribute)
    {
      if (!firstValue)
      {
        builder.append(", ");
      }
      builder.append(value);
      firstValue = false;
    }
    builder.append("})");
    return builder.toString();
  }

}
