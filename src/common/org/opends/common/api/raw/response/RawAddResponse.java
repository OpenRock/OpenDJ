package org.opends.common.api.raw.response;

import org.opends.server.types.ResultCode;
import org.opends.common.api.DN;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 26, 2009 Time: 10:11:02
 * AM To change this template use File | Settings | File Templates.
 */
public final class RawAddResponse extends RawResultResponse
{
  public RawAddResponse(ResultCode resultCode, String matchedDN,
                        String diagnosticMessage)
  {
    super(resultCode, matchedDN, diagnosticMessage);
  }

  public RawAddResponse(ResultCode resultCode, DN matchedDN,
                        String diagnosticMessage)
  {
    super(resultCode, matchedDN.toString(), diagnosticMessage);
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("AddResponse(resultCode=");
    buffer.append(resultCode);
    buffer.append(", matchedDN=");
    buffer.append(matchedDN);
    buffer.append(", diagnosticMessage=");
    buffer.append(diagnosticMessage);
    buffer.append(", referrals=");
    buffer.append(referrals);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
