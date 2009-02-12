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

package org.opends.server.core.operations;



import org.opends.server.types.AttributeType;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.DN;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.Entry;
import org.opends.server.types.RDN;



/**
 * The context in which a request is to be processed.
 * <p>
 * Implementations may query the context in order to:
 * <ul>
 * <li>query the schema associated with the request (attribute types,
 * decode DNs, etc)
 * <li>perform internal operations
 * <li>query information regarding client performing the request
 * </ul>
 * Context implementations take care of correctly routing internal
 * requests.
 * <p>
 * In addition, the context acts as a transaction manager, coordinating
 * any resources accessed during the processing of a request and any
 * subsequent requests forming part of the same logical transaction.
 */
public interface Context
{

  // ...

  boolean entryExists(DN dn) throws DirectoryException;



  // Internal operations.
  Entry getEntry(DN dn) throws DirectoryException;



  void search(SearchRequest request, SearchResponseHandler handler)
      throws DirectoryException;
  // ...
}
