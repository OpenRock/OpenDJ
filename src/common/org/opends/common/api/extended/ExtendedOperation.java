package org.opends.common.api.extended;

import org.opends.server.types.ByteString;
import org.opends.common.api.DecodeException;
import org.opends.common.api.raw.ResultCode;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 19, 2009 Time: 8:39:52
 * PM To change this template use File | Settings | File Templates.
 */
public interface ExtendedOperation
{
  public ExtendedRequest decodeRequest(String requestName,
                                       ByteString requestValue)
      throws DecodeException;

  public IntermediateResponse decodeIntermediateResponse(
      String responseName, ByteString responseValue)
      throws DecodeException;

  public ExtendedResponse decodeResponse(ResultCode resultCode,
                                         String matchedDN,
                                         String diagnosticMessage,
                                         String responseName,
                                         ByteString responseValue)
    throws DecodeException;

}
