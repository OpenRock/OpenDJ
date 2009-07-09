package org.opends.ldap.extensions;



import static org.opends.server.util.ServerConstants.*;

import org.opends.ldap.AbstractExtendedOperation;
import org.opends.ldap.DecodeException;
import org.opends.ldap.ExtendedRequest;
import org.opends.ldap.ExtendedResponse;
import org.opends.ldap.ResultCode;
import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 22, 2009 Time: 6:40:06
 * PM To change this template use File | Settings | File Templates.
 */
public final class WhoAmIExtendedOperation extends
    AbstractExtendedOperation
{
  public static class Request extends
      ExtendedRequest<WhoAmIExtendedOperation>
  {
    public Request()
    {
      super(OID_WHO_AM_I_REQUEST);
    }



    @Override
    public WhoAmIExtendedOperation getExtendedOperation()
    {
      return SINGLETON;
    }



    @Override
    public ByteString getRequestValue()
    {
      return null;
    }



    @Override
    public void toString(StringBuilder buffer)
    {
      buffer.append("WhoAmIExtendedRequest(requestName=");
      buffer.append(requestName);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }

  public static class Response extends
      ExtendedResponse<WhoAmIExtendedOperation>
  {
    private String authzId;



    public Response(ResultCode resultCode, String matchedDN,
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
    public String getAuthzId()
    {
      return authzId;
    }



    @Override
    public WhoAmIExtendedOperation getExtendedOperation()
    {
      return SINGLETON;
    }



    @Override
    public ByteString getResponseValue()
    {
      if (authzId != null)
      {
        ByteString.valueOf(authzId);
      }
      return null;
    }



    public Response setAuthzId(String authzId)
    {
      this.authzId = authzId;
      return this;
    }



    @Override
    public void toString(StringBuilder buffer)
    {
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



  private static final WhoAmIExtendedOperation SINGLETON =
      new WhoAmIExtendedOperation();



  private WhoAmIExtendedOperation()
  {
    super();
    // We could register the result codes here if they are not
    // already included in the default set.
  }



  @Override
  public Request decodeRequest(String requestName,
      ByteString requestValue) throws DecodeException
  {
    return new Request();
  }



  @Override
  public Response decodeResponse(ResultCode resultCode,
      String matchedDN, String diagnosticMessage, String responseName,
      ByteString responseValue) throws DecodeException
  {
    // TODO: Should we check oid is null?
    String authzId = null;
    if (responseValue != null)
    {
      authzId = responseValue.toString();
    }
    return new Response(resultCode, matchedDN, diagnosticMessage)
        .setAuthzId(authzId);
  }
}
