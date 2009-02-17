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



import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opends.server.types.DirectoryException;
import org.opends.server.types.OperationType;



/**
 * A generic raw request. This interface defines methods common to all
 * types of raw request. A raw request is a request whose parameters
 * have not been fully decoded. A raw request is decoded using a call to
 * {@link #toRequest(Schema)}.
 */
public abstract class RawRequest
{

  // The list of controls included with this request.
  private final List<RawControl> controls =
      new LinkedList<RawControl>();

  // The type of this operation.
  private final OperationType operationType;



  /**
   * Creates a new request having the specified operation type.
   *
   * @param operationType
   *          The type of this request.
   */
  RawRequest(OperationType operationType)
  {
    this.operationType = operationType;
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
  public final boolean addControl(RawControl control)
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
  public final RawControl getControl(String oid)
  {
    // Avoid creating an iterator if possible.
    if (controls.isEmpty())
    {
      return null;
    }

    for (RawControl control : controls)
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
   *         this request
   */
  public final Iterable<RawControl> getControls()
  {
    return controls;
  }



  /**
   * Returns the type of this request.
   *
   * @return The type of this request.
   */
  public final OperationType getOperationType()
  {
    return operationType;
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
  public final RawControl removeControl(String oid)
  {
    // Avoid creating an iterator if possible.
    if (controls.isEmpty())
    {
      return null;
    }

    Iterator<RawControl> iterator = controls.iterator();
    while (iterator.hasNext())
    {
      RawControl control = iterator.next();
      if (control.getOID().equals(oid))
      {
        iterator.remove();
        return control;
      }
    }

    return null;
  }



  /**
   * Returns a decoded request representing this raw request. Subsequent
   * changes to this raw request will not be reflected in the returned
   * request.
   *
   * @param schema
   *          The schema to use when decoding this raw request.
   * @return A decoded request representing this raw request.
   * @throws DirectoryException
   *           If this raw request could not be decoded.
   */
  public abstract Request toRequest(Schema schema)
      throws DirectoryException;



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
