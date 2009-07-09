package org.opends.ldap.responses;



import org.opends.ldap.ResultCode;
import org.opends.types.DN;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 26, 2009 Time:
 * 10:11:02 AM To change this template use File | Settings | File
 * Templates.
 */
public final class AddResponse extends AbstractResultResponse
{
  public AddResponse(ResultCode resultCode, DN matchedDN,
      String diagnosticMessage)
  {
    super(resultCode, matchedDN.toString(), diagnosticMessage);
  }



  public AddResponse(ResultCode resultCode, String matchedDN,
      String diagnosticMessage)
  {
    super(resultCode, matchedDN, diagnosticMessage);
  }



  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("AddResponse(resultCode=");
    buffer.append(resultCode);
    buffer.append(", matchedDN=");
    buffer.append(matchedDN);
    buffer.append(", diagnosticMessage=");
    buffer.append(diagnosticMessage);
    buffer.append(", referrals=");
    buffer.append(referrals);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
