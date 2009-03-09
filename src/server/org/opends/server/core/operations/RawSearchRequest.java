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



import java.util.HashSet;
import java.util.Set;

import org.opends.server.types.ByteString;
import org.opends.server.types.DereferencePolicy;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.OperationType;
import org.opends.server.types.RawFilter;
import org.opends.server.types.SearchScope;
import org.opends.server.util.Validator;



/**
 * A raw search request.
 */
public final class RawSearchRequest extends RawRequest
{
  // The set of requested attributes.
  private final Set<String> attributes = new HashSet<String>(2);

  // The search base DN.
  private ByteString baseDN;

  // The alias dereferencing policy.
  private DereferencePolicy dereferencePolicy =
      DereferencePolicy.NEVER_DEREF_ALIASES;

  // The search filter.
  private RawFilter filter;

  // The search scope.
  private SearchScope scope;

  // The search size limit.
  private int sizeLimit = 0;

  // The search time limit.
  private int timeLimit = 0;

  // Indicates whether search results are to contain both attribute
  // descriptions and values, or just attribute descriptions.
  private boolean typesOnly = false;



  /**
   * Creates a new raw search request using the provided base DN, scope,
   * and filter.
   * <p>
   * The new raw search request will contain an empty list of controls,
   * an empty list of attributes (indicating all user attributes), no
   * size limit, no time limit, and will never dereference aliases.
   *
   * @param baseDN
   *          The raw, unprocessed base DN for this search request.
   * @param scope
   *          The scope for this search request.
   * @param filter
   *          The partially decoded filter as included in the request
   *          from the client.
   */
  public RawSearchRequest(ByteString baseDN, SearchScope scope,
      RawFilter filter)
  {
    super(OperationType.SEARCH);
    Validator.ensureNotNull(baseDN, scope, filter);
    this.baseDN = baseDN;
    this.scope = scope;
    this.filter = filter;
  }



  /**
   * Returns the set of requested attributes for this search request.
   * <p>
   * Any modifications made to the returned attribute {@code Set} will
   * be reflected in this search request.
   *
   * @return The set of requested attributes for this search request.
   */
  public Set<String> getAttributes()
  {
    return attributes;
  }



  /**
   * Returns the raw, unprocessed base DN as included in the request
   * from the client.
   * <p>
   * This may or may not contain a valid DN, as no validation will have
   * been performed.
   *
   * @return The raw, unprocessed base DN as included in the request
   *         from the client.
   */
  public ByteString getBaseDN()
  {
    return baseDN;
  }



  /**
   * Returns the alias dereferencing policy for this search request.
   *
   * @return The alias dereferencing policy for this search request.
   */
  public DereferencePolicy getDereferencePolicy()
  {
    return dereferencePolicy;
  }



  /**
   * Returns the partially decoded filter as included in the request
   * from the client.
   * <p>
   * It may or may not contain a valid filter (e.g., unsupported
   * attribute types or values with an invalid syntax) because no
   * validation will have been performed on it.
   *
   * @return The partially decoded filter as included in the request
   *         from the client.
   */
  public RawFilter getFilter()
  {
    return filter;
  }



  /**
   * Returns the scope for this search request.
   *
   * @return The scope for this search request.
   */
  public SearchScope getScope()
  {
    return scope;
  }



  /**
   * Returns the size limit that restricts the maximum number of entries
   * to be returned as a result of the search.
   * <p>
   * A value of zero indicates that no client-requested size limit
   * restrictions are in effect for the search.
   *
   * @return The size limit for this search request.
   */
  public int getSizeLimit()
  {
    return sizeLimit;
  }



  /**
   * Returns the time limit that restricts the maximum time (in seconds)
   * allowed for the search.
   * <p>
   * A value of zero indicates that no client-requested time limit
   * restrictions are in effect for the search.
   *
   * @return The time limit for this search request.
   */
  public int getTimeLimit()
  {
    return timeLimit;
  }



  /**
   * Indicates whether search results are to contain both attribute
   * descriptions and values, or just attribute descriptions.
   *
   * @return {@code true} if search results will contain just attribute
   *         descriptions (no values).
   */
  public boolean isTypesOnly()
  {
    return typesOnly;
  }



