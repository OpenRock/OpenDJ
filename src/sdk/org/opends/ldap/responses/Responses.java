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

package org.opends.ldap.responses;



import org.opends.ldap.ResultCode;
import org.opends.types.DN;



/**
 * This class contains various methods for creating and manipulating
 * LDAP response messages.
 */
public final class Responses
{
  /**
   * Creates a new bind result using the provided result code.
   *
   * @param resultCode
   *          The result code.
   * @return The new bind result.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  public static BindResult newBindResult(ResultCode resultCode)
      throws NullPointerException
  {
    return new BindResultImpl(resultCode);
  }



  /**
   * Creates a new compare result using the provided result code.
   *
   * @param resultCode
   *          The result code.
   * @return The new compare result.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  public static CompareResult newCompareResult(ResultCode resultCode)
      throws NullPointerException
  {
    return new CompareResultImpl(resultCode);
  }



  /**
   * Creates a new generic extended result using the provided result
   * code.
   *
   * @param resultCode
   *          The result code.
   * @return The new generic extended result.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  public static GenericExtendedResult newGenericExtendedResult(
      ResultCode resultCode) throws NullPointerException
  {
    return new GenericExtendedResultImpl(resultCode);
  }



  /**
   * Creates a new generic intermediate response.
   *
   * @return The new generic intermediate response.
   */
  public static GenericIntermediateResponse newGenericIntermediateResponse()
  {
    return new GenericIntermediateResponseImpl();
  }



  /**
   * Creates a new result using the provided result code.
   *
   * @param resultCode
   *          The result code.
   * @return The new result.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  public static Result newResult(ResultCode resultCode)
      throws NullPointerException
  {
    return new ResultImpl(resultCode);
  }



  /**
   * Creates a new search result using the provided result code.
   *
   * @param resultCode
   *          The result code.
   * @return The new search result.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  public static SearchResult newSearchResult(ResultCode resultCode)
      throws NullPointerException
  {
    return new SearchResultImpl(resultCode);
  }



  /**
   * Creates a new search result entry using the provided DN.
   *
   * @param dn
   *          The DN of the search result entry.
   * @return The new search result entry.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  public static SearchResultEntry newSearchResultEntry(DN dn)
      throws NullPointerException
  {
    return new SearchResultEntryImpl(dn.toString());
  }



  /**
   * Creates a new search result entry using the provided DN.
   *
   * @param dn
   *          The DN of the search result entry.
   * @return The new search result entry.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  public static SearchResultEntry newSearchResultEntry(String dn)
      throws NullPointerException
  {
    return new SearchResultEntryImpl(dn);
  }



  /**
   * Creates a new search result reference using the provided
   * continuation reference URI.
   *
   * @param uri
   *          The first continuation reference URI to be added to this
   *          search result reference.
   * @return The new search result reference.
   * @throws NullPointerException
   *           If {@code uri} was {@code null}.
   */
  public static SearchResultReference newSearchResultReference(
      String uri) throws NullPointerException
  {
    return new SearchResultReferenceImpl(uri);
  }



  // Private constructor.
  private Responses()
  {
    // Prevent instantiation.
  }
}
