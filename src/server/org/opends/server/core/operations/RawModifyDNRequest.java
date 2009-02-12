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



import org.opends.server.types.ByteString;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.OperationType;
import org.opends.server.util.Validator;



/**
 * A raw modify DN request.
 */
public final class RawModifyDNRequest extends RawRequest
{
  // The DN of the entry to be renamed.
  private ByteString dn;

  // The DN of the new superior if present.
  private ByteString newSuperior = null;

  // The new RDN.
  private ByteString newRDN;

  // Indicates whether the old RDN attribute value should be removed.
  private boolean deleteOldRDN = false;



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
  public RawModifyDNRequest(ByteString dn, ByteString newRDN)
  {
    super(OperationType.MODIFY_DN);
    Validator.ensureNotNull(dn, newRDN);
    this.dn = dn;
    this.newRDN = newRDN;
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
   * Sets the raw, unprocessed entry DN for this modify DN request.
   * <p>
   * This may or may not contain a valid DN.
   *
   * @param dn
   *          The raw, unprocessed entry DN for this modify DN request.
   * @return This raw modify DN request.
   */
  public RawModifyDNRequest setDN(ByteString dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn;
    return this;
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
  public ByteString getNewRDN()
  {
    return newRDN;
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
  public RawModifyDNRequest setNewRDN(ByteString newRDN)
  {
    Validator.ensureNotNull(newRDN);
    this.newRDN = newRDN;
    return this;
  }



  /**
   * Returns the raw, unprocessed new superior DN as included in the
   * request from the client.
   * <p>
   * This may be {@code null}, or may not contain a valid DN, as no
   * validation will have been performed.
   *
   * @return The raw, unprocessed new superior DN as included in the
   *         request from the client.
   */
  public ByteString getNewSuperior()
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
   * Sets the raw, unprocessed new superior DN for this modify DN
   * request.
   * <p>
   * This may be {@code null}, or may not contain a valid DN.
   *
   * @param newSuperior
   *          The raw, unprocessed new superior DN for this modify DN
   *          request.
   * @return This raw modify DN request.
   */
  public RawModifyDNRequest setNewSuperior(ByteString newSuperior)
  {
    this.newSuperior = newSuperior;
    return this;
  }



  /**
   * Returns a decoded modify DN request representing this raw modify DN
   * request. Subsequent changes to this raw modify DN request will not
   * be reflected in the returned modify DN request.
   *
   * @return A decoded modify DN request representing this raw modify DN
   *         request.
   * @throws DirectoryException
   *           If this raw modify DN request could not be decoded.
   */
  @Override
  public ModifyDNRequest toRequest() throws DirectoryException
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
