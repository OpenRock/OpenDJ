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



/**
 * LDIF reader implementation interface.
 */
interface LDIFReaderImpl
{

  /**
   * Closes any resources associated with this LDIF reader
   * implementation.
   * 
   * @throws IOException
   *           If an error occurs while closing.
   */
  void close() throws IOException;



  /**
   * Reads the next line of LDIF from the underlying LDIF source.
   * Implementations must remove trailing line delimiters.
   * 
   * @return The next line of LDIF, or {@code null} if the end of the
   *         LDIF source has been reached.
   * @throws IOException
   *           If an error occurs while reading from the LDIF source.
   */
  String readLine() throws IOException;
}
