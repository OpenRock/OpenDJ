package org.opends.common.api.raw.response;

import org.opends.server.types.ByteString;
import org.opends.server.types.ResultCode;
import org.opends.server.util.Validator;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 25, 2009 Time: 6:51:21
 * PM To change this template use File | Settings | File Templates.
 */
public class RawBindResponse extends RawResultResponse
{
  private ByteString serverSASLCreds;

  public RawBindResponse(ResultCode resultCode,
                         String matchedDN,
                         String diagnosticMessage)
  {
    super(resultCode, matchedDN, diagnosticMessage);
    serverSASLCreds = ByteString.empty();
  }

  public ByteString getServerSASLCreds()
  {
    return serverSASLCreds;
  }

  public RawBindResponse setServerSASLCreds(
      ByteString serverSASLCreds)
  {
    Validator.ensureNotNull(serverSASLCreds);
    this.serverSASLCreds = serverSASLCreds;
    return this;
  }
}
