package org.opends.client.api;

import org.opends.common.api.extended.ExtendedResponse;
import org.opends.common.api.extended.ExtendedOperation;
import org.opends.common.api.extended.IntermediateResponse;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 11, 2009 Time: 11:34:28
 * AM To change this template use File | Settings | File Templates.
 */
public interface ExtendedResponseHandler
    extends ResponseHandler<ExtendedResponse>
{
  public void handleIntermediateResponse(
      IntermediateResponse intermediateResponse);
}
