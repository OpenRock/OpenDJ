package org.opends.common.protocols.ldap;

import org.opends.common.protocols.ldap.asn1.ASN1StreamReader;
import org.opends.common.protocols.ldap.asn1.ASN1StreamWriter;
import org.opends.server.protocols.asn1.ASN1Exception;

import java.io.IOException;

import com.sun.grizzly.filterchain.*;
import com.sun.grizzly.utils.ConcurrentQueuePool;
import com.sun.grizzly.streams.StreamReader;
import com.sun.grizzly.streams.StreamWriter;
import com.sun.grizzly.Connection;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 27, 2009 Time: 6:54:49
 * PM To change this template use File | Settings | File Templates.
 */
public abstract class AbstractLDAPTransport
    implements PatternFilterChainFactory
{
  private FilterChain defaultFilterChain;
  private int maxASN1ElementSize = 0;

  private ASN1ReaderPool asn1ReaderPool;
  private ASN1WriterPool asn1WriterPool;

  public AbstractLDAPTransport(FilterChainEnabledTransport nioTransport)
  {
    this.defaultFilterChain = nioTransport.getFilterChain();
    this.defaultFilterChain.add(new TransportFilter());
    this.defaultFilterChain.add(new LDAPFilter());

    this.asn1ReaderPool = new ASN1ReaderPool();
    this.asn1WriterPool = new ASN1WriterPool();
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
          try
          {
            LDAPDecoder.decode(asn1Reader, handler);
          }
          catch(LDAPProtocolException le)
          {
            if(le.isDisconnect()) return ctx.getStopAction();
          }
        }
        while(asn1Reader.hasNextElement());
      }
      catch(ASN1Exception ae)
      {
        return ctx.getStopAction();
      }
      finally
      {
        releaseASN1Reader(asn1Reader);
      }

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
    // Do nothing for now.
  }

  protected abstract LDAPMessageHandler getMessageHandler(Connection connection);
}
