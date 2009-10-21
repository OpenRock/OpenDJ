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
 *      Copyright 2009 Sun Microsystems, Inc.
 */

package org.opends.sdk.ldif;



import org.opends.sdk.schema.Schema;



/**
 * LDIF reader options.
 */
interface LDIFReaderOptions
{
  /**
   * Returns the schema which should be used for decoding entries and
   * change records. The default schema is used if no other is
   * specified.
   * 
   * @return The schema which should be used for decoding entries and
   *         change records.
   */
  Schema getSchema();



  /**
   * Indicates whether or not schema validation should be performed for
   * entries and change records. The default is {@code true}.
   * 
   * @return {@code true} if schema validation should be performed for
   *         entries and change records, or {@code false} otherwise.
   */
  boolean validateSchema();
}
