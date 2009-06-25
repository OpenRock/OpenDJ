package org.opends.common.api.response;

import org.opends.server.util.Validator;
import org.opends.common.api.RawMessage;
import org.opends.common.api.ResultCode;
import org.opends.common.api.DN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 25, 2009 Time: 1:54:39
 * PM To change this template use File | Settings | File Templates.
 */
public abstract class RawResultResponse extends RawMessage
    implements RawResponse
{
  protected ResultCode resultCode;
  protected String matchedDN;
  protected String diagnosticMessage;
  protected List<String> referrals;

  public RawResultResponse(ResultCode resultCode,
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

  public RawResultResponse setResultCode(ResultCode resultCode)
  {
    Validator.ensureNotNull(resultCode);
    this.resultCode = resultCode;
    return this;
  }

  public String getMatchedDN()
  {
    return matchedDN;
  }

  public RawResultResponse setMatchedDN(String matchedDN)
  {
    Validator.ensureNotNull(matchedDN);
    this.matchedDN = matchedDN;
    return this;
  }

  public RawResultResponse setMatchedDN(DN matchedDN)
  {
    Validator.ensureNotNull(matchedDN);
    this.matchedDN = matchedDN.toString();
    return this;
  }

  public String getDiagnosticMessage()
  {
    return diagnosticMessage;
  }

  public RawResultResponse setDiagnosticMessage(String diagnosticMessage)
  {
    Validator.ensureNotNull(diagnosticMessage);
    this.diagnosticMessage = diagnosticMessage;
    return this;
  }

  public RawResultResponse addReferral(String... referrals)
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
