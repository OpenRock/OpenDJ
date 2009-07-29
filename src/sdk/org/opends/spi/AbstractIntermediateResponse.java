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

package org.opends.spi;



import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opends.ldap.controls.Control;
import org.opends.ldap.responses.IntermediateResponse;
import org.opends.server.types.ByteString;



/**
 * An abstract LDAP intermediate response message implementation, which
 * can be used as the basis for implementing new intermediate responses.
 *
 * @param <R>
 *          The type of intermediate response.
 */
public abstract class AbstractIntermediateResponse<R extends IntermediateResponse>
    implements IntermediateResponse<R>
{
  private final List<Control> controls = new LinkedList<Control>();
  private String name = null;



  /**
   * Creates a new intermediate response.
   */
  protected AbstractIntermediateResponse()
  {
    this(null);
  }



  /**
   * Creates a new intermediate response using the provided response
   * name.
   *
   * @param name
   *          The response name associated with this intermediate
   *          response, which may be {@code null} indicating that none
   *          was provided.
   */
  protected AbstractIntermediateResponse(String name)
  {
    this.name = name;
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
  public final String getResponseName()
  {
    return name;
  }



  /**
   * {@inheritDoc}
   */
  public abstract ByteString getResponseValue();



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
  public final R setResponseName(String name)
  {
    this.name = name;
    return getThis();
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
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("IntermediateResponse(responseName=");
    buffer.append(name == null ? "" : name);
    buffer.append(", responseValue=");
    ByteString value = getResponseValue();
    buffer.append(value == null ? ByteString.empty() : value);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }



  /**
   * Returns a type-safe reference to this response.
   *
   * @return This response as a T.
   */
  @SuppressWarnings("unchecked")
  private final R getThis()
  {
    return (R) this;
  }
}
