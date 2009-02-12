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
 *      Portions Copyright 2009 Sun Microsystems, Inc.
 */
package org.opends.server.core.dataproviders;



import java.util.List;

import org.opends.server.types.Entry;
import org.opends.server.types.Modification;



/**
 * An object that registers to be notified whenever updates are made to
 * a {@link DataProvider}.
 * <p>
 * TODO: may need to pass around the post response operation so that
 * implementations can handle controls (e.g. group manager uses controls
 * at the moment).
 * <p>
 * TODO: should subtreeDeleted and subtreeRenamed be called for each
 * updated entry? The existing interface makes listener implementation
 * harder I think.
 */
public interface DataProviderChangeListener
{

  /**
   * An entry was added to the data provider.
   *
   * @param addedEntry
   *          The entry that was added.
   */
  void entryAdded(Entry addedEntry);



  /**
   * An entry was replaced in the data provider.
   *
   * @param oldEntry
   *          The old entry.
   * @param newEntry
   *          The new entry.
   * @param modifications
   *          The modifications which were used to transform
   *          <code>oldEntry</code> into <code>newEntry</code>.
   */
  void entryReplaced(Entry oldEntry, Entry newEntry,
      List<Modification> modifications);



  /**
   * A subtree of entries was deleted in the data provider.
   *
   * @param deletedBaseEntry
   *          The base entry in the subtree that was deleted.
   */
  void subtreeDeleted(Entry deletedBaseEntry);



  /**
   * A subtree of entries was renamed in the data provider.
   *
   * @param oldBaseEntry
   *          The base entry in the subtree before it was renamed.
   * @param renamedBaseEntry
   *          The base entry in the subtree after it was renamed.
   * @param modifications
   *          The modifications which were used to transform
   *          <code>oldBaseEntry</code> into
   *          <code>renamedBaseEntry</code>.
   */
  void subtreeRenamed(Entry oldBaseEntry, Entry renamedBaseEntry,
      List<Modification> modifications);
}
