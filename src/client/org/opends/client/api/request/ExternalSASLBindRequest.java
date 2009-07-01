package org.opends.client.api.request;

import static org.opends.server.util.ServerConstants.SASL_MECHANISM_EXTERNAL;
import org.opends.common.api.DN;
/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 1, 2009
 * Time: 5:55:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExternalSASLBindRequest extends AbstractSASLBindRequest
{
  public ExternalSASLBindRequest()
  {
    super(SASL_MECHANISM_EXTERNAL);
  }

  public ExternalSASLBindRequest(String authorizationID)
  {
    super(SASL_MECHANISM_EXTERNAL, authorizationID);
  }

  public ExternalSASLBindRequest(DN authorizationDN)
  {
    super(SASL_MECHANISM_EXTERNAL, authorizationDN);
  }

  public String getAuthorizationID() {
    return authorizationID;
  }

  public void toString(StringBuilder buffer) {
    buffer.append("ExternalSASLBindRequest(bindDN=");
    buffer.append(getBindDN());
    buffer.append(", authentication=SASL");
    buffer.append(", saslMechanism=");
    buffer.append(saslMechanism);
    buffer.append(", authorizationID=");
    buffer.append(authorizationID);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
