package org.opends.common.api.response;

import org.opends.common.api.DN;
import org.opends.common.api.ResultCode;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 26, 2009 Time: 10:12:23
 * AM To change this template use File | Settings | File Templates.
 */
public final class RawCompareResponse extends RawResultResponse
{
  public RawCompareResponse(ResultCode resultCode, String matchedDN,
                            String diagnosticMessage)
  {
    super(resultCode, matchedDN, diagnosticMessage);
  }

  public RawCompareResponse(ResultCode resultCode, DN matchedDN,
                            String diagnosticMessage)
  {
    super(resultCode, matchedDN.toString(), diagnosticMessage);
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("CompareResponse(resultCode=");
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
