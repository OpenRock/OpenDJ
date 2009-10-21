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

import org.opends.sdk.schema.Schema;
import org.opends.sdk.util.Validator;



/**
 * An LDIF change record reader reads change records using the LDAP Data
 * Interchange Format (LDIF) from a user defined source.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc2849">RFC 2849 - The LDAP
 *      Data Interchange Format (LDIF) - Technical Specification </a>
 */
public final class LDIFChangeRecordReader implements
    ChangeRecordReader, LDIFReaderOptions
{

  private final LDIFReader reader;

  private Schema schema = Schema.getDefaultSchema();

  private boolean validateSchema = true;



  /**
   * Creates a new LDIF entry reader whose source is the provided input
   * stream.
   * 
   * @param in
   *          The input stream to use.
   */
  public LDIFChangeRecordReader(InputStream in)
  {
    Validator.ensureNotNull(in);
    this.reader =
        new LDIFReader(this, new LDIFReaderInputStreamImpl(in));
  }



  /**
   * Creates a new LDIF entry reader which will read lines of LDIF from
   * the provided list.
   * 
   * @param ldifLines
   *          The list from which lines of LDIF should be read.
   */
  public LDIFChangeRecordReader(List<String> ldifLines)
  {
    Validator.ensureNotNull(ldifLines);
    this.reader =
        new LDIFReader(this, new LDIFReaderListImpl(ldifLines));
  }



  /**
   * {@inheritDoc}
   */
  public void close() throws IOException
  {
    reader.close();
  }



  /**
   * {@inheritDoc}
   */
  public Schema getSchema()
  {
    return schema;
  }



  /**
   * {@inheritDoc}
   */
  public ChangeRecord readChangeRecord() throws IOException
  {
    return reader.readChangeRecord();
  }



  /**
   * Sets the schema which should be used for decoding entries and
   * change records. The default schema is used if no other is
   * specified.
   * 
   * @param schema
   *          The schema which should be used for decoding entries and
   *          change records.
   * @return A reference to this {@code LDIFChangeRecordReader}.
   */
  public LDIFChangeRecordReader setSchema(Schema schema)
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
   * @return A reference to this {@code LDIFChangeRecordReader}.
   */
  public LDIFChangeRecordReader setValidateSchema(boolean validateSchema)
  {
    this.validateSchema = validateSchema;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public boolean validateSchema()
  {
    return validateSchema;
  }

}
