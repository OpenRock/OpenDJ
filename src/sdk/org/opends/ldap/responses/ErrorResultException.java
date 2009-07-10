package org.opends.ldap.responses;



import java.util.concurrent.ExecutionException;

import org.opends.ldap.Control;
import org.opends.ldap.GenericControl;
import org.opends.ldap.ResultCode;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 10:51:48
 * AM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class ErrorResultException extends ExecutionException implements
    Result
{
  private final Result result;



  public ErrorResultException(Result resultResponse)
  {
    super(resultResponse.getResultCode() + ": "
        + resultResponse.getDiagnosticMessage());
    this.result = resultResponse;
  }



  /**
   * {@inheritDoc}
   */
  public String getDiagnosticMessage()
  {
    return result.getDiagnosticMessage();
  }



  /**
   * {@inheritDoc}
   */
  public String getMatchedDN()
  {
    return result.getMatchedDN();
  }



  /**
   * {@inheritDoc}
   */
  public Iterable<String> getReferrals()
  {
    return result.getReferrals();
  }



  /**
   * {@inheritDoc}
   */
  public ResultCode getResultCode()
  {
    return result.getResultCode();
  }



  /**
   * {@inheritDoc}
   */
  public boolean hasReferrals()
  {
    return result.hasReferrals();
  }



  /**
   * {@inheritDoc}
   */
  public Control getControl(String oid)
  {
    return result.getControl(oid);
  }



  /**
   * {@inheritDoc}
   */
  public Iterable<GenericControl> getControls()
  {
    return result.getControls();
  }



  /**
   * {@inheritDoc}
   */
  public boolean hasControls()
  {
    return result.hasControls();
  }
}
