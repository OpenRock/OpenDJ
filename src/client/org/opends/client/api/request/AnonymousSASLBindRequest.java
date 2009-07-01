package org.opends.client.api.request;

import static org.opends.server.util.ServerConstants.SASL_MECHANISM_ANONYMOUS;
import org.opends.server.util.Validator;
import org.opends.client.api.TextInputCallbackHandler;

import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 1, 2009
 * Time: 11:34:10 AM
 * To change this template use File | Settings | File Templates.
 */
public final class AnonymousSASLBindRequest
    extends AbstractSASLBindRequest
{
  private String traceString;
  private TextInputCallbackHandler callbackHandler;

  public AnonymousSASLBindRequest()
  {
    super(SASL_MECHANISM_ANONYMOUS);
    this.traceString = "".intern();
  }

  public AnonymousSASLBindRequest(String traceString)
  {
    super(SASL_MECHANISM_ANONYMOUS);
    Validator.ensureNotNull(traceString);
    this.traceString = traceString;
  }

  public String getTraceString()
  {
    return traceString;
  }

  public AnonymousSASLBindRequest setTraceString(String traceString)
  {
    Validator.ensureNotNull(traceString);
    this.traceString = traceString;
    return this;
  }

  public TextInputCallbackHandler getCallbackHandler() {
    return callbackHandler;
  }

  public AnonymousSASLBindRequest setCallbackHandler(
      TextInputCallbackHandler callbackHandler)
  {
    this.callbackHandler = callbackHandler;
    return this;
  }

  @Override
  protected void handle(TextInputCallback callback)
      throws UnsupportedCallbackException
  {
    if(callbackHandler == null)
    {
      callback.setText(traceString);
    }
    else
    {
      callbackHandler.handle(callback);
    }
  }

  public void toString(StringBuilder buffer) {
    buffer.append("AnonymousSASLBindRequest(bindDN=");
    buffer.append(getBindDN());
    buffer.append(", authentication=SASL");
    buffer.append(", saslMechanism=");
    buffer.append(saslMechanism);
    buffer.append(", traceString=");
    buffer.append(traceString);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
