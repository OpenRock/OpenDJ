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
 *      Portions Copyright 2008 Sun Microsystems, Inc.
 */
package org.opends.server.core.dataproviders;



import static org.opends.server.loggers.debug.DebugLogger.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opends.messages.Message;
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
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.types.CanceledOperationException;
import org.opends.server.types.DN;
import org.opends.server.types.DebugLogLevel;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.Entry;
import org.opends.server.types.SearchFilter;
import org.opends.server.types.SearchScope;



/**
 * This class provides a skeletal implementation of the
 * {@link DataProvider} interface, to minimize the effort required to
 * implement this interface.
 */
public abstract class AbstractDataProvider implements DataProvider
{

  // The tracer object for the debug logger.
  private static final DebugTracer TRACER = getTracer();

  // The list of event listeners associated with this data provider.
  private final List<DataProviderConnectionEventListener> listeners =
      new CopyOnWriteArrayList<DataProviderConnectionEventListener>();



  /**
   * Creates a new abstract data provider.
   */
  protected AbstractDataProvider()
  {
    // No implementation required.
  }



  /**
   * Creates a connection which can be used for interaction with this
   * data provider. The connection must be closed when it is no longer
   * needed.
   * <p>
   * This data provider is guaranteed to have been started using
   * {@link #startDataProvider()} before this method has been called.
   * <p>
   * The default implementation is to create a new
   * {@link AbstractDataProviderConnection} wrapping this data provider
   * instance.
   *
   * @return A connection which can be used for interaction with this
   *         data provider.
   */
  public DataProviderConnection connect()
  {
    return new AbstractDataProviderConnection()
      {

        @Override
        protected AbstractDataProvider getAbstractDataProvider()
        {
          return AbstractDataProvider.this;
        }

      };
  }



  /**
   * Indicates whether this data provider contains the specified entry.
   * <p>
   * The default implementation is to invoke {@code getEntry(dn)} and
   * return {@code true} if the entry was successfully retrieved.
   *
   * @param dn
   *          The DN of the entry.
   * @return {@code true} if this data provider contains the specified
   *         entry, or {@code false} if it does not.
   * @throws DirectoryException
   *           If a problem occurs while trying to make the
   *           determination, or if <code>dn</code> is not a DN equal to
   *           or subordinate to one of the base DNs managed by this
   *           data provider.
   */
  protected boolean containsEntry(DN dn) throws DirectoryException
  {
    return getEntry(dn) != null;
  }



  /**
   * Deregisters a change listener from this data provider.
   * <p>
   * The default implementation is to always throw an
   * <code>UnsupportedOperationException</code> because change
   * notification is not supported.
   *
   * @param baseDN
   *          The base DN in this data provider.
   * @param listener
   *          The change listener.
   * @throws UnsupportedOperationException
   *           If this data provider does not support change
   *           notification.
   * @throws DirectoryException
   *           If <code>baseDN</code> is not one of the base DNs managed
   *           by this data provider.
   */
  protected void deregisterChangeListener(DN baseDN,
      DataProviderChangeListener listener)
      throws UnsupportedOperationException, DirectoryException
  {
    throw new UnsupportedOperationException(
        "Change notification not supported");
  }



  /**
   * Executes an add request against this data provider.
   * <p>
   * The data provider will execute the request and notify the provided
   * response handler on completion. Note that this method may return
   * before the request has completed.
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
  protected abstract void executeAdd(Context context,
      AddRequest request, ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a bind request against this data provider.
   * <p>
   * The data provider will execute the request and notify the provided
   * response handler on completion. Note that this method may return
   * before the request has completed.
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
  protected abstract void executeBind(Context context,
      BindRequest request, ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a compare request against this data provider.
   * <p>
   * The data provider will execute the request and notify the provided
   * response handler on completion. Note that this method may return
   * before the request has completed.
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
  protected abstract void executeCompare(Context context,
      CompareRequest request, ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a delete request against this data provider.
   * <p>
   * The data provider will execute the request and notify the provided
   * response handler on completion. Note that this method may return
   * before the request has completed.
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
  protected abstract void executeDelete(Context context,
      DeleteRequest request, ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes an extended request against this data provider.
   * <p>
   * The data provider will execute the request and notify the provided
   * response handler on completion. Note that this method may return
   * before the request has completed.
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
  protected abstract void executeExtended(Context context,
      ExtendedRequest request, ExtendedResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a modify request against this data provider.
   * <p>
   * The data provider will execute the request and notify the provided
   * response handler on completion. Note that this method may return
   * before the request has completed.
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
  protected abstract void executeModify(Context context,
      ModifyRequest request, ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a modify DN request against this data provider.
   * <p>
   * The data provider will execute the request and notify the provided
   * response handler on completion. Note that this method may return
   * before the request has completed.
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
  protected abstract void executeModifyDN(Context context,
      ModifyDNRequest request, ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a search request against this data provider.
   * <p>
   * The data provider will execute the request and notify the provided
   * response handler on completion. Note that this method may return
   * before the request has completed.
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
  protected abstract void executeSearch(Context context,
      SearchRequest request, SearchResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException;



  /**
   * Returns an unmodifiable set containing the base DNs of the
   * sub-trees which this data provider contains.
   *
   * @return An unmodifiable set containing the base DNs of the
   *         sub-trees which this data provider contains.
   */
  protected abstract Set<DN> getBaseDNs();



