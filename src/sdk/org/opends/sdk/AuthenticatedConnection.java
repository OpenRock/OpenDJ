package org.opends.sdk;

import org.opends.sdk.requests.BindRequest;
import org.opends.sdk.responses.BindResult;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Nov 11, 2009
 * Time: 2:36:49 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AuthenticatedConnection extends Connection
{
  public BindRequest getAuthenticatedBindRequest();
  public BindResult getAuthenticatedBindResult();
}
