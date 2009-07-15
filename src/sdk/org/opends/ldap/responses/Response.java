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

package org.opends.ldap.responses;



import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opends.ldap.Control;
import org.opends.ldap.GenericControl;



/**
 * A generic LDAP response. This class provides access to common
 * response parameters.
 */
public abstract class Response
{

  // The list of controls included with this response.
  private final List<GenericControl> controls =
      new LinkedList<GenericControl>();



  /**
   * Creates a new response.
   */
  protected Response()
  {
    // Nothing to do.
  }



  /**
   * Ensures that this response contains the specified control,
   * replacing any existing control having the same OID.
   * 
   * @param control
   *          The control to be added to this response.
   * @return {@code false} if this response already contained a control
   *         with the same OID, or {@code true} otherwise.
   */
  public final boolean addControl(GenericControl control)
  {
    boolean result = (removeControl(control.getOID()) == null);
    controls.add(control);
    return result;
  }



  /**
   * Returns the specified control included with this response.
   * 
   * @param oid
   *          The OID of the control to be returned.
   * @return The control, or {@code null} if the control is not included
   *         with this response.
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
   * this response. The returned {@code Iterable} may be used to remove
   * controls from this response.
   * 
   * @return An {@code Iterable} containing the controls included with
   *         this response.
   */
  public final Iterable<GenericControl> getControls()
  {
    return controls;
  }



  /**
   * Indicates whether or not this response has any controls.
   * 
   * @return {@code true} if this response has any controls, otherwise
   *         {@code false}.
   */
  public final boolean hasControls()
  {
    return !controls.isEmpty();
  }



  /**
   * Removes the specified control from this response if present.
   * 
   * @param oid
   *          The OID of the control to be removed.
   * @return The removed control, or {@code null} if the control is not
   *         included with this response.
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
   * Returns a string representation of this response.
   * 
   * @return A string representation of this response.
   */
  @Override
  public final String toString()
  {
    StringBuilder builder = new StringBuilder();
    toString(builder);
    return builder.toString();
  }



  /**
   * Appends a string representation of this response to the provided
   * buffer.
   * 
   * @param buffer
   *          The buffer into which a string representation of this
   *          response should be appended.
   */
  public abstract void toString(StringBuilder buffer);
}
