package org.opends.common.api.extended;

import org.opends.server.types.ByteString;
import static org.opends.server.util.ServerConstants.OID_WHO_AM_I_REQUEST;
import org.opends.common.api.ResultCode;
import org.opends.common.api.DecodeException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jun 22, 2009
 * Time: 6:40:06 PM
 * To change this template use File | Settings | File Templates.
 */
public final class WhoAmIExtendedOperation
    extends AbstractExtendedOperation
{
private static final WhoAmIExtendedOperation SINGLETON =
      new WhoAmIExtendedOperation();

  private WhoAmIExtendedOperation() {
    super();
    // We could register the result codes here if they are not
    // already included in the default set.
  }

  public static class WhoAmIExtendedRequest extends
      ExtendedRequest<WhoAmIExtendedOperation>
  {
    public WhoAmIExtendedRequest()
    {
      super(OID_WHO_AM_I_REQUEST);
    }

    public WhoAmIExtendedOperation getExtendedOperation() {
      return SINGLETON;
    }

    public ByteString getRequestValue() {
      return null;
    }

    public void toString(StringBuilder buffer) {
      buffer.append("WhoAmIExtendedRequest(requestName=");
      buffer.append(requestName);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }

  public static class WhoAmIExtendedResponse extends
      ExtendedResponse<WhoAmIExtendedOperation>
  {
    private String authzId;

    public WhoAmIExtendedResponse(ResultCode resultCode,
                                  String matchedDN,
                                  String diagnosticMessage)
    {
      super(resultCode, matchedDN, diagnosticMessage);
    }

    /**
     * Get the authzId to return or <code>null</code> if it is not
     * available.
     *
     * @return The authzID or <code>null</code>.
     */
    public String getAuthzId() {
      return authzId;
    }

    public WhoAmIExtendedResponse setAuthzId(String authzId) {
      this.authzId = authzId;
      return this;
    }

    public WhoAmIExtendedOperation getExtendedOperation() {
      return SINGLETON;
    }

    public ByteString getResponseValue() {
      if(authzId != null)
      {
        ByteString.valueOf(authzId);
      }
      return null;
    }

    public void toString(StringBuilder buffer) {
      buffer.append("WhoAmIExtendedResponse(resultCode=");
      buffer.append(resultCode);
      buffer.append(", matchedDN=");
      buffer.append(matchedDN);
      buffer.append(", diagnosticMessage=");
      buffer.append(diagnosticMessage);
      buffer.append(", referrals=");
      buffer.append(referrals);
      buffer.append(", authzId=");
      buffer.append(authzId);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }

  @Override
  public WhoAmIExtendedRequest decodeRequest(String requestName,
                                             ByteString requestValue)
      throws DecodeException
  {
    return new WhoAmIExtendedRequest();
  }

  @Override
  public WhoAmIExtendedResponse decodeResponse(
      ResultCode resultCode, String matchedDN,
      String diagnosticMessage, String responseName,
      ByteString responseValue)
      throws DecodeException
  {
    // TODO: Should we check oid is null?
    String authzId = null;
    if(responseValue != null)
    {
      authzId = responseValue.toString();
    }
    return new WhoAmIExtendedResponse(resultCode, matchedDN,
        diagnosticMessage).setAuthzId(authzId);
  }
}
