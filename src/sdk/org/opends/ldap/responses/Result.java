package org.opends.ldap.responses;



import org.opends.ldap.Control;
import org.opends.ldap.GenericControl;
import org.opends.ldap.ResultCode;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 25, 2009 Time:
 * 1:54:39 PM To change this template use File | Settings | File
 * Templates.
 */
public interface Result extends Response
{
  Control getControl(String oid);



  Iterable<GenericControl> getControls();



  String getDiagnosticMessage();



  String getMatchedDN();



  Iterable<String> getReferrals();



  ResultCode getResultCode();



  boolean hasControls();



  boolean hasReferrals();
}
