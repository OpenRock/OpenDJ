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
import java.util.ArrayList;
import java.util.List;

import org.opends.messages.Message;
import org.opends.sdk.AttributeDescription;
import org.opends.sdk.DN;
import org.opends.sdk.DecodeException;
import org.opends.sdk.Entry;
import org.opends.sdk.ModificationType;
import org.opends.sdk.RDN;
import org.opends.sdk.SortedEntry;
import org.opends.sdk.requests.ModifyDNRequest;
import org.opends.sdk.requests.ModifyRequest;
import org.opends.sdk.requests.Requests;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.util.LocalizedIllegalArgumentException;
import org.opends.sdk.util.Validator;
import org.opends.server.types.ByteString;



/**
 * An LDIF change record reader reads change records using the LDAP Data
 * Interchange Format (LDIF) from a user defined source.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2849">RFC 2849 - The LDAP
 *      Data Interchange Format (LDIF) - Technical Specification </a>
 */
public final class LDIFChangeRecordReader extends AbstractLDIFReader
    implements ChangeRecordReader
{

  /**
   * Creates a new LDIF entry reader whose source is the provided input
   * stream.
   *
   * @param in
   *          The input stream to use.
   */
  public LDIFChangeRecordReader(InputStream in)
  {
    super(in);
  }



  /**
   * Creates a new LDIF entry reader which will read lines of LDIF from
   * the provided list.
   *
   * @param ldifLines
   *          The list from which lines of LDIF should be read.
   */
  public LDIFChangeRecordReader(List<String> ldifLines)
  {
    super(ldifLines);
  }



  /**
   * {@inheritDoc}
   */
  public void close() throws IOException
  {
    close0();
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
   * Sets the schema which should be used for decoding entries and
   * change records. The default schema is used if no other is
   * specified.
   *
   * @param schema
   *          The schema which should be used for decoding entries and
   *          change records.
   * @return A reference to this {@code LDIFChangeRecordReader}.
   */
  public LDIFChangeRecordReader setSchema(Schema schema)
  {
    Validator.ensureNotNull(schema);
    this.schema = schema;
    return this;
  }



  /**
   * Specifies whether or not schema validation should be performed for
   * entries and change records. The default is {@code true}.
   *
   * @param validateSchema
   *          {@code true} if schema validation should be performed for
   *          entries and change records, or {@code false} otherwise.
   * @return A reference to this {@code LDIFChangeRecordReader}.
   */
  public LDIFChangeRecordReader setValidateSchema(boolean validateSchema)
  {
    this.validateSchema = validateSchema;
    return this;
  }



  private ChangeRecord parseAddChangeRecordEntry(DN entryDN,
      LDIFRecord record) throws DecodeException
  {
    // Use an Entry for the AttributeSequence.
    final Entry entry = new SortedEntry(schema).setNameDN(entryDN);

    while (record.iterator.hasNext())
    {
      readLDIFRecordAttributeValue(record, entry);
    }

    // TODO: skip entry if excluded based on filtering.
    return Requests.asAddRequest(entry);
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
            AttributeDescription.valueOf(pair.value, schema);
      }
      catch (final LocalizedIllegalArgumentException e)
      {
        throw new DecodeException(e.getMessageObject());
      }

      // Ensure that the binary option is present if required.
      if (!attributeDescription.getAttributeType().getSyntax()
          .isBEREncodingRequired())
      {
        if (validateSchema
            && attributeDescription.containsOption("binary"))
        {
          Message message =
              ERR_LDIF_INVALID_ATTR_OPTION.get(entryDN.toString(),
                  record.lineNumber, pair.value);
          throw new DecodeException(message);
        }
      }
      else
      {
        attributeDescription =
            AttributeDescription.create(attributeDescription, "binary");
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
              AttributeDescription.valueOf(attrDescr, schema);
        }
        catch (final LocalizedIllegalArgumentException e)
        {
          throw new DecodeException(e.getMessageObject());
        }

        // Ensure that the binary option is present if required.
        if (attributeDescription.getAttributeType().getSyntax()
            .isBEREncodingRequired())
        {
          attributeDescription2 =
              AttributeDescription.create(attributeDescription2,
                  "binary");
        }

        if (!attributeDescription2.equals(attributeDescription))
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
      final RDN newRDN = RDN.valueOf(pair.value, schema);
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
        final DN newSuperiorDN = DN.valueOf(pair.value, schema);
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

}
