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
 * An attribute.
 * <p>
 * TODO: must be schema aware, matching, immutable, and support virtual
 * attributes.
 */
public final class Attribute implements AttributeValueSequence
{

  /**
   * Returns the attribute description associated with this attribute.
   * This includes the attribute name and any attribute options.
   *
   * @return The attribute description.
   */
  public AttributeDescription getAttributeDescription()
  {
    // TODO Auto-generated method stub
    return null;
  }



  /**
   * Returns the string representation of the attribute description
   * associated with this attribute. This includes the attribute name
   * and any attribute options.
   *
   * @return The string representation of the attribute description.
   */
  public String getAttributeDescriptionAsString()
  {
    // TODO Auto-generated method stub
    return null;
  }



  /**
   * {@inheritDoc}
   */
  public boolean isEmpty()
  {
    // TODO Auto-generated method stub
    return false;
  }



  /**
   * {@inheritDoc}
   */
  public Iterator<ByteString> iterator()
  {
    // TODO Auto-generated method stub
    return null;
  }



  /**
   * {@inheritDoc}
   */
  public int size()
  {
    // TODO Auto-generated method stub
    return 0;
  }

}
