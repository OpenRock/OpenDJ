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
 *      Copyright 2008-2009 Sun Microsystems, Inc.
 */
package org.opends.server.core.dataproviders;



import java.util.Set;

import org.opends.sdk.*;
import org.opends.sdk.requests.*;
import org.opends.sdk.responses.BindResult;
import org.opends.sdk.responses.CompareResult;
import org.opends.sdk.responses.ExtendedResult;
import org.opends.sdk.responses.Result;
import org.opends.server.types.CanceledOperationException;
import org.opends.server.types.DirectoryException;



/**
 * A connection to a data provider. When a connection is no longer
 * needed it must be closed.
 */
public interface DataProviderConnection
{

  /**
   * Closes this data provider connection. When this method returns the
   * connection can no longer be used.
   */
  void close();



  /**
   * Indicates whether the underlying data provider contains the
   * specified entry.
   *
   * @param dn
   *          The DN of the entry.
   * @return {@code true} if the underlying data provider contains the
   *         specified entry, or {@code false} if it does not.
   * @throws DirectoryException
   *           If a problem occurs while trying to make the
   *           determination, or if {@code dn} is not a DN equal to or
   *           subordinate to one of the base DNs managed by the
   *           underlying data provider.
   */
  boolean containsEntry(DN dn) throws DirectoryException;



  /**
   * Deregisters an event listener from the underlying data provider.
   *
   * @param listener
   *          The event listener.
   */
  void deregisterEventListener(DataProviderEventListener listener);



  /**
   * Executes an add request against the underlying data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The add request to execute.
   * @param resultHandler
   *          The response handler which will be notified when the
   *          request completes.
   * @throws CanceledOperationException
   *           If this add request should be canceled.
   */
  void executeAdd(RequestContext context, AddRequest request,
                  ResultHandler<Result> resultHandler,
                  IntermediateResponseHandler intermediateResponseHandler)
      throws UnsupportedOperationException, CanceledOperationException;



  /**
   * Executes a bind request against the underlying data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The bind request to execute.
   * @param resultHandler
   *          The response handler which will be notified when the
   *          request completes.
   * @throws CanceledOperationException
   *           If this bind request should be canceled.
   */
  void executeBind(RequestContext context, SimpleBindRequest request,
                   ResultHandler<? super BindResult> resultHandler,
                   IntermediateResponseHandler intermediateResponseHandler)
      throws UnsupportedOperationException, CanceledOperationException;



  /**
   * Executes a compare request against the underlying data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The compare request to execute.
   * @param resultHandler
   *          The response handler which will be notified when the
   *          request completes.
   * @throws CanceledOperationException
   *           If this compare request should be canceled.
   */
  void executeCompare(RequestContext context, CompareRequest request,
                      ResultHandler<? super CompareResult> resultHandler,
                      IntermediateResponseHandler intermediateResponseHandler)
      throws UnsupportedOperationException, CanceledOperationException;



  /**
   * Executes a delete request against the underlying data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The delete request to execute.
   * @param resultHandler
   *          The response handler which will be notified when the
   *          request completes.
   * @throws CanceledOperationException
   *           If this delete request should be canceled.
   */
  void executeDelete(RequestContext context, DeleteRequest request,
                     ResultHandler<Result> resultHandler,
                     IntermediateResponseHandler intermediateResponseHandler)
      throws UnsupportedOperationException, CanceledOperationException;



  /**
   * Executes an extended request against the underlying data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The extended request to execute.
   * @param resultHandler
   *          The extended response handler which will be notified when
   *          the request completes.
   * @throws CanceledOperationException
   *           If this delete request should be canceled.
   */
  <R extends ExtendedResult> void executeExtended(
      RequestContext context, ExtendedRequest<R> request,
      ResultHandler<R> resultHandler,
      IntermediateResponseHandler intermediateResponseHandler)
      throws UnsupportedOperationException, CanceledOperationException;



  /**
   * Executes a modify request against the underlying data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The modify request to execute.
   * @param resultHandler
   *          The response handler which will be notified when the
   *          request completes.
   * @throws CanceledOperationException
   *           If this modify request should be canceled.
   */
  void executeModify(RequestContext context, ModifyRequest request,
                     ResultHandler<Result> resultHandler,
                     IntermediateResponseHandler intermediateResponseHandler)
      throws UnsupportedOperationException, CanceledOperationException;



