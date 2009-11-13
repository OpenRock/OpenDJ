package org.opends.sdk.sasl;

import static org.opends.messages.ExtensionMessages.INFO_SASL_UNSUPPORTED_CALLBACK;

import javax.security.auth.callback.*;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.RealmChoiceCallback;
import javax.security.sasl.SaslException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Nov 13, 2009
 * Time: 2:34:09 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSASLContext
    implements SASLContext, CallbackHandler
{
  /**
   * The name of the default protocol used.
   */
  protected static final String SASL_DEFAULT_PROTOCOL = "ldap";

  public void handle(Callback[] callbacks) throws IOException,
      UnsupportedCallbackException
  {
    for (Callback callback : callbacks)
    {
      if (callback instanceof NameCallback)
      {
        handle((NameCallback) callback);
      }
      else if (callback instanceof PasswordCallback)
      {
        handle((PasswordCallback) callback);
      }
      else if (callback instanceof AuthorizeCallback)
      {
        handle((AuthorizeCallback) callback);
      }
      else if (callback instanceof RealmCallback)
      {
        handle((RealmCallback) callback);
      }
      else if (callback instanceof RealmChoiceCallback)
      {
        handle((RealmChoiceCallback) callback);
      }
      else if (callback instanceof ChoiceCallback)
      {
        handle((ChoiceCallback) callback);
      }
      else if (callback instanceof ConfirmationCallback)
      {
        handle((ConfirmationCallback) callback);
      }
      else if (callback instanceof LanguageCallback)
      {
        handle((LanguageCallback) callback);
      }
      else if (callback instanceof TextInputCallback)
      {
        handle((TextInputCallback) callback);
      }
      else if (callback instanceof TextOutputCallback)
      {
        handle((TextOutputCallback) callback);
      }
      else
      {
        org.opends.messages.Message message =
            INFO_SASL_UNSUPPORTED_CALLBACK.get(
                getSASLBindRequest().getSASLMechanism(),
                String.valueOf(callback));
        throw new UnsupportedCallbackException(callback, message
            .toString());
      }
    }
  }



  /**
   * Default implemenation just returns the copy of the bytes.
   *
   * @param incoming
   * @param offset
   * @param len
   * @return
   */
  public byte[] unwrap(byte[] incoming, int offset, int len)
      throws SaslException
  {
    byte[] copy = new byte[len];
    System.arraycopy(incoming, offset, copy, 0, len);
    return copy;
  }



  /**
   * Default implemenation just returns the copy of the bytes.
   *
   * @param outgoing
   * @param offset
   * @param len
   * @return
   */
  public byte[] wrap(byte[] outgoing, int offset, int len)
      throws SaslException
  {
    byte[] copy = new byte[len];
    System.arraycopy(outgoing, offset, copy, 0, len);
    return copy;
  }



  protected void handle(AuthorizeCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(
                getSASLBindRequest().getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(ChoiceCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(
                getSASLBindRequest().getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(ConfirmationCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(
                getSASLBindRequest().getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(LanguageCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(
                getSASLBindRequest().getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(NameCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(
                getSASLBindRequest().getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(PasswordCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(
                getSASLBindRequest().getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(RealmCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(
                getSASLBindRequest().getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(RealmChoiceCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(
                getSASLBindRequest().getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(TextInputCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(
                getSASLBindRequest().getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(TextOutputCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(
                getSASLBindRequest().getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }
}
