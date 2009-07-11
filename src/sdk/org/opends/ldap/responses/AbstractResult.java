package org.opends.ldap.responses;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opends.ldap.Message;
import org.opends.ldap.ResultCode;
import org.opends.server.util.Validator;
import org.opends.types.DN;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 11:24:57
 * AM To change this template use File | Settings | File Templates.
 */
public abstract class AbstractResult extends Message implements
    Result
{
  protected ResultCode resultCode;
  protected String matchedDN;
  protected String diagnosticMessage;
  protected List<String> referrals;



  public AbstractResult(ResultCode resultCode,
      String matchedDN, String diagnosticMessage)
  {
    Validator.ensureNotNull(resultCode, matchedDN, diagnosticMessage);
    this.resultCode = resultCode;
    this.matchedDN = matchedDN;
    this.diagnosticMessage = diagnosticMessage;
    this.referrals = Collections.emptyList();
  }



  public Result addReferral(String... referrals)
  {
    if (referrals != null)
    {
      if (this.referrals == Collections.EMPTY_LIST)
      {
        this.referrals = new ArrayList<String>();
      }
      for (String referral : referrals)
      {
        Validator.ensureNotNull(referral);
        this.referrals.add(referral);
      }
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
}
