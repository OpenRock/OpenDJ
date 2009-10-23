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



import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.opends.sdk.DN;
import org.opends.sdk.DecodeException;
import org.opends.sdk.Entry;
import org.opends.sdk.SortedEntry;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.util.Validator;



/**
 * An LDIF entry reader reads attribute value records (entries) using
 * the LDAP Data Interchange Format (LDIF) from a user defined source.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2849">RFC 2849 - The LDAP
 *      Data Interchange Format (LDIF) - Technical Specification </a>
 */
public final class LDIFEntryReader extends AbstractLDIFReader implements
    EntryReader
{
  /**
   * Creates a new LDIF entry reader whose source is the provided input
   * stream.
   *
   * @param in
   *          The input stream to use.
   */
  public LDIFEntryReader(InputStream in)
  {
    super(in);
  }



  /**
   * Creates a new LDIF entry reader which will read lines of LDIF from
   * the provided list.
   *
   * @param ldifLines
   *          The list from which lines of LDIF should be read.
   */
  public LDIFEntryReader(List<String> ldifLines)
  {
    super(ldifLines);
  }



  /**
   * {@inheritDoc}
   */
  public void close() throws IOException
  {
    close0();
  }



  /**
   * {@inheritDoc}
   */
  public Entry readEntry() throws DecodeException,
      IOException
  {
    // Continue until an unfiltered entry is obtained.
    while (true)
    {
      LDIFRecord record = null;

      // Read the set of lines that make up the next entry.
      record = readLDIFRecord();
      if (record == null)
      {
        return null;
      }

      // Read the DN of the entry and see if it is one that should be
      // included in the import.
      DN entryDN;
      try
      {
        entryDN = readLDIFRecordDN(record);
        if (entryDN == null)
        {
          // Skip version record.
          continue;
        }
      }
      catch (final DecodeException e)
      {
        rejectLDIFRecord(record, e.getMessageObject());
        continue;
      }

      // TODO: skip if entry DN is excluded.

      // Use an Entry for the AttributeSequence.
      final Entry entry = new SortedEntry(schema).setNameDN(entryDN);
      try
      {
        while (record.iterator.hasNext())
        {
          readLDIFRecordAttributeValue(record, entry);
        }
      }
      catch (final DecodeException e)
      {
        rejectLDIFRecord(record, e.getMessageObject());
        continue;
      }

      // TODO: skip entry if excluded based on filtering.

      return entry;
    }
  }



  /**
   * Sets the schema which should be used for decoding entries and
   * change records. The default schema is used if no other is
   * specified.
   *
   * @param schema
   *          The schema which should be used for decoding entries and
   *          change records.
   * @return A reference to this {@code LDIFEntryReader}.
   */
  public LDIFEntryReader setSchema(Schema schema)
  {
    Validator.ensureNotNull(schema);
    this.schema = schema;
    return this;
  }



  /**
   * Specifies whether or not schema validation should be performed for
   * entries and change records. The default is {@code true}.
   *
   * @param validateSchema
   *          {@code true} if schema validation should be performed for
   *          entries and change records, or {@code false} otherwise.
   * @return A reference to this {@code LDIFEntryReader}.
   */
  public LDIFEntryReader setValidateSchema(boolean validateSchema)
  {
    this.validateSchema = validateSchema;
    return this;
  }

}
