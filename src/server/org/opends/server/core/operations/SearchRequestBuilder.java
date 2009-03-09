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



import org.opends.server.types.DN;



/**
 * A class for incrementally constructing search requests.
 */
public final class SearchRequestBuilder
{
  /**
   * Creates a new search request builder using the provided base DN.
   * The search request builder will have the following default
   * parameters:
   * <ul>
   * <li>base object search scope
   * <li>a filter of {@code (objectClass=*)}
   * <li>return all attributes
   * <li>no controls
   * </ul>
   *
   * @param baseDN
   *          The search base DN.
   */
  public SearchRequestBuilder(DN baseDN)
  {
    // NYI.
  }



  // TODO: setters for search request parameters.

  /**
   * Returns a {@code SearchRequest} representing the current state of
   * this search request builder. Subsequent changes to this builder
   * will not impact the return request.
   *
   * @return A {@code SearchRequest} representing the current state of
   *         this search request builder.
   */
  public SearchRequest toRequest()
  {
    // NYI.
    return null;
  }
}
