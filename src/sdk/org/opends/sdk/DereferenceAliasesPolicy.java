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

package org.opends.sdk;



import java.util.Arrays;
import java.util.List;


/**
 * A Search operation alias dereferencing policy as defined in RFC 4511
 * section 4.5.1.3 is used to indicate whether or not alias entries (as
 * defined in RFC 4512) are to be dereferenced during stages of a Search
 * operation. The act of dereferencing an alias includes recursively
 * dereferencing aliases that refer to aliases.
 *
 * @see <a href="http://tools.ietf.org/html/rfc4511#section-4.5.1.3">RFC
 *      4511 - Lightweight Directory Access Protocol (LDAP): The
 *      Protocol </a>
 * @see <a href="http://tools.ietf.org/html/rfc4512">RFC 4512 -
 *      Lightweight Directory Access Protocol (LDAP): Directory
 *      Information Models </a>
 */
public final class DereferenceAliasesPolicy
{
    private static final DereferenceAliasesPolicy[] ELEMENTS =
        new DereferenceAliasesPolicy[4];

    /**
   * Do not dereference aliases in searching or in locating the base
   * object of a Search operation.
   */
  public static final DereferenceAliasesPolicy NEVER =
      register(0, "never");

    /**
   * While searching subordinates of the base object, dereference any
   * alias within the scope of the Search operation. Dereferenced
   * objects become the vertices of further search scopes where the
   * Search operation is also applied. If the search scope is {@code
   * WHOLE_SUBTREE}, the Search continues in the subtree(s) of any
   * dereferenced object. If the search scope is {@code SINGLE_LEVEL},
   * the search is applied to any dereferenced objects and is not
   * applied to their subordinates.
   */
  public static final DereferenceAliasesPolicy IN_SEARCHING =
      register(1, "search");

    /**
   * Dereference aliases in locating the base object of a Search
   * operation, but not when searching subordinates of the base object.
   */
  public static final DereferenceAliasesPolicy FINDING_BASE =
      register(2, "find");

    /**
   * Dereference aliases both in searching and in locating the base
   * object of a Search operation.
   */
  public static final DereferenceAliasesPolicy ALWAYS =
      register(3, "always");

  public static DereferenceAliasesPolicy register(int intValue, String name)
  {
    DereferenceAliasesPolicy t = new DereferenceAliasesPolicy(intValue, name);
    ELEMENTS[intValue] = t;
    return t;
  }


  /**
   * Returns the dereference aliases policy having the specified integer
   * value as defined in RFC 4511 section 4.5.1.
   *
   * @param intValue
   *          The integer value of the dereference aliases policy to be
   *          returned.
   * @return The dereference aliases policy.
   * @throws IllegalArgumentException
   *           If {@code intValue} is less than {@code 0} or greater
   *           than {@code 3}.
   */
  public static DereferenceAliasesPolicy valueOf(int intValue)
  {
    DereferenceAliasesPolicy e = ELEMENTS[intValue];
    if (e == null)
    {
      e = new DereferenceAliasesPolicy(intValue, "undefined("+intValue+")");
    }
    return e;
  }

  public static List<DereferenceAliasesPolicy> values()
  {
    return Arrays.asList(ELEMENTS);
  }

  private final int intValue;

  private final String name;



  private DereferenceAliasesPolicy(int intValue, String name)
  {
    this.intValue = intValue;
    this.name = name;
  }

  @Override
  public int hashCode()
  {
    return intValue;
  }

  /**
   * Returns the integer value of this dereference aliases policy as
   * defined in RFC 4511 section 4.5.1.
   *
   * @return The integer value of this dereference aliases policy.
   */
  public int intValue()
  {
    return intValue;
  }



  /**
   * Returns the string representation of this alias dereferencing
   * policy.
   *
   * @return The string representation of this alias dereferencing
   *         policy.
   */
  @Override
  public String toString()
  {
    return name;
  }
}
