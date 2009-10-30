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



import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.opends.sdk.AttributeValueSequence;
import org.opends.sdk.Change;
import org.opends.sdk.ModificationType;
import org.opends.sdk.ldif.ChangeRecordVisitor;
import org.opends.sdk.util.Validator;



/**
 * Modify request implementation.
 */
final class ModifyRequestImpl extends AbstractMessage<ModifyRequest>
    implements ModifyRequest
{
  private final List<Change> changes = new LinkedList<Change>();
  private String name;



  /**
   * Creates a new modify request using the provided distinguished name.
   *
   * @param name
   *          The the distinguished name of the entry to be modified.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  ModifyRequestImpl(String name) throws NullPointerException
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
    final Change change = new ChangeImpl(type, attribute);
    return addChange(change);
  }



  /**
   * {@inheritDoc}
   */
  public ModifyRequest addChange(ModificationType type,
      String attributeDescription) throws NullPointerException
  {
    final Change change =
        new ChangeImpl(type, Attributes.create(attributeDescription));
    return addChange(change);
  }



  /**
   * {@inheritDoc}
   */
  public ModifyRequest addChange(ModificationType type,
      String attributeDescription, Collection<?> values)
      throws NullPointerException
  {
    final ChangeImpl change =
        new ChangeImpl(type, Attributes.create(attributeDescription,
            values));
    return addChange(change);
  }



  /**
   * {@inheritDoc}
   */
  public ModifyRequest addChange(ModificationType type,
      String attributeDescription, Object value)
      throws NullPointerException
  {
    final ChangeImpl change =
        new ChangeImpl(type, Attributes.create(attributeDescription,
            value));
    return addChange(change);
  }



  /**
   * {@inheritDoc}
   */
  public ModifyRequest addChange(ModificationType type,
      String attributeDescription, Object... values)
      throws NullPointerException
  {
    final ChangeImpl change =
        new ChangeImpl(type, Attributes.create(attributeDescription,
            values));
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
  public String getName()
  {
    return name;
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
  public ModifyRequest setName(String dn) throws NullPointerException
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
    builder.append("ModifyRequest(dn=");
    builder.append(name);
    builder.append(", changes=");
    builder.append(changes);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder.toString();
  }
}
