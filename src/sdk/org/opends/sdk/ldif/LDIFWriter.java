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
 * Common LDIF writer functionality.
 */
final class LDIFWriter implements ChangeRecordWriter, EntryWriter
{
  // Regular expression used for splitting comments on line-breaks.
  private static final Pattern SPLIT_NEWLINE =
      Pattern.compile("\\r?\\n");

  private final LDIFWriterOptions options;

  private final LDIFWriterImpl impl;

  private final StringBuilder builder = new StringBuilder(80);

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
      };



  /**
   * Creates a new {@code LDIFWriter} with the provided options.
   * 
   * @param options
   *          The LDIF writer options.
   * @param impl
   *          The LDIF writer implementation.
   */
  LDIFWriter(LDIFWriterOptions options, LDIFWriterImpl impl)
  {
    this.options = options;
    this.impl = impl;
  }



  /**
   * {@inheritDoc}
   */
  public final void close() throws IOException
  {
    flush();
    impl.close();
  }



  /**
   * {@inheritDoc}
   */
  public void flush() throws IOException
  {
    impl.flush();
  }



  /**
   * {@inheritDoc}
   */
  public final LDIFWriter writeChangeRecord(AddRequest change)
      throws IOException, NullPointerException
  {
    Validator.ensureNotNull(change);

    writeKeyAndValue("dn", change.getName());
    writeControls(change.getControls());
    writeLine("changetype: add");
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
    impl.println();

    return this;
  }



  /**
   * {@inheritDoc}
   */
  public final LDIFWriter writeChangeRecord(ChangeRecord change)
      throws IOException, NullPointerException
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
  public final LDIFWriter writeChangeRecord(DeleteRequest change)
      throws IOException, NullPointerException
  {
    Validator.ensureNotNull(change);

    writeKeyAndValue("dn", change.getName());
    writeControls(change.getControls());
    writeLine("changetype: delete");

    // Make sure there is a blank line after the entry.
    impl.println();

    return this;
  }



  /**
   * {@inheritDoc}
   */
  public final LDIFWriter writeChangeRecord(ModifyDNRequest change)
      throws IOException, NullPointerException
  {
    Validator.ensureNotNull(change);

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
    impl.println();

    return this;
  }



  /**
   * {@inheritDoc}
   */
  public final LDIFWriter writeChangeRecord(ModifyRequest change)
      throws IOException, NullPointerException
  {
    Validator.ensureNotNull(change);

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
    impl.println();

    return this;
  }



  /**
   * {@inheritDoc}
   */
  public final LDIFWriter writeComment(CharSequence comment)
      throws IOException, NullPointerException
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
        impl.print("# ");
        impl.print(line);
        impl.println();
      }
      else
      {
        final int breakColumn = options.getWrapColumn() - 2;

        if (line.length() <= breakColumn)
        {
          impl.print("# ");
          impl.print(line);
          impl.println();
        }
        else
        {
          int startPos = 0;
          outerLoop: while (startPos < line.length())
          {
            if (startPos + breakColumn >= line.length())
            {
              impl.print("# ");
              impl.print(line.substring(startPos));
              impl.println();
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
                  impl.print("# ");
                  impl.print(line.substring(startPos, i));
                  impl.println();

                  startPos = i + 1;
                  continue outerLoop;
                }

                i--;
              }

              // If we've gotten here, then there are no spaces on the
              // entire line. If that happens, then we'll have to break
              // in the middle of a word.
              impl.print("# ");
              impl.print(line.substring(startPos, endPos));
              impl.println();

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
  public final LDIFWriter writeEntry(AttributeSequence entry)
      throws IOException, NullPointerException
  {
    Validator.ensureNotNull(entry);

    writeKeyAndValue("dn", entry.getName());
    for (final AttributeValueSequence attribute : entry.getAttributes())
    {
      final String attributeDescription =
          attribute.getAttributeDescriptionAsString();
      for (final ByteString value : attribute)
      {
        writeKeyAndValue(attributeDescription, value);
      }
    }

    // Make sure there is a blank line after the entry.
    impl.println();

    return this;
  }



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
    return options.getWrapColumn() > 1;
  }



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
      if (options.isAddUserFriendlyComments())
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



  private void writeKeyAndValue(CharSequence key, CharSequence value)
      throws IOException
  {
    // FIXME: We should optimize this at some point.
    writeKeyAndValue(key, ByteString.valueOf(value.toString()));
  }



  private void writeLine(CharSequence line) throws IOException
  {
    final int length = line.length();
    if (shouldWrap() && length > options.getWrapColumn())
    {
      impl.print(line.subSequence(0, options.getWrapColumn()));
      impl.println();
      int pos = options.getWrapColumn();
      while (pos < length)
      {
        final int writeLength =
            Math.min(options.getWrapColumn() - 1, length - pos);
        impl.print(" ");
        impl.print(line.subSequence(pos, pos + writeLength));
        impl.println();
        pos += options.getWrapColumn() - 1;
      }
    }
    else
    {
      impl.print(line);
      impl.println();
    }
  }
}
