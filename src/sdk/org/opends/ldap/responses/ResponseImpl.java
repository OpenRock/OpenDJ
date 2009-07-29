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

import org.opends.ldap.controls.Control;



/**
 * An abstract LDAP response message.
 *
 * @param <R>
 *          The type of response.
 */
abstract class ResponseImpl<R extends Response> implements
    Response
{

  // The list of controls included with this response.
  private final List<Control> controls = new LinkedList<Control>();



  /**
   * Creates a new response.
   */
  ResponseImpl()
  {
    // Nothing to do.
  }



  /**
   * {@inheritDoc}
   */
  public final R addControl(Control control)
      throws NullPointerException
  {
    if (control == null)
    {
      throw new NullPointerException();
    }

    controls.add(control);
    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public final R clearControls()
  {
    controls.clear();
    return getThis();
  }



  /**
   * {@inheritDoc}
   */
  public final Control getControl(String oid)
  {
    if (oid == null)
    {
      throw new NullPointerException();
    }

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
   * {@inheritDoc}
   */
  public final Iterable<Control> getControls()
  {
    return controls;
  }



  /**
   * {@inheritDoc}
   */
  public final boolean hasControls()
  {
    return !controls.isEmpty();
  }



  /**
   * {@inheritDoc}
   */
  public final Control removeControl(String oid)
      throws NullPointerException
  {
    if (oid == null)
    {
      throw new NullPointerException();
    }

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
   * {@inheritDoc}
   */
  @Override
  public final String toString()
  {
    StringBuilder builder = new StringBuilder();
    toString(builder);
    return builder.toString();
  }



  /**
   * {@inheritDoc}
   */
  public abstract void toString(StringBuilder buffer);



  /**
   * Returns a type-safe reference to this response.
   *
   * @return This response as a T.
   */
  @SuppressWarnings("unchecked")
  final R getThis()
  {
    return (R) this;
  }
}
