package org.opends.client.protocol.ldap;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 27, 2009 Time: 9:47:34
 * AM To change this template use File | Settings | File Templates.
 */
public interface ConnectionFactory
{
  LDAPConnection getConnection() throws IOException;
}
