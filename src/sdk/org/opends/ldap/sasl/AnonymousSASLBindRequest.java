package org.opends.ldap.sasl;



import static org.opends.server.util.ServerConstants.*;

import javax.security.sasl.SaslException;

import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 1, 2009 Time: 11:34:10
 * AM To change this template use File | Settings | File Templates.
 */
public final class AnonymousSASLBindRequest extends
    AbstractSASLBindRequest
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



  @Override
  public ByteString getSASLCredentials()
  {
    return ByteString.valueOf(traceString);
  }



  @Override
  public String getSASLMechanism()
  {
    return SASL_MECHANISM_ANONYMOUS;
  }



  public String getTraceString()
  {
    return traceString;
  }



  public void initialize(String serverName) throws SaslException
  {
    // Nothing to initialize.
  }



  public boolean isComplete()
  {
    return true;
  }



  public boolean isSecure()
  {
    return false;
  }



  public AnonymousSASLBindRequest setTraceString(String traceString)
  {
    Validator.ensureNotNull(traceString);
    this.traceString = traceString;
    return this;
  }



  @Override
  public void toString(StringBuilder buffer)
  {
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
