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



import org.opends.sdk.ldif.ChangeRecordVisitor;
import org.opends.sdk.util.Validator;



/**
 * Modify DN request implementation.
 */
final class ModifyDNRequestImpl extends
    AbstractMessage<ModifyDNRequest> implements ModifyDNRequest
{
  private boolean deleteOldRDN = false;

  private String name;

  private String newRDN;

  private String newSuperior = null;



  /**
   * Creates a new modify DN request using the provided distinguished
   * name and new RDN.
   *
   * @param name
   *          The distinguished name of the entry to be renamed.
   * @param newRDN
   *          The new RDN of the entry.
   * @throws NullPointerException
   *           If {@code name} or {@code newRDN} was {@code null}.
   */
  ModifyDNRequestImpl(String name, String newRDN)
      throws NullPointerException
  {
    Validator.ensureNotNull(name, newRDN);

    this.name = name;
    this.newRDN = newRDN;
  }



  /**
   * {@inheritDoc}
   */
  public <R, P> R accept(ChangeRecordVisitor<R, P> v, P p)
  {
    return v.visitChangeRecord(p, this);
  }



  /**
   * {@inheritDoc}
   */
  public String getName()
  {
    return name;
  }



  /**
   * {@inheritDoc}
   */
  public String getNewRDN()
  {
    return newRDN;
  }



  /**
   * {@inheritDoc}
   */
  public String getNewSuperior()
  {
    return newSuperior;
  }



  /**
   * {@inheritDoc}
   */
  public boolean isDeleteOldRDN()
  {
    return deleteOldRDN;
  }



  /**
   * {@inheritDoc}
   */
  public ModifyDNRequest setDeleteOldRDN(boolean deleteOldRDN)
  {
    this.deleteOldRDN = deleteOldRDN;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public ModifyDNRequest setName(String dn) throws NullPointerException
  {
    Validator.ensureNotNull(dn);

    this.name = dn;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public ModifyDNRequestImpl setNewRDN(String rdn)
      throws NullPointerException
  {
    Validator.ensureNotNull(rdn);

    this.newRDN = rdn;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public ModifyDNRequestImpl setNewSuperior(String dn)
      throws NullPointerException
  {
    Validator.ensureNotNull(dn);

    this.newSuperior = dn;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("ModifyDNRequest(name=");
    builder.append(name);
    builder.append(", newRDN=");
    builder.append(newRDN);
    builder.append(", deleteOldRDN=");
    builder.append(deleteOldRDN);
    builder.append(", newSuperior=");
    builder.append(String.valueOf(newSuperior));
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder.toString();
  }
}
