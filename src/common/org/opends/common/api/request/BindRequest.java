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

package org.opends.common.api.request;



import org.opends.server.util.Validator;
import org.opends.common.api.Message;
import org.opends.common.api.DN;


/**
 * A raw bind request.
 */
public abstract class BindRequest extends Message implements Request
{
  // The bind DN.
  private String bindDN;

  /**
   * Creates a new raw bind request using the provided protocol version.
   * <p>
   * The new raw bind request will contain an empty list of controls.
   */
  protected BindRequest()
  {
    this.bindDN = "".intern();
  }
  
  /**
   * Creates a new raw bind request using the provided protocol version.
   * <p>
   * The new raw bind request will contain an empty list of controls.
   *
   * @param bindDN
   *          The raw, unprocessed bind DN for this bind request as
   *          contained in the client request.
   */
  protected BindRequest(String bindDN)
  {
    Validator.ensureNotNull(bindDN);
    this.bindDN = bindDN;
  }

  /**
   * Creates a new raw bind request using the provided protocol version.
   * <p>
   * The new raw bind request will contain an empty list of controls.
   *
   * @param bindDN
   *          The raw, unprocessed bind DN for this bind request as
   *          contained in the client request.
   */
  protected BindRequest(DN bindDN)
  {
    Validator.ensureNotNull(bindDN);
    this.bindDN = bindDN.toString();
  }


  /**
   * Returns the raw, unprocessed bind DN for this bind request as
   * contained in the client request.
   * <p>
   * The value may not actually contain a valid DN, as no validation
   * will have been performed.
   *
   * @return The raw, unprocessed bind DN for this bind request as
   *         contained in the client request.
   */
  public String getBindDN()
  {
    return bindDN;
  }



  /**
   * Sets the raw, unprocessed bind DN for this bind request.
   * <p>
   * This may or may not contain a valid DN.
   *
   * @param bindDN
   *          The raw, unprocessed bind DN for this bind request.
   * @return This raw bind request.
   */
  public BindRequest setBindDN(String bindDN)
  {
    Validator.ensureNotNull(bindDN);
    this.bindDN = bindDN;
    return this;
  }



  /**
   * Sets the raw, unprocessed bind DN for this bind request.
   * <p>
   * This may or may not contain a valid DN.
   *
   * @param bindDN
   *          The raw, unprocessed bind DN for this bind request.
   * @return This raw bind request.
   */
  public BindRequest setBindDN(DN bindDN)
  {
    Validator.ensureNotNull(bindDN);
    this.bindDN = bindDN.toString();
    return this;
  }
}
