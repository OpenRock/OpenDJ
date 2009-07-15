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



import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opends.ldap.Control;
import org.opends.ldap.GenericControl;



/**
 * A generic LDAP request. This class provides access to common request
 * parameters.
 */
public abstract class Request
{

  // The list of controls included with this request.
  private final List<GenericControl> controls =
      new LinkedList<GenericControl>();



  /**
   * Creates a new request.
   */
  protected Request()
  {
    // Nothing to do.
  }



  /**
   * Ensures that this request contains the specified control, replacing
   * any existing control having the same OID.
   * 
   * @param control
   *          The control to be added to this request.
   * @return {@code false} if this request already contained a control
   *         with the same OID, or {@code true} otherwise.
   */
  public final boolean addControl(GenericControl control)
  {
    boolean result = (removeControl(control.getOID()) == null);
    controls.add(control);
    return result;
  }



  /**
   * Returns the specified control included with this request.
   * 
   * @param oid
   *          The OID of the control to be returned.
   * @return The control, or {@code null} if the control is not included
   *         with this request.
   */
  public final Control getControl(String oid)
  {
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
   * this request. The returned {@code Iterable} may be used to remove
   * controls from this request.
   * 
   * @return An {@code Iterable} containing the controls included with
   *         this request.
   */
  public final Iterable<GenericControl> getControls()
  {
    return controls;
  }



  /**
   * Indicates whether or not this request has any controls.
   * 
   * @return {@code true} if this request has any controls, otherwise
   *         {@code false}.
   */
  public final boolean hasControls()
  {
    return !controls.isEmpty();
  }



  /**
   * Removes the specified control from this request if present.
   * 
   * @param oid
   *          The OID of the control to be removed.
   * @return The removed control, or {@code null} if the control is not
   *         included with this request.
   */
  public final Control removeControl(String oid)
  {
    // Avoid creating an iterator if possible.
    if (controls.isEmpty())
    {
      return null;
    }

    Iterator<GenericControl> iterator = controls.iterator();
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
   * Returns a string representation of this request.
   * 
   * @return A string representation of this request.
   */
  @Override
  public final String toString()
  {
    StringBuilder builder = new StringBuilder();
    toString(builder);
    return builder.toString();
  }



  /**
   * Appends a string representation of this request to the provided
   * buffer.
   * 
   * @param buffer
   *          The buffer into which a string representation of this
   *          request should be appended.
   */
  public abstract void toString(StringBuilder buffer);
}
