package org.opends.ldap;



import com.sun.grizzly.filterchain.Filter;
import com.sun.grizzly.nio.transport.TCPNIOTransport;
import com.sun.grizzly.ssl.SSLFilter;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 2, 2009 Time:
 * 4:36:32 PM To change this template use File | Settings | File
 * Templates.
 */
public class LDAPSConnectionFactory extends LDAPConnectionFactory
{
  public LDAPSConnectionFactory(String host, int port,
      TCPNIOTransport tcpTransport)
  {
    super(host, port, tcpTransport);

    // Install the SSLFilter in the default filter chain
    Filter oldFilter =
        getDefaultFilterChainFactory().getFilterChainPattern()
            .remove(2);
    getDefaultFilterChainFactory().getFilterChainPattern().add(
        getSSLFilter());
    if (!(oldFilter instanceof SSLFilter))
    {
      getDefaultFilterChainFactory().getFilterChainPattern().add(
          oldFilter);
    }
  }
}
