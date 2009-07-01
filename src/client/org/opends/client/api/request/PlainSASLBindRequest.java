package org.opends.client.api.request;

import org.opends.server.types.ByteString;
import static org.opends.server.util.ServerConstants.SASL_MECHANISM_PLAIN;
import org.opends.server.util.Validator;
import org.opends.common.api.DN;
import org.opends.client.api.NameCallbackHandler;
import org.opends.client.api.PasswordCallbackHandler;

import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.PasswordCallback;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 1, 2009
 * Time: 2:40:31 PM
 * To change this template use File | Settings | File Templates.
 */
public final class PlainSASLBindRequest extends AbstractSASLBindRequest
{
  private String authenticationID;
  private ByteString password;

  private NameCallbackHandler authIDHandler;
  private PasswordCallbackHandler passHandler;

  public PlainSASLBindRequest(String authenticationID,
                              ByteString password)
  {
    super(SASL_MECHANISM_PLAIN);
    Validator.ensureNotNull(authenticationID, password);
    this.authenticationID = authenticationID;
    this.password = password;
  }

  public PlainSASLBindRequest(DN authenticationDN,
                              ByteString password)
  {
    super(SASL_MECHANISM_PLAIN);
    Validator.ensureNotNull(authenticationDN, password);
    this.authenticationID = "dn:" + authenticationDN.toString();
    this.password = password;
  }


  public PlainSASLBindRequest(String authenticationID,
                              String authorizationID,
                              ByteString password)
  {
    super(SASL_MECHANISM_PLAIN, authorizationID);
    Validator.ensureNotNull(authenticationID, password);
    this.authenticationID = authenticationID;
    this.password = password;
  }

  public PlainSASLBindRequest(DN authenticationDN,
                              DN authorizationDN,
                              ByteString password)
  {
    super(SASL_MECHANISM_PLAIN, authorizationDN);
    Validator.ensureNotNull(authenticationDN, password);
    this.authenticationID = "dn:" + authenticationDN.toString();
    this.password = password;
  }

  public String getAuthorizationID() {
    return authorizationID;
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

  public PlainSASLBindRequest setAuthenticationID(
      String authenticationID)
  {
    Validator.ensureNotNull(authenticationID);
    this.authenticationID = authenticationID;
    return this;
  }

  public PlainSASLBindRequest setPassword(ByteString password)
  {
    Validator.ensureNotNull(password);
    this.password = password;
    return this;
  }

  public PlainSASLBindRequest setAuthIDHandler(
      NameCallbackHandler authIDHandler)
  {
    this.authIDHandler = authIDHandler;
    return this;
  }

  public PlainSASLBindRequest setPassHandler(
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
    buffer.append("PlainSASLBindRequest(bindDN=");
    buffer.append(getBindDN());
    buffer.append(", authentication=SASL");
    buffer.append(", saslMechanism=");
    buffer.append(saslMechanism);
    buffer.append(", authenticationID=");
    buffer.append(authenticationID);
    buffer.append(", authorizationID=");
    buffer.append(authorizationID);
    buffer.append(", password=");
    buffer.append(password);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
