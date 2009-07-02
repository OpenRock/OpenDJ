package org.opends.client.api.request;

import org.opends.server.types.ByteString;
import static org.opends.server.util.StaticUtils.getExceptionMessage;
import static org.opends.server.util.ServerConstants.SASL_MECHANISM_GSSAPI;
import static org.opends.server.util.ServerConstants.SASL_DEFAULT_PROTOCOL;
import org.opends.server.util.Validator;
import org.opends.messages.Message;
import static org.opends.messages.ExtensionMessages.ERR_SASL_PROTOCOL_ERROR;
import static org.opends.messages.ExtensionMessages.ERR_SASL_CONTEXT_CREATE_ERROR;
import org.opends.common.api.DN;

import javax.security.auth.Subject;
import javax.security.sasl.SaslException;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 1, 2009
 * Time: 3:56:46 PM
 * To change this template use File | Settings | File Templates.
 */
public final class GSSAPISASLBindRequest extends AbstractSASLBindRequest
{
  private SaslClient saslClient;
  private ByteString outgoingCredentials = null;

  private Subject subject;
  private String authorizationID;
  
  private ByteString incomingCredentials;
  private String serverName;

  public GSSAPISASLBindRequest(Subject subject)
  {
    Validator.ensureNotNull(subject);
    this.subject = subject;
  }

  public GSSAPISASLBindRequest(Subject subject,
                               String authorizationID)
  {
    Validator.ensureNotNull(subject, authorizationID);
    this.subject = subject;
  }

  public GSSAPISASLBindRequest(Subject subject,
                               DN authorizationDN)
  {
    Validator.ensureNotNull(subject, authorizationDN);
    this.subject = subject;
  }

  public String getAuthorizationID() {
    return authorizationID;
  }

  public GSSAPISASLBindRequest setAuthorizationID(String authorizationID)
  {
    this.authorizationID = authorizationID;
    return this;
  }

  public ByteString getSASLCredentials()
  {
    return outgoingCredentials;
  }

  public String getSASLMechanism()
  {
    return saslClient.getMechanismName();
  }

  public void dispose() throws SaslException
  {
    saslClient.dispose();
  }

  public boolean isComplete() {
    return saslClient.isComplete();
  }  


  /**
   * Override so the Sasl client can be used as the subject.
   *
   * @param incomingCredentials
   * @return
   * @throws SaslException
   */
  public boolean evaluateCredentials(ByteString incomingCredentials)
      throws SaslException
  {
    this.incomingCredentials = incomingCredentials;
    try
    {
      return Subject.doAs(subject, evaluateAction);
    }
    catch(PrivilegedActionException e)
    {
      if(e.getCause() instanceof SaslException)
      {
        throw (SaslException)e.getCause();
      }

      // This should not happen. Must be a bug.
      Message msg =
          ERR_SASL_PROTOCOL_ERROR.get(SASL_MECHANISM_GSSAPI,
              getExceptionMessage(e));
      throw new SaslException(msg.toString(), e.getCause());
    }
  }

  /**
   * Override so the Sasl client can be initialized as the subject.
   *
   * @param serverName
   * @throws SaslException
   */
  public void initialize(String serverName) throws SaslException
  {
    this.serverName = serverName;

    try
    {
      Subject.doAs(subject, invokeAction);
    }
    catch(PrivilegedActionException e)
    {
      if(e.getCause() instanceof SaslException)
      {
        throw (SaslException)e.getCause();
      }

      // This should not happen. Must be a bug.
      Message msg =
          ERR_SASL_CONTEXT_CREATE_ERROR.get(SASL_MECHANISM_GSSAPI,
              getExceptionMessage(e));
      throw new SaslException(msg.toString(), e.getCause());
    }
  }

  PrivilegedExceptionAction<Boolean> evaluateAction =
      new PrivilegedExceptionAction<Boolean>()
      {
        public Boolean run() throws Exception
        {
          byte[] bytes =
              saslClient.evaluateChallenge(
                  incomingCredentials.toByteArray());
          if(bytes != null)
          {
            outgoingCredentials = ByteString.wrap(bytes);
          }
          else
          {
            outgoingCredentials = null;
          }

          return isComplete();
        }
      };

  PrivilegedExceptionAction<Object> invokeAction =
      new PrivilegedExceptionAction<Object>()
      {
        public Object run() throws Exception
        {
          saslClient = Sasl.createSaslClient(
              new String[]{SASL_MECHANISM_GSSAPI},
              authorizationID, SASL_DEFAULT_PROTOCOL, serverName, null,
              GSSAPISASLBindRequest.this);

          if(saslClient.hasInitialResponse())
          {
            byte[] bytes = saslClient.evaluateChallenge(new byte[0]);
            if(bytes != null)
            {
              outgoingCredentials = ByteString.wrap(bytes);
            }
          }
          return null;
        }
      };

  public void toString(StringBuilder buffer) {
    buffer.append("GSSAPISASLBindRequest(bindDN=");
    buffer.append(getBindDN());
    buffer.append(", authentication=SASL");
    buffer.append(", saslMechanism=");
    buffer.append(getSASLMechanism());
    buffer.append(", subject=");
    buffer.append(subject);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