  /**
   * Retrieves the specified entry from this data provider.
   *
   * @param dn
   *          The DN of the entry.
   * @return The requested entry, or {@code null} if this data provider
   *         does not contain the specified entry.
   * @throws DirectoryException
   *           If a problem occurs while trying to retrieve the entry,
   *           or if <code>dn</code> is not a DN equal to or subordinate
   *           to one of the base DNs managed by this data provider.
   */
  protected abstract Entry getEntry(DN dn) throws DirectoryException;



  /**
   * Returns the current status of the provided base DN in this data
   * provider.
   *
   * @param baseDN
   *          The base DN in this data provider.
   * @return The current status of the provided base DN in this data
   *         provider.
   * @throws DirectoryException
   *           If <code>baseDN</code> is not one of the base DNs managed
   *           by this data provider.
   */
  protected abstract DataProviderStatus getStatus(DN baseDN)
      throws DirectoryException;



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
   *           If <code>baseDN</code> is not one of the base DNs managed
   *           by this data provider.
   */
  protected abstract Set<String> getSupportedControls(DN baseDN)
      throws DirectoryException;



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
   *           If <code>baseDN</code> is not one of the base DNs managed
   *           by this data provider.
   */
  protected abstract Set<String> getSupportedFeatures(DN baseDN)
      throws DirectoryException;



  /**
   * Notify all listeners that this data provider has changed state due
   * to an operational error, configuration change, or an administrative
   * action.
   * <p>
   * This method can be used to forward up the stack towards the parent
   * network group {@link DataProviderEvent}s generated by subordinate
   * data providers.
   *
   * @param event
   *          The data provider event.
   */
  protected final void notifyDataProviderStateChanged(
      DataProviderEvent event)
  {
    for (DataProviderConnectionEventListener listener : listeners)
    {
      try
      {
        listener.dataProviderStateChanged(event);
      }
      catch (Exception e)
      {
        if (debugEnabled())
        {
          TRACER.debugCaught(DebugLogLevel.ERROR, e);
        }
      }
    }
  }



  /**
   * Notify all listeners that this data provider has changed state due
   * to an operational error, configuration change, or an administrative
   * action.
   * <p>
   * This method is equivalent to the following code:
   *
   * <pre>
   * DataProviderEvent event = new DataProviderEvent(reason, types);
   * notifyDataProviderStateChanged(event);
   * </pre>
   *
   * @param reason
   *          A message describing this event.
   * @param types
   *          The types of event that have occurred in the data
   *          provider.
   */
  protected final void notifyDataProviderStateChanged(Message reason,
      Set<DataProviderEvent.Type> types)
  {
    DataProviderEvent event = new DataProviderEvent(reason, types);
    notifyDataProviderStateChanged(event);
  }



  /**
   * Registers a change listener with this data provider. The change
   * listener will be notified whenever an update occurs within the
   * subtree associated with the specified base DN in this data
   * provider.
   * <p>
   * The default implementation is to always throw an
   * <code>UnsupportedOperationException</code> because change
   * notification is not supported.
   *
   * @param baseDN
   *          The base DN in this data provider.
   * @param listener
   *          The change listener.
   * @throws UnsupportedOperationException
   *           If this data provider does not support change
   *           notification.
   * @throws DirectoryException
   *           If <code>baseDN</code> is not one of the base DNs managed
   *           by this data provider.
   */
  protected void registerChangeListener(DN baseDN,
      DataProviderChangeListener listener)
      throws UnsupportedOperationException, DirectoryException
  {
    throw new UnsupportedOperationException(
        "Change notification not supported");
  }



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
   *           {@link DataProviderSearchHandler}, or if
   *           <code>baseDN</code> is not a DN equal to or subordinate
   *           to one of the base DNs managed by this data provider.
   */
  protected abstract void search(DN baseDN, SearchScope scope,
      SearchFilter filter, DataProviderSearchHandler handler)
      throws DirectoryException;



  /**
   * Indicates whether or not the provided base DN in this data provider
   * supports change notification.
   * <p>
   * The default implementation is to return false for all base DNs
   * indicating that change notification is not supported.
   *
   * @param baseDN
   *          The base DN in this data provider.
   * @return <code>true</code> if the provided base DN in this data
   *         provider supports change notification.
   * @throws DirectoryException
   *           If <code>baseDN</code> is not one of the base DNs managed
   *           by this data provider.
   */
  protected boolean supportsChangeNotification(DN baseDN)
      throws DirectoryException
  {
    return false;
  }



  /**
   * Deregisters an event listener from this data provider.
   *
   * @param listener
   *          The event listener.
   */
  final void deregisterEventListener(
      DataProviderConnectionEventListener listener)
  {
    listeners.remove(listener);
  }



  /**
   * Registers an event listener with this data provider.
   *
   * @param listener
   *          The event listener.
   */
  final void registerEventListener(
      DataProviderConnectionEventListener listener)
  {
    listeners.add(listener);
  }

}
