package org.opends.common.api.raw.response;

import org.opends.server.types.ByteString;
import org.opends.server.types.ResultCode;
import org.opends.server.util.Validator;

/**
 * Created by IntelliJ IDEA.
 * User: digitalperk
 * Date: May 26, 2009
 * Time: 9:40:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class RawExtendedResponse extends RawResultResponse
{
  private String responseName;
  private ByteString responseValue;

  public RawExtendedResponse(ResultCode resultCode,
                                   String matchedDN,
                                   String diagnosticMessage) {
    super(resultCode, matchedDN, diagnosticMessage);
    responseName = "";
    responseValue = ByteString.empty();
  }

  public String getResponseName() {
    return responseName;
  }

  public RawExtendedResponse setResponseName(
      String responseName)
  {
    Validator.ensureNotNull(responseName);
    this.responseName = responseName;
    return this;
  }

  public ByteString getResponseValue() {
    return responseValue;
  }

  public RawExtendedResponse setResponseValue(
      ByteString responseValue)
  {
    Validator.ensureNotNull(responseValue);
    this.responseValue = responseValue;
    return this;
  }
}
