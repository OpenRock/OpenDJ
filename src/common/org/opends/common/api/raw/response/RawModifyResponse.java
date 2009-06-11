package org.opends.common.api.raw.response;

import org.opends.server.types.ResultCode;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 26, 2009 Time: 10:11:50
 * AM To change this template use File | Settings | File Templates.
 */
public final class RawModifyResponse extends RawResultResponse
{
  public RawModifyResponse(int resultCode, String matchedDN,
                           String diagnosticMessage)
  {
    super(resultCode, matchedDN, diagnosticMessage);
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("ModifyResponse(resultCode=");
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
