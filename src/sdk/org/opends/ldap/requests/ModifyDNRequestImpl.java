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

package org.opends.ldap.requests;



import org.opends.spi.AbstractMessage;
import org.opends.util.Validator;



/**
 * Modify DN request implementation.
 */
final class ModifyDNRequestImpl extends
    AbstractMessage<ModifyDNRequest> implements ModifyDNRequest
{
  private boolean deleteOldRDN = false;

  private String dn;

  private String newRDN;

  private String newSuperior = null;



  /**
   * Creates a new modify DN request using the provided entry DN and new
   * RDN.
   *
   * @param dn
   *          The name of the entry to be renamed.
   * @param rdn
   *          The new RDN of the entry to be renamed.
   * @throws NullPointerException
   *           If {@code dn} or {@code rdn} was {@code null}.
   */
  ModifyDNRequestImpl(String dn, String rdn)
      throws NullPointerException
  {
    Validator.ensureNotNull(dn, rdn);

    this.dn = dn;
    this.newRDN = rdn;
  }



  /**
   * {@inheritDoc}
   */
  public String getDN()
  {
    return dn;
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
  public String getNewSuperiorDN()
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
  public ModifyDNRequest setDN(String dn) throws NullPointerException
  {
    Validator.ensureNotNull(dn);

    this.dn = dn;
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
  public ModifyDNRequestImpl setNewSuperiorDN(String dn)
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
  public StringBuilder toString(StringBuilder builder)
      throws NullPointerException
  {
    builder.append("ModifyDNRequest(entry=");
    builder.append(dn);
    builder.append(", newRDN=");
    builder.append(newRDN);
    builder.append(", deleteOldRDN=");
    builder.append(deleteOldRDN);
    builder.append(", newSuperior=");
    builder.append(String.valueOf(newSuperior));
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder;
  }
}
