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

package org.opends.sdk.util;

import static org.opends.messages.UtilityMessages.ERR_HEX_DECODE_INVALID_CHARACTER;
import static org.opends.messages.UtilityMessages.ERR_HEX_DECODE_INVALID_LENGTH;
import static org.opends.messages.UtilityMessages.ERR_INVALID_ESCAPE_CHAR;

import org.opends.messages.Message;
import org.opends.sdk.LocalizedIllegalArgumentException;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;


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

  private static char evaluateEscapedChar(SubstringReader reader,
                                          char[] escapeChars)
      throws LocalizedIllegalArgumentException
  {
    char c1 = reader.read();
    byte b;
    switch (c1)
    {
      case '0':
        b = 0x00;
        break;
      case '1':
        b = 0x10;
        break;
      case '2':
        b = 0x20;
        break;
      case '3':
        b = 0x30;
        break;
      case '4':
        b = 0x40;
        break;
      case '5':
        b = 0x50;
        break;
      case '6':
        b = 0x60;
        break;
      case '7':
        b = 0x70;
        break;
      case '8':
        b = (byte) 0x80;
        break;
      case '9':
        b = (byte) 0x90;
        break;
      case 'A':
      case 'a':
        b = (byte) 0xA0;
        break;
      case 'B':
      case 'b':
        b = (byte) 0xB0;
        break;
      case 'C':
      case 'c':
        b = (byte) 0xC0;
        break;
      case 'D':
      case 'd':
        b = (byte) 0xD0;
        break;
      case 'E':
      case 'e':
        b = (byte) 0xE0;
        break;
      case 'F':
      case 'f':
        b = (byte) 0xF0;
        break;
      default:
        if(c1 == 0x5C)
        {
          return c1;
        }
        if(escapeChars != null)
        {
          for(char escapeChar : escapeChars)
          {
            if(c1 == escapeChar)
            {
              return c1;
            }
          }
        }
        Message message = ERR_INVALID_ESCAPE_CHAR.get(reader.getString(), c1);
        throw new LocalizedIllegalArgumentException(message);
    }

    // The two positions must be the hex characters that
    // comprise the escaped value.
    if (reader.remaining() == 0)
    {
      Message message =
          ERR_HEX_DECODE_INVALID_LENGTH.get(reader.getString());

      throw new LocalizedIllegalArgumentException(message);
    }

    char c2 = reader.read();
    switch (c2)
    {
      case '0':
        // No action required.
        break;
      case '1':
        b |= 0x01;
        break;
      case '2':
        b |= 0x02;
        break;
      case '3':
        b |= 0x03;
        break;
      case '4':
        b |= 0x04;
        break;
      case '5':
        b |= 0x05;
        break;
      case '6':
        b |= 0x06;
        break;
      case '7':
        b |= 0x07;
        break;
      case '8':
        b |= 0x08;
        break;
      case '9':
        b |= 0x09;
        break;
      case 'A':
      case 'a':
        b |= 0x0A;
        break;
      case 'B':
      case 'b':
        b |= 0x0B;
        break;
      case 'C':
      case 'c':
        b |= 0x0C;
        break;
      case 'D':
      case 'd':
        b |= 0x0D;
        break;
      case 'E':
      case 'e':
        b |= 0x0E;
        break;
      case 'F':
      case 'f':
        b |= 0x0F;
        break;
      default:
        Message message = ERR_HEX_DECODE_INVALID_CHARACTER.get(
            new String(new char[]{c1, c2}), c1);
        throw new LocalizedIllegalArgumentException(message);
    }
    return (char) b;
  }

   public static ByteString evaluateEscapes(String string, int startIndex,
                                            int endIndex, char[] escapeChars)
      throws LocalizedIllegalArgumentException
  {
    return evaluateEscapes(new SubstringReader(
        string.substring(startIndex, endIndex)), escapeChars);
  }

  public static ByteString evaluateEscapes(SubstringReader reader,
                                           char[] escapeChars)
  {
    int length = 0;
    char c;
    ByteStringBuilder valueBuffer = null;
    reader.mark();
    while(reader.remaining() > 0)
    {
      c = reader.read();
      if (c == 0x5C) // The backslash character
      {
        if(valueBuffer == null)
        {
          valueBuffer = new ByteStringBuilder();
        }
        valueBuffer.append(reader.read(length));
        valueBuffer.append(evaluateEscapedChar(reader, escapeChars));
        reader.mark();
        length = 0;
      }
      if (escapeChars != null)
      {
        for(char escapeChar : escapeChars)
        {
          if(c == escapeChar)
          {
            reader.reset();
            if(valueBuffer != null)
            {
              valueBuffer.append(reader.read(length));
              return valueBuffer.toByteString();
            }
            else
            {
              if(length > 0)
              {
                return ByteString.valueOf(reader.read(length));
              }
              return ByteString.empty();
            }
          }
        }
      }
      length++;
    }

    reader.reset();
    if(length > 0)
    {
      return ByteString.valueOf(reader.read(length));
    }
    return ByteString.empty();
  }

  // Prevent instantiation.
  private StaticUtils()
  {
    // No implementation required.
  }
}
