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



import java.io.Closeable;
import java.io.IOException;

import org.opends.sdk.AttributeSequence;



/**
 * An interface for reading change records from a data source, typically
 * an LDIF file.
 * <p>
 * Implementations must specify the following:
 * <ul>
 * <li>Whether or not arbitrary types of change record may be returned,
 * or just {@code Add} change records. In particular, whether or not an
 * application can use the {@link #readEntry()} method safely.
 * <li>Whether or not it is possible for the implementation to encounter
 * malformed change records and, if it is possible, how they are
 * handled.
 * <li>Any synchronization limitations.
 * </ul>
 * <p>
 * TODO: LDIFInputStreamReader
 * <p>
 * TODO: FilteredChangeRecordReader
 * <p>
 * TODO: SearchResultEntryReader
 *
 * @see <a href="http://tools.ietf.org/html/rfc2849">RFC 2849 - The LDAP
 *      Data Interchange Format (LDIF) - Technical Specification </a>
 */
public interface ChangeRecordReader extends Closeable
{

  /**
   * Reads the next {@code ChangeRecord}, blocking if necessary until a
   * change record is available. If the next change record does not
   * contain a change type then it will be treated as an {@code Add}
   * change record.
   *
   * @return The next {@code ChangeRecord}, or {@code null} if there are
   *         no more change records to be read.
   * @throws IOException
   *           If an error occurs while reading the change record.
   */
  ChangeRecord readChangeRecord() throws IOException;



  /**
   * Reads the next {@code Add} change record as an {@code
   * AttributeSequence}, blocking if necessary until an entry is
   * available.
   * <p>
   * <b>NOTE:</b> implementations must specify how they behave when a
   * non- {@code Add} change record is encountered, if this situation is
   * possible.
   *
   * @return The next change record or {@code null} if there are no more
   *         change records to be read.
   * @throws IOException
   *           If an error occurs while reading the change record.
   */
  AttributeSequence readEntry() throws IOException;



  /**
   * Closes this change record reader if it not already closed. Note
   * that this method does not need to be called if a previous call of
   * {@link #readChangeRecord()} has returned {@code null}.
   *
   * @throws IOException
   *           If an error occurs while closing.
   */
  void close() throws IOException;
}
