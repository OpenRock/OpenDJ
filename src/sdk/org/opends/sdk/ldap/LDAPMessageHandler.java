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

package org.opends.sdk.ldap;



import org.opends.sdk.AbandonRequest;
import org.opends.sdk.AddRequest;
import org.opends.sdk.BindResult;
import org.opends.sdk.CompareRequest;
import org.opends.sdk.CompareResult;
import org.opends.sdk.DeleteRequest;
import org.opends.sdk.GenericBindRequest;
import org.opends.sdk.GenericExtendedRequest;
import org.opends.sdk.GenericExtendedResult;
import org.opends.sdk.GenericIntermediateResponse;
import org.opends.sdk.ModifyDNRequest;
import org.opends.sdk.ModifyRequest;
import org.opends.sdk.Result;
import org.opends.sdk.SearchRequest;
import org.opends.sdk.SearchResult;
import org.opends.sdk.SearchResultEntry;
import org.opends.sdk.SearchResultReference;
import org.opends.sdk.SimpleBindRequest;
import org.opends.sdk.UnbindRequest;
import org.opends.sdk.sasl.SASLBindRequest;
import org.opends.server.types.ByteString;



/**
 * LDAP message handler interface.
 */
interface LDAPMessageHandler
{
  void handleException(Throwable throwable);



  void handleUnrecognizedMessage(int messageID, byte messageTag,
      ByteString messageBytes) throws UnsupportedMessageException;



  void handleAbandonRequest(int messageID, AbandonRequest request)
      throws UnexpectedRequestException;



  void handleAddRequest(int messageID, AddRequest request)
      throws UnexpectedRequestException;



  void handleCompareRequest(int messageID, CompareRequest request)
      throws UnexpectedRequestException;



  void handleDeleteRequest(int messageID, DeleteRequest request)
      throws UnexpectedRequestException;



  void handleExtendedRequest(int messageID,
      GenericExtendedRequest request) throws UnexpectedRequestException;



  void handleBindRequest(int messageID, int version,
      GenericBindRequest request) throws UnexpectedRequestException;



  void handleBindRequest(int messageID, int version,
      SASLBindRequest<?> request) throws UnexpectedRequestException;



  void handleBindRequest(int messageID, int version,
      SimpleBindRequest request) throws UnexpectedRequestException;



  void handleModifyDNRequest(int messageID, ModifyDNRequest request)
      throws UnexpectedRequestException;



  void handleModifyRequest(int messageID, ModifyRequest request)
      throws UnexpectedRequestException;



  void handleSearchRequest(int messageID, SearchRequest request)
      throws UnexpectedRequestException;



  void handleUnbindRequest(int messageID, UnbindRequest request)
      throws UnexpectedRequestException;



  void handleAddResult(int messageID, Result result)
      throws UnexpectedResponseException;



  void handleBindResult(int messageID, BindResult result)
      throws UnexpectedResponseException;



  void handleCompareResult(int messageID, CompareResult result)
      throws UnexpectedResponseException;



  void handleDeleteResult(int messageID, Result result)
      throws UnexpectedResponseException;



  void handleExtendedResult(int messageID, GenericExtendedResult result)
      throws UnexpectedResponseException;



  void handleIntermediateResponse(int messageID,
      GenericIntermediateResponse response)
      throws UnexpectedResponseException;



  void handleModifyDNResult(int messageID, Result result)
      throws UnexpectedResponseException;



  void handleModifyResult(int messageID, Result result)
      throws UnexpectedResponseException;



  void handleSearchResult(int messageID, SearchResult result)
      throws UnexpectedResponseException;



  void handleSearchResultEntry(int messageID, SearchResultEntry entry)
      throws UnexpectedResponseException;



  void handleSearchResultReference(int messageID,
      SearchResultReference reference)
      throws UnexpectedResponseException;
}
