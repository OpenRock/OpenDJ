package org.opends.client.api.request;

import org.opends.common.api.request.SASLBindRequest;
import org.opends.common.api.request.BindRequest;
import org.opends.common.api.DN;
import org.opends.common.protocols.sasl.SASLContext;
import org.opends.server.types.ByteString;
import static org.opends.server.util.ServerConstants.SASL_DEFAULT_PROTOCOL;
import org.opends.server.util.Validator;
import static org.opends.messages.ExtensionMessages.INFO_SASL_UNSUPPORTED_CALLBACK;

import javax.security.sasl.*;
import javax.security.auth.callback.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 1, 2009
 * Time: 12:49:50 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSASLBindRequest extends BindRequest
    implements SASLBindRequest, SASLContext, CallbackHandler
{
  protected SaslClient saslClient;
  protected ByteString outgoingCredentials = null;

  protected String saslMechanism;
  protected String authorizationID;

  protected AbstractSASLBindRequest(String saslMechanism)
  {
    this.saslMechanism = saslMechanism;
  }

  protected AbstractSASLBindRequest(String saslMechanism,
                                    String authorizationID)
  {
    this.saslMechanism = saslMechanism;
    Validator.ensureNotNull(authorizationID);
    this.authorizationID = authorizationID;
  }

  protected AbstractSASLBindRequest(String saslMechanism,
                                    DN authorizationDN)
  {
    this.saslMechanism = saslMechanism;
    Validator.ensureNotNull(authorizationDN);
    this.authorizationID = "dn:" + authorizationDN.toString();
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

  public boolean evaluateCredentials(ByteString incomingCredentials)
      throws SaslException
  {
    byte[] bytes =
        saslClient.evaluateChallenge(incomingCredentials.toByteArray());
    if(bytes != null)
    {
      this.outgoingCredentials = ByteString.wrap(bytes);
    }
    else
    {
      this.outgoingCredentials = null;
    }

    return isComplete();
  }

  public void initialize(String serverName) throws SaslException
  {
    saslClient = Sasl.createSaslClient(new String[]{saslMechanism},
        authorizationID, SASL_DEFAULT_PROTOCOL, serverName, null,
        this);

    if(saslClient.hasInitialResponse())
    {
      byte[] bytes = saslClient.evaluateChallenge(new byte[0]);
      if(bytes != null)
      {
        this.outgoingCredentials = ByteString.wrap(bytes);
      }
    }
  }

  public boolean isComplete() {
    return saslClient.isComplete();
  }

  public byte[] unwrap(byte[] incoming, int offset, int len) {
    return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  public byte[] wrap(byte[] outgoing, int offset, int len) {
    return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void handle(Callback[] callbacks)
      throws IOException, UnsupportedCallbackException
  {
    for (Callback callback : callbacks) {
      if (callback instanceof NameCallback) {
        handle((NameCallback) callback);
      } else if (callback instanceof PasswordCallback) {
        handle((PasswordCallback) callback);
      } else if (callback instanceof AuthorizeCallback)  {
        handle((AuthorizeCallback) callback);
      } else if (callback instanceof RealmCallback) {
        handle((RealmCallback) callback);
      } else if (callback instanceof RealmChoiceCallback) {
        handle((RealmChoiceCallback) callback);
      } else if (callback instanceof ChoiceCallback)  {
        handle((ChoiceCallback) callback);
      } else if (callback instanceof ConfirmationCallback)  {
        handle((ConfirmationCallback) callback);
      } else if (callback instanceof LanguageCallback)  {
        handle((LanguageCallback) callback);
      } else if (callback instanceof TextInputCallback)  {
        handle((TextInputCallback) callback);
      } else if (callback instanceof TextOutputCallback)  {
        handle((TextOutputCallback) callback);
      } else {
        org.opends.messages.Message message =
            INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(),
                String.valueOf(callback));
        throw new UnsupportedCallbackException(callback,
            message.toString());
      }
    }
  }

  protected void handle(AuthorizeCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(),
            String.valueOf(callback));
    throw new UnsupportedCallbackException(callback,
        message.toString());
  }

  protected void handle(ChoiceCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(),
            String.valueOf(callback));
    throw new UnsupportedCallbackException(callback,
        message.toString());
  }

  protected void handle(ConfirmationCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(),
            String.valueOf(callback));
    throw new UnsupportedCallbackException(callback,
        message.toString());
  }

  protected void handle(LanguageCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(),
            String.valueOf(callback));
    throw new UnsupportedCallbackException(callback,
        message.toString());
  }

  protected void handle(NameCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(),
            String.valueOf(callback));
    throw new UnsupportedCallbackException(callback,
        message.toString());
  }
  protected void handle(PasswordCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(),
            String.valueOf(callback));
    throw new UnsupportedCallbackException(callback,
        message.toString());
  }
  protected void handle(RealmCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(),
            String.valueOf(callback));
    throw new UnsupportedCallbackException(callback,
        message.toString());
  }

  protected void handle(RealmChoiceCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(),
            String.valueOf(callback));
    throw new UnsupportedCallbackException(callback,
        message.toString());
  }

  protected void handle(TextInputCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(),
            String.valueOf(callback));
    throw new UnsupportedCallbackException(callback,
        message.toString());
  }

  protected void handle(TextOutputCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(),
            String.valueOf(callback));
    throw new UnsupportedCallbackException(callback,
        message.toString());
  }
}
