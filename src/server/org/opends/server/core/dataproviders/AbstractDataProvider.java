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
 *      Portions Copyright 2008-2009 Sun Microsystems, Inc.
 */
package org.opends.server.core.dataproviders;



import static org.opends.server.loggers.debug.DebugLogger.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opends.messages.Message;
import org.opends.sdk.DN;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.types.DebugLogLevel;
import org.opends.server.types.DirectoryException;



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
  private final List<DataProviderEventListener> eventListeners =
      new CopyOnWriteArrayList<DataProviderEventListener>();



  /**
   * Creates a new abstract data provider.
   */
  protected AbstractDataProvider()
  {
    // No implementation required.
  }



  /**
   * {@inheritDoc}
   * <p>
   * The default implementation is to invoke {@code getEntry(dn)} and
   * return {@code true} if the entry was successfully retrieved.
   */
  public boolean containsEntry(DN dn) throws DirectoryException
  {
    return getEntry(dn) != null;
  }



  /**
   * {@inheritDoc}
   */
  public final void deregisterEventListener(
      DataProviderEventListener listener)
  {
    eventListeners.remove(listener);
  }



  /**
   * {@inheritDoc}
   */
  public final void registerEventListener(
      DataProviderEventListener listener)
  {
    eventListeners.add(listener);
  }



  /**
   * {@inheritDoc}
   * <p>
   * The default implementation is to return false for all base DNs
   * indicating that change notification is not supported.
   */
  public boolean supportsChangeNotification(DN baseDN)
      throws DirectoryException
  {
    return false;
  }



  /**
   * Notify all event listeners that this data provider has changed
   * state due to an operational error, configuration change, or an
   * administrative action.
   * <p>
   * This method can be used to forward events to parent data providers.
   *
   * @param event
   *          The data provider event.
   */
  protected final void notifyDataProviderEventOccurred(
      DataProviderEvent event)
  {
    for (DataProviderEventListener listener : eventListeners)
    {
      try
      {
        listener.dataProviderEventOccurred(event);
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
   * Notify all event listeners that this data provider has changed
   * state due to an operational error, configuration change, or an
   * administrative action.
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
  protected final void notifyDataProviderEventOccurred(Message reason,
      Set<DataProviderEvent.Type> types)
  {
    DataProviderEvent event = new DataProviderEvent(reason, types);
    notifyDataProviderEventOccurred(event);
  }

}
