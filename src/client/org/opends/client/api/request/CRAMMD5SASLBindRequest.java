package org.opends.client.api.request;

import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;
import static org.opends.server.util.ServerConstants.SASL_MECHANISM_CRAM_MD5;
import org.opends.client.api.NameCallbackHandler;
import org.opends.client.api.PasswordCallbackHandler;
import org.opends.common.api.DN;

import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.PasswordCallback;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 1, 2009
 * Time: 3:34:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class CRAMMD5SASLBindRequest extends AbstractSASLBindRequest
{
  private String authenticationID;
  private ByteString password;

  private NameCallbackHandler authIDHandler;
  private PasswordCallbackHandler passHandler;

  public CRAMMD5SASLBindRequest(String authenticationID,
                                ByteString password)
  {
    super(SASL_MECHANISM_CRAM_MD5);
    Validator.ensureNotNull(authenticationID, password);
    this.authenticationID = authenticationID;
    this.password = password;
  }

  public CRAMMD5SASLBindRequest(DN authenticationDN,
                                ByteString password)
  {
    super(SASL_MECHANISM_CRAM_MD5);
    Validator.ensureNotNull(authenticationDN, password);
    this.authenticationID = "dn:" + authenticationDN.toString();
    this.password = password;
  }

  public String getAuthenticationID() {
    return authenticationID;
  }

  public ByteString getPassword() {
    return password;
  }

  public NameCallbackHandler getAuthIDHandler() {
    return authIDHandler;
  }

  public PasswordCallbackHandler getPassHandler() {
    return passHandler;
  }

  public CRAMMD5SASLBindRequest setAuthenticationID(
      String authenticationID)
  {
    Validator.ensureNotNull(authenticationID);
    this.authenticationID = authenticationID;
    return this;
  }

  public CRAMMD5SASLBindRequest setPassword(ByteString password)
  {
    Validator.ensureNotNull(password);
    this.password = password;
    return this;
  }

  public CRAMMD5SASLBindRequest setAuthIDHandler(
      NameCallbackHandler authIDHandler)
  {
    this.authIDHandler = authIDHandler;
    return this;
  }

  public CRAMMD5SASLBindRequest setPassHandler(
      PasswordCallbackHandler passHandler)
  {
    this.passHandler = passHandler;
    return this;
  }

  @Override
  protected void handle(NameCallback callback)
      throws UnsupportedCallbackException
  {
    if(authIDHandler == null)
    {
      callback.setName(authenticationID);
    }
    else
    {
      authIDHandler.handle(callback);
    }
  }

  @Override
  protected void handle(PasswordCallback callback)
      throws UnsupportedCallbackException
  {
    if(passHandler == null)
    {
      callback.setPassword(password.toString().toCharArray());
    }
    else
    {
      passHandler.handle(callback);
    }
  }

  public void toString(StringBuilder buffer) {
    buffer.append("CRAMMD5SASLBindRequest(bindDN=");
    buffer.append(getBindDN());
    buffer.append(", authentication=SASL");
    buffer.append(", saslMechanism=");
    buffer.append(saslMechanism);
    buffer.append(", authenticationID=");
    buffer.append(authenticationID);
    buffer.append(", password=");
    buffer.append(password);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
