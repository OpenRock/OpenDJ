package org.opends.common.protocols.sasl;

import com.sun.grizzly.streams.StreamWriterDecorator;
import com.sun.grizzly.streams.StreamReader;
import com.sun.grizzly.streams.StreamWriter;
import com.sun.grizzly.Buffer;
import com.sun.grizzly.CompletionHandler;
import com.sun.grizzly.impl.FutureImpl;
import com.sun.grizzly.impl.ReadyFutureImpl;
import com.sun.grizzly.ssl.SSLUtils;

import java.io.IOException;
import java.util.concurrent.Future;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 3, 2009 Time: 1:40:18
 * PM To change this template use File | Settings | File Templates.
 */
public class SASLStreamWriter extends StreamWriterDecorator
{
  private SASLFilter saslFilter;

  public SASLStreamWriter(StreamWriter underlyingWriter, SASLFilter saslFilter)
  {
    super(underlyingWriter);
    this.saslFilter = saslFilter;
  }

  @Override
  protected Future<Integer> flush0(Buffer buffer,
                                   CompletionHandler<Integer> completionHandler)
      throws IOException
  {
    Future lastWriterFuture = null;

    if (buffer != null) {
      buffer.flip();

      Buffer underlyingBuffer = underlyingWriter.getBuffer();
      byte[] netBuffer = saslFilter.wrap(buffer, getConnection());
      int remaining = netBuffer.length;
      while(remaining > 0)
      {
        int writeSize = Math.min(remaining,
            underlyingBuffer.remaining());
        underlyingBuffer.put(netBuffer, netBuffer.length - remaining,
            writeSize);
        lastWriterFuture = underlyingWriter.flush();
        remaining -= writeSize;
      }
      buffer.clear();
    }

    return lastWriterFuture;
  }
}
