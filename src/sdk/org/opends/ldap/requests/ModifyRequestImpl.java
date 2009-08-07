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



import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.opends.server.types.ByteString;
import org.opends.spi.AbstractMessage;
import org.opends.types.AttributeValueSequence;
import org.opends.types.Change;
import org.opends.types.ModificationType;
import org.opends.util.Validator;



/**
 * Modify request implementation.
 */
final class ModifyRequestImpl extends AbstractMessage<ModifyRequest>
    implements ModifyRequest
{
  private final List<Change> changes = new LinkedList<Change>();
  private String dn;



  /**
   * Creates a new modify request using the provided DN.
   *
   * @param dn
   *          The the name of the entry to be modified.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  ModifyRequestImpl(String dn) throws NullPointerException
  {
    Validator.ensureNotNull(dn);

    this.dn = dn;
  }



  /**
   * {@inheritDoc}
   */
  public ModifyRequest addChange(Change change)
      throws NullPointerException
  {
    Validator.ensureNotNull(change);

    changes.add(change);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public ModifyRequest addChange(ModificationType type,
      AttributeValueSequence attribute) throws NullPointerException
  {
    final Change change = new Change(type, attribute);
    return addChange(change);
  }



  /**
   * {@inheritDoc}
   */
  public ModifyRequest addChange(ModificationType type,
      String attributeDescription) throws NullPointerException
  {
    final Change change = new Change(type, attributeDescription);
    return addChange(change);
  }



  /**
   * {@inheritDoc}
   */
  public ModifyRequest addChange(ModificationType type,
      String attributeDescription, ByteString value)
      throws NullPointerException
  {
    final Change change = new Change(type, attributeDescription, value);
    return addChange(change);
  }



  /**
   * {@inheritDoc}
   */
  public ModifyRequest addChange(ModificationType type,
      String attributeDescription, ByteString firstValue,
      ByteString... remainingValues) throws NullPointerException
  {
    final Change change =
        new Change(type, attributeDescription, firstValue,
            remainingValues);
    return addChange(change);
  }



  /**
   * {@inheritDoc}
   */
  public ModifyRequest addChange(ModificationType type,
      String attributeDescription, Collection<ByteString> values)
      throws NullPointerException
  {
    final Change change =
        new Change(type, attributeDescription, values);
    return addChange(change);
  }



  /**
   * {@inheritDoc}
   */
  public ModifyRequest addChange(ModificationType type,
      String attributeDescription, String value)
      throws NullPointerException
  {
    final Change change = new Change(type, attributeDescription, value);
    return addChange(change);
  }



  /**
   * {@inheritDoc}
   */
  public ModifyRequest addChange(ModificationType type,
      String attributeDescription, String firstValue,
      String... remainingValues) throws NullPointerException
  {
    final Change change =
        new Change(type, attributeDescription, firstValue,
            remainingValues);
    return addChange(change);
  }



  /**
   * {@inheritDoc}
   */
  public ModifyRequest clearChanges()
  {
    changes.clear();
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public int getChangeCount()
  {
    return changes.size();
  }



  /**
   * {@inheritDoc}
   */
  public Iterable<Change> getChanges()
  {
    return changes;
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
  public boolean hasChanges()
  {
    return !changes.isEmpty();
  }



  /**
   * {@inheritDoc}
   */
  public ModifyRequest setDN(String dn) throws NullPointerException
  {
    Validator.ensureNotNull(dn);

    this.dn = dn;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public StringBuilder toString(StringBuilder builder)
      throws NullPointerException
  {
    builder.append("ModifyRequest(dn=");
    builder.append(dn);
    builder.append(", changes=");
    builder.append(changes);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder;
  }
}
