package org.opends.common.protocols.ldap;

import org.opends.common.protocols.asn1.ASN1StreamReader;
import org.opends.common.protocols.asn1.ASN1StreamWriter;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;

import com.sun.grizzly.filterchain.*;
import com.sun.grizzly.utils.ConcurrentQueuePool;
import com.sun.grizzly.streams.StreamReader;
import com.sun.grizzly.streams.StreamWriter;
import com.sun.grizzly.Connection;

import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 27, 2009 Time: 6:54:49
 * PM To change this template use File | Settings | File Templates.
 */
public abstract class AbstractLDAPTransport
{
  private final SSLContext sslContext;
  private PatternFilterChainFactory defaultFilterChainFactory;
  private int maxASN1ElementSize = 0;

  private TrustManager trustManager;
  private KeyManager keyManager;

  private ASN1ReaderPool asn1ReaderPool;
  private ASN1WriterPool asn1WriterPool;

  protected AbstractLDAPTransport(FilterChainEnabledTransport nioTransport)
  {
    this.defaultFilterChainFactory =
        new DefaultFilterChainFactory(nioTransport);

    this.asn1ReaderPool = new ASN1ReaderPool();
    this.asn1WriterPool = new ASN1WriterPool();

    try
    {
      this.sslContext = SSLContext.getInstance("TLSv1");
      this.sslContext.init(null, null, null);
    }
    catch(Exception e)
    {
      // This should never happen. Running with non Sun JVM?
      throw new RuntimeException("Failed to initialize SSLContext: "+ e);
    }
  }

  private class DefaultFilterChainFactory implements PatternFilterChainFactory
  {
    private FilterChain defaultFilterChain;

    private DefaultFilterChainFactory(FilterChainEnabledTransport nioTransport)
    {
      this.defaultFilterChain = nioTransport.getFilterChain();
      this.defaultFilterChain.add(new TransportFilter());
      this.defaultFilterChain.add(new LDAPFilter());
    }

    public FilterChain getFilterChainPattern()
    {
      return defaultFilterChain;
    }

    public void setFilterChainPattern(FilterChain chain)
    {
      defaultFilterChain = chain;
    }

    public FilterChain create()
    {
      FilterChain filterChain = new DefaultFilterChain(this);
      filterChain.addAll(defaultFilterChain);
      return filterChain;
    }

    public void release(FilterChain chain)
    {
      // TODO: Nothing yet.
    }
  }

  private class ASN1ReaderPool extends ConcurrentQueuePool<ASN1StreamReader>
  {
    public ASN1StreamReader newInstance()
    {
      return new ASN1StreamReader(maxASN1ElementSize);
    }
  }

  private class ASN1WriterPool extends ConcurrentQueuePool<ASN1StreamWriter>
  {
    public ASN1StreamWriter newInstance()
    {
      return new ASN1StreamWriter();
    }
  }

  private class LDAPFilter extends FilterAdapter
  {
    @Override
    public NextAction handleRead(FilterChainContext ctx, NextAction nextAction)
        throws IOException
    {
      Connection connection = ctx.getConnection();
      StreamReader streamReader = ctx.getStreamReader();
      LDAPMessageHandler handler = getMessageHandler(connection);
      ASN1StreamReader asn1Reader = getASN1Reader(streamReader);

      try
      {
        do
        {
          LDAPDecoder.decode(asn1Reader, handler);
        }
        while(asn1Reader.hasNextElement());
      }
      finally
      {
        releaseASN1Reader(asn1Reader);
      }

      return nextAction;
    }

    @Override
    public void exceptionOccurred(FilterChainContext ctx, Throwable error)
    {
      Connection connection = ctx.getConnection();
      LDAPMessageHandler handler = getMessageHandler(connection);
      handler.handleException(error);
    }

    @Override
    public NextAction handleClose(FilterChainContext ctx, NextAction nextAction)
        throws IOException
    {
      Connection connection = ctx.getConnection();
      LDAPMessageHandler handler = getMessageHandler(connection);
      handler.handleClose();
      return nextAction;
    }


  }

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

  public ASN1StreamWriter getASN1Writer(StreamWriter streamWriter)
  {
    ASN1StreamWriter asn1Writer = asn1WriterPool.poll();
    asn1Writer.setStreamWriter(streamWriter);
    return asn1Writer;
  }

  public void releaseASN1Writer(ASN1StreamWriter asn1Writer)
  {
    asn1WriterPool.offer(asn1Writer);
  }

  public PatternFilterChainFactory getDefaultFilterChainFactory()
  {
    return defaultFilterChainFactory;
  }

  public void setTrustManager(TrustManager trustManager)
      throws KeyManagementException
  {
    initSSLContext(trustManager, keyManager);
    this.trustManager = trustManager;
    // TODO: Refresh existing SSLFilter. What about existin connections?
  }

  public TrustManager getTrustManager()
  {
    return trustManager;
  }

  public void setKeyManager(KeyManager keyManager)
      throws GeneralSecurityException
  {
   initSSLContext(trustManager, keyManager);
    this.keyManager = keyManager;
    // TODO: Refresh existing SSLFilter. What about existin connections?
  }

  public KeyManager getKeyManager()
  {
    return keyManager;
  }

  protected SSLContext getSSLContext()
  {
    return sslContext;
  }

  private void initSSLContext(TrustManager trustManager, KeyManager keyManager)
      throws KeyManagementException
  {
    KeyManager[] km = null;
    TrustManager[] tm = null;

    if (trustManager != null)
    {
      tm = new TrustManager[] { trustManager };
    }
    if (keyManager != null)
    {
      km = new KeyManager[] { keyManager };
    }
    sslContext.init(km, tm, null);
  }

  protected abstract LDAPMessageHandler getMessageHandler(Connection connection);
}
