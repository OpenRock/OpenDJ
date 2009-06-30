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

package org.opends.common.api;



import java.util.List;

import org.opends.server.types.ByteString;



/**
 * A visitor of filters, in the style of the visitor design pattern.
 * Classes implementing this interface can query filters in a type-safe
 * manner. When a visitor is passed to a filter's accept method, the
 * corresponding visit method most applicable to that filter is invoked.
 *
 * @param <R>
 *          The return type of this visitor's methods. Use
 *          {@link java.lang.Void} for visitors that do not need to
 *          return results.
 * @param <P>
 *          The type of the additional parameter to this visitor's
 *          methods. Use {@link java.lang.Void} for visitors that do not
 *          need an additional parameter.
 */
public interface FilterVisitor<R, P>
{

  /**
   * Visits an "and" filter component.
   *
   * @param p
   *          A visitor specified parameter.
   * @param subFilters
   *          The unmodifiable list of sub-filters. An empty sub-filter
   *          list should always evaluate to {@code true} as per
   *          RFC4526.
   * @return Returns a visitor specified result.
   */
  R visitAnd(P p, List<Filter> subFilters);



  /**
   * Visits an "approximate match" filter component.
   *
   * @param p
   *          A visitor specified parameter.
   * @param attributeDescription
   *          The attribute description.
   * @param assertionValue
   *          The assertion value.
   * @return Returns a visitor specified result.
   */
  R visitApproxMatch(P p, String attributeDescription,
      ByteString assertionValue);



  /**
   * Visits an "equality match" filter component.
   *
   * @param p
   *          A visitor specified parameter.
   * @param attributeDescription
   *          The attribute description.
   * @param assertionValue
   *          The assertion value.
   * @return Returns a visitor specified result.
   */
  R visitEqualityMatch(P p, String attributeDescription,
      ByteString assertionValue);



  /**
   * Visits an "extensible" filter component.
   *
   * @param p
   *          A visitor specified parameter.
   * @param matchingRule
   *          The matching rule name.
   * @param attributeDescription
   *          The attribute description.
   * @param matchValue
   *          The assertion value.
   * @param dnAttributes
   *          Indicates whether DN matching should be performed.
   * @return Returns a visitor specified result.
   */
  R visitExtensibleMatch(P p, String matchingRule,
      String attributeDescription, ByteString matchValue,
      boolean dnAttributes);



  /**
   * Visits a "greater or equal" filter component.
   *
   * @param p
   *          A visitor specified parameter.
   * @param attributeDescription
   *          The attribute description.
   * @param assertionValue
   *          The assertion value.
   * @return Returns a visitor specified result.
   */
  R visitGreaterOrEqual(P p, String attributeDescription,
      ByteString assertionValue);



  /**
   * Visits a "less or equal" filter component.
   *
   * @param p
   *          A visitor specified parameter.
   * @param attributeDescription
   *          The attribute description.
   * @param assertionValue
   *          The assertion value.
   * @return Returns a visitor specified result.
   */
  R visitLessOrEqual(P p, String attributeDescription,
      ByteString assertionValue);



  /**
   * Visits a "not" filter component.
   *
   * @param p
   *          A visitor specified parameter.
   * @param subFilter
   *          The sub-filter.
   * @return Returns a visitor specified result.
   */
  R visitNot(P p, Filter subFilter);



  /**
   * Visits an "or" filter component.
   *
   * @param p
   *          A visitor specified parameter.
   * @param subFilters
   *          The unmodifiable list of sub-filters. An empty sub-filter
   *          list should always evaluate to {@code false} as per
   *          RFC4526.
   * @return Returns a visitor specified result.
   */
  R visitOr(P p, List<Filter> subFilters);



  /**
   * Visits a "present" filter component.
   *
   * @param p
   *          A visitor specified parameter.
   * @param attributeDescription
   *          The attribute description.
   * @return Returns a visitor specified result.
   */
  R visitPresent(P p, String attributeDescription);



  /**
   * Visits a "less or equal" filter component.
   *
   * @param p
   *          A visitor specified parameter.
   * @param attributeDescription
   *          The attribute description.
   * @param initialString
   *          The initial sub-string, may be {@code null}.
   * @param anyStrings
   *          The unmodifiable list of any sub-strings, may be empty.
   * @param finalString
   *          The final sub-string, may be {@code null}.
   * @return Returns a visitor specified result.
   */
  R visitSubstrings(P p, String attributeDescription,
      ByteString initialString, List<ByteString> anyStrings,
      ByteString finalString);

}