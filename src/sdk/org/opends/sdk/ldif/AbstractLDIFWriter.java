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

package org.opends.sdk.ldif;



import java.io.IOException;
import java.util.regex.Pattern;

import org.opends.sdk.AttributeSequence;
import org.opends.sdk.AttributeValueSequence;
import org.opends.sdk.Change;
import org.opends.sdk.ModificationType;
import org.opends.sdk.controls.Control;
import org.opends.sdk.requests.AddRequest;
import org.opends.sdk.requests.DeleteRequest;
import org.opends.sdk.requests.ModifyDNRequest;
import org.opends.sdk.requests.ModifyRequest;
import org.opends.sdk.util.Base64;
import org.opends.sdk.util.Validator;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;



/**
 * An abstract {@code LDIFWriter} which can be used as the basis for
 * implementing new LDIF writer implementations.
 */
abstract class AbstractLDIFWriter implements ChangeRecordWriter
{
  // Regular expression used for splitting comments on line-breaks.
  private static final Pattern SPLIT_NEWLINE =
      Pattern.compile("\\r?\\n");

  boolean addUserFriendlyComments = false;

  // Zero means do not wrap.
  int wrapColumn = 0;

  private final StringBuilder builder = new StringBuilder(80);

  private final boolean isEntryWriter;

  // Visitor used for writing generic change records.
  private static final ChangeRecordVisitor<IOException, ChangeRecordWriter> VISITOR =
      new ChangeRecordVisitor<IOException, ChangeRecordWriter>()
      {

        public IOException visitChangeRecord(ChangeRecordWriter p,
            AddRequest change)
        {
          try
          {
            p.writeChangeRecord(change);
            return null;
          }
          catch (final IOException e)
          {
            return e;
          }
        }



        public IOException visitChangeRecord(ChangeRecordWriter p,
            DeleteRequest change)
        {
          try
          {
            p.writeChangeRecord(change);
            return null;
          }
          catch (final IOException e)
          {
            return e;
          }
        }



        public IOException visitChangeRecord(ChangeRecordWriter p,
            ModifyRequest change)
        {
          try
          {
            p.writeChangeRecord(change);
            return null;
          }
          catch (final IOException e)
          {
            return e;
          }
        }



        public IOException visitChangeRecord(ChangeRecordWriter p,
            ModifyDNRequest change)
        {
          try
          {
            p.writeChangeRecord(change);
            return null;
          }
          catch (final IOException e)
          {
            return e;
          }
        }
      };



  /**
   * Creates a new {@code AbstractLDIFWriter}.
   */
  AbstractLDIFWriter(boolean isEntryWriter)
  {
    this.isEntryWriter = isEntryWriter;
  }



  /**
   * {@inheritDoc}
   */
  public final void close() throws IOException
  {
    flush();
    close0();
  }



  /**
   * {@inheritDoc}
   */
  public abstract void flush() throws IOException;



  /**
   * {@inheritDoc}
   */
  public final ChangeRecordWriter writeChangeRecord(AddRequest change)
      throws IOException, NullPointerException
  {
    Validator.ensureNotNull(change);

    writeKeyAndValue("dn", change.getName());
    if (!isEntryWriter)
    {
      writeControls(change.getControls());
      writeLine("changetype: add");
    }
    for (final AttributeValueSequence attribute : change
        .getAttributes())
    {
      final String attributeDescription =
          attribute.getAttributeDescriptionAsString();
      for (final ByteString value : attribute)
      {
        writeKeyAndValue(attributeDescription, value);
      }
    }

    // Make sure there is a blank line after the entry.
    println();

    return this;
  }



  /**
   * {@inheritDoc}
   */
  public final ChangeRecordWriter writeEntry(AttributeSequence change)
      throws IOException, NullPointerException
  {
    Validator.ensureNotNull(change);

    writeKeyAndValue("dn", change.getName());
    if (!isEntryWriter)
    {
      writeLine("changetype: add");
    }
    for (final AttributeValueSequence attribute : change
        .getAttributes())
    {
      final String attributeDescription =
          attribute.getAttributeDescriptionAsString();
      for (final ByteString value : attribute)
      {
        writeKeyAndValue(attributeDescription, value);
      }
    }

    // Make sure there is a blank line after the entry.
    println();

    return this;
  }



  /**
   * {@inheritDoc}
   */
  public ChangeRecordWriter writeChangeRecord(ChangeRecord change)
      throws IOException, UnsupportedOperationException,
      NullPointerException
  {
    Validator.ensureNotNull(change);

    final IOException e = change.accept(VISITOR, this);
    if (e != null)
    {
      throw e;
    }
    else
    {
      return this;
    }
  }



