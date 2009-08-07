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

package org.opends.ldap.requests;



import org.opends.server.types.ByteString;
import org.opends.spi.AbstractMessage;
import org.opends.util.Validator;



/**
 * Compare request implementation.
 */
final class CompareRequestImpl extends AbstractMessage<CompareRequest>
    implements CompareRequest
{
  private String attributeDescription;
  private ByteString ava;
  private String dn;



  /**
   * Creates a new compare request using the provided DN, attribute
   * name, and assertion.
   *
   * @param dn
   *          The DN of the entry to be compared.
   * @param attributeDescription
   *          The name of the attribute to be compared.
   * @param ava
   *          The attribute value assertion to be compared.
   * @throws NullPointerException
   *           If {@code dn}, {@code AttributeDescription}, or {@code
   *           ava} was {@code null}.
   */
  CompareRequestImpl(String dn, String attributeDescription,
      ByteString ava) throws NullPointerException
  {
    Validator.ensureNotNull(dn, attributeDescription, ava);

    this.dn = dn;
    this.attributeDescription = attributeDescription;
    this.ava = ava;
  }



  /**
   * {@inheritDoc}
   */
  public ByteString getAssertionValue()
  {
    return ava;
  }



  /**
   * {@inheritDoc}
   */
  public String getAssertionValueAsString()
  {
    return ava.toString();
  }



  /**
   * {@inheritDoc}
   */
  public String getAttributeDescription()
  {
    return attributeDescription;
  }



  /**
   * {@inheritDoc}
   */
  public String getDN()
  {
    return dn;
  }



  /**
   * {@inheritDoc}
   */
  public CompareRequest setAssertionValue(ByteString ava)
      throws NullPointerException
  {
    Validator.ensureNotNull(ava);

    this.ava = ava;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public CompareRequest setAssertionValue(String ava)
      throws NullPointerException
  {
    Validator.ensureNotNull(ava);

    this.ava = ByteString.valueOf(ava);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public CompareRequest setAttributeDescription(
      String attributeDescription) throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescription);

    this.attributeDescription = attributeDescription;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public CompareRequest setDN(String dn) throws NullPointerException
  {
    Validator.ensureNotNull(dn);

    this.dn = dn;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public StringBuilder toString(StringBuilder builder)
      throws NullPointerException
  {
    builder.append("CompareRequest(entry=");
    builder.append(dn);
    builder.append(", attributeDesc=");
    builder.append(attributeDescription);
    builder.append(", assertionValue=");
    builder.append(ava);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder;
  }
}
