package org.opends.ldap.sasl;



import static org.opends.messages.ExtensionMessages.*;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.LanguageCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.RealmChoiceCallback;
import javax.security.sasl.SaslException;




/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 1, 2009 Time: 12:49:50
 * PM To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSASLBindRequest extends SASLBindRequest
    implements SASLContext, CallbackHandler
{
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
            INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(),
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
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(ChoiceCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(ConfirmationCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(LanguageCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(NameCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(PasswordCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(RealmCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(RealmChoiceCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(TextInputCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }



  protected void handle(TextOutputCallback callback)
      throws UnsupportedCallbackException
  {
    org.opends.messages.Message message =
        INFO_SASL_UNSUPPORTED_CALLBACK.get(getSASLMechanism(), String
            .valueOf(callback));
    throw new UnsupportedCallbackException(callback, message.toString());
  }
}
