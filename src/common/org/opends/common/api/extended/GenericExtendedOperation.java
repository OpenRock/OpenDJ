package org.opends.common.api.extended;

import org.opends.server.types.ByteString;
import org.opends.common.api.DecodeException;
import org.opends.common.api.raw.ResultCode;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 19, 2009 Time: 8:38:43
 * PM To change this template use File | Settings | File Templates.
 */
public class GenericExtendedOperation implements ExtendedOperation
{
  private static final GenericExtendedOperation SINGLETON =
      new GenericExtendedOperation();

  protected GenericExtendedOperation() {
  }

  public ExtendedRequest decodeRequest(String requestName,
                                       ByteString requestValue)
      throws DecodeException
  {
    return new GenericExtendedRequest(requestName).
        setRequestValue(requestValue);
  }

  public IntermediateResponse decodeIntermediateResponse(
      String responseName, ByteString responseValue)
      throws DecodeException
  {
    return new GenericIntermediateResponse().
        setResponseName(responseName).setResponseValue(responseValue);
  }

  public ExtendedResponse decodeResponse(ResultCode resultCode,
                                         String matchedDN,
                                         String diagnosticMessage,
                                         String responseName,
                                         ByteString responseValue)
      throws DecodeException
  {
    return new GenericExtendedResponse(resultCode, matchedDN,
        diagnosticMessage, responseValue).
        setResponseName(responseName);
  }

  public static GenericExtendedOperation getInstance()
  {
    return SINGLETON;
  }
}
