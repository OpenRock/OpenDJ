package org.opends.ldap;



import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.responses.ExtendedResponse;
import org.opends.ldap.responses.IntermediateResponse;
import org.opends.messages.Message;
import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 25, 2009 Time: 2:36:05
 * PM To change this template use File | Settings | File Templates.
 */
public abstract class AbstractExtendedOperation implements
    ExtendedOperation
{

  public IntermediateResponse decodeIntermediateResponse(
      String responseName, ByteString responseValue)
      throws DecodeException
  {
    // TODO: I18n these
    throw new DecodeException(Message.raw("No intermediate "
        + "response decoding for this operation"));
  }



  public ExtendedRequest decodeRequest(String requestName,
      ByteString requestValue) throws DecodeException
  {
    // TODO: I18n these
    throw new DecodeException(Message.raw("No extended request "
        + "decoding for this operation"));
  }



  public ExtendedResponse decodeResponse(ResultCode resultCode,
      String matchedDN, String diagnosticMessage, String responseName,
      ByteString responseValue) throws DecodeException
  {
    // TODO: I18n these
    throw new DecodeException(Message.raw("No extended response "
        + "decoding for this operation"));
  }
}
