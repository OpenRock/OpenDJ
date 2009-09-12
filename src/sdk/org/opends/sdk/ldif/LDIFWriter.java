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
import java.io.Flushable;
import java.io.IOException;

import org.opends.sdk.AddRequest;
import org.opends.sdk.AttributeSequence;
import org.opends.sdk.DeleteRequest;
import org.opends.sdk.ModifyDNRequest;
import org.opends.sdk.ModifyRequest;



/**
 * An interface for writing LDIF change records to a data source.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2849">RFC 2849 - The LDAP
 *      Data Interchange Format (LDIF) - Technical Specification </a>
 */
public interface LDIFWriter extends Closeable, Flushable
{
  /**
   * Closes this LDIF writer, flushing it first. Closing a previously
   * closed LDIF writer has no effect.
   *
   * @throws IOException
   *           If an error occurs while closing.
   */
  void close() throws IOException;



  /**
   * Flushes this LDIF writer so that any buffered data is written
   * immediately to underlying stream, flushing the stream if it is also
   * {@code Flushable}.
   * <p>
   * If the intended destination of this stream is an abstraction
   * provided by the underlying operating system, for example a file,
   * then flushing the stream guarantees only that bytes previously
   * written to the stream are passed to the operating system for
   * writing; it does not guarantee that they are actually written to a
   * physical device such as a disk drive.
   *
   * @throws IOException
   *           If an error occurs while flushing.
   */
  void flush() throws IOException;



  /**
   * Writes the provided {@code AttributeSequence} to this LDIF writer
   * as an attribute value record containing the distinguished name and
   * attributes of the entry, but no change type or controls.
   *
   * @param entry
   *          The {@code AttributeSequence} to be written as an
   *          attribute value record.
   * @return A reference to this LDIF writer.
   * @throws IOException
   *           If an error occurs while writing the attribute value
   *           record.
   * @throws NullPointerException
   *           If {@code entry} was {@code null}.
   */
  LDIFWriter writeAttrValRecord(AttributeSequence entry)
      throws IOException, NullPointerException;



  /**
   * Writes the provided {@code AddRequest} to this LDIF writer as a
   * change record containing the distinguished name, any controls
   * included with the request, the change type {@code add}, and any
   * attributes included in the request.
   *
   * @param change
   *          The {@code AddRequest} to be written as a change record.
   * @return A reference to this LDIF writer.
   * @throws IOException
   *           If an error occurs while writing the change record.
   * @throws NullPointerException
   *           If {@code change} was {@code null}.
   */
  LDIFWriter writeChangeRecord(AddRequest change) throws IOException,
      NullPointerException;



  /**
   * Writes the provided {@code AttributeSequence} to this LDIF writer
   * as a change record containing the distinguished name, the change
   * type {@code add}, and any attributes included in the sequence, but
   * no controls.
   *
   * @param change
   *          The {@code AttributeSequence} to be written as a change
   *          record.
   * @return A reference to this LDIF writer.
   * @throws IOException
   *           If an error occurs while writing the change record.
   * @throws NullPointerException
   *           If {@code change} was {@code null}.
   */
  LDIFWriter writeChangeRecord(AttributeSequence change)
      throws IOException, NullPointerException;



  /**
   * Writes the provided {@code DeleteRequest} to this LDIF writer as a
   * change record containing the distinguished name, any controls
   * included with the request, and the change type {@code delete}.
   *
   * @param change
   *          The {@code DeleteRequest} to be written as a change
   *          record.
   * @return A reference to this LDIF writer.
   * @throws IOException
   *           If an error occurs while writing the change record.
   * @throws NullPointerException
   *           If {@code change} was {@code null}.
   */
  LDIFWriter writeChangeRecord(DeleteRequest change)
      throws IOException, NullPointerException;



  /**
   * Writes the provided {@code ModifyDNRequest} to this LDIF writer as
   * a change record containing the distinguished name, any controls
   * included with the request, the change type {@code modrdn}, and any
   * additional parameters included in the request.
   *
   * @param change
   *          The {@code ModifyDNRequest} to be written as a change
   *          record.
   * @return A reference to this LDIF writer.
   * @throws IOException
   *           If an error occurs while writing the change record.
   * @throws NullPointerException
   *           If {@code change} was {@code null}.
   */
  LDIFWriter writeChangeRecord(ModifyDNRequest change)
      throws IOException, NullPointerException;



  /**
   * Writes the provided {@code ModifyRequest} to this LDIF writer as a
   * change record containing the distinguished name, any controls
   * included with the request, the change type {@code modify}, and any
   * modifications included in the request.
   *
   * @param change
   *          The {@code ModifyRequest} to be written as a change
   *          record.
   * @return A reference to this LDIF writer.
   * @throws IOException
   *           If an error occurs while writing the change record.
   * @throws NullPointerException
   *           If {@code change} was {@code null}.
   */
  LDIFWriter writeChangeRecord(ModifyRequest change)
      throws IOException, NullPointerException;



  /**
   * Writes the provided {@code CharSequence} to this LDIF writer as a
   * comment.
   *
   * @param comment
   *          The {@code CharSequence} to be written as a comment.
   * @return A reference to this LDIF writer.
   * @throws IOException
   *           If an error occurs while writing the comment.
   * @throws NullPointerException
   *           If {@code comment} was {@code null}.
   */
  LDIFWriter writeComment(CharSequence comment) throws IOException,
      NullPointerException;

}
