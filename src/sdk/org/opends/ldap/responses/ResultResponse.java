package org.opends.ldap.responses;



import org.opends.ldap.ResultCode;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 25, 2009 Time:
 * 1:54:39 PM To change this template use File | Settings | File
 * Templates.
 */
public interface ResultResponse extends Response
{
  public String getDiagnosticMessage();



  public String getMatchedDN();



  public Iterable<String> getReferrals();



  public ResultCode getResultCode();



  public boolean hasReferrals();
}
