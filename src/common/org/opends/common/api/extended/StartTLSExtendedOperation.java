package org.opends.common.api.extended;

import org.opends.server.types.ByteString;
import static org.opends.server.util.ServerConstants.OID_START_TLS_REQUEST;
import org.opends.common.api.ResultCode;
import org.opends.common.api.DecodeException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jun 22, 2009
 * Time: 6:21:44 PM
 * To change this template use File | Settings | File Templates.
 */
public final class StartTLSExtendedOperation
    extends AbstractExtendedOperation
{
  private static final StartTLSExtendedOperation SINGLETON =
      new StartTLSExtendedOperation();

  private StartTLSExtendedOperation() {
    super();
    // We could register the result codes here if they are not
    // already included in the default set.
  }

  public static class StartTLSExtendedRequest extends
      ExtendedRequest<StartTLSExtendedOperation>
  {
    public StartTLSExtendedRequest()
    {
      super(OID_START_TLS_REQUEST);
    }

    public StartTLSExtendedOperation getExtendedOperation() {
      return SINGLETON;
    }

    public ByteString getRequestValue() {
      return null;
    }

    public void toString(StringBuilder buffer) {
      buffer.append("StartTLSExtendedRequest(requestName=");
      buffer.append(requestName);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }

  public static class StartTLSExtendedResponse extends
      ExtendedResponse<StartTLSExtendedOperation>
  {
    public StartTLSExtendedResponse(ResultCode resultCode,
                                  String matchedDN,
                                  String diagnosticMessage)
    {
      super(resultCode, matchedDN, diagnosticMessage);
      responseName = OID_START_TLS_REQUEST;
    }

    public StartTLSExtendedOperation getExtendedOperation() {
      return SINGLETON;
    }

    public ByteString getResponseValue() {
      return null;
    }

    public void toString(StringBuilder buffer) {
      buffer.append("StartTLSExtendedResponse(resultCode=");
      buffer.append(resultCode);
      buffer.append(", matchedDN=");
      buffer.append(matchedDN);
      buffer.append(", diagnosticMessage=");
      buffer.append(diagnosticMessage);
      buffer.append(", referrals=");
      buffer.append(referrals);
      buffer.append(", responseName=");
      buffer.append(responseName);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }



  @Override
  public StartTLSExtendedRequest decodeRequest(String requestName,
                                               ByteString requestValue)
      throws DecodeException
  {
    return new StartTLSExtendedRequest();
  }

  @Override
  public StartTLSExtendedResponse decodeResponse(
      ResultCode resultCode, String matchedDN,
      String diagnosticMessage, String responseName,
      ByteString responseValue)
      throws DecodeException
  {
    // TODO: Should we check oid is NOT null and matches but
    // value is null?
    return new StartTLSExtendedResponse(resultCode, matchedDN,
        diagnosticMessage);
  }
}
