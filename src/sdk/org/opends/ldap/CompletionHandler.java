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

package org.opends.ldap;



import org.opends.ldap.responses.ErrorResultException;



/**
 * A handler for consuming the result of an asynchronous operation.
 * <p>
 * The connection and connection factory interfaces defined in this
 * package allow a completion handler to be specified to consume the
 * result of an asynchronous operation. The {@code completed} method is
 * invoked when the operation completes successfully. The {@code failed}
 * method is invoked if the operations fails.
 * <p>
 * Implementations of these methods should complete in a timely manner
 * so as to avoid keeping the invoking thread from dispatching to other
 * completion handlers.
 *
 * @param <T>
 *          The type of object handled by this completion handler.
 */
public interface CompletionHandler<T>
{
  /**
   * Invoked when the asynchronous operation has completed successfully.
   *
   * @param result
   *          The result of the asynchronous operation.
   */
  void completed(T result);



  /**
   * Invoked when the asynchronous operation has failed.
   *
   * @param error
   *          The error result exception indicating why the asynchronous
   *          operation has failed.
   */
  void failed(ErrorResultException error);
}
