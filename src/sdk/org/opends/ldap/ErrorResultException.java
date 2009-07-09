package org.opends.ldap;



import java.util.concurrent.ExecutionException;

import org.opends.ldap.responses.ResultResponse;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 10:51:48
 * AM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class ErrorResultException extends ExecutionException implements
    ResultResponse
{
  private final ResultResponse resultResponse;



  public ErrorResultException(ResultResponse resultResponse)
  {
    super(resultResponse.getResultCode() + ": "
        + resultResponse.getDiagnosticMessage());
    this.resultResponse = resultResponse;
  }



  public String getDiagnosticMessage()
  {
    return resultResponse.getDiagnosticMessage();
  }



  public String getMatchedDN()
  {
    return resultResponse.getMatchedDN();
  }



  public Iterable<String> getReferrals()
  {
    return resultResponse.getReferrals();
  }



  public ResultCode getResultCode()
  {
    return resultResponse.getResultCode();
  }



  public boolean hasReferrals()
  {
    return resultResponse.hasReferrals();
  }
}
