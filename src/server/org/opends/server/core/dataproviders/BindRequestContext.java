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
 *       Copyright 2010 Sun Microsystems, Inc.
 */

package org.opends.server.core.dataproviders;

import org.opends.sdk.Entry;
import org.opends.sdk.requests.BindRequest;

/**
 * The bind request context allows for updating the authentication state of the
 * connection.
 */
public interface BindRequestContext extends RequestContext {

  /**
   * Specifies information about the authentication that has been
   * performed for this connection.
   *
   * @param bindRequest The bind request the client used for authentication.
   * @param authenticationEntry
   *          The entry for the user as whom the client is
   *          authenticated, or {@code null} if the client is
   *          unauthenticated.
   * @param  authorizationEntry  The entry for the user that should be
   *                             considered the authorization identity
   *                             for this client, or {@code null}
   *                             if it should be the unauthenticated
   *                             user.
   */
  void setAuthenticationInfo(BindRequest bindRequest,
                             Entry authenticationEntry,
                             Entry authorizationEntry);
}
