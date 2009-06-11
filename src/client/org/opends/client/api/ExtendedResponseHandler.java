package org.opends.client.api;

import org.opends.common.api.raw.response.RawExtendedResponse;
import org.opends.common.api.raw.response.RawIntermediateResponse;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 11, 2009 Time: 11:34:28
 * AM To change this template use File | Settings | File Templates.
 */
public interface ExtendedResponseHandler
    extends ResponseHandler<RawExtendedResponse>
{
  public void handleIntermediateResponse(RawIntermediateResponse intermediateResponse);
}
