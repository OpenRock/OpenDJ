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



import static org.opends.messages.UtilityMessages.*;
import static org.opends.sdk.util.StaticUtils.toLowerCase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opends.messages.Message;
import org.opends.messages.MessageBuilder;
import org.opends.sdk.Attribute;
import org.opends.sdk.AttributeDescription;
import org.opends.sdk.AttributeSequence;
import org.opends.sdk.DN;
import org.opends.sdk.DecodeException;
import org.opends.sdk.Entry;
import org.opends.sdk.LocalizedIllegalArgumentException;
import org.opends.sdk.ModificationType;
import org.opends.sdk.RDN;
import org.opends.sdk.SortedEntry;
import org.opends.sdk.Types;
import org.opends.sdk.requests.ModifyDNRequest;
import org.opends.sdk.requests.ModifyRequest;
import org.opends.sdk.requests.Requests;
import org.opends.sdk.util.Base64;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;



/**
 * Common LDIF reader functionality.
 */
final class LDIFReader implements ChangeRecordReader, EntryReader
{
  private static final class KeyValuePair
  {
    private String key;
    private String value;
  }



  private static final class LDIFRecord
  {
    private final long lineNumber;
    private final LinkedList<String> ldifLines;
    private final Iterator<String> iterator;



    private LDIFRecord(long lineNumber, LinkedList<String> ldifLines)
    {
      this.lineNumber = lineNumber;
      this.ldifLines = ldifLines;
      this.iterator = ldifLines.iterator();
    }
  }

  private long lineNumber = 0;
  private final LDIFReaderOptions options;

  private final LDIFReaderImpl impl;



  /**
   * Creates a new {@code LDIFReader} with the provided options.
   * 
   * @param options
   *          The LDIF reader options.
   * @param impl
   *          The LDIF reader implementation.
   */
  LDIFReader(LDIFReaderOptions options, LDIFReaderImpl impl)
  {
    this.options = options;
    this.impl = impl;
  }



  /**
   * {@inheritDoc}
   */
  public final void close() throws IOException
  {
    impl.close();
  }



  /**
   * {@inheritDoc}
   */
  public ChangeRecord readChangeRecord() throws IOException
  {
    // Continue until an unfiltered entry is obtained.
    while (true)
    {
      LDIFRecord record = null;

      // Read the set of lines that make up the next entry.
      record = readLDIFRecord();
      if (record == null)
      {
        return null;
      }

      // Read the DN of the entry and see if it is one that should be
      // included in the import.
      DN entryDN;
      try
      {
        entryDN = readLDIFRecordDN(record);
        if (entryDN == null)
        {
          // Skip version record.
          continue;
        }
      }
      catch (final DecodeException e)
      {
        rejectLDIFRecord(record, e.getMessageObject());
        continue;
      }

      // TODO: skip if entry DN is excluded.
      ChangeRecord changeRecord = null;
      try
      {
        if (!record.iterator.hasNext())
        {
          // FIXME: improve error.
          final Message message = Message.raw("Missing changetype");
          throw new DecodeException(message);
        }

        final KeyValuePair pair = new KeyValuePair();
        readLDIFRecordKeyValuePair(record, pair, false);

        if (!toLowerCase(pair.key).equals("changetype"))
        {
          // FIXME: improve error.
          final Message message = Message.raw("Missing changetype");
          throw new DecodeException(message);
        }

        final String changeType = toLowerCase(pair.value);
        if (changeType.equals("add"))
        {
          changeRecord = parseAddChangeRecordEntry(entryDN, record);
        }
        else if (changeType.equals("delete"))
        {
          changeRecord = parseDeleteChangeRecordEntry(entryDN, record);
        }
        else if (changeType.equals("modify"))
        {
          changeRecord = parseModifyChangeRecordEntry(entryDN, record);
        }
        else if (changeType.equals("modrdn"))
        {
          changeRecord =
              parseModifyDNChangeRecordEntry(entryDN, record);
        }
        else if (changeType.equals("moddn"))
        {
          changeRecord =
              parseModifyDNChangeRecordEntry(entryDN, record);
        }
        else
        {
          // FIXME: improve error.
          final Message message =
              ERR_LDIF_INVALID_CHANGETYPE_ATTRIBUTE.get(pair.value,
                  "add, delete, modify, moddn, modrdn");
          throw new DecodeException(message);
        }
      }
      catch (final DecodeException e)
      {
        rejectLDIFRecord(record, e.getMessageObject());
        continue;
      }

      if (changeRecord != null)
      {
        return changeRecord;
      }
    }
  }



