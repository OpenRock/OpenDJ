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

package org.opends.sdk.ldif;



import java.util.HashSet;
import java.util.Set;

import org.opends.sdk.Attribute;
import org.opends.sdk.AttributeDescription;
import org.opends.sdk.AttributeValueSequence;
import org.opends.sdk.schema.AttributeType;
import org.opends.sdk.schema.Schema;



/**
 * Common LDIF reader/writer functionality.
 */
abstract class AbstractLDIFStream
{

  final Set<AttributeDescription> excludedAttributes =
      new HashSet<AttributeDescription>();
  boolean excludeOperationalAttributes = false;
  boolean excludeUserAttributes = false;
  final Set<AttributeDescription> includedAttributes =
      new HashSet<AttributeDescription>();
  Schema schema = Schema.getDefaultSchema();



  /**
   * Creates a new abstract LDIF stream.
   */
  AbstractLDIFStream()
  {
    // Nothing to do.
  }



  final boolean isAttributeIncluded(
      AttributeDescription attributeDescription)
  {
    if (!excludedAttributes.isEmpty()
        && excludedAttributes.contains(attributeDescription))
    {
      return false;
    }

    // Let explicit include override more general exclude.
    if (!includedAttributes.isEmpty())
    {
      return includedAttributes.contains(attributeDescription);
    }

    AttributeType type = attributeDescription.getAttributeType();

    if (excludeOperationalAttributes && type.isOperational())
    {
      return false;
    }

    if (excludeUserAttributes && !type.isOperational())
    {
      return false;
    }

    return true;
  }



  final boolean isAttributeIncluded(AttributeValueSequence attribute)
  {
    // Filter the attribute if required.
    if (!isAttributeFilteringEnabled())
    {
      return true;
    }

    AttributeDescription attributeDescription;
    if (attribute instanceof Attribute)
    {
      attributeDescription =
          ((Attribute) attribute).getAttributeDescription();
    }
    else
    {
      attributeDescription =
          AttributeDescription.valueOf(attribute
              .getAttributeDescriptionAsString(), schema);
    }

    return isAttributeIncluded(attributeDescription);
  }



  final boolean isAttributeFilteringEnabled()
  {
    return !excludedAttributes.isEmpty()
        || !includedAttributes.isEmpty()
        || excludeOperationalAttributes || excludeUserAttributes;
  }

}
