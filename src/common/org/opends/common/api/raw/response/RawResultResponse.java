package org.opends.common.api.raw.response;

import org.opends.server.core.operations.Response;
import org.opends.server.core.operations.Schema;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.ResultCode;
import org.opends.server.util.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 25, 2009 Time: 1:54:39
 * PM To change this template use File | Settings | File Templates.
 */
public abstract class RawResultResponse extends RawResponse
{
  private int resultCode;
  private String matchedDN;
  private String diagnosticMessage;
  private List<String> referrals;

  public RawResultResponse(int resultCode,
                           String matchedDN,
                           String diagnosticMessage)
  {
    Validator.ensureNotNull(resultCode, matchedDN, diagnosticMessage);
    this.resultCode = resultCode;
    this.matchedDN = matchedDN;
    this.diagnosticMessage = diagnosticMessage;
    this.referrals = Collections.emptyList();
  }

  public int getResultCode()
  {
    return resultCode;
  }

  public RawResultResponse setResultCode(int resultCode)
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

  public RawResultResponse addReferral(String referral)
  {
    Validator.ensureNotNull(referral);
    if (this.referrals == Collections.EMPTY_LIST)
    {
      referrals = new ArrayList<String>();
    }
    referrals.add(referral);
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

  public Response toResponse(Schema schema) throws DirectoryException
  {
    // TODO: not yet implemented.
    return null;
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("ResultResponse(resultCode=");
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
