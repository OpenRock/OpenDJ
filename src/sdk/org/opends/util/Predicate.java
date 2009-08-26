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

package org.opends.util;



/**
 * Predicates map values of type {@code T} to a boolean value and are
 * typically used for performing filtering.
 *
 * @param <T>
 *          The type of values matched by this predicate.
 * @param <P>
 *          The type of the additional parameter to this predicate's
 *          {@code matches} method. Use {@link java.lang.Void} for
 *          predicates that do not need an additional parameter.
 * @see FilteredIterable
 */
public interface Predicate<T, P>
{
  /**
   * Indicates whether or not this predicate matches the provided value.
   *
   * @param p
   *          A predicate specified parameter.
   * @param value
   *          The value for which to make the determination.
   * @return {@code true} if this predicate matches the provided value,
   *         otherwise {@code false}.
   */
  boolean matches(P p, T value);
}
