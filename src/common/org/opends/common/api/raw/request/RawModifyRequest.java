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



import org.opends.server.types.ByteString;
import org.opends.server.types.Attribute;
import org.opends.server.types.AttributeValue;
import org.opends.server.util.Validator;
import org.opends.common.api.raw.RawMessage;
import org.opends.common.api.raw.ModificationType;
import org.opends.common.api.raw.RawPartialAttribute;
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
   * @param change
   * @return This raw modify request.
   */
  public RawModifyRequest addChange(Change change)
  {
    Validator.ensureNotNull(change);
    changes.add(change);
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
    changes.add(new Change(modificationType, attribute));
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

  public static final class Change extends RawPartialAttribute
  {
    private ModificationType modificationType;

    private Change(ModificationType modificationType, String attributeDescription,
           ByteString... attributeValues)
    {
      super(attributeDescription, attributeValues);
      this.modificationType = modificationType;
    }

    private Change(ModificationType modificationType, Attribute attribute)
    {
      super(attribute);
      this.modificationType = modificationType;
    }

    public ModificationType getModificationType()
    {
      return modificationType;
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
      buffer.append(", modification=");
      super.toString(buffer);
      buffer.append(")");
    }
  }
}
