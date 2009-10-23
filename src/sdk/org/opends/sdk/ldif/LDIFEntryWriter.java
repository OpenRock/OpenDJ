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
import java.io.OutputStream;
import java.util.List;

import org.opends.sdk.AttributeSequence;
import org.opends.sdk.AttributeValueSequence;
import org.opends.sdk.util.Validator;
import org.opends.server.types.ByteString;



/**
 * An LDIF entry writer writes attribute value records (entries) using
 * the LDAP Data Interchange Format (LDIF) to a user defined
 * destination.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2849">RFC 2849 - The LDAP
 *      Data Interchange Format (LDIF) - Technical Specification </a>
 */
public final class LDIFEntryWriter extends AbstractLDIFWriter implements
    EntryWriter
{

  /**
   * Creates a new LDIF entry writer which will append lines of LDIF to
   * the provided list.
   *
   * @param ldifLines
   *          The list to which lines of LDIF should be appended.
   */
  public LDIFEntryWriter(List<String> ldifLines)
  {
    super(ldifLines);
  }



  /**
   * Creates a new LDIF entry writer whose destination is the provided
   * output stream.
   *
   * @param out
   *          The output stream to use.
   */
  public LDIFEntryWriter(OutputStream out)
  {
    super(out);
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
  public void flush() throws IOException
  {
    flush0();
  }



  /**
   * Specifies whether or not user-friendly comments should be added
   * whenever distinguished names or UTF-8 attribute values are
   * encountered which contained non-ASCII characters. The default is
   * {@code false}.
   *
   * @param addUserFriendlyComments
   *          {@code true} if user-friendly comments should be added, or
   *          {@code false} otherwise.
   * @return A reference to this {@code LDIFEntryWriter}.
   */
  public LDIFEntryWriter setAddUserFriendlyComments(
      boolean addUserFriendlyComments)
  {
    this.addUserFriendlyComments = addUserFriendlyComments;
    return this;
  }



  /**
   * Specifies the column at which long lines should be wrapped. A value
   * less than or equal to zero (the default) indicates that no wrapping
   * should be performed.
   *
   * @param wrapColumn
   *          The column at which long lines should be wrapped.
   * @return A reference to this {@code LDIFEntryWriter}.
   */
  public LDIFEntryWriter setWrapColumn(int wrapColumn)
  {
    this.wrapColumn = wrapColumn;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public LDIFEntryWriter writeComment(CharSequence comment)
      throws IOException, NullPointerException
  {
    writeComment0(comment);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public LDIFEntryWriter writeEntry(AttributeSequence entry)
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

}
