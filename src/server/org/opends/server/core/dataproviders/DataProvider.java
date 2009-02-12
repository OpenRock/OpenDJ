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
 *      Copyright 2008 Sun Microsystems, Inc.
 */
package org.opends.server.core.dataproviders;



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
   * Creates a connection which can be used for interaction with this
   * data provider. The connection must be closed when it is no longer
   * needed.
   * <p>
   * This data provider is guaranteed to have been started using
   * {@link #startDataProvider()} before this method has been called.
   *
   * @return A connection which can be used for interaction with this
   *         data provider.
   */
  DataProviderConnection connect();



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
   * Starts this data provider so that it is ready to process client
   * requests. This method is called immediately before the first data
   * provider connection is opened using {@link #connect()}.
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

}
