package org.opends.common.api.response;

import org.opends.common.api.ResultCode;
import org.opends.common.api.DN;
import org.opends.common.api.Message;
import org.opends.server.util.Validator;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 8, 2009
 * Time: 11:24:57 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractResultResponse extends Message 
    implements ResultResponse
{
  protected ResultCode resultCode;
  protected String matchedDN;
  protected String diagnosticMessage;
  protected List<String> referrals;

  public AbstractResultResponse(ResultCode resultCode,
                           String matchedDN,
                           String diagnosticMessage)
  {
    Validator.ensureNotNull(resultCode, matchedDN, diagnosticMessage);
    this.resultCode = resultCode;
    this.matchedDN = matchedDN;
    this.diagnosticMessage = diagnosticMessage;
    this.referrals = Collections.emptyList();
  }

  public ResultCode getResultCode()
  {
    return resultCode;
  }

  public ResultResponse setResultCode(ResultCode resultCode)
  {
    Validator.ensureNotNull(resultCode);
    this.resultCode = resultCode;
    return this;
  }

  public String getMatchedDN()
  {
    return matchedDN;
  }

  public ResultResponse setMatchedDN(String matchedDN)
  {
    Validator.ensureNotNull(matchedDN);
    this.matchedDN = matchedDN;
    return this;
  }

  public ResultResponse setMatchedDN(DN matchedDN)
  {
    Validator.ensureNotNull(matchedDN);
    this.matchedDN = matchedDN.toString();
    return this;
  }

  public String getDiagnosticMessage()
  {
    return diagnosticMessage;
  }

  public ResultResponse setDiagnosticMessage(String diagnosticMessage)
  {
    Validator.ensureNotNull(diagnosticMessage);
    this.diagnosticMessage = diagnosticMessage;
    return this;
  }

  public ResultResponse addReferral(String... referrals)
  {
    if(referrals != null)
    {
      if (this.referrals == Collections.EMPTY_LIST)
      {
        this.referrals = new ArrayList<String>();
      }
      for(String referral : referrals)
      {
        Validator.ensureNotNull(referral);
        this.referrals.add(referral);
      }
    }
    return this;
  }

  public Iterable<String> getReferrals()
  {
    return referrals;
  }

  public boolean hasReferrals()
  {
    return !referrals.isEmpty();
  }
}
