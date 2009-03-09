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

import org.opends.server.core.operations.AddRequest;
import org.opends.server.core.operations.BindRequest;
import org.opends.server.core.operations.CompareRequest;
import org.opends.server.core.operations.Context;
import org.opends.server.core.operations.DeleteRequest;
import org.opends.server.core.operations.ExtendedRequest;
import org.opends.server.core.operations.ExtendedResponseHandler;
import org.opends.server.core.operations.ModifyDNRequest;
import org.opends.server.core.operations.ModifyRequest;
import org.opends.server.core.operations.ResponseHandler;
import org.opends.server.core.operations.SearchRequest;
import org.opends.server.core.operations.SearchResponseHandler;
import org.opends.server.types.CanceledOperationException;
import org.opends.server.types.DN;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.Entry;
import org.opends.server.types.SearchFilter;
import org.opends.server.types.SearchScope;



/**
 * An entry container which provides the content of one or more
 * sub-trees.
 * <p>
 * A data provider can be:
 * <ul>
 * <li>a simple data source such as a local back-end, a remote LDAP
 * server or a local LDIF file.
 * <li>used to route operations. This is the case for load balancing and
 * distribution.
 * <li>combine and transform data from underlying data providers. For
 * example, DN mapping, attribute renaming, attribute value
 * transformations, etc.
 * </ul>
 * Data providers operate in two states:
 * <ul>
 * <li>initialized
 * <li>accepting requests
 * </ul>
 * Data providers are created in the <i>initialized</i> state. In this
 * state a data provider has validated its configuration and registered
 * support for off-line services such as export, import, backup, and
 * restore if available.
 * <p>
 * A data provider transitions to the <i>accepting requests</i> state
 * when the {@link #startDataProvider()} method is invoked. In this
 * state a data provider has acquired any remaining resources that it
 * needs in order to be fully operational. This may include connections
 * to underlying data providers. See the documentation for
 * {@link #startDataProvider()} for more information.
 * <p>
 * A data provider transitions back to the <i>initialized</i> state
 * using the {@link #stopDataProvider()} method. This occurs when the
 * data provider is no longer needed in order process client requests,
 * but may still be needed in order to perform off-line services such as
 * import, export, backup, and restore.
 * <p>
 * If data provider is disabled or deleted from the server configuration
 * or if the server is shutdown, then the
 * {@link #finalizeDataProvider()} method is invoked. This method should
 * ensure that the data provider is stopped and no longer available for
 * off-line services such as import, export, backup, and restore.
 */
public interface DataProvider
{

  /**
   * Indicates whether this data provider contains the specified entry.
   *
   * @param dn
   *          The DN of the entry.
   * @return {@code true} if this data provider contains the specified
   *         entry, or {@code false} if it does not.
   * @throws DirectoryException
   *           If a problem occurs while trying to make the
   *           determination, or if {@code dn} is not a DN equal to or
   *           subordinate to one of the base DNs managed by this data
   *           provider.
   */
  boolean containsEntry(DN dn) throws DirectoryException;



  /**
   * Deregisters a change listener from this data provider.
   * <p>
   * Implementations must throw an {@code UnsupportedOperationException}
   * if change notification is not supported for the provided base DN.
   *
   * @param baseDN
   *          The base DN in this data provider.
   * @param listener
   *          The change listener.
   * @throws UnsupportedOperationException
   *           If this data provider does not support change
   *           notification for the provided base DN.
   * @throws DirectoryException
   *           If {@code baseDN} is not one of the base DNs managed by
   *           this data provider.
   */
  void deregisterChangeListener(DN baseDN,
      DataProviderChangeListener listener)
      throws UnsupportedOperationException, DirectoryException;



  /**
   * Deregisters an event listener from this data provider.
   *
   * @param listener
   *          The event listener.
   */
  void deregisterEventListener(DataProviderEventListener listener);