  /**
   * Sets the raw, unprocessed base DN for this search request.
   * <p>
   * This may or may not contain a valid DN.
   *
   * @param baseDN
   *          The raw, unprocessed base DN for this search request.
   * @return This raw search request.
   */
  public RawSearchRequest setBaseDN(ByteString baseDN)
  {
    Validator.ensureNotNull(baseDN);
    this.baseDN = baseDN;
    return this;
  }



  /**
   * Sets the alias dereferencing policy for this search request.
   *
   * @param dereferencePolicy
   *          The alias dereferencing policy for this search request.
   * @return This raw search request.
   */
  public RawSearchRequest setDereferencePolicy(
      DereferencePolicy dereferencePolicy)
  {
    Validator.ensureNotNull(dereferencePolicy);
    this.dereferencePolicy = dereferencePolicy;
    return this;
  }



  /**
   * Sets the partially decoded filter for this search request.
   * <p>
   * It may or may not contain a valid filter (e.g., unsupported
   * attribute types or values with an invalid syntax).
   *
   * @param filter
   *          The partially decoded filter as included in the request
   *          from the client.
   * @return This raw search request.
   */
  public RawSearchRequest setFilter(RawFilter filter)
  {
    Validator.ensureNotNull(filter);
    this.filter = filter;
    return this;
  }



  /**
   * Sets the scope for this search request.
   *
   * @param scope
   *          The scope for this search request.
   * @return This raw search request.
   */
  public RawSearchRequest setScope(SearchScope scope)
  {
    Validator.ensureNotNull(scope);
    this.scope = scope;
    return this;
  }



  /**
   * Sets the size limit that restricts the maximum number of entries to
   * be returned as a result of the search.
   * <p>
   * A value of zero indicates that no client-requested size limit
   * restrictions are in effect for the search.
   *
   * @param sizeLimit
   *          The size limit for this search request.
   * @return This raw search request.
   * @throws IllegalArgumentException
   *           If {@code sizeLimit} is less than 0.
   */
  public RawSearchRequest setSizeLimit(int sizeLimit)
      throws IllegalArgumentException
  {
    if (sizeLimit < 0)
    {
      throw new IllegalArgumentException("Negative sizeLimit");
    }
    this.sizeLimit = sizeLimit;
    return this;
  }



  /**
   * Sets the time limit that restricts the maximum time (in seconds)
   * allowed for the search.
   * <p>
   * A value of zero indicates that no client-requested time limit
   * restrictions are in effect for the search.
   *
   * @param timeLimit
   *          The time limit for this search request.
   * @return This raw search request.
   * @throws IllegalArgumentException
   *           If {@code timeLimit} is less than 0.
   */
  public RawSearchRequest setTimeLimit(int timeLimit)
      throws IllegalArgumentException
  {
    if (timeLimit < 0)
    {
      throw new IllegalArgumentException("Negative timeLimit");
    }
    this.timeLimit = timeLimit;
    return this;
  }



  /**
   * Specifies whether search results are to contain both attribute
   * descriptions and values, or just attribute descriptions.
   *
   * @param typesOnly
   *          {@code true} if search results should contain just
   *          attribute descriptions (no values).
   * @return This raw search request.
   */
  public RawSearchRequest setTypesOnly(boolean typesOnly)
  {
    this.typesOnly = typesOnly;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public SearchRequest toRequest(Schema schema)
      throws DirectoryException
  {
    // TODO: not yet implemented.
    return null;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("SearchRequest(baseObject=");
    buffer.append(baseDN);
    buffer.append(", scope=");
    buffer.append(scope);
    buffer.append(", derefAliases=");
    buffer.append(dereferencePolicy);
    buffer.append(", sizeLimit=");
    buffer.append(sizeLimit);
    buffer.append(", timeLimit=");
    buffer.append(timeLimit);
    buffer.append(", typesOnly=");
    buffer.append(typesOnly);
    buffer.append(", filter=");
    buffer.append(filter);
    buffer.append(", attributes=");
    buffer.append(attributes);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
