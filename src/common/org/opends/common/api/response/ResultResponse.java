package org.opends.common.api.response;

import org.opends.server.util.Validator;
import org.opends.common.api.Message;
import org.opends.common.api.ResultCode;
import org.opends.common.api.DN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 25, 2009 Time: 1:54:39
 * PM To change this template use File | Settings | File Templates.
 */
public interface ResultResponse extends Response
{
  public ResultCode getResultCode();

  public String getMatchedDN();

  public String getDiagnosticMessage();

  public Iterable<String> getReferrals();

  public boolean hasReferrals();
}
