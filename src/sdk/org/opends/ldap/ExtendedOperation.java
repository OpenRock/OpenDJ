package org.opends.ldap;



import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.responses.ExtendedResult;
import org.opends.ldap.responses.IntermediateResponse;
import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 19, 2009 Time:
 * 8:39:52 PM To change this template use File | Settings | File
 * Templates.
 */
public interface ExtendedOperation
{
  public IntermediateResponse decodeIntermediateResponse(
      String responseName, ByteString responseValue)
      throws DecodeException;



  public ExtendedRequest decodeRequest(String requestName,
      ByteString requestValue) throws DecodeException;



  public ExtendedResult decodeResponse(ResultCode resultCode,
      String matchedDN, String diagnosticMessage, String responseName,
      ByteString responseValue) throws DecodeException;

}
