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

import org.opends.server.core.AddOperation;
import org.opends.server.core.BindOperation;
import org.opends.server.core.CompareOperation;
import org.opends.server.core.DeleteOperation;
import org.opends.server.core.ModifyDNOperation;
import org.opends.server.core.ModifyOperation;
import org.opends.server.core.SearchOperation;
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
  public final void execute(AddOperation addOperation)
      throws CanceledOperationException, DirectoryException
  {
    getAbstractDataProvider().execute(addOperation);
  }



  /**
   * {@inheritDoc}
   */
  public final void execute(BindOperation bindOperation)
      throws CanceledOperationException, DirectoryException
  {
    getAbstractDataProvider().execute(bindOperation);
  }



  /**
   * {@inheritDoc}
   */
  public final void execute(CompareOperation compareOperation)
      throws CanceledOperationException, DirectoryException
  {
    getAbstractDataProvider().execute(compareOperation);
  }



  /**
   * {@inheritDoc}
   */
  public final void execute(DeleteOperation deleteOperation)
      throws CanceledOperationException, DirectoryException
  {
    getAbstractDataProvider().execute(deleteOperation);
  }



  /**
   * {@inheritDoc}
   */
  public final void execute(ModifyDNOperation modifyDNOperation)
      throws CanceledOperationException, DirectoryException
  {
    getAbstractDataProvider().execute(modifyDNOperation);
  }



  /**
   * {@inheritDoc}
   */
  public final void execute(ModifyOperation modifyOperation)
      throws CanceledOperationException, DirectoryException
  {
    getAbstractDataProvider().execute(modifyOperation);
  }



  /**
   * {@inheritDoc}
   */
  public final void execute(SearchOperation searchOperation)
      throws CanceledOperationException, DirectoryException
  {
    getAbstractDataProvider().execute(searchOperation);
  }



  /**
   * Gets the provider.
   *
   * @return Returns the provider.
   */
  protected abstract AbstractDataProvider getAbstractDataProvider();



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

}
