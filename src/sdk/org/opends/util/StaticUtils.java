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
 * Common utility methods.
 */
public final class StaticUtils
{

  /**
   * Retrieves a lower-case representation of the given string. This
   * implementation presumes that the provided string will contain only
   * ASCII characters and is optimized for that case. However, if a
   * non-ASCII character is encountered it will fall back on a more
   * expensive algorithm that will work properly for non-ASCII
   * characters.
   *
   * @param s
   *          The string for which to obtain the lower-case
   *          representation.
   * @return The lower-case representation of the given string.
   */
  public static String toLowerCase(String s)
  {
    Validator.ensureNotNull(s);
    StringBuilder builder = new StringBuilder(s.length());
    toLowerCase0(s, builder);
    return builder.toString();
  }



  /**
   * Appends a lower-case representation of the given string to the
   * provided buffer. This implementation presumes that the provided
   * string will contain only ASCII characters and is optimized for that
   * case. However, if a non-ASCII character is encountered it will fall
   * back on a more expensive algorithm that will work properly for
   * non-ASCII characters.
   *
   * @param s
   *          The string for which to obtain the lower-case
   *          representation.
   * @param builder
   *          The {@code StringBuilder} to which the lower-case form of
   *          the string should be appended.
   * @return The updated {@code StringBuilder}.
   */
  public static StringBuilder toLowerCase(String s,
      StringBuilder builder)
  {
    Validator.ensureNotNull(s, builder);
    builder.ensureCapacity(builder.length() + s.length());
    toLowerCase0(s, builder);
    return builder;
  }



  // toLowerCase implementation.
  private static void toLowerCase0(String s, StringBuilder builder)
  {
    int length = s.length();
    for (int i = 0; i < length; i++)
    {
      char c = s.charAt(i);

      if ((c & 0x7F) != c)
      {
        builder.append(s.substring(i).toLowerCase());
        return;
      }

      switch (c)
      {
      case 'A':
        builder.append('a');
        break;
      case 'B':
        builder.append('b');
        break;
      case 'C':
        builder.append('c');
        break;
      case 'D':
        builder.append('d');
        break;
      case 'E':
        builder.append('e');
        break;
      case 'F':
        builder.append('f');
        break;
      case 'G':
        builder.append('g');
        break;
      case 'H':
        builder.append('h');
        break;
      case 'I':
        builder.append('i');
        break;
      case 'J':
        builder.append('j');
        break;
      case 'K':
        builder.append('k');
        break;
      case 'L':
        builder.append('l');
        break;
      case 'M':
        builder.append('m');
        break;
      case 'N':
        builder.append('n');
        break;
      case 'O':
        builder.append('o');
        break;
      case 'P':
        builder.append('p');
        break;
      case 'Q':
        builder.append('q');
        break;
      case 'R':
        builder.append('r');
        break;
      case 'S':
        builder.append('s');
        break;
      case 'T':
        builder.append('t');
        break;
      case 'U':
        builder.append('u');
        break;
      case 'V':
        builder.append('v');
        break;
      case 'W':
        builder.append('w');
        break;
      case 'X':
        builder.append('x');
        break;
      case 'Y':
        builder.append('y');
        break;
      case 'Z':
        builder.append('z');
        break;
      default:
        builder.append(c);
      }
    }
  }



  // Prevent instantiation.
  private StaticUtils()
  {
    // No implementation required.
  }
}
