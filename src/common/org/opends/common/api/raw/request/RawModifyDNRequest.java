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



import org.opends.server.core.operations.ModifyDNRequest;
import org.opends.server.core.operations.Schema;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.OperationType;
import org.opends.server.util.Validator;
import org.opends.common.api.raw.RawMessage;


/**
 * A raw modify DN request.
 */
public final class RawModifyDNRequest extends RawMessage implements RawRequest
{
  // Indicates whether the old RDN attribute value should be removed.
  private boolean deleteOldRDN = false;

  // The DN of the entry to be renamed.
  private String dn;

  // The new RDN.
  private String newRDN;

  // The DN of the new superior if present.
  private String newSuperior;



  /**
   * Creates a new raw modify DN request using the provided entry DN and
   * new RDN.
   * <p>
   * The new raw modify DN request will contain an empty list of
   * controls, no new superior, and will not request deletion of the old
   * RDN attribute value.
   *
   * @param dn
   *          The raw, unprocessed entry DN for this modify DN request.
   * @param newRDN
   *          The raw, unprocessed new RDN for this modify DN request.
   */
  public RawModifyDNRequest(String dn, String newRDN)
  {
    Validator.ensureNotNull(dn, newRDN);
    this.dn = dn;
    this.newRDN = newRDN;
    this.newSuperior = "".intern();
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
   * Returns the raw, unprocessed new RDN as included in the request
   * from the client.
   * <p>
   * This may or may not contain a valid RDN, as no validation will have
   * been performed.
   *
   * @return The raw, unprocessed new RDN as included in the request
   *         from the client.
   */
  public String getNewRDN()
  {
    return newRDN;
  }



  /**
   * Returns the raw, unprocessed new superior DN as included in the
   * request from the client.
   * <p>
   * This may not contain a valid DN, as no validation will have been performed.
   *
   * @return The raw, unprocessed new superior DN as included in the
   *         request from the client.
   */
  public String getNewSuperior()
  {
    return newSuperior;
  }



  /**
   * Indicates whether the attribute value contained in the old RDN
   * should be removed from the entry.
   *
   * @return {@code true} if the attribute value contained in the old
   *         RDN should be removed from the entry.
   */
  public boolean isDeleteOldRDN()
  {
    return deleteOldRDN;
  }



  /**
   * Specifies whether the attribute value contained in the old RDN
   * should be removed from the entry.
   *
   * @param deleteOldRDN
   *          {@code true} if the attribute value contained in the old
   *          RDN should be removed from the entry.
   * @return This raw search request.
   */
  public RawModifyDNRequest setDeleteOldRDN(boolean deleteOldRDN)
  {
    this.deleteOldRDN = deleteOldRDN;
    return this;
  }



  /**
   * Sets the raw, unprocessed entry DN for this modify DN request.
   * <p>
   * This may or may not contain a valid DN.
   *
   * @param dn
   *          The raw, unprocessed entry DN for this modify DN request.
   * @return This raw modify DN request.
   */
  public RawModifyDNRequest setDN(String dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn;
    return this;
  }



  /**
   * Sets the raw, unprocessed new RDN for this modify DN request.
   * <p>
   * This may or may not contain a valid RDN.
   *
   * @param newRDN
   *          The raw, unprocessed new RDN for this modify DN request.
   * @return This raw modify DN request.
   */
  public RawModifyDNRequest setNewRDN(String newRDN)
  {
    Validator.ensureNotNull(newRDN);
    this.newRDN = newRDN;
    return this;
  }



  /**
   * Sets the raw, unprocessed new superior DN for this modify DN
   * request.
   * <p>
   *
   * @param newSuperior
   *          The raw, unprocessed new superior DN for this modify DN
   *          request.
   * @return This raw modify DN request.
   */
  public RawModifyDNRequest setNewSuperior(String newSuperior)
  {
    Validator.ensureNotNull(newSuperior);
    this.newSuperior = newSuperior;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public ModifyDNRequest toRequest(Schema schema)
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
    buffer.append("ModifyDNRequest(entry=");
    buffer.append(dn);
    buffer.append(", newRDN=");
    buffer.append(newRDN);
    buffer.append(", deleteOldRDN=");
    buffer.append(deleteOldRDN);
    buffer.append(", newSuperior=");
    buffer.append(String.valueOf(newSuperior));
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
