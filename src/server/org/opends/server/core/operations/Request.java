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



import java.util.List;

import org.opends.server.controls.ControlDecoder;
import org.opends.server.types.Control;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.OperationType;



/**
 * A generic operation request. This interface defines methods common to
 * all types of request.
 */
public interface Request
{
  /**
   * Returns the specified control included with this request, decoding
   * it using the specified decoder.
   *
   * @param <T>
   *          The type of the requested control.
   * @param d
   *          The requested control's decoder.
   * @return The decoded control, or {@code null} if the control is not
   *         included with this request.
   * @throws DirectoryException
   *           If the control was found but it could not be decoded.
   */
  <T extends Control> T getControl(ControlDecoder<T> d)
      throws DirectoryException;



  /**
   * Returns an unmodifiable list containing the controls included with
   * this request. The returned list may be empty (but never {@code
   * null}) if there are no controls associated with this request.
   *
   * @return The unmodifiable list containing the controls included with
   *         this request.
   */
  List<Control> getControls();



  /**
   * Returns the type of this request.
   *
   * @return The type of this request.
   */
  OperationType getType();



  /**
   * Returns a raw request representing this request. Subsequent changes
   * to the returned raw request will not be reflected in this request.
   * <p>
   * TODO: use covalent return types.
   *
   * @return A raw request representing this request.
   */
  RawRequest toRawRequest();



  /**
   * Returns a string representation of this request.
   *
   * @return A string representation of this request.
   */
  String toString();



  /**
   * Appends a string representation of this request to the provided
   * buffer.
   *
   * @param buffer
   *          The buffer into which a string representation of this
   *          request should be appended.
   */
  void toString(StringBuilder buffer);
}
