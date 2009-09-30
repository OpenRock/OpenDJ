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



import org.opends.sdk.controls.Control;



/**
 * The Bind operation allows authentication information to be exchanged
 * between the client and server. The Bind operation should be thought
 * of as the "authenticate" operation.
 */
public interface BindRequest extends Request
{

  /**
   * {@inheritDoc}
   */
  BindRequest addControl(Control control)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  BindRequest clearControls() throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  Control getControl(String oid) throws NullPointerException;



  /**
   * {@inheritDoc}
   */
  Iterable<Control> getControls();



  /**
   * {@inheritDoc}
   */
  boolean hasControls();



  /**
   * {@inheritDoc}
   */
  Control removeControl(String oid)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Returns the distinguished name of the Directory object that the
   * client wishes to bind as. The distinguished name may be empty (but
   * never {@code null}) when used for of anonymous binds, or when using
   * SASL authentication. The server shall not dereference any aliases
   * in locating the named object.
   *
   * @return The distinguished name of the Directory object that the
   *         client wishes to bind as.
   */
  String getName();



  /**
   * Sets the distinguished name of the Directory object that the client
   * wishes to bind as. The distinguished name may be empty (but never
   * {@code null} when used for of anonymous binds, or when using SASL
   * authentication. The server shall not dereference any aliases in
   * locating the named object.
   *
   * @param dn
   *          The distinguished name of the Directory object that the
   *          client wishes to bind as.
   * @return This bind request.
   * @throws UnsupportedOperationException
   *           If this bind request does not permit the distinguished
   *           name to be set.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  BindRequest setName(String dn) throws UnsupportedOperationException,
      NullPointerException;
}