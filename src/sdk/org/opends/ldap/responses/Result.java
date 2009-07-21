package org.opends.ldap.responses;



import java.util.LinkedList;
import java.util.List;

import org.opends.ldap.ResultCode;
import org.opends.server.util.Validator;
import org.opends.types.DN;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 11:24:57
 * AM To change this template use File | Settings | File Templates.
 */
public class Result extends Response
{
  private ResultCode resultCode;
  private String matchedDN;
  private String diagnosticMessage;
  private List<String> referrals;

  // For local errors caused by internal exceptions.
  private Throwable cause;



  public Result(ResultCode resultCode, String matchedDN,
      String diagnosticMessage)
  {
    this(resultCode, matchedDN, diagnosticMessage, null);
  }



  public Result(ResultCode resultCode, String diagnosticMessage,
      Throwable cause)
  {
    this(resultCode, "", diagnosticMessage, cause);
  }



  private Result(ResultCode resultCode, String matchedDN,
      String diagnosticMessage, Throwable cause)
  {
    Validator.ensureNotNull(resultCode, matchedDN, diagnosticMessage);

    this.resultCode = resultCode;
    this.matchedDN = matchedDN;
    this.diagnosticMessage = diagnosticMessage;
    this.cause = cause;
    this.referrals = new LinkedList<String>();
  }



  public Result addReferral(String... referrals)
  {
    for (String referral : referrals)
    {
      Validator.ensureNotNull(referral);
      this.referrals.add(referral);
    }
    return this;
  }



  public String getDiagnosticMessage()
  {
    return diagnosticMessage;
  }



  public String getMatchedDN()
  {
    return matchedDN;
  }



  public Iterable<String> getReferrals()
  {
    return referrals;
  }



  public ResultCode getResultCode()
  {
    return resultCode;
  }



  public Throwable getCause()
  {
    return cause;
  }



  public boolean hasReferrals()
  {
    return !referrals.isEmpty();
  }



  public Result setDiagnosticMessage(String diagnosticMessage)
  {
    Validator.ensureNotNull(diagnosticMessage);
    this.diagnosticMessage = diagnosticMessage;
    return this;
  }



  public Result setMatchedDN(DN matchedDN)
  {
    Validator.ensureNotNull(matchedDN);
    this.matchedDN = matchedDN.toString();
    return this;
  }



  public Result setMatchedDN(String matchedDN)
  {
    Validator.ensureNotNull(matchedDN);
    this.matchedDN = matchedDN;
    return this;
  }



  public Result setResultCode(ResultCode resultCode)
  {
    Validator.ensureNotNull(resultCode);
    this.resultCode = resultCode;
    return this;
  }



  public Result setCause(Throwable cause)
  {
    this.cause = cause;
    return this;
  }



  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("Result(resultCode=");
    buffer.append(getResultCode());
    buffer.append(", matchedDN=");
    buffer.append(getMatchedDN());
    buffer.append(", diagnosticMessage=");
    buffer.append(getDiagnosticMessage());
    buffer.append(", referrals=");
    buffer.append(getReferrals());
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