  /**
   * {@inheritDoc}
   */
  public AttributeSequence readEntry() throws DecodeException,
      IOException
  {
    // Continue until an unfiltered entry is obtained.
    while (true)
    {
      LDIFRecord record = null;

      // Read the set of lines that make up the next entry.
      record = readLDIFRecord();
      if (record == null)
      {
        return null;
      }

      // Read the DN of the entry and see if it is one that should be
      // included in the import.
      DN entryDN;
      try
      {
        entryDN = readLDIFRecordDN(record);
        if (entryDN == null)
        {
          // Skip version record.
          continue;
        }
      }
      catch (final DecodeException e)
      {
        rejectLDIFRecord(record, e.getMessageObject());
        continue;
      }

      // TODO: skip if entry DN is excluded.

      // Use an Entry for the AttributeSequence.
      final Entry entry =
          new SortedEntry(options.getSchema()).setNameDN(entryDN);
      try
      {
        while (record.iterator.hasNext())
        {
          readLDIFRecordAttributeValue(record, entry);
        }
      }
      catch (final DecodeException e)
      {
        rejectLDIFRecord(record, e.getMessageObject());
        continue;
      }

      // TODO: skip entry if excluded based on filtering.

      return entry;
    }
  }



  // Determine whether the provided line is a continuation line. Note
  // that while RFC 2849 technically only allows a space in this
  // position, both OpenLDAP and the Sun Java System Directory Server
  // allow a tab as well, so we will too for compatibility reasons. See
  // issue #852 for details.
  private boolean isContinuationLine(String line)
  {
    return line.charAt(0) == ' ' || line.charAt(0) == '\t';
  }



  private ChangeRecord parseAddChangeRecordEntry(DN entryDN,
      LDIFRecord record) throws DecodeException
  {
    // Use an Entry for the AttributeSequence.
    final Entry entry =
        new SortedEntry(options.getSchema()).setNameDN(entryDN);

    while (record.iterator.hasNext())
    {
      readLDIFRecordAttributeValue(record, entry);
    }

    // TODO: skip entry if excluded based on filtering.
    return Requests.asAddRequest(entry);
  }



  private int parseColonPosition(LDIFRecord record, String ldifLine)
      throws DecodeException
  {
    final int colonPos = ldifLine.indexOf(":");
    if (colonPos <= 0)
    {
      final Message message =
          ERR_LDIF_NO_ATTR_NAME.get(record.lineNumber, ldifLine);
      throw new DecodeException(message);
    }
    return colonPos;
  }



  private ChangeRecord parseDeleteChangeRecordEntry(DN entryDN,
      LDIFRecord record) throws DecodeException
  {
    if (record.iterator.hasNext())
    {
      // FIXME: include line number in error.
      final Message message = ERR_LDIF_INVALID_DELETE_ATTRIBUTES.get();
      throw new DecodeException(message);
    }

    return Requests.newDeleteRequest(entryDN.toString());
  }



