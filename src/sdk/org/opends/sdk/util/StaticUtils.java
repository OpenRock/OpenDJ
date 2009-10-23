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
import org.opends.messages.MessageDescriptor;
import org.opends.messages.MessageBuilder;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.logging.Logger;

import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;


/**
 * Common utility methods.
 */
public final class StaticUtils
{
  public static final Logger DEBUG_LOG = Logger.getLogger("org.opends.sdk");

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

    /**
   * Appends a lowercase string representation of the contents of the given byte
   * array to the provided buffer, optionally trimming leading and trailing
   * spaces.  This implementation presumes that the provided string will contain
   * only ASCII characters and is optimized for that case.  However, if a
   * non-ASCII character is encountered it will fall back on a more expensive
   * algorithm that will work properly for non-ASCII characters.
   *
   * @param  b       The byte array for which to obtain the lowercase string
   *                 representation.
   * @param  buffer  The buffer to which the lowercase form of the string should
   *                 be appended.
   * @param  trim    Indicates whether leading and trailing spaces should be
   *                 omitted from the string representation.
   */
  public static void toLowerCase(ByteSequence b, StringBuilder buffer,
                                 boolean trim)
  {
    if (b == null)
    {
      return;
    }

    int origBufferLen = buffer.length();
    int length = b.length();
    for (int i=0; i < length; i++)
    {
      if ((b.byteAt(i) & 0x7F) != b.byteAt(i))
      {
        buffer.replace(origBufferLen, buffer.length(),
            b.toString().toLowerCase());
        break;
      }

      int bufferLength = buffer.length();
      switch (b.byteAt(i))
      {
        case ' ':
          // If we don't care about trimming, then we can always append the
          // space.  Otherwise, only do so if there are other characters in the
          // value.
          if (trim && (bufferLength == 0))
          {
            break;
          }

          buffer.append(' ');
          break;
        case 'A':
          buffer.append('a');
          break;
        case 'B':
          buffer.append('b');
          break;
        case 'C':
          buffer.append('c');
          break;
        case 'D':
          buffer.append('d');
          break;
        case 'E':
          buffer.append('e');
          break;
        case 'F':
          buffer.append('f');
          break;
        case 'G':
          buffer.append('g');
          break;
        case 'H':
          buffer.append('h');
          break;
        case 'I':
          buffer.append('i');
          break;
        case 'J':
          buffer.append('j');
          break;
        case 'K':
          buffer.append('k');
          break;
        case 'L':
          buffer.append('l');
          break;
        case 'M':
          buffer.append('m');
          break;
        case 'N':
          buffer.append('n');
          break;
        case 'O':
          buffer.append('o');
          break;
        case 'P':
          buffer.append('p');
          break;
        case 'Q':
          buffer.append('q');
          break;
        case 'R':
          buffer.append('r');
          break;
        case 'S':
          buffer.append('s');
          break;
        case 'T':
          buffer.append('t');
          break;
        case 'U':
          buffer.append('u');
          break;
        case 'V':
          buffer.append('v');
          break;
        case 'W':
          buffer.append('w');
          break;
        case 'X':
          buffer.append('x');
          break;
        case 'Y':
          buffer.append('y');
          break;
        case 'Z':
          buffer.append('z');
          break;
        default:
          buffer.append((char) b.byteAt(i));
      }
    }

    if (trim)
    {
      // Strip off any trailing spaces.
      for (int i=buffer.length()-1; i > 0; i--)
      {
        if (buffer.charAt(i) == ' ')
        {
          buffer.delete(i, i+1);
        }
        else
        {
          break;
        }
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

  public static ByteString evaluateEscapes(SubstringReader reader,
                                           char[] escapeChars, boolean trim)
  {
    return evaluateEscapes(reader, escapeChars, escapeChars, trim);
  }

  public static ByteString evaluateEscapes(SubstringReader reader, 
                                           char[] escapeChars,
                                           char[] delimiterChars,
                                           boolean trim)
  {
    int length = 0;
    int lengthWithoutSpace = 0;
    char c;
    ByteStringBuilder valueBuffer = null;
    
    if(trim)
    {
      reader.skipWhitespaces();
    }

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
        length = lengthWithoutSpace = 0;
      }
      if (delimiterChars != null)
      {
        for(char delimiterChar : delimiterChars)
        {
          if(c == delimiterChar)
          {
            reader.reset();
            if(valueBuffer != null)
            {
              if(trim)
              {
                valueBuffer.append(reader.read(lengthWithoutSpace));
              }
              else
              {
                valueBuffer.append(reader.read(length));
              }
              return valueBuffer.toByteString();
            }
            else
            {
              if(trim)
              {
                if(lengthWithoutSpace > 0)
                {
                 return ByteString.valueOf(reader.read(lengthWithoutSpace));
                }
                return ByteString.empty();
              }
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
      if(c != ' ')
      {
        lengthWithoutSpace++;
      }
    }

    reader.reset();
    if(valueBuffer != null)
    {
      if(trim)
      {
        valueBuffer.append(reader.read(lengthWithoutSpace));
      }
      else
      {
        valueBuffer.append(reader.read(length));
      }
      return valueBuffer.toByteString();
    }
    else
    {
      if(trim)
      {
        if(lengthWithoutSpace > 0)
        {
          return ByteString.valueOf(reader.read(lengthWithoutSpace));
        }
        return ByteString.empty();
      }
      if(length > 0)
      {
        return ByteString.valueOf(reader.read(length));
      }
      return ByteString.empty();
    }
  }

  /**
   * Construct a byte array containing the UTF-8 encoding of the
   * provided string. This is significantly faster
   * than calling {@link String#getBytes(String)} for ASCII strings.
   *
   * @param s
   *          The string to convert to a UTF-8 byte array.
   * @return Returns a byte array containing the UTF-8 encoding of the
   *         provided string.
   */
  public static byte[] getBytes(String s)
  {
    if (s == null) return null;

    try
    {
      char c;
      int length = s.length();
      byte[] returnArray = new byte[length];
      for (int i=0; i < length; i++)
      {
        c = s.charAt(i);
        returnArray[i] = (byte) (c & 0x0000007F);
        if (c != returnArray[i])
        {
          return s.getBytes("UTF-8");
        }
      }

      return returnArray;
    }
    catch (Exception e)
    {
      DEBUG_LOG.warning("Unable to encode UTF-8 string " + s);

      return s.getBytes();
    }
  }


  /**
   * Indicates whether the provided character is a numeric digit.
   *
   * @param  c  The character for which to make the determination.
   *
   * @return  <CODE>true</CODE> if the provided character represents a numeric
   *          digit, or <CODE>false</CODE> if not.
   */
  public static boolean isDigit(char c)
  {
    switch (c)
    {
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        return true;
      default:
        return false;
    }
  }

  /**
   * Indicates whether the provided character is an ASCII alphabetic character.
   *
   * @param  c  The character for which to make the determination.
   *
   * @return  <CODE>true</CODE> if the provided value is an uppercase or
   *          lowercase ASCII alphabetic character, or <CODE>false</CODE> if it
   *          is not.
   */
  public static boolean isAlpha(char c)
  {
    switch (c)
    {
      case 'A':
      case 'B':
      case 'C':
      case 'D':
      case 'E':
      case 'F':
      case 'G':
      case 'H':
      case 'I':
      case 'J':
      case 'K':
      case 'L':
      case 'M':
      case 'N':
      case 'O':
      case 'P':
      case 'Q':
      case 'R':
      case 'S':
      case 'T':
      case 'U':
      case 'V':
      case 'W':
      case 'X':
      case 'Y':
      case 'Z':
        return true;

      case '[':
      case '\\':
      case ']':
      case '^':
      case '_':
      case '`':
        // Making sure all possible cases are present in one contiguous range
        // can result in a performance improvement.
        return false;

      case 'a':
      case 'b':
      case 'c':
      case 'd':
      case 'e':
      case 'f':
      case 'g':
      case 'h':
      case 'i':
      case 'j':
      case 'k':
      case 'l':
      case 'm':
      case 'n':
      case 'o':
      case 'p':
      case 'q':
      case 'r':
      case 's':
      case 't':
      case 'u':
      case 'v':
      case 'w':
      case 'x':
      case 'y':
      case 'z':
        return true;
      default:
        return false;
    }
  }



  /**
   * Indicates whether the provided character is a hexadecimal digit.
   *
   * @param  c  The character for which to make the determination.
   *
   * @return  <CODE>true</CODE> if the provided character represents a
   *          hexadecimal digit, or <CODE>false</CODE> if not.
   */
  public static boolean isHexDigit(char c)
  {
    switch (c)
    {
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
      case 'A':
      case 'B':
      case 'C':
      case 'D':
      case 'E':
      case 'F':
      case 'a':
      case 'b':
      case 'c':
      case 'd':
      case 'e':
      case 'f':
        return true;
      default:
        return false;
    }
  }

    /**
   * Retrieves the best human-readable message for the provided exception.  For
   * exceptions defined in the OpenDS project, it will attempt to use the
   * message (combining it with the message ID if available).  For some
   * exceptions that use encapsulation (e.g., InvocationTargetException), it
   * will be unwrapped and the cause will be treated.  For all others, the
   *
   *
   * @param  t  The {@code Throwable} object for which to retrieve the message.
   *
   * @return  The human-readable message generated for the provided exception.
   */
  public static Message getExceptionMessage(Throwable t)
  {
    if (t instanceof LocalizableException)
    {
      LocalizableException ie = (LocalizableException) t;

      StringBuilder message = new StringBuilder();
      message.append(ie.getMessageObject());
      message.append(" (id=");
      Message ieMsg = ie.getMessageObject();
      if (ieMsg != null) {
        message.append(ieMsg.getDescriptor().getId());
      } else {
        message.append(MessageDescriptor.NULL_ID);
      }
      message.append(")");
      return Message.raw(message.toString());
    }
    else if (t instanceof NullPointerException)
    {
      StackTraceElement[] stackElements = t.getStackTrace();

      MessageBuilder message = new MessageBuilder();
      message.append("NullPointerException(");
      message.append(stackElements[0].getFileName());
      message.append(":");
      message.append(stackElements[0].getLineNumber());
      message.append(")");
      return message.toMessage();
    }
    else if ((t instanceof InvocationTargetException) &&
             (t.getCause() != null))
    {
      return getExceptionMessage(t.getCause());
    }
    else
    {
      StringBuilder message = new StringBuilder();

      String className = t.getClass().getName();
      int periodPos = className.lastIndexOf('.');
      if (periodPos > 0)
      {
        message.append(className.substring(periodPos+1));
      }
      else
      {
        message.append(className);
      }

      message.append("(");
      if (t.getMessage() == null)
      {
        StackTraceElement[] stackElements = t.getStackTrace();
        message.append(stackElements[0].getFileName());
        message.append(":");
        message.append(stackElements[0].getLineNumber());

        // FIXME Temporary to debug issue 2256.
        if (t instanceof IllegalStateException)
        {
          for (int i = 1; i < stackElements.length; i++)
          {
            message.append(' ');
            message.append(stackElements[i].getFileName());
            message.append(":");
            message.append(stackElements[i].getLineNumber());
          }
        }
      }
      else
      {
        message.append(t.getMessage());
      }

      message.append(")");

      return Message.raw(message.toString());
    }
  }

  /**
   * Retrieves a string representation of the provided byte in hexadecimal.
   *
   * @param  b  The byte for which to retrieve the hexadecimal string
   *            representation.
   *
   * @return  The string representation of the provided byte in hexadecimal.
   */
  public static String byteToHex(byte b)
  {
    switch (b & 0xFF)
    {
      case 0x00:  return "00";
      case 0x01:  return "01";
      case 0x02:  return "02";
      case 0x03:  return "03";
      case 0x04:  return "04";
      case 0x05:  return "05";
      case 0x06:  return "06";
      case 0x07:  return "07";
      case 0x08:  return "08";
      case 0x09:  return "09";
      case 0x0A:  return "0A";
      case 0x0B:  return "0B";
      case 0x0C:  return "0C";
      case 0x0D:  return "0D";
      case 0x0E:  return "0E";
      case 0x0F:  return "0F";
      case 0x10:  return "10";
      case 0x11:  return "11";
      case 0x12:  return "12";
      case 0x13:  return "13";
      case 0x14:  return "14";
      case 0x15:  return "15";
      case 0x16:  return "16";
      case 0x17:  return "17";
      case 0x18:  return "18";
      case 0x19:  return "19";
      case 0x1A:  return "1A";
      case 0x1B:  return "1B";
      case 0x1C:  return "1C";
      case 0x1D:  return "1D";
      case 0x1E:  return "1E";
      case 0x1F:  return "1F";
      case 0x20:  return "20";
      case 0x21:  return "21";
      case 0x22:  return "22";
      case 0x23:  return "23";
      case 0x24:  return "24";
      case 0x25:  return "25";
      case 0x26:  return "26";
      case 0x27:  return "27";
      case 0x28:  return "28";
      case 0x29:  return "29";
      case 0x2A:  return "2A";
      case 0x2B:  return "2B";
      case 0x2C:  return "2C";
      case 0x2D:  return "2D";
      case 0x2E:  return "2E";
      case 0x2F:  return "2F";
      case 0x30:  return "30";
      case 0x31:  return "31";
      case 0x32:  return "32";
      case 0x33:  return "33";
      case 0x34:  return "34";
      case 0x35:  return "35";
      case 0x36:  return "36";
      case 0x37:  return "37";
      case 0x38:  return "38";
      case 0x39:  return "39";
      case 0x3A:  return "3A";
      case 0x3B:  return "3B";
      case 0x3C:  return "3C";
      case 0x3D:  return "3D";
      case 0x3E:  return "3E";
      case 0x3F:  return "3F";
      case 0x40:  return "40";
      case 0x41:  return "41";
      case 0x42:  return "42";
      case 0x43:  return "43";
      case 0x44:  return "44";
      case 0x45:  return "45";
      case 0x46:  return "46";
      case 0x47:  return "47";
      case 0x48:  return "48";
      case 0x49:  return "49";
      case 0x4A:  return "4A";
      case 0x4B:  return "4B";
      case 0x4C:  return "4C";
      case 0x4D:  return "4D";
      case 0x4E:  return "4E";
      case 0x4F:  return "4F";
      case 0x50:  return "50";
      case 0x51:  return "51";
      case 0x52:  return "52";
      case 0x53:  return "53";
      case 0x54:  return "54";
      case 0x55:  return "55";
      case 0x56:  return "56";
      case 0x57:  return "57";
      case 0x58:  return "58";
      case 0x59:  return "59";
      case 0x5A:  return "5A";
      case 0x5B:  return "5B";
      case 0x5C:  return "5C";
      case 0x5D:  return "5D";
      case 0x5E:  return "5E";
      case 0x5F:  return "5F";
      case 0x60:  return "60";
      case 0x61:  return "61";
      case 0x62:  return "62";
      case 0x63:  return "63";
      case 0x64:  return "64";
      case 0x65:  return "65";
      case 0x66:  return "66";
      case 0x67:  return "67";
      case 0x68:  return "68";
      case 0x69:  return "69";
      case 0x6A:  return "6A";
      case 0x6B:  return "6B";
      case 0x6C:  return "6C";
      case 0x6D:  return "6D";
      case 0x6E:  return "6E";
      case 0x6F:  return "6F";
      case 0x70:  return "70";
      case 0x71:  return "71";
      case 0x72:  return "72";
      case 0x73:  return "73";
      case 0x74:  return "74";
      case 0x75:  return "75";
      case 0x76:  return "76";
      case 0x77:  return "77";
      case 0x78:  return "78";
      case 0x79:  return "79";
      case 0x7A:  return "7A";
      case 0x7B:  return "7B";
      case 0x7C:  return "7C";
      case 0x7D:  return "7D";
      case 0x7E:  return "7E";
      case 0x7F:  return "7F";
      case 0x80:  return "80";
      case 0x81:  return "81";
      case 0x82:  return "82";
      case 0x83:  return "83";
      case 0x84:  return "84";
      case 0x85:  return "85";
      case 0x86:  return "86";
      case 0x87:  return "87";
      case 0x88:  return "88";
      case 0x89:  return "89";
      case 0x8A:  return "8A";
      case 0x8B:  return "8B";
      case 0x8C:  return "8C";
      case 0x8D:  return "8D";
      case 0x8E:  return "8E";
      case 0x8F:  return "8F";
      case 0x90:  return "90";
      case 0x91:  return "91";
      case 0x92:  return "92";
      case 0x93:  return "93";
      case 0x94:  return "94";
      case 0x95:  return "95";
      case 0x96:  return "96";
      case 0x97:  return "97";
      case 0x98:  return "98";
      case 0x99:  return "99";
      case 0x9A:  return "9A";
      case 0x9B:  return "9B";
      case 0x9C:  return "9C";
      case 0x9D:  return "9D";
      case 0x9E:  return "9E";
      case 0x9F:  return "9F";
      case 0xA0:  return "A0";
      case 0xA1:  return "A1";
      case 0xA2:  return "A2";
      case 0xA3:  return "A3";
      case 0xA4:  return "A4";
      case 0xA5:  return "A5";
      case 0xA6:  return "A6";
      case 0xA7:  return "A7";
      case 0xA8:  return "A8";
      case 0xA9:  return "A9";
      case 0xAA:  return "AA";
      case 0xAB:  return "AB";
      case 0xAC:  return "AC";
      case 0xAD:  return "AD";
      case 0xAE:  return "AE";
      case 0xAF:  return "AF";
      case 0xB0:  return "B0";
      case 0xB1:  return "B1";
      case 0xB2:  return "B2";
      case 0xB3:  return "B3";
      case 0xB4:  return "B4";
      case 0xB5:  return "B5";
      case 0xB6:  return "B6";
      case 0xB7:  return "B7";
      case 0xB8:  return "B8";
      case 0xB9:  return "B9";
      case 0xBA:  return "BA";
      case 0xBB:  return "BB";
      case 0xBC:  return "BC";
      case 0xBD:  return "BD";
      case 0xBE:  return "BE";
      case 0xBF:  return "BF";
      case 0xC0:  return "C0";
      case 0xC1:  return "C1";
      case 0xC2:  return "C2";
      case 0xC3:  return "C3";
      case 0xC4:  return "C4";
      case 0xC5:  return "C5";
      case 0xC6:  return "C6";
      case 0xC7:  return "C7";
      case 0xC8:  return "C8";
      case 0xC9:  return "C9";
      case 0xCA:  return "CA";
      case 0xCB:  return "CB";
      case 0xCC:  return "CC";
      case 0xCD:  return "CD";
      case 0xCE:  return "CE";
      case 0xCF:  return "CF";
      case 0xD0:  return "D0";
      case 0xD1:  return "D1";
      case 0xD2:  return "D2";
      case 0xD3:  return "D3";
      case 0xD4:  return "D4";
      case 0xD5:  return "D5";
      case 0xD6:  return "D6";
      case 0xD7:  return "D7";
      case 0xD8:  return "D8";
      case 0xD9:  return "D9";
      case 0xDA:  return "DA";
      case 0xDB:  return "DB";
      case 0xDC:  return "DC";
      case 0xDD:  return "DD";
      case 0xDE:  return "DE";
      case 0xDF:  return "DF";
      case 0xE0:  return "E0";
      case 0xE1:  return "E1";
      case 0xE2:  return "E2";
      case 0xE3:  return "E3";
      case 0xE4:  return "E4";
      case 0xE5:  return "E5";
      case 0xE6:  return "E6";
      case 0xE7:  return "E7";
      case 0xE8:  return "E8";
      case 0xE9:  return "E9";
      case 0xEA:  return "EA";
      case 0xEB:  return "EB";
      case 0xEC:  return "EC";
      case 0xED:  return "ED";
      case 0xEE:  return "EE";
      case 0xEF:  return "EF";
      case 0xF0:  return "F0";
      case 0xF1:  return "F1";
      case 0xF2:  return "F2";
      case 0xF3:  return "F3";
      case 0xF4:  return "F4";
      case 0xF5:  return "F5";
      case 0xF6:  return "F6";
      case 0xF7:  return "F7";
      case 0xF8:  return "F8";
      case 0xF9:  return "F9";
      case 0xFA:  return "FA";
      case 0xFB:  return "FB";
      case 0xFC:  return "FC";
      case 0xFD:  return "FD";
      case 0xFE:  return "FE";
      case 0xFF:  return "FF";
      default:    return "??";
    }
  }

    /**
   * Converts the provided hexadecimal string to a byte array.
   *
   * @param  hexString  The hexadecimal string to convert to a byte array.
   *
   * @return  The byte array containing the binary representation of the
   *          provided hex string.
   *
   * @throws java.text.ParseException  If the provided string contains invalid
   *                          hexadecimal digits or does not contain an even
   *                          number of digits.
   */
  public static byte[] hexStringToByteArray(String hexString)
         throws ParseException
  {
    int length;
    if ((hexString == null) || ((length = hexString.length()) == 0))
    {
      return new byte[0];
    }


    if ((length % 2) == 1)
    {
      Message message = ERR_HEX_DECODE_INVALID_LENGTH.get(hexString);
      throw new ParseException(message.toString(), 0);
    }

    int arrayLength = (length / 2);
    byte[] returnArray = new byte[arrayLength];
    for (int i=0; i < arrayLength; i++)
    {
      returnArray[i] = hexToByte(hexString.charAt(i*2),
          hexString.charAt(i*2+1));
    }

    return returnArray;
  }

  /**
   * Converts the provided pair of characters to a byte.
   *
   * @param  c1  The first hexadecimal character.
   * @param  c2  The second hexadecimal character.
   *
   * @return  The byte containing the binary representation of the
   *          provided hex characters.
   *
   * @throws  ParseException  If the provided string contains invalid
   *                          hexadecimal digits or does not contain an even
   *                          number of digits.
   */
  public static byte hexToByte(char c1, char c2)
         throws ParseException
  {
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
        Message message = ERR_HEX_DECODE_INVALID_CHARACTER.get(
            new String(new char[]{c1, c2}), c1);
        throw new ParseException(message.toString(), 0);
    }

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
        throw new ParseException(message.toString(), 0);
    }

    return b;
  }

  // Prevent instantiation.
  private StaticUtils()
  {
    // No implementation required.
  }
}
