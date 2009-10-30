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
 * Simple bind request implementation.
 */
final class SimpleBindRequestImpl extends
    AbstractBindRequest<SimpleBindRequest> implements SimpleBindRequest
{
  private ByteString password = ByteString.empty();



  /**
   * Creates a new simple bind request.
   */
  SimpleBindRequestImpl()
  {
    // No implementation required.
  }



  /**
   * {@inheritDoc}
   */
  public ByteString getPassword()
  {
    return password;
  }



  /**
   * {@inheritDoc}
   */
  public String getPasswordAsString()
  {
    return password.toString();
  }



  /**
   * {@inheritDoc}
   */
  public SimpleBindRequest setPassword(ByteString password)
      throws NullPointerException
  {
    Validator.ensureNotNull(password);

    this.password = password;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public SimpleBindRequest setPassword(String password)
      throws NullPointerException
  {
    Validator.ensureNotNull(password);

    this.password = ByteString.valueOf(password);
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("SimpleBindRequest(name=");
    builder.append(getName());
    builder.append(", authentication=simple");
    builder.append(", password=");
    builder.append(password);
    builder.append(", controls=");
    builder.append(getControls());
    builder.append(")");
    return builder.toString();
  }
}
