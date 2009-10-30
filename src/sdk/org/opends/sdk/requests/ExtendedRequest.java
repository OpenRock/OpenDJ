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
import org.opends.sdk.extensions.ExtendedOperation;
import org.opends.sdk.extensions.StartTLSRequest;
import org.opends.sdk.responses.Result;
import org.opends.sdk.util.ByteString;



/**
 * The Extended operation allows additional operations to be defined for
 * services not already available in the protocol; for example, to
 * implement an operation which installs transport layer security (see
 * {@link StartTLSRequest}).
 *
 * @param <S>
 *          The type of result.
 */
public interface ExtendedRequest<S extends Result> extends Request
{

  /**
   * {@inheritDoc}
   */
  ExtendedRequest<S> addControl(Control control)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  ExtendedRequest<S> clearControls()
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
   * Returns the dotted-decimal representation of the unique OID
   * corresponding to this extended request.
   *
   * @return The dotted-decimal representation of the unique OID.
   */
  String getRequestName();



  /**
   * Returns the content of this extended request in a form defined by
   * the extended request.
   *
   * @return The content of this extended request, or {@code null} if
   *         there is no content.
   */
  ByteString getRequestValue();



  /**
   * Sets the dotted-decimal representation of the unique OID
   * corresponding to this extended request.
   *
   * @param oid
   *          The dotted-decimal representation of the unique OID
   *          corresponding to this extended request.
   * @return This extended request.
   * @throws UnsupportedOperationException
   *           If this extended request does not permit the response
   *           name to be set.
   * @throws NullPointerException
   *           If {@code oid} was {@code null}.
   */
  ExtendedRequest<S> setRequestName(String oid)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Returns the extended operation associated with this extended
   * request.
   * <p>
   * FIXME: this should not be exposed to clients.
   *
   * @return The extended operation associated with this extended
   *         request.
   */
  ExtendedOperation<?, S> getExtendedOperation();

}