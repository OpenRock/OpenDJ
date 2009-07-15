package org.opends.ldap.responses;



import java.util.concurrent.ExecutionException;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 10:51:48
 * AM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class ErrorResultException extends ExecutionException
{
  private final Result result;



  public ErrorResultException(Result result)
  {
    super(result.getResultCode() + ": " + result.getDiagnosticMessage());
    this.result = result;
  }



  public Result getResult()
  {
    return result;
  }
}
