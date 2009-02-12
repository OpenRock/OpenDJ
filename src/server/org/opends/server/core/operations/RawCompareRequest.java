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

package org.opends.server.core.operations;



import org.opends.server.types.ByteString;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.OperationType;
import org.opends.server.util.Validator;



/**
 * A raw compare request.
 */
public final class RawCompareRequest extends RawRequest
{
  // The DN of the entry to be compared.
  private ByteString dn;

  // The attribute description.
  private String attributeDescription;

  // The assertion value.
  private ByteString assertionValue;



  /**
   * Creates a new raw compare request using the provided entry DN and
   * attribute value assertion.
   * <p>
   * The new raw add request will contain an empty list of controls.
   *
   * @param dn
   *          The raw, unprocessed entry DN for this compare request.
   * @param attributeDescription
   *          The raw, unprocessed attribute description for this
   *          compare request.
   * @param assertionValue
   *          The raw, unprocessed assertion value for this compare
   *          request.
   */
  public RawCompareRequest(ByteString dn, String attributeDescription,
      ByteString assertionValue)
  {
    super(OperationType.COMPARE);
    Validator.ensureNotNull(dn, attributeDescription, assertionValue);
    this.dn = dn;
    this.attributeDescription = attributeDescription;
    this.assertionValue = assertionValue;
  }



  /**
   * Returns the raw, unprocessed entry DN as included in the request
   * from the client.
   * <p>
   * This may or may not contain a valid DN, as no validation will have
   * been performed.
   *
   * @return The raw, unprocessed entry DN as included in the request
   *         from the client.
   */
  public ByteString getDN()
  {
    return dn;
  }



  /**
   * Sets the raw, unprocessed entry DN for this compare request.
   * <p>
   * This may or may not contain a valid DN.
   *
   * @param dn
   *          The raw, unprocessed entry DN for this compare request.
   * @return This raw compare request.
   */
  public RawCompareRequest setDN(ByteString dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn;
    return this;
  }



  /**
   * Returns the raw, unprocessed attribute description as included in
   * the request from the client.
   * <p>
   * This may or may not contain a valid attribute description, as no
   * validation will have been performed.
   *
   * @return The raw, unprocessed attribute description as included in
   *         the request from the client.
   */
  public String getAttributeDescription()
  {
    return attributeDescription;
  }



  /**
   * Sets the raw, unprocessed attribute description for this compare
   * request.
   * <p>
   * This may or may not contain a valid attribute description.
   *
   * @param attributeDescription
   *          The raw, unprocessed attribute description for this
   *          compare request.
   * @return This raw compare request.
   */
  public RawCompareRequest setAttributeDescription(
      String attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);
    this.attributeDescription = attributeDescription;
    return this;
  }



  /**
   * Returns the raw, unprocessed assertion value as included in the
   * request from the client.
   * <p>
   * This may or may not contain a valid assertion value, as no
   * validation will have been performed.
   *
   * @return The raw, unprocessed assertion value as included in the
   *         request from the client.
   */
  public ByteString getAssertionValue()
  {
    return assertionValue;
  }



  /**
   * Sets the raw, unprocessed assertion value for this compare request.
   * <p>
   * This may or may not contain a valid assertion value.
   *
   * @param assertionValue
   *          The raw, unprocessed assertion value for this compare
   *          request.
   * @return This raw compare request.
   */
  public RawCompareRequest setAssertionValue(ByteString assertionValue)
  {
    Validator.ensureNotNull(assertionValue);
    this.assertionValue = assertionValue;
    return this;
  }



  /**
   * Returns a decoded compare request representing this raw compare
   * request. Subsequent changes to this raw compare request will not be
   * reflected in the returned compare request.
   *
   * @return A decoded compare request representing this raw compare
   *         request.
   * @throws DirectoryException
   *           If this raw compare request could not be decoded.
   */
  @Override
  public CompareRequest toRequest() throws DirectoryException
  {
    // TODO: not yet implemented.
    return null;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("CompareRequest(entry=");
    buffer.append(dn);
    buffer.append(", attributeDesc=");
    buffer.append(attributeDescription);
    buffer.append(", assertionValue=");
    buffer.append(assertionValue);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
