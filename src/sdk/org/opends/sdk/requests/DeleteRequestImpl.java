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
 * Delete request implementation.
 */
final class DeleteRequestImpl extends AbstractMessage<DeleteRequest>
    implements DeleteRequest
{
  private String name;



  /**
   * Creates a new delete request using the provided distinguished name.
   *
   * @param name
   *          The distinguished name of the entry to be deleted.
   * @throws NullPointerException
   *           If {@code name} was {@code null}.
   */
  DeleteRequestImpl(String name) throws NullPointerException
  {
    Validator.ensureNotNull(name);

    this.name = name;
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
  public DeleteRequest setName(String dn) throws NullPointerException
  {
    Validator.ensureNotNull(dn);

    this.name = dn;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("DeleteRequest(name=");
    builder.append(name);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder.toString();
  }
}
