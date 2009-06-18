package org.opends.client.protocol.ldap;

import com.sun.grizzly.filterchain.Filter;
import com.sun.grizzly.ssl.SSLFilter;
import com.sun.grizzly.ssl.SSLStreamReader;
import com.sun.grizzly.ssl.SSLStreamWriter;
import com.sun.grizzly.nio.transport.TCPNIOTransport;
import com.sun.grizzly.Connection;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 2, 2009 Time: 4:36:32
 * PM To change this template use File | Settings | File Templates.
 */
public class LDAPSConnectionFactory extends LDAPConnectionFactory
{
  public LDAPSConnectionFactory(String host, int port,
                                TCPNIOTransport tcpTransport)
  {
    super(host, port, tcpTransport);
    
    // Install the SSLFilter in the default filter chain
    Filter oldFilter =
        getDefaultFilterChainFactory().getFilterChainPattern().remove(2);
    getDefaultFilterChainFactory().getFilterChainPattern().add(getSSLFilter());
    if(!(oldFilter instanceof SSLFilter))
    {
      getDefaultFilterChainFactory().getFilterChainPattern().add(oldFilter);
    }
  }
}
