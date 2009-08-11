package org.opends.spi;



import org.opends.ldap.DecodeException;
import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.responses.Result;
import org.opends.server.types.ByteString;
import org.opends.types.ResultCode;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 19, 2009 Time:
 * 8:39:52 PM To change this template use File | Settings | File
 * Templates.
 */
public interface ExtendedOperation<R extends ExtendedRequest<S>, S extends Result>
{
  R decodeRequest(String requestName, ByteString requestValue)
      throws DecodeException;



  S decodeResponse(ResultCode resultCode, String matchedDN,
      String diagnosticMessage);



  S decodeResponse(ResultCode resultCode, String matchedDN,
      String diagnosticMessage, String responseName,
      ByteString responseValue) throws DecodeException;

}