  /**
   * Executes a modify DN request against the underlying data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The modify DN request to execute.
   * @param resultHandler
   *          The response handler which will be notified when the
   *          request completes.
   * @throws CanceledOperationException
   *           If this modify DN request should be canceled.
   */
  void executeModifyDN(RequestContext context, ModifyDNRequest request,
                       ResultHandler<Result> resultHandler,
                       IntermediateResponseHandler intermediateResponseHandler)
      throws UnsupportedOperationException, CanceledOperationException;



  /**
   * Executes a search request against the underlying data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The search request to execute.
   * @param resultHandler
   *          The search response handler which will be used to process
   *          search response entries, response referrals, and be
   *          notified when the request completes.
   * @throws CanceledOperationException
   *           If this search request should be canceled.
   */
  void executeSearch(RequestContext context, SearchRequest request,
                     SearchResultHandler resultHandler,
                     IntermediateResponseHandler intermediateResponseHandler)
      throws UnsupportedOperationException, CanceledOperationException;



  /**
   * Returns an unmodifiable set containing the base DNs of the
   * sub-trees which the underlying data provider contains.
   *
   * @return An unmodifiable set containing the base DNs of the
   *         sub-trees which the underlying data provider contains.
   */
  Set<DN> getBaseDNs();



  /**
   * Retrieves the specified entry from the underlying data provider.
   *
   * @param dn
   *          The DN of the entry.
   * @return The requested entry, or {@code null} if the underlying data
   *         provider does not contain the specified entry.
   * @throws DirectoryException
   *           If a problem occurs while trying to retrieve the entry,
   *           or if {@code dn} is not a DN equal to or subordinate to
   *           one of the base DNs managed by the underlying data
   *           provider.
   */
  Entry getEntry(DN dn) throws DirectoryException;



  /**
   * Returns the current status of the provided base DN in the
   * underlying data provider.
   *
   * @param baseDN
   *          The base DN in the underlying data provider.
   * @return The current status of the provided base DN in the
   *         underlying data provider.
   * @throws DirectoryException
   *           If {@code baseDN} is not one of the base DNs managed by
   *           the underlying data provider.
   */
  DataProviderStatus getStatus(DN baseDN) throws DirectoryException;



  /**
   * Returns an unmodifiable set containing the OIDs of the controls
   * that may be supported by the provided base DN in the underlying
   * data provider.
   *
   * @param baseDN
   *          The base DN in the underlying data provider.
   * @return An unmodifiable set containing the OIDs of the controls
   *         that may be supported by the provided base DN in the
   *         underlying data provider.
   * @throws DirectoryException
   *           If {@code baseDN} is not one of the base DNs managed by
   *           the underlying data provider.
   */
  Set<String> getSupportedControls(DN baseDN) throws DirectoryException;



  /**
   * Returns an unmodifiable set containing the OIDs of the features
   * that may be supported by the provided base DN in the underlying
   * data provider.
   *
   * @param baseDN
   *          The base DN in the underlying data provider.
   * @return An unmodifiable set containing the OIDs of the features
   *         that may be supported by the provided base DN in the
   *         underlying data provider.
   * @throws DirectoryException
   *           If {@code baseDN} is not one of the base DNs managed by
   *           the underlying data provider.
   */
  Set<String> getSupportedFeatures(DN baseDN) throws DirectoryException;



  /**
   * Registers an event listener with the underlying data provider.
   *
   * @param listener
   *          The event listener.
   */
  void registerEventListener(DataProviderEventListener listener);


  /**
   * Indicates whether or not the provided base DN in the underlying
   * data provider supports change notification.
   *
   * @param baseDN
   *          The base DN in the underlying data provider.
   * @return {@code true} if the provided base DN in the underlying data
   *         provider supports change notification.
   * @throws DirectoryException
   *           If {@code baseDN} is not one of the base DNs managed by
   *           the underlying data provider.
   */
  boolean supportsChangeNotification(DN baseDN)
      throws DirectoryException;
}
