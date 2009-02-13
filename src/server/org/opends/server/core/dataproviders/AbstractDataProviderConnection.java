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
 * {@link DataProviderConnection} interface which wraps an underlying
 * {@link AbstractDataProvider}.
 */
public abstract class AbstractDataProviderConnection implements
    DataProviderConnection
{

  // The tracer object for the debug logger.
  private static final DebugTracer TRACER = getTracer();

  // The list of event listeners associated with this data provider.
  private final List<DataProviderConnectionEventListener> listeners =
      new CopyOnWriteArrayList<DataProviderConnectionEventListener>();



  /**
   * Creates a new abstract data provider connection.
   */
  protected AbstractDataProviderConnection()
  {
    // No implementation required.
  }



  /**
   * Notifies any registered event listeners that this connection has
   * been closed.
   */
  public final void close()
  {
    for (DataProviderConnectionEventListener listener : listeners)
    {
      try
      {
        listener.dataProviderConnectionClosed();
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
   * {@inheritDoc}
   */
  public final boolean containsEntry(DN dn) throws DirectoryException
  {
    return getAbstractDataProvider().containsEntry(dn);
  }



  /**
   * {@inheritDoc}
   */
  public final void deregisterChangeListener(DN baseDN,
      DataProviderChangeListener listener)
      throws UnsupportedOperationException, DirectoryException
  {
    getAbstractDataProvider()
        .deregisterChangeListener(baseDN, listener);
  }



  /**
   * {@inheritDoc}
   */
  public final void deregisterEventListener(
      DataProviderConnectionEventListener listener)
  {
    listeners.remove(listener);
    getAbstractDataProvider().deregisterEventListener(listener);
  }



  /**
   * {@inheritDoc}
   */
  public final void executeAdd(Context context, AddRequest request,
      ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException
  {
    getAbstractDataProvider().executeAdd(context, request,
        responseHandler);
  }



  /**
   * {@inheritDoc}
   */
  public final void executeBind(Context context, BindRequest request,
      ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException
  {
    getAbstractDataProvider().executeBind(context, request,
        responseHandler);
  }



  /**
   * {@inheritDoc}
   */
  public final void executeCompare(Context context,
      CompareRequest request, ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException
  {
    getAbstractDataProvider().executeCompare(context, request,
        responseHandler);
  }



  /**
   * {@inheritDoc}
   */
  public final void executeDelete(Context context,
      DeleteRequest request, ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException
  {
    getAbstractDataProvider().executeDelete(context, request,
        responseHandler);
  }



  /**
   * {@inheritDoc}
   */
  public final void executeExtended(Context context,
      ExtendedRequest request, ExtendedResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException
  {
    getAbstractDataProvider().executeExtended(context, request,
        responseHandler);
  }



  /**
   * {@inheritDoc}
   */
  public final void executeModify(Context context,
      ModifyRequest request, ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException
  {
    getAbstractDataProvider().executeModify(context, request,
        responseHandler);
  }



  /**
   * {@inheritDoc}
   */
  public final void executeModifyDN(Context context,
      ModifyDNRequest request, ResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException
  {
    getAbstractDataProvider().executeModifyDN(context, request,
        responseHandler);
  }



  /**
   * {@inheritDoc}
   */
  public final void executeSearch(Context context,
      SearchRequest request, SearchResponseHandler responseHandler)
      throws CanceledOperationException, DirectoryException
  {
    getAbstractDataProvider().executeSearch(context, request,
        responseHandler);
  }



  /**
   * {@inheritDoc}
   */
  public final Set<DN> getBaseDNs()
  {
    return getAbstractDataProvider().getBaseDNs();
  }



  /**
   * {@inheritDoc}
   */
  public final Entry getEntry(DN dn) throws DirectoryException
  {
    return getAbstractDataProvider().getEntry(dn);
  }



  /**
   * {@inheritDoc}
   */
  public final DataProviderStatus getStatus(DN baseDN)
      throws DirectoryException
  {
    return getAbstractDataProvider().getStatus(baseDN);
  }



  /**
   * {@inheritDoc}
   */
  public final Set<String> getSupportedControls(DN baseDN)
      throws DirectoryException
  {
    return getAbstractDataProvider().getSupportedControls(baseDN);
  }



  /**
   * {@inheritDoc}
   */
  public final Set<String> getSupportedFeatures(DN baseDN)
      throws DirectoryException
  {
    return getAbstractDataProvider().getSupportedFeatures(baseDN);
  }



  /**
   * {@inheritDoc}
   */
  public final void registerChangeListener(DN baseDN,
      DataProviderChangeListener listener)
      throws UnsupportedOperationException, DirectoryException
  {
    getAbstractDataProvider().registerChangeListener(baseDN, listener);
  }



  /**
   * {@inheritDoc}
   */
  public final void registerEventListener(
      DataProviderConnectionEventListener listener)
  {
    listeners.add(listener);
    getAbstractDataProvider().registerEventListener(listener);
  }



  /**
   * {@inheritDoc}
   */
  public final void search(DN baseDN, SearchScope scope,
      SearchFilter filter, DataProviderSearchHandler handler)
      throws DirectoryException
  {
    getAbstractDataProvider().search(baseDN, scope, filter, handler);
  }



  /**
   * {@inheritDoc}
   */
  public final boolean supportsChangeNotification(DN baseDN)
      throws DirectoryException
  {
    return getAbstractDataProvider().supportsChangeNotification(baseDN);
  }



  /**
   * Gets the provider.
   *
   * @return Returns the provider.
   */
  protected abstract AbstractDataProvider getAbstractDataProvider();

}
