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
   * entry.
   * <p>
   * TODO: exceptions, return value?
   * 
   * @param entry
   *          The entry being returned to the client.
   * @param controls
   *          The non-{@code null} possibly empty list of controls being
   *          returned to the client.
   */
  void handleSearchEntry(Entry entry, List<Control> controls);



  /**
   * Performs any processing required to handle the provided search
   * references.
   * <p>
   * TODO: exceptions, return value?
   * 
   * @param urls
   *          The non-empty list of referral urls being returned to the
   *          client.
   * @param controls
   *          The non-{@code null} possibly empty list of controls being
   *          returned to the client.
   */
  void handleSearchReference(List<String> urls, List<Control> controls);
}
