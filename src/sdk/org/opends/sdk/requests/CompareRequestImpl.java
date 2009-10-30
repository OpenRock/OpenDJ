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

package org.opends.sdk.requests;



import org.opends.sdk.util.Validator;
import org.opends.sdk.util.ByteString;



/**
 * Compare request implementation.
 */
final class CompareRequestImpl extends AbstractMessage<CompareRequest>
    implements CompareRequest
{
  private String attributeDescription;
  private ByteString assertionValue;
  private String name;



  /**
   * Creates a new compare request using the provided distinguished
   * name, attribute name, and assertion value.
   *
   * @param name
   *          The distinguished name of the entry to be compared.
   * @param attributeDescription
   *          The name of the attribute to be compared.
   * @param assertionValue
   *          The attribute value assertion to be compared.
   * @throws NullPointerException
   *           If {@code name}, {@code AttributeDescription}, or {@code
   *           assertionValue} was {@code null}.
   */
  CompareRequestImpl(String name, String attributeDescription,
      ByteString assertionValue) throws NullPointerException
  {
    Validator.ensureNotNull(name, attributeDescription, assertionValue);

    this.name = name;
    this.attributeDescription = attributeDescription;
    this.assertionValue = assertionValue;
  }



  /**
   * {@inheritDoc}
   */
  public ByteString getAssertionValue()
  {
    return assertionValue;
  }



  /**
   * {@inheritDoc}
   */
  public String getAssertionValueAsString()
  {
    return assertionValue.toString();
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
  public String getName()
  {
    return name;
  }



  /**
   * {@inheritDoc}
   */
  public CompareRequest setAssertionValue(ByteString ava)
      throws NullPointerException
  {
    Validator.ensureNotNull(ava);

    this.assertionValue = ava;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public CompareRequest setAssertionValue(Object ava)
      throws NullPointerException
  {
    Validator.ensureNotNull(ava);

    this.assertionValue = ByteString.valueOf(ava);
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
  public CompareRequest setName(String dn) throws NullPointerException
  {
    Validator.ensureNotNull(dn);

    this.name = dn;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("CompareRequest(name=");
    builder.append(name);
    builder.append(", attributeDescription=");
    builder.append(attributeDescription);
    builder.append(", assertionValue=");
    builder.append(assertionValue);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder.toString();
  }
}
