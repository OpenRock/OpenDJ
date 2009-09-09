package org.opends.sdk.ldap;



import java.io.IOException;

import javax.security.sasl.SaslException;

import com.sun.grizzly.Buffer;
import com.sun.grizzly.streams.StreamReader;
import com.sun.grizzly.streams.StreamReaderDecorator;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 3, 2009 Time:
 * 10:24:36 AM To change this template use File | Settings | File
 * Templates.
 */
public class SASLStreamReader extends StreamReaderDecorator
{
  private final SASLFilter saslFilter;



  public SASLStreamReader(StreamReader underlyingReader,
      SASLFilter saslFilter)
  {
    super(underlyingReader);
    this.saslFilter = saslFilter;
  }



  @Override
  public boolean appendBuffer(Buffer buffer)
  {
    if (buffer == null)
    {
      return false;
    }

    byte[] appBuffer;
    try
    {
      appBuffer = saslFilter.unwrap(buffer, getConnection());
    }
    catch (SaslException e)
    {
      throw new IllegalStateException(e);
    }

    if (appBuffer.length == 0)
    {
      return false;
    }

    Buffer newBuffer = newBuffer(appBuffer.length);
    newBuffer.put(appBuffer);

    if (super.appendBuffer(newBuffer))
    {
      buffer.dispose();
      return true;
    }

    return false;
  }



  @Override
  protected Buffer read0() throws IOException
  {
    return underlyingReader.readBuffer();
  }



  @Override
  protected Buffer unwrap(Object data)
  {
    return (Buffer) data;
  }



  @Override
  protected final Object wrap(Buffer buffer)
  {
    return buffer;
  }
}
