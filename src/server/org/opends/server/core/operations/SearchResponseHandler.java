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

import org.opends.server.types.Control;
import org.opends.server.types.Entry;



/**
 * An interface for processing search response messages.
 */
public interface SearchResponseHandler extends ResponseHandler
{
  /**
   * Performs any processing required to handle the provided search
   * result entry.
   *
   * @param entry
   *          The entry being returned to the client.
   * @param controls
   *          The non-{@code null} possibly empty unmodifiable list of
   *          controls being returned to the client.
   * @return {@code true} if the caller should continue processing the
   *         request and sending additional responses, or {@code false}
   *         if not for some reason (e.g. the request has been abandoned
   *         or a resource limit exceeded).
   */
  boolean handleSearchResponseEntry(Entry entry, List<Control> controls);



  /**
   * Performs any processing required to handle the provided search
   * result reference.
   *
   * @param urls
   *          The non-{@code null} non-empty unmodifiable list of
   *          reference URLs being returned to the client.
   * @param controls
   *          The non-{@code null} possibly empty unmodifiable list of
   *          controls being returned to the client.
   * @return {@code true} if the caller should continue processing the
   *         request and sending additional responses, or {@code false}
   *         if not for some reason (e.g. the request has been abandoned
   *         or a resource limit exceeded).
   */
  boolean handleSearchResponseReference(List<String> urls,
      List<Control> controls);
}
