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



/**
 * An interface for processing extended response messages and any
 * intermediate responses.
 */
public interface ExtendedResponseHandler
{
  /**
   * Performs any processing required to handle the provided extended
   * response.
   * 
   * @param response
   *          The extended response being returned to the client.
   */
  void handleExtendedResponse(ExtendedResponse response);



  /**
   * Performs any processing required to handle the provided
   * intermediate response.
   * 
   * @param response
   *          The intermediate response being returned to the client.
   * @return {@code true} if the caller should continue processing the
   *         request and sending additional intermediate responses, or
   *         {@code false} if not for some reason (e.g. the request has
   *         been abandoned or a resource limit exceeded).
   */
  boolean handleIntermediateResponse(IntermediateResponse response);
}
