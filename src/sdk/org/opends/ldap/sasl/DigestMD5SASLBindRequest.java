package org.opends.ldap.sasl;



import static org.opends.server.util.ServerConstants.*;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;
import org.opends.types.DN;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 1, 2009 Time: 3:42:18
 * PM To change this template use File | Settings | File Templates.
 */
public final class DigestMD5SASLBindRequest extends
    AbstractSASLBindRequest
{
  private SaslClient saslClient;
  private ByteString outgoingCredentials = null;

  private String authenticationID;
  private String authorizationID;
  private ByteString password;
  private String realm;

  private NameCallbackHandler authIDHandler;
  private PasswordCallbackHandler passHandler;
  private TextInputCallbackHandler realmHandler;



  public DigestMD5SASLBindRequest(DN authenticationDN,
      ByteString password)
  {
    Validator.ensureNotNull(authenticationDN, password);
    this.authenticationID = "dn:" + authenticationDN.toString();
    this.password = password;
  }



  public DigestMD5SASLBindRequest(DN authenticationDN,
      DN authorizationDN, ByteString password)
  {
    Validator
        .ensureNotNull(authenticationDN, authorizationDN, password);
    this.authenticationID = "dn:" + authenticationDN.toString();
    this.authorizationID = "dn:" + authorizationDN.toString();
    this.password = password;
  }



  public DigestMD5SASLBindRequest(DN authenticationDN,
      DN authorizationDN, ByteString password, String realm)
  {
    Validator.ensureNotNull(authenticationDN, authorizationDN,
        password, realm);
    this.authenticationID = "dn:" + authenticationDN.toString();
    this.authorizationID = "dn:" + authorizationDN.toString();
    this.password = password;
    this.realm = realm;
  }



  public DigestMD5SASLBindRequest(String authenticationID,
      ByteString password)
  {
    Validator.ensureNotNull(authenticationID, password);
    this.authenticationID = authenticationID;
    this.password = password;
  }



  public DigestMD5SASLBindRequest(String authenticationID,
      String authorizationID, ByteString password)
  {
    Validator
        .ensureNotNull(authenticationID, authorizationID, password);
    this.authenticationID = authenticationID;
    this.authorizationID = authorizationID;
    this.password = password;
  }



  public DigestMD5SASLBindRequest(String authenticationID,
      String authorizationID, ByteString password, String realm)
  {
    Validator.ensureNotNull(authenticationID, authorizationID,
        password, realm);
    this.authenticationID = authenticationID;
    this.authorizationID = authorizationID;
    this.password = password;
    this.realm = realm;
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
    if (bytes != null)
    {
      this.outgoingCredentials = ByteString.wrap(bytes);
    }
    else
    {
      this.outgoingCredentials = null;
    }

    return isComplete();
  }



  public String getAuthenticationID()
  {
    return authenticationID;
  }



  public NameCallbackHandler getAuthIDHandler()
  {
    return authIDHandler;
  }



  public String getAuthorizationID()
  {
    return authorizationID;
  }



  public PasswordCallbackHandler getPassHandler()
  {
    return passHandler;
  }



  public ByteString getPassword()
  {
    return password;
  }



  public String getRealm()
  {
    return realm;
  }



  public TextInputCallbackHandler getRealmHandler()
  {
    return realmHandler;
  }



  @Override
  public ByteString getSASLCredentials()
  {
    return outgoingCredentials;
  }



  @Override
  public String getSASLMechanism()
  {
    return saslClient.getMechanismName();
  }



  public void initialize(String serverName) throws SaslException
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put(Sasl.QOP, "auth-int");
    saslClient =
        Sasl.createSaslClient(
            new String[] { SASL_MECHANISM_DIGEST_MD5 },
            authorizationID, SASL_DEFAULT_PROTOCOL, serverName, props,
            this);

    if (saslClient.hasInitialResponse())
    {
      byte[] bytes = saslClient.evaluateChallenge(new byte[0]);
      if (bytes != null)
      {
        this.outgoingCredentials = ByteString.wrap(bytes);
      }
    }
  }



  public boolean isComplete()
  {
    return saslClient.isComplete();
  }



  public boolean isSecure()
  {
    String qop = (String) saslClient.getNegotiatedProperty(Sasl.QOP);
    return (qop.equalsIgnoreCase("auth-int") || qop
        .equalsIgnoreCase("auth-conf"));
  }



  public DigestMD5SASLBindRequest setAuthenticationID(
      String authenticationID)
  {
    Validator.ensureNotNull(authenticationID);
    this.authenticationID = authenticationID;
    return this;
  }



  public DigestMD5SASLBindRequest setAuthIDHandler(
      NameCallbackHandler authIDHandler)
  {
    this.authIDHandler = authIDHandler;
    return this;
  }



  public DigestMD5SASLBindRequest setAuthorizationID(
      String authorizationID)
  {
    this.authorizationID = authorizationID;
    return this;
  }



  public DigestMD5SASLBindRequest setPassHandler(
      PasswordCallbackHandler passHandler)
  {
    this.passHandler = passHandler;
    return this;
  }



  public DigestMD5SASLBindRequest setPassword(ByteString password)
  {
    Validator.ensureNotNull(password);
    this.password = password;
    return this;
  }



  public DigestMD5SASLBindRequest setRealm(String realm)
  {
    this.realm = realm;
    return this;
  }



  public void setRealmHandler(TextInputCallbackHandler realmHandler)
  {
    this.realmHandler = realmHandler;
  }



  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("DigestMD5SASLBindRequest(bindDN=");
    buffer.append(getBindDN());
    buffer.append(", authentication=SASL");
    buffer.append(", saslMechanism=");
    buffer.append(getSASLMechanism());
    buffer.append(", authenticationID=");
    buffer.append(authenticationID);
    buffer.append(", authorizationID=");
    buffer.append(authorizationID);
    buffer.append(", realm=");
    buffer.append(realm);
    buffer.append(", password=");
    buffer.append(password);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }



  @Override
  public byte[] unwrap(byte[] incoming, int offset, int len)
      throws SaslException
  {
    return saslClient.unwrap(incoming, offset, len);
  }



  @Override
  public byte[] wrap(byte[] outgoing, int offset, int len)
      throws SaslException
  {
    return saslClient.wrap(outgoing, offset, len);
  }



  @Override
  protected void handle(NameCallback callback)
      throws UnsupportedCallbackException
  {
    if (authIDHandler == null)
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
    if (passHandler == null)
    {
      callback.setPassword(password.toString().toCharArray());
    }
    else
    {
      passHandler.handle(callback);
    }
  }



  @Override
  protected void handle(RealmCallback callback)
      throws UnsupportedCallbackException
  {
    if (realmHandler == null)
    {
      if (realm == null)
      {
        callback.setText(callback.getDefaultText());
      }
      else
      {
        callback.setText(realm);
      }
    }
    else
    {
      realmHandler.handle(callback);
    }
  }
}
