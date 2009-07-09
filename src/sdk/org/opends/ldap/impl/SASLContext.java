package org.opends.ldap.impl;



import javax.security.sasl.SaslException;

import org.opends.server.types.ByteString;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 1, 2009 Time: 12:43:50
 * PM To change this template use File | Settings | File Templates.
 */
public interface SASLContext
{
  public void dispose() throws SaslException;



  public boolean evaluateCredentials(ByteString saslCredentials)
      throws SaslException;



  public String getSASLMechanism();



  public void initialize(String serverName) throws SaslException;



  public boolean isComplete();



  public boolean isSecure();



  public byte[] unwrap(byte[] incoming, int offset, int len)
      throws SaslException;



  public byte[] wrap(byte[] outgoing, int offset, int len)
      throws SaslException;
}
