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



import org.opends.ldap.controls.Control;
import org.opends.server.types.ByteString;



/**
 * A generic Intermediate response provides a mechanism for
 * communicating unrecognized or unsupported Intermediate responses to
 * the client.
 */
public interface GenericIntermediateResponse extends
    IntermediateResponse
{

  /**
   * {@inheritDoc}
   */
  GenericIntermediateResponse addControl(Control control)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  GenericIntermediateResponse clearControls()
      throws UnsupportedOperationException;



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
   * {@inheritDoc}
   */
  String toString();



  /**
   * {@inheritDoc}
   */
  StringBuilder toString(StringBuilder builder)
      throws NullPointerException;



  /**
   * {@inheritDoc}
   */
  String getResponseName();



  /**
   * {@inheritDoc}
   */
  ByteString getResponseValue();



  /**
   * {@inheritDoc}
   */
  GenericIntermediateResponse setResponseName(String name)
      throws UnsupportedOperationException;



  /**
   * Sets the response value associated with this generic intermediate
   * response.
   *
   * @param value
   *          The response value associated with this generic
   *          intermediate response, which may be {@code null}
   *          indicating that none was provided.
   * @return This generic intermediate response.
   * @throws UnsupportedOperationException
   *           If this generic intermediate response does not permit the
   *           response value to be set.
   */
  GenericIntermediateResponse setResponseValue(ByteString value)
      throws UnsupportedOperationException;

}