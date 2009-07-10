package org.opends.ldap;



import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.requests.GenericExtendedRequest;
import org.opends.ldap.responses.ExtendedResponse;
import org.opends.ldap.responses.GenericExtendedResponse;
import org.opends.ldap.responses.GenericIntermediateResponse;
import org.opends.ldap.responses.IntermediateResponse;
import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 19, 2009 Time:
 * 8:38:43 PM To change this template use File | Settings | File
 * Templates.
 */
public final class GenericExtendedOperation extends
    AbstractExtendedOperation
{
  private static final GenericExtendedOperation SINGLETON =
      new GenericExtendedOperation();



  public static GenericExtendedOperation getInstance()
  {
    return SINGLETON;
  }



  protected GenericExtendedOperation()
  {
  }



  @Override
  public IntermediateResponse decodeIntermediateResponse(
      String responseName, ByteString responseValue)
      throws DecodeException
  {
    return new GenericIntermediateResponse().setResponseName(
        responseName).setResponseValue(responseValue);
  }



  @Override
  public ExtendedRequest decodeRequest(String requestName,
      ByteString requestValue) throws DecodeException
  {
    return new GenericExtendedRequest(requestName)
        .setRequestValue(requestValue);
  }



  @Override
  public ExtendedResponse decodeResponse(ResultCode resultCode,
      String matchedDN, String diagnosticMessage, String responseName,
      ByteString responseValue) throws DecodeException
  {
    return new GenericExtendedResponse(resultCode, matchedDN,
        diagnosticMessage).setResponseName(responseName)
        .setResponseValue(responseValue);
  }
}