  private ChangeRecord parseModifyChangeRecordEntry(DN entryDN,
      LDIFRecord record) throws DecodeException
  {
    final ModifyRequest modifyRequest =
        Requests.newModifyRequest(entryDN.toString());

    final KeyValuePair pair = new KeyValuePair();
    final List<ByteString> attributeValues =
        new ArrayList<ByteString>();

    while (record.iterator.hasNext())
    {
      readLDIFRecordKeyValuePair(record, pair, false);
      final String changeType = toLowerCase(pair.key);

      ModificationType modType;
      if (changeType.equals("add"))
      {
        modType = ModificationType.ADD;
      }
      else if (changeType.equals("delete"))
      {
        modType = ModificationType.DELETE;
      }
      else if (changeType.equals("replace"))
      {
        modType = ModificationType.REPLACE;
      }
      else if (changeType.equals("increment"))
      {
        modType = ModificationType.INCREMENT;
      }
      else
      {
        // FIXME: improve error.
        final Message message =
            ERR_LDIF_INVALID_MODIFY_ATTRIBUTE.get(pair.key,
                "add, delete, replace, increment");
        throw new DecodeException(message);
      }

      AttributeDescription attributeDescription;
      try
      {
        attributeDescription =
            AttributeDescription.valueOf(pair.value, options
                .getSchema());
      }
      catch (final LocalizedIllegalArgumentException e)
      {
        throw new DecodeException(e.getMessageObject());
      }

      // Now go through the rest of the attributes until the "-" line is
      // reached.
      attributeValues.clear();
      while (record.iterator.hasNext())
      {
        final String ldifLine = record.iterator.next();
        if (ldifLine.equals("-"))
        {
          break;
        }

        // Parse the attribute description.
        final int colonPos = parseColonPosition(record, ldifLine);
        final String attrDescr = ldifLine.substring(0, colonPos);

        AttributeDescription attributeDescription2;
        try
        {
          attributeDescription2 =
              AttributeDescription.valueOf(attrDescr, options
                  .getSchema());
        }
        catch (final LocalizedIllegalArgumentException e)
        {
          throw new DecodeException(e.getMessageObject());
        }

        if (!attributeDescription2.equals(attributeDescription2))
        {
          // TODO: include line number.
          final Message message =
              ERR_LDIF_INVALID_CHANGERECORD_ATTRIBUTE.get(
                  attributeDescription2.toString(),
                  attributeDescription.toString());
          throw new DecodeException(message);
        }

        // Now parse the attribute value.
        attributeValues.add(parseSingleValue(record, ldifLine, entryDN
            .toString(), colonPos, attrDescr));
      }

      modifyRequest.addChange(modType, attributeDescription.toString(),
          attributeValues);
    }

    return modifyRequest;
  }



  private ChangeRecord parseModifyDNChangeRecordEntry(DN entryDN,
      LDIFRecord record) throws DecodeException
  {
    ModifyDNRequest modifyDNRequest;

    // Parse the newrdn.
    if (!record.iterator.hasNext())
    {
      // TODO: include line number.
      final Message message = ERR_LDIF_NO_MOD_DN_ATTRIBUTES.get();
      throw new DecodeException(message);
    }

    final KeyValuePair pair = new KeyValuePair();
    String ldifLine = record.iterator.next();
    readLDIFRecordKeyValuePair(record, pair, true);
    if (!toLowerCase(pair.key).equals("newrdn"))
    {
      // FIXME: improve error.
      final Message message = Message.raw("Missing newrdn");
      throw new DecodeException(message);
    }

    try
    {
      final RDN newRDN = RDN.valueOf(pair.value, options.getSchema());
      modifyDNRequest =
          Requests.newModifyDNRequest(entryDN.toString(), newRDN
              .toString());
    }
    catch (final LocalizedIllegalArgumentException e)
    {
      final Message message =
          ERR_LDIF_INVALID_DN.get(record.lineNumber, ldifLine, e
              .getMessageObject());
      throw new DecodeException(message);
    }

    // Parse the deleteoldrdn.
    if (!record.iterator.hasNext())
    {
      // TODO: include line number.
      final Message message = ERR_LDIF_NO_DELETE_OLDRDN_ATTRIBUTE.get();
      throw new DecodeException(message);
    }

    ldifLine = record.iterator.next();
    readLDIFRecordKeyValuePair(record, pair, true);
    if (!toLowerCase(pair.key).equals("deleteoldrdn"))
    {
      // FIXME: improve error.
      final Message message = Message.raw("Missing deleteoldrdn");
      throw new DecodeException(message);
    }

    final String delStr = toLowerCase(pair.value);
    if (delStr.equals("false") || delStr.equals("no")
        || delStr.equals("0"))
    {
      modifyDNRequest.setDeleteOldRDN(false);
    }
    else if (delStr.equals("true") || delStr.equals("yes")
        || delStr.equals("1"))
    {
      modifyDNRequest.setDeleteOldRDN(true);
    }
    else
    {
      // FIXME: improve error.
      final Message message =
          ERR_LDIF_INVALID_DELETE_OLDRDN_ATTRIBUTE.get(pair.value);
      throw new DecodeException(message);
    }

    // Parse the newsuperior if present.
    if (record.iterator.hasNext())
    {
      ldifLine = record.iterator.next();
      readLDIFRecordKeyValuePair(record, pair, true);
      if (!toLowerCase(pair.key).equals("newsuperior"))
      {
        // FIXME: improve error.
        final Message message = Message.raw("Missing newsuperior");
        throw new DecodeException(message);
      }

      try
      {
        final DN newSuperiorDN =
            DN.valueOf(pair.value, options.getSchema());
        modifyDNRequest.setNewSuperior(newSuperiorDN.toString());
      }
      catch (final LocalizedIllegalArgumentException e)
      {
        final Message message =
            ERR_LDIF_INVALID_DN.get(record.lineNumber, ldifLine, e
                .getMessageObject());
        throw new DecodeException(message);
      }
    }

    return modifyDNRequest;
  }