  /**
   * Executes an add request against this data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The add request to execute.
   * @param responseHandler
   *          The response handler which will be notified when the
   *          request completes.
   * @throws CanceledOperationException
   *           If this add request should be canceled.
   * @throws DirectoryException
   *           If the request target entry is not a DN equal to or
   *           subordinate to one of the base DNs managed by this data
   *           provider.
   */
  void executeAdd(Context context, AddRequest request,
      ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a bind request against this data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The bind request to execute.
   * @param responseHandler
   *          The response handler which will be notified when the
   *          request completes.
   * @throws CanceledOperationException
   *           If this bind request should be canceled.
   * @throws DirectoryException
   *           If the request target entry is not a DN equal to or
   *           subordinate to one of the base DNs managed by this data
   *           provider.
   */
  void executeBind(Context context, BindRequest request,
      ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a compare request against this data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The compare request to execute.
   * @param responseHandler
   *          The response handler which will be notified when the
   *          request completes.
   * @throws CanceledOperationException
   *           If this compare request should be canceled.
   * @throws DirectoryException
   *           If the request's target entry is not a DN equal to or
   *           subordinate to one of the base DNs managed by this data
   *           provider.
   */
  void executeCompare(Context context, CompareRequest request,
      ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a delete request against this data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The delete request to execute.
   * @param responseHandler
   *          The response handler which will be notified when the
   *          request completes.
   * @throws CanceledOperationException
   *           If this delete request should be canceled.
   * @throws DirectoryException
   *           If the request's target entry is not a DN equal to or
   *           subordinate to one of the base DNs managed by this data
   *           provider.
   */
  void executeDelete(Context context, DeleteRequest request,
      ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes an extended request against this data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The extended request to execute.
   * @param responseHandler
   *          The extended response handler which will be notified when
   *          the request completes.
   * @throws CanceledOperationException
   *           If this delete request should be canceled.
   * @throws DirectoryException
   *           If the request's target entry is not a DN equal to or
   *           subordinate to one of the base DNs managed by this data
   *           provider.
   */
  void executeExtended(Context context, ExtendedRequest request,
      ExtendedResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a modify request against this data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The modify request to execute.
   * @param responseHandler
   *          The response handler which will be notified when the
   *          request completes.
   * @throws CanceledOperationException
   *           If this modify request should be canceled.
   * @throws DirectoryException
   *           If the request's target entry is not a DN equal to or
   *           subordinate to one of the base DNs managed by this data
   *           provider.
   */
  void executeModify(Context context, ModifyRequest request,
      ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a modify DN request against this data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The modify DN request to execute.
   * @param responseHandler
   *          The response handler which will be notified when the
   *          request completes.
   * @throws CanceledOperationException
   *           If this modify DN request should be canceled.
   * @throws DirectoryException
   *           If the request's target entry or new superior entry is
   *           not a DN equal to or subordinate to one of the base DNs
   *           managed by this data provider.
   */
  void executeModifyDN(Context context, ModifyDNRequest request,
      ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a search request against this data provider. The data
   * provider will execute the request and notify the provided response
   * handler on completion.
   * <p>
   * Implementations may return before the request has completed.
   *
   * @param context
   *          The request context.
   * @param request
   *          The search request to execute.
   * @param responseHandler
   *          The search response handler which will be used to process
   *          search response entries, response referrals, and be
   *          notified when the request completes.
   * @throws CanceledOperationException
   *           If this search request should be canceled.
   * @throws DirectoryException
   *           If the request's target entry is not a DN equal to or
   *           subordinate to one of the base DNs managed by this data
   *           provider.
   */
  void executeSearch(Context context, SearchRequest request,
      SearchResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Performs any necessary work to finalize this data provider. This
   * may include closing any connections to underlying data providers,
   * databases, and deregistering any listeners, etc.
   * <p>
   * This method may be called during the Directory Server shutdown
   * process or if a data provider is disabled with the server online.
   * It must not return until this data provider is finalized.
   * <p>
   * Implementations should assume that this data provider has already
   * been stopped using {@link #stopDataProvider()}.
   * <p>
   * Implementations must deregister any listeners such as those
   * required for performing import, export, backup, and restore.
   * <p>
   * Implementations must not throw any exceptions. If any problems are
   * encountered, then they may be logged but the closure should
   * progress as completely as possible.
   */
  void finalizeDataProvider();



  /**
   * Returns an unmodifiable set containing the base DNs of the
   * sub-trees which this data provider contains.
   *
   * @return An unmodifiable set containing the base DNs of the
   *         sub-trees which this data provider contains.
   */
  Set<DN> getBaseDNs();



  /**
   * Retrieves the specified entry from this data provider.
   *
   * @param dn
   *          The DN of the entry.
   * @return The requested entry, or {@code null} if this data provider
   *         does not contain the specified entry.
   * @throws DirectoryException
   *           If a problem occurs while trying to retrieve the entry,
   *           or if {@code dn} is not a DN equal to or subordinate to
   *           one of the base DNs managed by this data provider.
   */
  Entry getEntry(DN dn) throws DirectoryException;



  /**
   * Returns the current status of the provided base DN in this data
   * provider.
   *
   * @param baseDN
   *          The base DN in this data provider.
   * @return The current status of the provided base DN in this data
   *         provider.
   * @throws DirectoryException
   *           If {@code baseDN} is not one of the base DNs managed by
   *           this data provider.
   */
  DataProviderStatus getStatus(DN baseDN) throws DirectoryException;



  /**
   * Returns an unmodifiable set containing the OIDs of the controls
   * that may be supported by the provided base DN in this data
   * provider.
   *
   * @param baseDN
   *          The base DN in this data provider.
   * @return An unmodifiable set containing the OIDs of the controls
   *         that may be supported by the provided base DN in this data
   *         provider.
   * @throws DirectoryException
   *           If {@code baseDN} is not one of the base DNs managed by
   *           this data provider.
   */
  Set<String> getSupportedControls(DN baseDN) throws DirectoryException;



  /**
   * Returns an unmodifiable set containing the OIDs of the features
   * that may be supported by the provided base DN in this data
   * provider.
   *
   * @param baseDN
   *          The base DN in this data provider.
   * @return An unmodifiable set containing the OIDs of the features
   *         that may be supported by the provided base DN in this data
   *         provider.
   * @throws DirectoryException
   *           If {@code baseDN} is not one of the base DNs managed by
   *           this data provider.
   */
  Set<String> getSupportedFeatures(DN baseDN) throws DirectoryException;



  /**
   * Registers a change listener with this data provider. The change
   * listener will be notified whenever an update occurs within the
   * subtree associated with the specified base DN in this data
   * provider.
   * <p>
   * Implementations must throw an {@code UnsupportedOperationException}
   * if change notification is not supported for the provided base DN.
   *
   * @param baseDN
   *          The base DN in this data provider.
   * @param listener
   *          The change listener.
   * @throws UnsupportedOperationException
   *           If this data provider does not support change
   *           notification for the provided base DN.
   * @throws DirectoryException
   *           If {@code baseDN} is not one of the base DNs managed by
   *           this data provider.
   */
  void registerChangeListener(DN baseDN,
      DataProviderChangeListener listener)
      throws UnsupportedOperationException, DirectoryException;



  /**
   * Registers an event listener with this data provider.
   *
   * @param listener
   *          The event listener.
   */
  void registerEventListener(DataProviderEventListener listener);



  /**
   * Searches this data provider for all entries which match the
   * specified search criteria.
   *
   * @param baseDN
   *          The base DN for the search.
   * @param scope
   *          The search scope.
   * @param filter
   *          The search filter.
   * @param handler
   *          A handler which should be used to process entries returned
   *          from the search.
   * @throws DirectoryException
   *           If a problem occurs while processing the search. This
   *           will include any exceptions thrown by the
   *           {@link DataProviderSearchHandler}, or if {@code baseDN}
   *           is not a DN equal to or subordinate to one of the base
   *           DNs managed by this data provider.
   */
  void search(DN baseDN, SearchScope scope, SearchFilter filter,
      DataProviderSearchHandler handler) throws DirectoryException;



  /**
   * Starts this data provider so that it is ready to process client
   * requests. This method is called immediately before the first data
   * provider connection is opened.
   * <p>
   * Implementations must acquire any remaining resources in order to
   * make this data provider fully operational. This may include any of
   * the following:
   * <ul>
   * <li>connections to other data providers
   * <li>connections to remote databases
   * <li>connections to remote servers
   * <li>opening local databases and files
   * <li>pre-loading databases.
   * </ul>
   * Implementations must perform all required work synchronously such
   * that, on return, this data provider is fully operational.
   */
  void startDataProvider();



  /**
   * Performs any necessary work to stop this data provider. This
   * includes closing any connections to underlying data providers,
   * databases, etc.
   * <p>
   * This method is called immediately after the last data provider
   * connection is closed. It must not return until this data provider
   * is stopped.
   * <p>
   * Implementations must release all resources acquired when this data
   * provider was started. This includes:
   * <ul>
   * <li>connections to other data providers
   * <li>connections to remote databases
   * <li>connections to remote servers
   * <li>closing local databases and files.
   * </ul>
   * Implementations must not deregister this data provider or any
   * associated listeners such as those required for performing import,
   * export, backup, and restore.
   * <p>
   * Implementations must not throw any exceptions. If any problems are
   * encountered, then they may be logged but the shutdown should
   * progress as completely as possible.
   */
  void stopDataProvider();



  /**
   * Indicates whether or not the provided base DN in this data provider
   * supports change notification.
   *
   * @param baseDN
   *          The base DN in this data provider.
   * @return {@code true} if the provided base DN in this data provider
   *         supports change notification.
   * @throws DirectoryException
   *           If {@code baseDN} is not one of the base DNs managed by
   *           this data provider.
   */
  boolean supportsChangeNotification(DN baseDN)
      throws DirectoryException;

}
