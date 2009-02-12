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



import org.opends.server.types.DirectoryException;
import org.opends.server.types.LDIFImportConfig;
import org.opends.server.types.LDIFImportResult;



/**
 * A data provider which supports LDIF import functionality.
 * <p>
 * TODO: is there any boiler plate code that abstracted in order to make
 * implementation simpler? E.g. initialization, crypto.
 */
public interface ImportableDataProvider
{

  /**
   * Returns the ID of this data provider.
   *
   * @return The ID of this data provider.
   */
  DataProviderID getDataProviderID();



  /**
   * Imports information from an LDIF file into this data provider.
   * <p>
   * Note that the server will not explicitly initialize this data
   * provider before calling this method.
   *
   * @param importConfig
   *          The configuration to use when performing the import.
   * @return Information about the result of the import processing.
   * @throws DirectoryException
   *           If a problem occurs while performing the LDIF import.
   */
  LDIFImportResult importLDIF(LDIFImportConfig importConfig)
      throws DirectoryException;
}
