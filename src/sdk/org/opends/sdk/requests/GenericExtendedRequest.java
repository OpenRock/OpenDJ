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



import org.opends.sdk.ResultCode;
import org.opends.sdk.controls.Control;
import org.opends.sdk.extensions.GenericExtendedOperation;
import org.opends.sdk.responses.GenericExtendedResult;
import org.opends.sdk.util.ByteString;



/**
 * A generic Extended request which should be used for unsupported
 * extended operations. Servers list the names of Extended requests they
 * recognize in the {@code supportedExtension} attribute in the root
 * DSE. Where the name is not recognized, the server returns
 * {@link ResultCode#PROTOCOL_ERROR} (the server may return this error
 * in other cases).
 */
public interface GenericExtendedRequest extends
    ExtendedRequest<GenericExtendedResult>
{

  /**
   * {@inheritDoc}
   */
  GenericExtendedRequest addControl(Control control)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  GenericExtendedRequest clearControls()
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
  ByteString getRequestValue();



  /**
   * Sets the content of this generic extended request in a form defined
   * by the extended request.
   *
   * @param bytes
   *          The content of this generic extended request in a form
   *          defined by the extended request, or {@code null} if there
   *          is no content.
   * @return This generic extended request.
   * @throws UnsupportedOperationException
   *           If this generic extended request does not permit the
   *           request value to be set.
   */
  GenericExtendedRequest setRequestValue(ByteString bytes)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  GenericExtendedOperation getExtendedOperation();



  /**
   * {@inheritDoc}
   */
  String getRequestName();



  /**
   * {@inheritDoc}
   */
  GenericExtendedRequest setRequestName(String oid)
      throws UnsupportedOperationException, NullPointerException;
}