  /**
   * {@inheritDoc}
   */
  public final ChangeRecordWriter writeComment(CharSequence comment)
      throws IOException, UnsupportedOperationException,
      NullPointerException
  {
    Validator.ensureNotNull(comment);

    // First, break up the comment into multiple lines to preserve the
    // original spacing that it contained.
    final String[] lines = SPLIT_NEWLINE.split(comment);

    // Now iterate through the lines and write them out, prefixing and
    // wrapping them as necessary.
    for (final String line : lines)
    {
      if (!shouldWrap())
      {
        print("# ");
        print(line);
        println();
      }
      else
      {
        final int breakColumn = wrapColumn - 2;

        if (line.length() <= breakColumn)
        {
          print("# ");
          print(line);
          println();
        }
        else
        {
          int startPos = 0;
          outerLoop: while (startPos < line.length())
          {
            if (startPos + breakColumn >= line.length())
            {
              print("# ");
              print(line.substring(startPos));
              println();
              startPos = line.length();
            }
            else
            {
              final int endPos = startPos + breakColumn;

              int i = endPos - 1;
              while (i > startPos)
              {
                if (line.charAt(i) == ' ')
                {
                  print("# ");
                  print(line.substring(startPos, i));
                  println();

                  startPos = i + 1;
                  continue outerLoop;
                }

                i--;
              }

              // If we've gotten here, then there are no spaces on the
              // entire line. If that happens, then we'll have to break
              // in the middle of a word.
              print("# ");
              print(line.substring(startPos, endPos));
              println();

              startPos = endPos;
            }
          }
        }
      }
    }

    return this;
  }



  /**
   * {@inheritDoc}
   */
  public final ChangeRecordWriter writeChangeRecord(DeleteRequest change)
      throws IOException, UnsupportedOperationException,
      NullPointerException
  {
    Validator.ensureNotNull(change);
    ensureOperationSupported();

    writeKeyAndValue("dn", change.getName());
    writeControls(change.getControls());
    writeLine("changetype: delete");

    // Make sure there is a blank line after the entry.
    println();

    return this;
  }



  /**
   * {@inheritDoc}
   */
  public final ChangeRecordWriter writeChangeRecord(ModifyRequest change)
      throws IOException, UnsupportedOperationException,
      NullPointerException
  {
    Validator.ensureNotNull(change);
    ensureOperationSupported();

    // If there aren't any modifications, then there's nothing to do.
    if (!change.hasChanges())
    {
      return this;
    }

    writeKeyAndValue("dn", change.getName());
    writeControls(change.getControls());
    writeLine("changetype: modify");

    for (final Change modification : change.getChanges())
    {
      final ModificationType type = modification.getModificationType();
      final String attributeDescription =
          modification.getAttributeDescriptionAsString();

      writeKeyAndValue(type.toString(), attributeDescription);
      for (final ByteString value : modification)
      {
        writeKeyAndValue(attributeDescription, value);
      }
      writeLine("-");
    }

    // Make sure there is a blank line after the entry.
    println();

    return this;
  }



  /**
   * {@inheritDoc}
   */
  public final ChangeRecordWriter writeChangeRecord(
      ModifyDNRequest change) throws IOException,
      UnsupportedOperationException, NullPointerException
  {
    Validator.ensureNotNull(change);
    ensureOperationSupported();

    writeKeyAndValue("dn", change.getName());
    writeControls(change.getControls());

    // Write the changetype. Some older tools may not support the
    // "moddn" changetype, so only use it if a newSuperior element has
    // been provided, but use modrdn elsewhere.
    if (change.getNewSuperior() == null)
    {
      writeLine("changetype: modrdn");
    }
    else
    {
      writeLine("changetype: moddn");
    }

    writeKeyAndValue("newrdn", change.getNewRDN());
    writeKeyAndValue("deleteoldrdn", change.isDeleteOldRDN() ? "1"
        : "0");
    if (change.getNewSuperior() != null)
    {
      writeKeyAndValue("newsuperior", change.getNewSuperior());
    }

    // Make sure there is a blank line after the entry.
    println();

    return this;
  }



  /**
   * Closes any resources associated with this {@code LDIFWriter}.
   *
   * @throws IOException
   *           If an error occurs while closing.
   */
  abstract void close0() throws IOException;



  /**
   * Prints the provided {@code CharSequence} to the underlying output
   * stream. Implementations must not add a new-line character sequence.
   *
   * @param s
   *          The {@code CharSequence} to be printed.
   * @throws IOException
   *           If an error occurs while printing {@code s}.
   */
  abstract void print(CharSequence s) throws IOException;



  /**
   * Prints a new-line character sequence to the underlying output
   * stream.
   *
   * @throws IOException
   *           If an error occurs while printing the new-line character
   *           sequence.
   */
  abstract void println() throws IOException;



  // Ensures that change records are only written if supported by chosen
  // LDIF format.
  private void ensureOperationSupported()
      throws UnsupportedOperationException
  {
    if (isEntryWriter)
    {
      throw new UnsupportedOperationException();
    }
  }



