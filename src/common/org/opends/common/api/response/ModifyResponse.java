package org.opends.common.api.response;

import org.opends.common.api.DN;
import org.opends.common.api.ResultCode;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 26, 2009 Time: 10:11:50
 * AM To change this template use File | Settings | File Templates.
 */
public final class ModifyResponse extends ResultResponse
{
  public ModifyResponse(ResultCode resultCode, String matchedDN,
                           String diagnosticMessage)
  {
    super(resultCode, matchedDN, diagnosticMessage);
  }

  public ModifyResponse(ResultCode resultCode, DN matchedDN,
                           String diagnosticMessage)
  {
    super(resultCode, matchedDN.toString(), diagnosticMessage);
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
