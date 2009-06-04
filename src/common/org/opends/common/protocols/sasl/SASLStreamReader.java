package org.opends.common.protocols.sasl;

import com.sun.grizzly.streams.StreamReaderDecorator;
import com.sun.grizzly.streams.StreamReader;
import com.sun.grizzly.Buffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.security.sasl.SaslException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 3, 2009 Time: 10:24:36
 * AM To change this template use File | Settings | File Templates.
 */
public class SASLStreamReader extends StreamReaderDecorator
{
  private SASLFilter saslFilter;

  public SASLStreamReader(StreamReader underlyingReader, SASLFilter saslFilter)
  {
    super(underlyingReader);
    this.saslFilter = saslFilter;
  }

  @Override
  protected Buffer read0() throws IOException
  {
    return underlyingReader.readBuffer();
  }

  @Override
  public boolean appendBuffer(Buffer buffer) {
    if (buffer == null) return false;

    Buffer appBuffer;
    try {
      appBuffer = saslFilter.unwrap(buffer, getConnection());
    } catch (SaslException e) {
      throw new IllegalStateException(e);
    }
    while(!super.appendBuffer(appBuffer));

    buffer.dispose();

    return true;
  }

  @Override
  protected final Object wrap(Buffer buffer) {
    return buffer;
  }

  @Override
  protected Buffer unwrap(Object data) {
    return (Buffer) data;
  }
}
