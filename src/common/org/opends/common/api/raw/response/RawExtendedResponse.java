package org.opends.common.api.raw.response;

import org.opends.server.types.ByteString;
import org.opends.server.types.ResultCode;
import org.opends.server.util.Validator;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 26, 2009 Time: 9:40:28
 * AM To change this template use File | Settings | File Templates.
 */
public final class RawExtendedResponse extends RawResultResponse
{
  private String responseName;
  private ByteString responseValue;

  public RawExtendedResponse(int resultCode, String matchedDN,
                             String diagnosticMessage)
  {
    super(resultCode, matchedDN, diagnosticMessage);
    responseName = "".intern();
    responseValue = ByteString.empty();
  }

  public String getResponseName()
  {
    return responseName;
  }

  public RawExtendedResponse setResponseName(
      String responseName)
  {
    Validator.ensureNotNull(responseName);
    this.responseName = responseName;
    return this;
  }

  public ByteString getResponseValue()
  {
    return responseValue;
  }

  public RawExtendedResponse setResponseValue(
      ByteString responseValue)
  {
    Validator.ensureNotNull(responseValue);
    this.responseValue = responseValue;
    return this;
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("ExtendedResponse(resultCode=");
    buffer.append(resultCode);
    buffer.append(", matchedDN=");
    buffer.append(matchedDN);
    buffer.append(", diagnosticMessage=");
    buffer.append(diagnosticMessage);
    buffer.append(", referrals=");
    buffer.append(referrals);
    buffer.append(", responseName=");
    buffer.append(responseName);
    buffer.append(", responseValue=");
    buffer.append(responseValue);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
