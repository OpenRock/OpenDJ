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



import org.opends.server.types.BackupConfig;
import org.opends.server.types.BackupDirectory;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.RestoreConfig;



/**
 * A data provider which supports backup and restore functionality.
 * <p>
 * TODO: do we need supportsRestore?
 * <p>
 * TODO: do we need removeBackup?
 * <p>
 * TODO: is there any boiler plate code that abstracted in order to make
 * implementation simpler? E.g. initialization, crypto.
 */
public interface ArchivableDataProvider
{

  /**
   * Creates a backup of the contents of this data provider in a form
   * that may be restored at a later date if necessary. This method
   * should only be called if {@code supportsBackup} returns {@code
   * true}.
   * <p>
   * Note that the server will not explicitly initialize this data
   * provider before calling this method.
   *
   * @param backupConfig
   *          The configuration to use when performing the backup.
   * @throws DirectoryException
   *           If a problem occurs while performing the backup.
   */
  void createBackup(BackupConfig backupConfig)
      throws DirectoryException;



  /**
   * Returns the ID of this data provider.
   *
   * @return The ID of this data provider.
   */
  DataProviderID getDataProviderID();



  /**
   * Removes the specified backup if it is possible to do so.
   *
   * @param backupDirectory
   *          The backup directory structure with which the specified
   *          backup is associated.
   * @param backupID
   *          The backup ID for the backup to be removed.
   * @throws DirectoryException
   *           If it is not possible to remove the specified backup for
   *           some reason (e.g., no such backup exists or there are
   *           other backups that are dependent upon it).
   */
  void removeBackup(BackupDirectory backupDirectory, String backupID)
      throws DirectoryException;



  /**
   * Restores a backup of the contents of this data provider.
   * <p>
   * Note that the server will not explicitly initialize this data
   * provider before calling this method.
   *
   * @param restoreConfig
   *          The configuration to use when performing the restore.
   * @throws DirectoryException
   *           If a problem occurs while performing the restore.
   */
  void restoreBackup(RestoreConfig restoreConfig)
      throws DirectoryException;



  /**
   * Indicates whether this data provider provides a mechanism to
   * perform a backup of its contents in a form that can be restored
   * later, based on the provided configuration.
   *
   * @param backupConfig
   *          The configuration of the backup for which to make the
   *          determination.
   * @param unsupportedReason
   *          A buffer to which a message can be appended explaining why
   *          the requested backup is not supported.
   * @return {@code true} if this data provider provides a mechanism for
   *         performing backups with the provided configuration, or
   *         {@code false} if not.
   */
  boolean supportsBackup(BackupConfig backupConfig,
      StringBuilder unsupportedReason);
}
