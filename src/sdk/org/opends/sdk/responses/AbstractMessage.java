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

package org.opends.sdk.responses;



import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opends.sdk.controls.Control;
import org.opends.sdk.util.Validator;



/**
 * An abstract message which can be used as the basis for implementing
 * new requests and responses.
 *
 * @param <S>
 *          The type of message.
 */
abstract class AbstractMessage<S>
{
  private final List<Control> controls = new LinkedList<Control>();



  /**
   * Creates a new abstract message.
   */
  AbstractMessage()
  {
    // No implementation required.
  }



  /**
   * Adds the provided control to this message.
   *
   * @param control
   *          The control to be added to this message.
   * @return This message.
   * @throws UnsupportedOperationException
   *           If this message does not permit controls to be added.
   * @throws NullPointerException
   *           If {@code control} was {@code null}.
   */
  public final S addControl(Control control)
      throws NullPointerException
  {
    Validator.ensureNotNull(control);
    controls.add(control);
    return getThis();
  }



  /**
   * Removes all the controls included with this message.
   *
   * @return This message.
   * @throws UnsupportedOperationException
   *           If this message does not permit controls to be removed.
   */
  public final S clearControls()
  {
    controls.clear();
    return getThis();
  }



  /**
   * Returns the first control contained in this message having the
   * specified OID.
   *
   * @param oid
   *          The OID of the control to be returned.
   * @return The control, or {@code null} if the control is not included
   *         with this message.
   * @throws NullPointerException
   *           If {@code oid} was {@code null}.
   */
  public final Control getControl(String oid)
  {
    Validator.ensureNotNull(oid);

    // Avoid creating an iterator if possible.
    if (controls.isEmpty())
    {
      return null;
    }

    for (Control control : controls)
    {
      if (control.getOID().equals(oid))
      {
        return control;
      }
    }

    return null;
  }



  /**
   * Returns an {@code Iterable} containing the controls included with
   * this message. The returned {@code Iterable} may be used to remove
   * controls if permitted by this message.
   *
   * @return An {@code Iterable} containing the controls included with
   *         this message.
   */
  public final Iterable<Control> getControls()
  {
    return controls;
  }



  /**
   * Indicates whether or not this message has any controls.
   *
   * @return {@code true} if this message has any controls, otherwise
   *         {@code false}.
   */
  public final boolean hasControls()
  {
    return !controls.isEmpty();
  }



  /**
   * Removes the first control contained in this message having the
   * specified OID.
   *
   * @param oid
   *          The OID of the control to be removed.
   * @return The removed control, or {@code null} if the control is not
   *         included with this message.
   * @throws UnsupportedOperationException
   *           If this message does not permit controls to be removed.
   * @throws NullPointerException
   *           If {@code oid} was {@code null}.
   */
  public final Control removeControl(String oid)
      throws NullPointerException
  {
    Validator.ensureNotNull(oid);

    // Avoid creating an iterator if possible.
    if (controls.isEmpty())
    {
      return null;
    }

    Iterator<Control> iterator = controls.iterator();
    while (iterator.hasNext())
    {
      Control control = iterator.next();
      if (control.getOID().equals(oid))
      {
        iterator.remove();
        return control;
      }
    }

    return null;
  }



  /**
   * Returns a string representation of this message.
   *
   * @return A string representation of this message.
   */
  @Override
  public abstract String toString();



  /**
   * Returns a type-safe reference to this message.
   *
   * @return This message as a T.
   */
  @SuppressWarnings("unchecked")
  private final S getThis()
  {
    return (S) this;
  }
}
