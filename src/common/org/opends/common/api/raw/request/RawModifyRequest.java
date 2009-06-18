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

package org.opends.common.api.raw.request;



import org.opends.server.core.operations.ModifyRequest;
import org.opends.server.core.operations.Schema;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.ByteString;
import org.opends.server.types.Attribute;
import org.opends.server.types.AttributeValue;
import org.opends.server.util.Validator;
import org.opends.common.api.raw.RawMessage;
import org.opends.common.api.raw.ModificationType;
import org.opends.common.api.DN;

import java.util.*;


/**
 * A raw modify request.
 */
public final class RawModifyRequest extends RawMessage implements RawRequest
{
  // The DN of the entry to be modified.
  private String dn;

  // The list of changes associated with this request.
  private final List<Change> changes =
      new ArrayList<Change>();


  /**
   * Creates a new raw modify request using the provided entry DN.
   * <p>
   * The new raw modify request will contain an empty list of controls,
   * and an empty list of modifications.
   *
   * @param dn
   *          The raw, unprocessed entry DN for this modify request.
   */
  public RawModifyRequest(String dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn;
  }

  /**
   * Creates a new raw modify request using the provided entry DN.
   * <p>
   * The new raw modify request will contain an empty list of controls,
   * and an empty list of modifications.
   *
   * @param dn
   *          The raw, unprocessed entry DN for this modify request.
   */
  public RawModifyRequest(DN dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();
  }



  /**
   * Adds the provided modification to the set of raw modifications for
   * this modify request.
   *
   * @param modificationType
   * @param attributeDescription
   * @param attributeValue
   * @return This raw modify request.
   */
  public RawModifyRequest addChange(ModificationType modificationType,
                                    String attributeDescription,
                                    ByteString... attributeValue)
  {
    Validator.ensureNotNull(modificationType, attributeDescription);
    changes.add(new Change(modificationType, attributeDescription,
                           attributeValue));
    return this;
  }



  /**
   * Adds the provided modification to the set of raw modifications for
   * this modify request.
   *
   * @param modificationType
   * @param attribute
   * @return This raw modify request.
   */
  public RawModifyRequest addChange(ModificationType modificationType,
                                    Attribute attribute)
  {
    Validator.ensureNotNull(modificationType, attribute);
    List<ByteString> values;
    if(attribute.size() > 0)
    {
      values = new ArrayList<ByteString>(attribute.size());
    }
    else
    {
      values = Collections.emptyList();
    }

    for(AttributeValue attributeValue : attribute)
    {
      values.add(attributeValue.getValue());
    }
    changes.add(new Change(modificationType, attribute.getNameWithOptions(),
                           values));
    return this;
  }



  /**
   * Returns the raw, unprocessed entry DN as included in the request
   * from the client.
   * <p>
   * This may or may not contain a valid DN, as no validation will have
   * been performed.
   *
   * @return The raw, unprocessed entry DN as included in the request
   *         from the client.
   */
  public String getDN()
  {
    return dn;
  }



  /**
   * Returns the list of modifications in their raw, unparsed form as
   * read from the client request.
   * <p>
   * Some of these modifications may be invalid as no validation will
   * have been performed on them. Any modifications made to the returned
   * modification {@code List} will be reflected in this modify request.
   *
   * @return The list of modifications in their raw, unparsed form as
   *         read from the client request.
   */
  public Iterable<Change> getChanges()
  {
    return changes;
  }



  /**
   * Sets the raw, unprocessed entry DN for this modify request.
   * <p>
   * This may or may not contain a valid DN.
   *
   * @param dn
   *          The raw, unprocessed entry DN for this modify request.
   * @return This raw modify request.
   */
  public RawModifyRequest setDN(String dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn;
    return this;
  }



  /**
   * Sets the raw, unprocessed entry DN for this modify request.
   * <p>
   * This may or may not contain a valid DN.
   *
   * @param dn
   *          The raw, unprocessed entry DN for this modify request.
   * @return This raw modify request.
   */
  public RawModifyRequest setDN(DN dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("ModifyRequest(entry=");
    buffer.append(dn);
    buffer.append(", changes=");
    buffer.append(changes);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }

  private static final class Change
  {
    private ModificationType modificationType;
    private String attributeDescription;
    private List<ByteString> attributeValues;

    Change(ModificationType modificationType, String attributeDescription,
           ByteString... attributeValues)
    {
      this.modificationType = modificationType;
      this.attributeDescription = attributeDescription;

      if(attributeValues != null)
      {
        this.attributeValues =
            new ArrayList<ByteString>(attributeValues.length);
        for(ByteString value : attributeValues)
        {
          Validator.ensureNotNull(value);
          this.attributeValues.add(value);
        }
      }
      else
      {
        this.attributeValues = Collections.emptyList();
      }
    }

    Change(ModificationType modificationType, String attributeDescription,
           List<ByteString> attributeValues)
    {
      this.modificationType = modificationType;
      this.attributeDescription = attributeDescription;
      this.attributeValues = attributeValues;
    }

    public ModificationType getModificationType()
    {
      return modificationType;
    }

    public String getAttributeDescription()
    {
      return attributeDescription;
    }

    public Iterable<ByteString> getAttributeValues()
    {
      return attributeValues;
    }

    /**
     * Returns a string representation of this request.
     *
     * @return A string representation of this request.
     */
    @Override
    public final String toString()
    {
      StringBuilder builder = new StringBuilder();
      toString(builder);
      return builder.toString();
    }



    /**
     * Appends a string representation of this request to the provided
     * buffer.
     *
     * @param buffer
     *          The buffer into which a string representation of this
     *          request should be appended.
     */
    public void toString(StringBuilder buffer)
    {
      buffer.append("Change(modificationType=");
      buffer.append(modificationType);
      buffer.append(", attributeDescription=");
      buffer.append(attributeDescription);
      buffer.append(", attributeValues=");
      buffer.append(attributeValues);
      buffer.append(")");
    }
  }
}
