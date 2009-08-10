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
 * An Intermediate response provides a general mechanism for defining
 * single-request/multiple-response operations. This response is
 * intended to be used in conjunction with the Extended operation to
 * define new single-request/multiple-response operations or in
 * conjunction with a control when extending existing operations in a
 * way that requires them to return Intermediate response information.
 * <p>
 * An Intermediate response may convey an optional response name and
 * value. These can be retrieved using the {@link #getResponseName} and
 * {@link #getResponseValue} methods respectively.
 *
 * @param <S>
 *          The type of intermediate response.
 */
public interface IntermediateResponse<S extends IntermediateResponse>
    extends Response<S>
{

  /**
   * {@inheritDoc}
   */
  S addControl(Control control) throws UnsupportedOperationException,
      NullPointerException;



  /**
   * {@inheritDoc}
   */
  S clearControls() throws UnsupportedOperationException;



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
   * Returns the response name associated with this intermediate
   * response.
   *
   * @return The response name associated with this intermediate
   *         response, which may be {@code null} if none was provided.
   */
  String getResponseName();



  /**
   * Returns the response value associated with this intermediate
   * response.
   *
   * @return The response value associated with this intermediate
   *         response, which may be {@code null} if none was provided.
   */
  ByteString getResponseValue();



  /**
   * Sets the response name associated with this intermediate response.
   *
   * @param name
   *          The response name associated with this intermediate
   *          response, which may be {@code null} indicating that none
   *          was provided.
   * @return This intermediate response.
   * @throws UnsupportedOperationException
   *           If this intermediate response does not permit the
   *           response name to be set.
   */
  S setResponseName(String name) throws UnsupportedOperationException;

}