  private ByteString parseSingleValue(LDIFRecord record,
      String ldifLine, String entryDN, int colonPos, String attrName)
      throws DecodeException
  {

    // Look at the character immediately after the colon. If there is
    // none, then assume an attribute with an empty value. If it is
    // another colon, then the value must be base64-encoded. If it is a
    // less-than sign, then assume that it is a URL. Otherwise, it is a
    // regular value.
    final int length = ldifLine.length();
    ByteString value;
    if (colonPos == length - 1)
    {
      value = ByteString.empty();
    }
    else
    {
      final char c = ldifLine.charAt(colonPos + 1);
      if (c == ':')
      {
        // The value is base64-encoded. Find the first non-blank
        // character, take the rest of the line, and base64-decode it.
        int pos = colonPos + 2;
        while (pos < length && ldifLine.charAt(pos) == ' ')
        {
          pos++;
        }

        try
        {
          value = Base64.decode(ldifLine.substring(pos));
        }
        catch (final LocalizedIllegalArgumentException e)
        {
          // The value did not have a valid base64-encoding.
          final Message message =
              ERR_LDIF_COULD_NOT_BASE64_DECODE_ATTR.get(entryDN,
                  record.lineNumber, ldifLine, e.getMessageObject());
          throw new DecodeException(message);
        }
      }
      else if (c == '<')
      {
        // Find the first non-blank character, decode the rest of the
        // line as a URL, and read its contents.
        int pos = colonPos + 2;
        while (pos < length && ldifLine.charAt(pos) == ' ')
        {
          pos++;
        }

        URL contentURL;
        try
        {
          contentURL = new URL(ldifLine.substring(pos));
        }
        catch (final Exception e)
        {
          // The URL was malformed or had an invalid protocol.
          final Message message =
              ERR_LDIF_INVALID_URL.get(entryDN, record.lineNumber,
                  attrName, String.valueOf(e));
          throw new DecodeException(message);
        }

        InputStream inputStream = null;
        ByteStringBuilder builder = null;
        try
        {
          builder = new ByteStringBuilder();
          inputStream = contentURL.openConnection().getInputStream();

          int bytesRead;
          final byte[] buffer = new byte[4096];
          while ((bytesRead = inputStream.read(buffer)) > 0)
          {
            builder.append(buffer, 0, bytesRead);
          }

          value = builder.toByteString();
        }
        catch (final Exception e)
        {
          // We were unable to read the contents of that URL for some
          // reason.
          final Message message =
              ERR_LDIF_URL_IO_ERROR.get(entryDN, record.lineNumber,
                  attrName, String.valueOf(contentURL), String
                      .valueOf(e));
          throw new DecodeException(message);
        }
        finally
        {
          if (inputStream != null)
          {
            try
            {
              inputStream.close();
            }
            catch (final Exception e)
            {
              // Ignore.
            }
          }
        }
      }
      else
      {
        // The rest of the line should be the value. Skip over any
        // spaces and take the rest of the line as the value.
        int pos = colonPos + 1;
        while (pos < length && ldifLine.charAt(pos) == ' ')
        {
          pos++;
        }

        value = ByteString.valueOf(ldifLine.substring(pos));
      }
    }
    return value;
  }



