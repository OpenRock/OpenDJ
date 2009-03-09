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

package org.opends.server.core.operations;



import java.util.ArrayList;
import java.util.List;

import org.opends.server.types.ByteString;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.OperationType;
import org.opends.server.types.RawModification;
import org.opends.server.util.Validator;



/**
 * A raw modify request.
 */
public final class RawModifyRequest extends RawRequest
{
  // The DN of the entry to be modified.
  private ByteString dn;

  // The list of modifications associated with this request.
  private final List<RawModification> modifications =
      new ArrayList<RawModification>();



  /**
   * Creates a new raw modify request using the provided entry DN.
   * <p>
   * The new raw modify request will contain an empty list of controls,
   * and an empty list of modifications.
   *
   * @param dn
   *          The raw, unprocessed entry DN for this modify request.
   */
  public RawModifyRequest(ByteString dn)
  {
    super(OperationType.MODIFY);
    Validator.ensureNotNull(dn);
    this.dn = dn;
  }



  /**
   * Adds the provided modification to the set of raw modifications for
   * this modify request.
   *
   * @param modification
   *          The modification to add to the set of raw modifications
   *          for this modify request.
   * @return This raw modify request.
   */
  public RawModifyRequest addModification(RawModification modification)
  {
    Validator.ensureNotNull(modification);
    modifications.add(modification);
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
  public ByteString getDN()
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
  public List<RawModification> getModifications()
  {
    return modifications;
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
  public RawModifyRequest setDN(ByteString dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public ModifyRequest toRequest(Schema schema)
      throws DirectoryException
  {
    // TODO: not yet implemented.
    return null;
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
    buffer.append(modifications);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
