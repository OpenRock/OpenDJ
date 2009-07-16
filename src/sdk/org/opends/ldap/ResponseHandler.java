package org.opends.ldap;



import org.opends.ldap.responses.ErrorResultException;
import org.opends.ldap.responses.Result;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 1, 2009 Time:
 * 3:16:58 PM To change this template use File | Settings | File
 * Templates.
 */
public interface ResponseHandler<R extends Result>
{
  void handleErrorResult(ErrorResultException result);



  void handleResult(R result);
}
