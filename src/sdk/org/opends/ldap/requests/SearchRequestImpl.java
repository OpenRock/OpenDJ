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

package org.opends.ldap.requests;



import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.opends.spi.AbstractMessage;
import org.opends.types.DereferenceAliasesPolicy;
import org.opends.types.SearchScope;
import org.opends.types.filter.Filter;
import org.opends.util.Validator;



/**
 * Search request implementation.
 */
final class SearchRequestImpl extends AbstractMessage<SearchRequest>
    implements SearchRequest
{
  private final List<String> attributes = new LinkedList<String>();

  private String baseDN;

  private DereferenceAliasesPolicy dereferencePolicy =
      DereferenceAliasesPolicy.NEVER;

  private Filter filter;

  private SearchScope scope;

  private int sizeLimit = 0;

  private int timeLimit = 0;

  private boolean typesOnly = false;



  /**
   * Creates a new search request using the provided base DN, scope, and
   * filter.
   *
   * @param baseDN
   *          The name of the base entry relative to which the search is
   *          to be performed.
   * @param scope
   *          The scope of the search.
   * @param filter
   *          The filter that defines the conditions that must be
   *          fulfilled in order for an entry to be returned.
   * @throws NullPointerException
   *           If the {@code baseDN}, {@code scope}, or {@code filter}
   *           were {@code null}.
   */
  SearchRequestImpl(String baseDN, SearchScope scope,
      Filter filter) throws NullPointerException
  {
    Validator.ensureNotNull(baseDN, scope, filter);

    this.baseDN = baseDN;
    this.scope = scope;
    this.filter = filter;
  }



  /**
   * {@inheritDoc}
   */
  public SearchRequest addAttribute(
      Collection<String> attributeDescriptions)
      throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescriptions);

    attributes.addAll(attributeDescriptions);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchRequest addAttribute(String attributeDescription)
      throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescription);

    attributes.add(attributeDescription);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchRequest addAttribute(String... attributeDescriptions)
      throws NullPointerException
  {
    Validator.ensureNotNull((Object) attributeDescriptions);

    for (final String attributeDescription : attributeDescriptions)
    {
      attributes.add(attributeDescription);
    }
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchRequest clearAttributes()
  {
    attributes.clear();
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public Iterable<String> getAttributes()
  {
    return attributes;
  }



  /**
   * {@inheritDoc}
   */
  public String getBaseDN()
  {
    return baseDN;
  }



  /**
   * {@inheritDoc}
   */
  public DereferenceAliasesPolicy getDereferenceAliases()
  {
    return dereferencePolicy;
  }



  /**
   * {@inheritDoc}
   */
  public Filter getFilter()
  {
    return filter;
  }



  /**
   * {@inheritDoc}
   */
  public SearchScope getScope()
  {
    return scope;
  }



  /**
   * {@inheritDoc}
   */
  public int getSizeLimit()
  {
    return sizeLimit;
  }



  /**
   * {@inheritDoc}
   */
  public int getTimeLimit()
  {
    return timeLimit;
  }



  /**
   * {@inheritDoc}
   */
  public boolean hasAttributes()
  {
    return !attributes.isEmpty();
  }



  /**
   * {@inheritDoc}
   */
  public boolean isTypesOnly()
  {
    return typesOnly;
  }



  /**
   * {@inheritDoc}
   */
  public boolean removeAttribute(String attributeDescription)
      throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescription);

    return attributes.remove(attributeDescription);
  }



  /**
   * {@inheritDoc}
   */
  public SearchRequest setBaseDN(String dn) throws NullPointerException
  {
    Validator.ensureNotNull(dn);

    this.baseDN = dn;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchRequest setDereferenceAliases(
      DereferenceAliasesPolicy policy) throws NullPointerException
  {
    Validator.ensureNotNull(policy);

    this.dereferencePolicy = policy;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchRequest setFilter(Filter filter)
      throws NullPointerException
  {
    Validator.ensureNotNull(filter);

    this.filter = filter;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchRequest setFilter(String filter)
      throws IllegalArgumentException, NullPointerException
  {
    this.filter = Filter.valueOf(filter);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchRequest setScope(SearchScope scope)
      throws NullPointerException
  {
    Validator.ensureNotNull(scope);

    this.scope = scope;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchRequest setSizeLimit(int limit)
      throws IllegalArgumentException
  {
    Validator.ensureTrue(limit >= 0, "negative size limit");

    this.sizeLimit = limit;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchRequest setTimeLimit(int limit)
      throws IllegalArgumentException
  {
    Validator.ensureTrue(limit >= 0, "negative time limit");

    this.timeLimit = limit;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SearchRequest setTypesOnly(boolean typesOnly)
  {
    this.typesOnly = typesOnly;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public StringBuilder toString(StringBuilder builder)
      throws NullPointerException
  {
    builder.append("SearchRequest(baseObject=");
    builder.append(baseDN);
    builder.append(", scope=");
    builder.append(scope);
    builder.append(", derefAliases=");
    builder.append(dereferencePolicy);
    builder.append(", sizeLimit=");
    builder.append(sizeLimit);
    builder.append(", timeLimit=");
    builder.append(timeLimit);
    builder.append(", typesOnly=");
    builder.append(typesOnly);
    builder.append(", filter=");
    builder.append(filter);
    builder.append(", attributes=");
    builder.append(attributes);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder;
  }
}
