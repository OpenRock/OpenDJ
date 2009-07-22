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

package org.opends.ldap.extensions;



import static org.opends.server.util.ServerConstants.*;

import java.io.IOException;

import org.opends.asn1.ASN1;
import org.opends.asn1.ASN1Writer;
import org.opends.ldap.ResultCode;
import org.opends.ldap.responses.ExtendedResult;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;



public class GetConnectionIDResult extends
    ExtendedResult<GetConnectionIDResult>
{
  private int connectionID;



  public GetConnectionIDResult(ResultCode resultCode, String matchedDN,
      String diagnosticMessage, int connectionID)
  {
    super(resultCode, matchedDN, diagnosticMessage,
        OID_GET_CONNECTION_ID_EXTOP);
    this.connectionID = connectionID;
  }



  public int getConnectionID()
  {
    return connectionID;
  }



  public ByteString getResponseValue()
  {
    ByteStringBuilder buffer = new ByteStringBuilder(6);
    ASN1Writer writer = ASN1.getWriter(buffer);

    try
    {
      writer.writeInteger(connectionID);
    }
    catch (IOException ioe)
    {
      // This should never happen unless there is a bug somewhere.
      throw new RuntimeException(ioe);
    }

    return buffer.toByteString();
  }



  public GetConnectionIDResult setConnectionID(int connectionID)
  {
    this.connectionID = connectionID;
    return this;
  }



  public void toString(StringBuilder buffer)
  {
    buffer.append("GetConnectionIDExtendedResponse(resultCode=");
    buffer.append(getResultCode());
    buffer.append(", matchedDN=");
    buffer.append(getMatchedDN());
    buffer.append(", diagnosticMessage=");
    buffer.append(getDiagnosticMessage());
    buffer.append(", referrals=");
    buffer.append(getReferrals());
    buffer.append(", responseName=");
    buffer.append(getResponseName());
    buffer.append(", connectionID=");
    buffer.append(connectionID);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}