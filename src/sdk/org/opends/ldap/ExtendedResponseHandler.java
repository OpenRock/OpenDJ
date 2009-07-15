package org.opends.ldap;



import org.opends.ldap.responses.ExtendedResult;
import org.opends.ldap.responses.IntermediateResponse;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 11, 2009 Time:
 * 11:34:28 AM To change this template use File | Settings | File
 * Templates.
 */
public interface ExtendedResponseHandler extends
    ResponseHandler<ExtendedResult>
{
  public void handleIntermediateResponse(
      IntermediateResponse intermediateResponse);
}
