package org.opends.ldap.extensions;



import static org.opends.server.util.ServerConstants.*;

import org.opends.ldap.DecodeException;
import org.opends.ldap.ExtendedOperation;
import org.opends.ldap.ResultCode;
import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.responses.ExtendedResult;
import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 22, 2009 Time: 6:40:06
 * PM To change this template use File | Settings | File Templates.
 */
public final class WhoAmIExtendedOperation
{
  public static class Request extends
      ExtendedRequest<Request, Response>
  {
    public Request()
    {
      super(OID_WHO_AM_I_REQUEST);
    }



    public Operation getExtendedOperation()
    {
      return OPERATION;
    }



    public ByteString getRequestValue()
    {
      return null;
    }



    public void toString(StringBuilder buffer)
    {
      buffer.append("WhoAmIExtendedRequest(requestName=");
      buffer.append(getRequestName());
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }

  public static class Response extends ExtendedResult<Response>
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
      buffer.append(getResultCode());
      buffer.append(", matchedDN=");
      buffer.append(getMatchedDN());
      buffer.append(", diagnosticMessage=");
      buffer.append(getDiagnosticMessage());
      buffer.append(", referrals=");
      buffer.append(getReferrals());
      buffer.append(", authzId=");
      buffer.append(authzId);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }

  private static final class Operation implements
      ExtendedOperation<Request, Response>
  {

    public Request decodeRequest(String requestName,
        ByteString requestValue) throws DecodeException
    {
      return new Request();
    }



    public Response decodeResponse(ResultCode resultCode,
        String matchedDN, String diagnosticMessage,
        String responseName, ByteString responseValue)
        throws DecodeException
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



    public Response decodeResponse(ResultCode resultCode,
        String matchedDN, String diagnosticMessage)
    {
      return new Response(resultCode, matchedDN, diagnosticMessage);
    }
  }



  // Singleton instance.
  private static final Operation OPERATION = new Operation();
}