  private LDIFRecord readLDIFRecord() throws IOException
  {
    // Read the entry lines into a buffer.
    final StringBuilder lastLineBuilder = new StringBuilder();
    final LinkedList<String> ldifLines = new LinkedList<String>();
    long recordLineNumber = 0;

    final int START = 0;
    final int START_COMMENT_LINE = 1;
    final int GOT_LDIF_LINE = 2;
    final int GOT_COMMENT_LINE = 3;
    final int APPENDING_LDIF_LINE = 4;

    int state = START;

    while (true)
    {
      final String line = readLine();

      switch (state)
      {
      case START:
        if (line == null)
        {
          // We have reached the end of the LDIF source.
          return null;
        }
        else if (line.length() == 0)
        {
          // Skip leading blank lines.
        }
        else if (line.charAt(0) == '#')
        {
          // This is a comment at the start of the LDIF record.
          state = START_COMMENT_LINE;
        }
        else if (isContinuationLine(line))
        {
          // Fatal: got a continuation line at the start of the record.
          final Message message =
              ERR_LDIF_INVALID_LEADING_SPACE.get(lineNumber, line);
          throw new DecodeException(message);
        }
        else
        {
          // Got the first line of LDIF.
          ldifLines.add(line);
          recordLineNumber = lineNumber;
          state = GOT_LDIF_LINE;
        }
        break;
      case START_COMMENT_LINE:
        if (line == null)
        {
          // We have reached the end of the LDIF source.
          return null;
        }
        else if (line.length() == 0)
        {
          // Skip leading blank lines and comments.
          state = START;
        }
        else if (line.charAt(0) == '#')
        {
          // This is another comment at the start of the LDIF record.
        }
        else if (isContinuationLine(line))
        {
          // Skip comment continuation lines.
        }
        else
        {
          // Got the first line of LDIF.
          ldifLines.add(line);
          recordLineNumber = lineNumber;
          state = GOT_LDIF_LINE;
        }
        break;
      case GOT_LDIF_LINE:
        if (line == null)
        {
          // We have reached the end of the LDIF source.
          return new LDIFRecord(recordLineNumber, ldifLines);
        }
        else if (line.length() == 0)
        {
          // We have reached the end of the LDIF record.
          return new LDIFRecord(recordLineNumber, ldifLines);
        }
        else if (line.charAt(0) == '#')
        {
          // This is a comment.
          state = GOT_COMMENT_LINE;
        }
        else if (isContinuationLine(line))
        {
          // Got a continuation line for the previous line.
          lastLineBuilder.setLength(0);
          lastLineBuilder.append(ldifLines.removeLast());
          lastLineBuilder.append(line.substring(1));
          state = APPENDING_LDIF_LINE;
        }
        else
        {
          // Got the next line of LDIF.
          ldifLines.add(line);
          state = GOT_LDIF_LINE;
        }
        break;
      case GOT_COMMENT_LINE:
        if (line == null)
        {
          // We have reached the end of the LDIF source.
          return new LDIFRecord(recordLineNumber, ldifLines);
        }
        else if (line.length() == 0)
        {
          // We have reached the end of the LDIF record.
          return new LDIFRecord(recordLineNumber, ldifLines);
        }
        else if (line.charAt(0) == '#')
        {
          // This is another comment.
          state = GOT_COMMENT_LINE;
        }
        else if (isContinuationLine(line))
        {
          // Skip comment continuation lines.
        }
        else
        {
          // Got the next line of LDIF.
          ldifLines.add(line);
          state = GOT_LDIF_LINE;
        }
        break;
      case APPENDING_LDIF_LINE:
        if (line == null)
        {
          // We have reached the end of the LDIF source.
          ldifLines.add(lastLineBuilder.toString());
          return new LDIFRecord(recordLineNumber, ldifLines);
        }
        else if (line.length() == 0)
        {
          // We have reached the end of the LDIF record.
          ldifLines.add(lastLineBuilder.toString());
          return new LDIFRecord(recordLineNumber, ldifLines);
        }
        else if (line.charAt(0) == '#')
        {
          // This is a comment.
          ldifLines.add(lastLineBuilder.toString());
          state = GOT_COMMENT_LINE;
        }
        else if (isContinuationLine(line))
        {
          // Got another continuation line for the previous line.
          lastLineBuilder.append(line.substring(1));
        }
        else
        {
          // Got the next line of LDIF.
          ldifLines.add(lastLineBuilder.toString());
          ldifLines.add(line);
          state = GOT_LDIF_LINE;
        }
        break;
      }
    }
  }



