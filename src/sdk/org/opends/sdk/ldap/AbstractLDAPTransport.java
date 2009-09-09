package org.opends.sdk.ldap;



import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.opends.sdk.ConnectionOptions;

import com.sun.grizzly.Connection;
import com.sun.grizzly.filterchain.DefaultFilterChain;
import com.sun.grizzly.filterchain.FilterAdapter;
import com.sun.grizzly.filterchain.FilterChain;
import com.sun.grizzly.filterchain.FilterChainContext;
import com.sun.grizzly.filterchain.FilterChainEnabledTransport;
import com.sun.grizzly.filterchain.NextAction;
import com.sun.grizzly.filterchain.PatternFilterChainFactory;
import com.sun.grizzly.filterchain.TransportFilter;
import com.sun.grizzly.streams.StreamReader;
import com.sun.grizzly.streams.StreamWriter;
import com.sun.grizzly.utils.ConcurrentQueuePool;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 27, 2009 Time:
 * 6:54:49 PM To change this template use File | Settings | File
 * Templates.
 */
public abstract class AbstractLDAPTransport
{
  private class ASN1ReaderPool extends
      ConcurrentQueuePool<ASN1StreamReader>
  {
    @Override
    public ASN1StreamReader newInstance()
    {
      return new ASN1StreamReader(maxASN1ElementSize);
    }
  }

  private class ASN1WriterPool extends
      ConcurrentQueuePool<ASN1StreamWriter>
  {
    @Override
    public ASN1StreamWriter newInstance()
    {
      return new ASN1StreamWriter();
    }
  }

  private class DefaultFilterChainFactory implements
      PatternFilterChainFactory
  {
    private FilterChain defaultFilterChain;



    private DefaultFilterChainFactory(
        FilterChainEnabledTransport nioTransport)
    {
      this.defaultFilterChain = nioTransport.getFilterChain();
      this.defaultFilterChain.add(new MonitorFilter());
      this.defaultFilterChain.add(new TransportFilter());
      this.defaultFilterChain.add(new LDAPFilter());
    }



    public FilterChain create()
    {
      FilterChain filterChain = new DefaultFilterChain(this);
      filterChain.addAll(defaultFilterChain);
      return filterChain;
    }



    public FilterChain getFilterChainPattern()
    {
      return defaultFilterChain;
    }



    public void release(FilterChain chain)
    {
      // TODO: Nothing yet.
    }



    public void setFilterChainPattern(FilterChain chain)
    {
      defaultFilterChain = chain;
    }
  }

  private class LDAPFilter extends FilterAdapter
  {
    @Override
    public NextAction handleRead(FilterChainContext ctx,
        NextAction nextAction) throws IOException
    {
      Connection<?> connection = ctx.getConnection();
      StreamReader streamReader = ctx.getStreamReader();
      LDAPMessageHandler handler = getMessageHandler(connection);
      ASN1StreamReader asn1Reader = getASN1Reader(streamReader);

      try
      {
        do
        {
          LDAPDecoder.decode(asn1Reader, handler);
        }
        while (asn1Reader.hasNextElement());
      }
      finally
      {
        releaseASN1Reader(asn1Reader);
      }

      return nextAction;
    }
  }

  private class MonitorFilter extends FilterAdapter
  {
    @Override
    public void exceptionOccurred(FilterChainContext ctx,
        Throwable error)
    {
      Connection<?> connection = ctx.getConnection();
      LDAPMessageHandler handler = getMessageHandler(connection);
      handler.handleException(error);
    }



    @Override
    public NextAction handleClose(FilterChainContext ctx,
        NextAction nextAction) throws IOException
    {
      Connection<?> connection = ctx.getConnection();
      removeMessageHandler(connection);
      return nextAction;
    }
  }



  private final SSLContext sslContext;
  private final PatternFilterChainFactory defaultFilterChainFactory;

  private final int maxASN1ElementSize = 0;

  private final TrustManager trustManager;

  private final KeyManager keyManager;

  private final ASN1ReaderPool asn1ReaderPool;

  private final ASN1WriterPool asn1WriterPool;



  protected AbstractLDAPTransport(ConnectionOptions options,
      FilterChainEnabledTransport transport)
      throws KeyManagementException
  {
    this.defaultFilterChainFactory =
        new DefaultFilterChainFactory(transport);

    this.asn1ReaderPool = new ASN1ReaderPool();
    this.asn1WriterPool = new ASN1WriterPool();

    this.trustManager = options.getTrustManager();
    this.keyManager = options.getKeyManager();

    TrustManager[] tm = null;
    if (trustManager != null)
    {
      tm = new TrustManager[] { trustManager };
    }

    KeyManager[] km = null;
    if (keyManager != null)
    {
      km = new KeyManager[] { keyManager };
    }

    try
    {
      this.sslContext = SSLContext.getInstance("TLSv1");
    }
    catch (NoSuchAlgorithmException e)
    {
      // If TLSv1 is not supported then we are in real trouble.
      throw new RuntimeException("TLSv1 not support by this JVM", e);
    }

    this.sslContext.init(km, tm, null);
  }



  public ASN1StreamWriter getASN1Writer(StreamWriter streamWriter)
  {
    ASN1StreamWriter asn1Writer = asn1WriterPool.poll();
    asn1Writer.setStreamWriter(streamWriter);
    return asn1Writer;
  }



  public PatternFilterChainFactory getDefaultFilterChainFactory()
  {
    return defaultFilterChainFactory;
  }



  public void releaseASN1Writer(ASN1StreamWriter asn1Writer)
  {
    asn1WriterPool.offer(asn1Writer);
  }



  protected abstract LDAPMessageHandler getMessageHandler(
      Connection<?> connection);



  protected SSLContext getSSLContext()
  {
    return sslContext;
  }



  protected abstract LDAPMessageHandler removeMessageHandler(
      Connection<?> connection);



  private ASN1StreamReader getASN1Reader(StreamReader streamReader)
  {
    ASN1StreamReader asn1Reader = asn1ReaderPool.poll();
    asn1Reader.setStreamReader(streamReader);
    return asn1Reader;
  }



  private void releaseASN1Reader(ASN1StreamReader asn1Reader)
  {
    asn1ReaderPool.offer(asn1Reader);
  }
}
