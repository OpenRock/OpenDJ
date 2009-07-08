package org.opends.client.spi;

import org.opends.common.api.response.ResultResponse;
import org.opends.common.api.ResultCode;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 8, 2009
 * Time: 10:51:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class ErrorResultException extends ExecutionException
  implements ResultResponse
{
  private ResultResponse resultResponse;

  public ErrorResultException(ResultResponse resultResponse)
  {
    super(resultResponse.getResultCode() + ": " +
        resultResponse.getDiagnosticMessage());
    this.resultResponse = resultResponse;
  }

  public ResultCode getResultCode() {
    return resultResponse.getResultCode();
  }

  public String getMatchedDN() {
    return resultResponse.getMatchedDN();
  }

  public String getDiagnosticMessage() {
    return resultResponse.getDiagnosticMessage();
  }

  public Iterable<String> getReferrals() {
    return resultResponse.getReferrals();
  }

  public boolean hasReferrals() {
    return resultResponse.hasReferrals();
  }
}