  /**
   * Indicates whether the provided {@code ByteSequence} needs to be
   * base64 encoded if it is represented in LDIF form.
   *
   * @param bytes
   *          The {@code ByteSequence} which may need base64 encoding.
   * @return {@code true} if {@code bytes} needs to be base64 encoded,
   *         or {@code false} if not.
   */
  private boolean needsBase64Encoding(ByteSequence bytes)
  {
    final int length = bytes.length();
    if (length == 0)
    {
      return false;
    }

    // If the value starts with a space, colon, or less than, then it
    // needs to be base64 encoded.
    switch (bytes.byteAt(0))
    {
    case 0x20: // Space
    case 0x3A: // Colon
    case 0x3C: // Less-than
      return true;
    }

    // If the value ends with a space, then it needs to be
    // base64 encoded.
    if (length > 1 && bytes.byteAt(length - 1) == 0x20)
    {
      return true;
    }

    // If the value contains a null, newline, or return character, then
    // it needs to be base64 encoded.
    byte b;
    for (int i = 0; i < bytes.length(); i++)
    {
      b = bytes.byteAt(i);
      if (b > 127 || b < 0)
      {
        return true;
      }

      switch (b)
      {
      case 0x00: // Null
      case 0x0A: // New line
      case 0x0D: // Carriage return
        return true;
      }
    }

    // If we've made it here, then there's no reason to base64 encode.
    return false;
  }



  private boolean shouldWrap()
  {
    return wrapColumn > 1;
  }



  /**
   * Writes the provided controls.
   *
   * @param controls
   *          The controls to be written.
   * @throws IOException
   *           If a problem occurs while writing the information.
   */
  private void writeControls(Iterable<Control> controls)
      throws IOException
  {
    for (final Control control : controls)
    {
      final StringBuilder key = new StringBuilder("control: ");
      key.append(control.getOID());
      key.append(control.isCritical() ? " true" : " false");

      if (control.hasValue())
      {
        writeKeyAndValue(key, control.getValue());
      }
      else
      {
        writeLine(key);
      }
    }
  }



  /**
   * Writes the provided LDIF key and URL. The key, a single colon, a
   * left angle bracket, single space, and the provided URL will be
   * appended.
   *
   * @param key
   *          The LDIF key to be appended.
   * @param url
   *          The LDIF value URL to be appended.
   * @throws IOException
   *           If a problem occurs while writing the information.
   */
  @SuppressWarnings("unused")
  private void writeKeyAndURL(CharSequence key, CharSequence url)
      throws IOException
  {
    builder.setLength(0);

    builder.append(key);
    builder.append(":: ");
    builder.append(url);

    writeLine(builder);
  }



  /**
   * Writes the provided LDIF key and value. If the value is safe to
   * include as-is, then the key, a single colon, a single space, and
   * the provided value will be appended. Otherwise, two colons, a
   * single space, and a base64 encoded form of the value will be
   * appended.
   *
   * @param key
   *          The LDIF key to be appended.
   * @param value
   *          The LDIF value to be appended.
   * @throws IOException
   *           If a problem occurs while writing the information.
   */
  private void writeKeyAndValue(CharSequence key, ByteSequence value)
      throws IOException
  {
    builder.setLength(0);

    // If the value is empty, then just append a single colon and a
    // single space.
    if (value.length() == 0)
    {
      builder.append(key);
      builder.append(": ");
    }
    else if (needsBase64Encoding(value))
    {
      if (addUserFriendlyComments)
      {
        // TODO: Only display comments for valid UTF-8 values, not
        // binary values.
      }

      builder.setLength(0);
      builder.append(key);
      builder.append(":: ");
      builder.append(Base64.encode(value));
    }
    else
    {
      builder.append(key);
      builder.append(": ");
      builder.append(value.toString());
    }

    writeLine(builder);
  }



  /**
   * Writes the provided LDIF key and value. If the value is safe to
   * include as-is, then the key, a single colon, a single space, and
   * the provided value will be appended. Otherwise, two colons, a
   * single space, and a base64 encoded form of the value will be
   * appended.
   *
   * @param key
   *          The LDIF key to be appended.
   * @param value
   *          The LDIF value to be appended.
   * @throws IOException
   *           If a problem occurs while writing the information.
   */
  private void writeKeyAndValue(CharSequence key, CharSequence value)
      throws IOException
  {
    // FIXME: We should optimize this at some point.
    writeKeyAndValue(key, ByteString.valueOf(value.toString()));
  }



  /**
   * Writes a line of LDIF, wrapping if necessary.
   *
   * @param line
   *          The line to be written.
   * @throws IOException
   *           If a problem occurs while writing the information.
   */
  private void writeLine(CharSequence line) throws IOException
  {
    final int length = line.length();
    if (shouldWrap() && length > wrapColumn)
    {
      print(line.subSequence(0, wrapColumn));
      println();
      int pos = wrapColumn;
      while (pos < length)
      {
        final int writeLength = Math.min(wrapColumn - 1, length - pos);
        print(" ");
        print(line.subSequence(pos, pos + writeLength));
        println();
        pos += wrapColumn - 1;
      }
    }
    else
    {
      print(line);
      println();
    }
  }
}