  private void readLDIFRecordAttributeValue(LDIFRecord record,
      Entry entry) throws DecodeException
  {
    // Parse the attribute description.
    final String ldifLine = record.iterator.next();
    final int colonPos = parseColonPosition(record, ldifLine);
    final String attrDescr = ldifLine.substring(0, colonPos);

    AttributeDescription attributeDescription;
    try
    {
      attributeDescription =
          AttributeDescription.valueOf(attrDescr, options.getSchema());
    }
    catch (final LocalizedIllegalArgumentException e)
    {
      throw new DecodeException(e.getMessageObject());
    }

    // Now parse the attribute value.
    final ByteString value =
        parseSingleValue(record, ldifLine, entry.getName(), colonPos,
            attrDescr);

    // TODO: skip attribute if required.

    // TODO: check for binary option: adding if required, raising error
    // if present but not required.

    Attribute attribute = entry.getAttribute(attributeDescription);
    if (attribute == null)
    {
      if (options.validateSchema())
      {
        final MessageBuilder invalidReason = new MessageBuilder();
        if (!attributeDescription.getAttributeType().getSyntax()
            .valueIsAcceptable(value, invalidReason))
        {
          final Message message =
              WARN_LDIF_VALUE_VIOLATES_SYNTAX.get(entry.getName(),
                  record.lineNumber, value.toString(), attrDescr,
                  invalidReason);
          throw new DecodeException(message);
        }
      }

      attribute = Types.newAttribute(attributeDescription, value);
      entry.addAttribute(attribute, null);
    }
    else
    {
      if (options.validateSchema())
      {
        final MessageBuilder invalidReason = new MessageBuilder();
        if (!attributeDescription.getAttributeType().getSyntax()
            .valueIsAcceptable(value, invalidReason))
        {
          final Message message =
              WARN_LDIF_VALUE_VIOLATES_SYNTAX.get(entry.getName(),
                  record.lineNumber, value.toString(), attrDescr,
                  invalidReason);
          throw new DecodeException(message);
        }

        if (!attribute.add(value))
        {
          final Message message =
              WARN_LDIF_DUPLICATE_ATTR.get(entry.getName(),
                  record.lineNumber, attrDescr, value.toString());
          throw new DecodeException(message);
        }

        if (attributeDescription.getAttributeType().isSingleValue())
        {
          final Message message =
              ERR_LDIF_MULTIPLE_VALUES_FOR_SINGLE_VALUED_ATTR.get(entry
                  .getName(), record.lineNumber, attrDescr);
          throw new DecodeException(message);
        }
      }
      else
      {
        attribute.add(value);
      }
    }
  }



