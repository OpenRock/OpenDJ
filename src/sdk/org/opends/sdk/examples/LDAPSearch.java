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

package org.opends.sdk.examples;



import java.io.IOException;

import org.opends.sdk.Connection;
import org.opends.sdk.ErrorResultException;
import org.opends.sdk.Filter;
import org.opends.sdk.InitializationException;
import org.opends.sdk.SearchScope;
import org.opends.sdk.ldap.LDAPConnection;
import org.opends.sdk.ldap.LDAPConnectionOptions;
import org.opends.sdk.ldif.EntryWriter;
import org.opends.sdk.ldif.LDIFEntryWriter;
import org.opends.sdk.requests.Requests;
import org.opends.sdk.requests.SearchRequest;
import org.opends.sdk.responses.SearchResult;
import org.opends.sdk.responses.SearchResultEntry;
import org.opends.sdk.responses.SearchResultHandler;
import org.opends.sdk.responses.SearchResultReference;



/**
 * Very simple LDAP search test harness.
 */
public final class LDAPSearch
{

  // Prevent instantiation.
  private LDAPSearch()
  {
    // Do nothing.
  }



  /**
   * Performs an LDAP search using the provided parameters and dumps the
   * results as LDIF.
   *
   * @param args
   *          The command line arguments.
   * @throws InterruptedException
   *           If a thread was interrupted.
   */
  public static void main(String[] args) throws InterruptedException
  {
    if (args.length != 4)
    {
      System.err
          .println("Usage LDAPSearch <host> <port> <baseDN> <filter>");
      System.exit(1);
    }

    String host = args[0];
    int port = Integer.parseInt(args[1]);
    String baseDN = args[2];
    Filter filter = Filter.valueOf(args[3]);

    LDAPConnectionOptions options =
        LDAPConnectionOptions.defaultOptions();
    Connection connection;
    try
    {
      connection =
          LDAPConnection.connect(host, port, options, null).get();
    }
    catch (ErrorResultException e)
    {
      System.err.println("Connect failed: "
          + e.getResult().getDiagnosticMessage());
      System.exit(e.getResult().getResultCode().intValue());

      // Clear compiler warning.
      return;
    }
    catch (InitializationException e)
    {
      throw new RuntimeException(e);
    }

    final EntryWriter writer =
        new LDIFEntryWriter(System.out).setWrapColumn(75);

    SearchResultHandler handler = new SearchResultHandler()
    {

      public void handleResult(SearchResult result)
      {
        // Ignore.
      }



      public void handleError(ErrorResultException error)
      {
        // Ignore - will be handled at the end.
      }



      public void handleReference(SearchResultReference reference)
      {
        // Ignore.
      }



      public void handleEntry(SearchResultEntry entry)
      {
        try
        {
          writer.writeEntry(entry);
          writer.flush();
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      }
    };

    SearchRequest request =
        Requests.newSearchRequest(baseDN, SearchScope.WHOLE_SUBTREE,
            filter);
    try
    {
      connection.search(request, handler).get();
    }
    catch (ErrorResultException e)
    {
      System.err.println("Search failed: "
          + e.getResult().getDiagnosticMessage());
      System.exit(e.getResult().getResultCode().intValue());
    }

    connection.close();
  }
}
