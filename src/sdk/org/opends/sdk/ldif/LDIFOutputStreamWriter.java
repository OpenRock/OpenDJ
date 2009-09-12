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



import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;



/**
 * An {@code LDIFWriter} which writes its output to an {@code
 * OutputStream}.
 */
public final class LDIFOutputStreamWriter extends AbstractLDIFWriter
{
  private final BufferedWriter writer;



  /**
   * Creates a new LDIF writer whose destination is the provided output
   * stream.
   *
   * @param out
   *          The output stream to use.
   */
  public LDIFOutputStreamWriter(OutputStream out)
  {
    this.writer = new BufferedWriter(new OutputStreamWriter(out));
  }



  public void flush() throws IOException
  {
    writer.flush();
  }



  /**
   * Specifies whether or not user-friendly comments should be added
   * whenever distinguished names or UTF-8 attribute values are
   * encountered which contained non-ASCII characters.
   *
   * @param addUserFriendlyComments
   *          {@code true} if user-friendly comments should be added.
   * @return A reference to this {@code LDIFOutputStreamWriter}.
   */
  public LDIFOutputStreamWriter setAddUserFriendlyComments(
      boolean addUserFriendlyComments)
  {
    this.addUserFriendlyComments = addUserFriendlyComments;
    return this;
  }



  /**
   * Specifies the column at which long lines should be wrapped. A value
   * less than or equal to zero indicates that no wrapping should be
   * performed.
   *
   * @param wrapColumn
   *          The column at which long lines should be wrapped.
   * @return A reference to this {@code LDIFOutputStreamWriter}.
   */
  public LDIFOutputStreamWriter setWrapColumn(int wrapColumn)
  {
    this.wrapColumn = wrapColumn < 0 ? 0 : wrapColumn;
    return this;
  }



  void close0() throws IOException
  {
    writer.close();
  }



  void print(CharSequence s) throws IOException
  {
    writer.append(s);
  }



  void println() throws IOException
  {
    writer.newLine();
  }
}
