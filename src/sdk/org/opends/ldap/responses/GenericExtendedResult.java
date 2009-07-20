package org.opends.ldap.responses;



import org.opends.ldap.ResultCode;
import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 22, 2009 Time: 6:22:58
 * PM To change this template use File | Settings | File Templates.
 */
public final class GenericExtendedResult extends ExtendedResult<GenericExtendedResult>
{
  private ByteString responseValue;



  public GenericExtendedResult(ResultCode resultCode, String matchedDN,
      String diagnosticMessage)
  {
    super(resultCode, matchedDN, diagnosticMessage);
  }



  @Override
  public ByteString getResponseValue()
  {
    return responseValue;
  }



  public GenericExtendedResult setResponseValue(ByteString responseValue)
  {
    Validator.ensureNotNull(responseValue);
    this.responseValue = responseValue;
    return this;
  }



  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("ExtendedResponse(resultCode=");
    buffer.append(getResultCode());
    buffer.append(", matchedDN=");
    buffer.append(getMatchedDN());
    buffer.append(", diagnosticMessage=");
    buffer.append(getDiagnosticMessage());
    buffer.append(", referrals=");
    buffer.append(getReferrals());
    buffer.append(", responseName=");
    buffer.append(getResponseName());
    buffer.append(", responseValue=");
    buffer.append(responseValue);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
