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



import java.util.Set;

import org.opends.server.core.AddOperation;
import org.opends.server.core.BindOperation;
import org.opends.server.core.CompareOperation;
import org.opends.server.core.DeleteOperation;
import org.opends.server.core.ModifyDNOperation;
import org.opends.server.core.ModifyOperation;
import org.opends.server.core.SearchOperation;
import org.opends.server.types.CanceledOperationException;
import org.opends.server.types.DN;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.Entry;
import org.opends.server.types.SearchFilter;
import org.opends.server.types.SearchScope;



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
   *           determination, or if <code>dn</code> is not a DN equal to
   *           or subordinate to one of the base DNs managed by the
   *           underlying data provider.
   */
  boolean containsEntry(DN dn) throws DirectoryException;



  /**
   * Deregisters a change listener from the underlying data provider.
   *
   * @param baseDN
   *          The base DN in the underlying data provider.
   * @param listener
   *          The change listener.
   * @throws UnsupportedOperationException
   *           If the underlying data provider does not support change
   *           notification.
   * @throws DirectoryException
   *           If <code>baseDN</code> is not one of the base DNs managed
   *           by the underlying data provider.
   */
  void deregisterChangeListener(DN baseDN,
      DataProviderChangeListener listener)
      throws UnsupportedOperationException, DirectoryException;



  /**
   * Deregisters an event listener from the underlying data provider.
   *
   * @param listener
   *          The event listener.
   */
  void deregisterEventListener(
      DataProviderConnectionEventListener listener);



  /**
   * Executes an add operation against the underlying data provider.
   *
   * @param addOperation
   *          The add operation to execute.
   * @throws CanceledOperationException
   *           If this add operation should be canceled.
   * @throws DirectoryException
   *           If the operation's target entry is not a DN equal to or
   *           subordinate to one of the base DNs managed by the
   *           underlying data provider.
   */
  void execute(AddOperation addOperation)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a bind operation against the underlying data provider.
   *
   * @param bindOperation
   *          The bind operation to execute.
   * @throws CanceledOperationException
   *           If this bind operation should be canceled.
   * @throws DirectoryException
   *           If the operation's target entry is not a DN equal to or
   *           subordinate to one of the base DNs managed by the
   *           underlying data provider.
   */
  void execute(BindOperation bindOperation)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a compare operation against the underlying data provider.
   *
   * @param compareOperation
   *          The compare operation to execute.
   * @throws CanceledOperationException
   *           If this compare operation should be canceled.
   * @throws DirectoryException
   *           If the operation's target entry is not a DN equal to or
   *           subordinate to one of the base DNs managed by the
   *           underlying data provider.
   */
  void execute(CompareOperation compareOperation)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a delete operation against the underlying data provider.
   *
   * @param deleteOperation
   *          The delete operation to execute.
   * @throws CanceledOperationException
   *           If this delete operation should be canceled.
   * @throws DirectoryException
   *           If the operation's target entry is not a DN equal to or
   *           subordinate to one of the base DNs managed by the
   *           underlying data provider.
   */
  void execute(DeleteOperation deleteOperation)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a modify DN operation against the underlying data
   * provider.
   *
   * @param modifyDNOperation
   *          The modify DN operation to execute.
   * @throws CanceledOperationException
   *           If this modify DN operation should be canceled.
   * @throws DirectoryException
   *           If the operation's target entry or new superior entry is
   *           not a DN equal to or subordinate to one of the base DNs
   *           managed by the underlying data provider.
   */
  void execute(ModifyDNOperation modifyDNOperation)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a modify operation against the underlying data provider.
   *
   * @param modifyOperation
   *          The modify operation to execute.
   * @throws CanceledOperationException
   *           If this modify operation should be canceled.
   * @throws DirectoryException
   *           If the operation's target entry is not a DN equal to or
   *           subordinate to one of the base DNs managed by the
   *           underlying data provider.
   */
  void execute(ModifyOperation modifyOperation)
      throws CanceledOperationException, DirectoryException;



  /**
   * Executes a search operation against the underlying data provider.
   *
   * @param searchOperation
   *          The search operation to execute.
   * @throws CanceledOperationException
   *           If this search operation should be canceled.
   * @throws DirectoryException
   *           If the operation's target entry is not a DN equal to or
   *           subordinate to one of the base DNs managed by the
   *           underlying data provider.
   */
  void execute(SearchOperation searchOperation)
      throws CanceledOperationException, DirectoryException;



  /**
   * Returns the current status of the provided base DN in the
   * underlying data provider.
   *
   * @param baseDN
   *          The base DN in the underlying data provider.
   * @return The current status of the provided base DN in the
   *         underlying data provider.
   * @throws DirectoryException
   *           If <code>baseDN</code> is not one of the base DNs managed
   *           by the underlying data provider.
   */
  DataProviderStatus getStatus(DN baseDN) throws DirectoryException;



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
   *           or if <code>dn</code> is not a DN equal to or subordinate
   *           to one of the base DNs managed by the underlying data
   *           provider.
   */
  Entry getEntry(DN dn) throws DirectoryException;



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
   *           If <code>baseDN</code> is not one of the base DNs managed
   *           by the underlying data provider.
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
   *           If <code>baseDN</code> is not one of the base DNs managed
   *           by the underlying data provider.
   */
  Set<String> getSupportedFeatures(DN baseDN) throws DirectoryException;



  /**
   * Registers a change listener with the underlying data provider. The
   * change listener will be notified whenever an update occurs within
   * the subtree associated with the specified base DN in the underlying
   * data provider.
   *
   * @param baseDN
   *          The base DN in the underlying data provider.
   * @param listener
   *          The change listener.
   * @throws UnsupportedOperationException
   *           If the underlying data provider does not support change
   *           notification.
   * @throws DirectoryException
   *           If <code>baseDN</code> is not one of the base DNs managed
   *           by the underlying data provider.
   */
  void registerChangeListener(DN baseDN,
      DataProviderChangeListener listener)
      throws UnsupportedOperationException, DirectoryException;



  /**
   * Registers an event listener with the underlying data provider.
   *
   * @param listener
   *          The event listener.
   */
  void registerEventListener(
      DataProviderConnectionEventListener listener);



  /**
   * Searches the underlying data provider for all entries which match
   * the specified search criteria.
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
   *           to one of the base DNs managed by the underlying data
   *           provider.
   */
  void search(DN baseDN, SearchScope scope, SearchFilter filter,
      DataProviderSearchHandler handler) throws DirectoryException;



  /**
   * Indicates whether or not the provided base DN in the underlying
   * data provider supports change notification.
   *
   * @param baseDN
   *          The base DN in the underlying data provider.
   * @return <code>true</code> if the provided base DN in the underlying
   *         data provider supports change notification.
   * @throws DirectoryException
   *           If <code>baseDN</code> is not one of the base DNs managed
   *           by the underlying data provider.
   */
  boolean supportsChangeNotification(DN baseDN)
      throws DirectoryException;
}