  private DN readLDIFRecordDN(LDIFRecord record) throws DecodeException
  {
    String ldifLine = record.iterator.next();
    int colonPos = ldifLine.indexOf(":");
    if (colonPos <= 0)
    {
      final Message message =
          ERR_LDIF_NO_ATTR_NAME.get(record.lineNumber, ldifLine
              .toString());
      throw new DecodeException(message);
    }

    String attrName = toLowerCase(ldifLine.substring(0, colonPos));
    if (attrName.equals("version"))
    {
      // This is the version line, try the next line if there is one.
      if (!record.iterator.hasNext())
      {
        return null;
      }

      ldifLine = record.iterator.next();
      colonPos = ldifLine.indexOf(":");
      if (colonPos <= 0)
      {
        final Message message =
            ERR_LDIF_NO_ATTR_NAME.get(record.lineNumber, ldifLine
                .toString());
        throw new DecodeException(message);
      }

      attrName = toLowerCase(ldifLine.substring(0, colonPos));
    }

    if (!attrName.equals("dn"))
    {
      final Message message =
          ERR_LDIF_NO_DN.get(record.lineNumber, ldifLine.toString());
      throw new DecodeException(message);
    }

    // Look at the character immediately after the colon. If there is
    // none, then assume the null DN. If it is another colon, then the
    // DN must be base64-encoded. Otherwise, it may be one or more
    // spaces.
    final int length = ldifLine.length();
    if (colonPos == length - 1)
    {
      return DN.rootDN();
    }

    String dnString = null;

    if (ldifLine.charAt(colonPos + 1) == ':')
    {
      // The DN is base64-encoded. Find the first non-blank character
      // and take the rest of the line and base64-decode it.
      int pos = colonPos + 2;
      while (pos < length && ldifLine.charAt(pos) == ' ')
      {
        pos++;
      }

      final String base64DN = ldifLine.substring(pos);
      try
      {
        dnString = Base64.decode(base64DN).toString();
      }
      catch (final LocalizedIllegalArgumentException e)
      {
        // The value did not have a valid base64-encoding.
        final Message message =
            ERR_LDIF_COULD_NOT_BASE64_DECODE_DN.get(record.lineNumber,
                ldifLine, e.getMessageObject());
        throw new DecodeException(message);
      }
    }
    else
    {
      // The rest of the value should be the DN. Skip over any spaces
      // and attempt to decode the rest of the line as the DN.
      int pos = colonPos + 1;
      while (pos < length && ldifLine.charAt(pos) == ' ')
      {
        pos++;
      }

      dnString = ldifLine.substring(pos);
    }

    try
    {
      return DN.valueOf(dnString, options.getSchema());
    }
    catch (final LocalizedIllegalArgumentException e)
    {
      final Message message =
          ERR_LDIF_INVALID_DN.get(record.lineNumber, ldifLine, e
              .getMessageObject());
      throw new DecodeException(message);
    }
  }



  private void readLDIFRecordKeyValuePair(LDIFRecord record,
      KeyValuePair pair, boolean allowBase64) throws DecodeException
  {
    final String ldifLine = record.iterator.next();
    final int colonPos = ldifLine.indexOf(":");
    if (colonPos <= 0)
    {
      final Message message =
          ERR_LDIF_NO_ATTR_NAME.get(record.lineNumber, ldifLine);
      throw new DecodeException(message);
    }
    pair.key = ldifLine.substring(0, colonPos);

    // Look at the character immediately after the colon. If there is
    // none, then no value was specified. Throw an exception
    final int length = ldifLine.length();
    if (colonPos == length - 1)
    {
      // FIXME: improve error.
      final Message message =
          Message.raw("Malformed changetype attribute");
      throw new DecodeException(message);
    }

    if (allowBase64 && ldifLine.charAt(colonPos + 1) == ':')
    {
      // The value is base64-encoded. Find the first non-blank
      // character, take the rest of the line, and base64-decode it.
      int pos = colonPos + 2;
      while (pos < length && ldifLine.charAt(pos) == ' ')
      {
        pos++;
      }

      try
      {
        pair.value = Base64.decode(ldifLine.substring(pos)).toString();
      }
      catch (final LocalizedIllegalArgumentException e)
      {
        // The value did not have a valid base64-encoding.
        // FIXME: improve error.
        final Message message =
            Message.raw("Malformed base64 changetype attribute");
        throw new DecodeException(message);
      }
    }
    else
    {
      // The rest of the value should be the changetype. Skip over any
      // spaces and attempt to decode the rest of the line as the
      // changetype string.
      int pos = colonPos + 1;
      while (pos < length && ldifLine.charAt(pos) == ' ')
      {
        pos++;
      }

      pair.value = ldifLine.substring(pos);
    }
  }



  private String readLine() throws IOException
  {
    final String line = impl.readLine();
    if (line != null)
    {
      lineNumber++;
    }
    return line;
  }



  private void rejectLDIFRecord(LDIFRecord record, Message message)
  {
    // FIXME: not yet implemented.
  }
}
