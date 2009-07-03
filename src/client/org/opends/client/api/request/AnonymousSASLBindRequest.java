package org.opends.client.api.request;

import static org.opends.server.util.ServerConstants.SASL_MECHANISM_ANONYMOUS;
import static org.opends.server.util.ServerConstants.SASL_DEFAULT_PROTOCOL;
import org.opends.server.util.Validator;
import org.opends.server.types.ByteString;
import org.opends.client.api.TextInputCallbackHandler;

import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.SaslException;
import javax.security.sasl.Sasl;

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

  public AnonymousSASLBindRequest()
  {
    this.traceString = "".intern();
  }

  public AnonymousSASLBindRequest(String traceString)
  {
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

  public ByteString getSASLCredentials()
  {
    return ByteString.valueOf(traceString);
  }

  public String getSASLMechanism()
  {
    return SASL_MECHANISM_ANONYMOUS;
  }

  public void dispose() throws SaslException
  {
    // Nothing needed.
  }

  public boolean evaluateCredentials(ByteString incomingCredentials)
      throws SaslException
  {
    // This is a single stage SASL bind.
    return true;
  }

  public void initialize(String serverName) throws SaslException
  {
    // Nothing to initialize.
  }

  public boolean isComplete() {
    return true;
  }

  public boolean isSecure() {
    return false;
  }

  public void toString(StringBuilder buffer) {
    buffer.append("AnonymousSASLBindRequest(bindDN=");
    buffer.append(getBindDN());
    buffer.append(", authentication=SASL");
    buffer.append(", saslMechanism=");
    buffer.append(getSASLMechanism());
    buffer.append(", traceString=");
    buffer.append(traceString);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
