package org.opends.common.api.raw.response;

import org.opends.server.types.ResultCode;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 26, 2009 Time: 10:13:02
 * AM To change this template use File | Settings | File Templates.
 */
public class RawDeleteResponse extends RawResultResponse
{
  public RawDeleteResponse(int resultCode, String matchedDN,
                           String diagnosticMessage)
  {
    super(resultCode, matchedDN, diagnosticMessage);
  }
}
