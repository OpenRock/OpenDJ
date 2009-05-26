package org.opends.common.protocols.ldap.asn1;

import static org.opends.messages.ProtocolMessages.*;
import static org.opends.server.loggers.debug.DebugLogger.*;
import static org.opends.server.protocols.ldap.LDAPConstants.*;

import java.io.IOException;

import org.opends.messages.Message;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;
import org.opends.server.types.DebugLogLevel;
import org.opends.server.protocols.asn1.ASN1Exception;
import org.opends.server.protocols.asn1.ASN1Reader;
import org.glassfish.grizzly.streams.StreamReader;
import java.nio.BufferUnderflowException;

public class ASN1StreamReader implements ASN1Reader
{
  private static final DebugTracer TRACER = getTracer();
  private static final int MAX_STRING_BUFFER_SIZE = 1024;

  private int state = ELEMENT_READ_STATE_NEED_TYPE;
  private byte peekType = 0;
  private int peekLength = -1;
  private int lengthBytesNeeded = 0;
  private final int maxElementSize;

  private StreamReader streamReader;
  private SequenceLimiter readLimiter;
  private byte[] buffer;

  private interface SequenceLimiter
  {
    public SequenceLimiter endSequence() throws ASN1Exception;
    public SequenceLimiter startSequence(int readLimit)
        throws ASN1Exception;
    public void checkLimit(int readSize)
        throws IOException, BufferUnderflowException;
    public int remaining();
  }

  class RootSequenceLimiter implements SequenceLimiter
  {
    private ChildSequenceLimiter child;

    public ChildSequenceLimiter endSequence() throws ASN1Exception
    {
      Message message = ERR_ASN1_SEQUENCE_READ_NOT_STARTED.get();
      throw new ASN1Exception(message);
    }

    public ChildSequenceLimiter startSequence(int readLimit)
    {
      if(child == null)
      {
        child = new ChildSequenceLimiter();
        child.parent = this;
      }

      child.readLimit = readLimit;

      return child;
    }

    public void checkLimit(int readSize)
    {}

    public int remaining()
    {
      return streamReader.availableDataSize();
    }
  }

  class ChildSequenceLimiter implements SequenceLimiter
  {
    private SequenceLimiter parent;
    private ChildSequenceLimiter child;
    private int readLimit;
    private int bytesRead;

    public SequenceLimiter endSequence() throws ASN1Exception
    {
      bytesRead = 0;

      try
      {
        parent.checkLimit(remaining());
        for(int i = 0; i < remaining(); i++)
        {
          streamReader.readByte();
        }
      }
      catch(Exception ioe)
      {
        Message message =
            ERR_ASN1_READ_ERROR.get(ioe.toString());
        throw new ASN1Exception(message, ioe);
      }

      return parent;
    }

    public ChildSequenceLimiter startSequence(int readLimit)
    {
      if(child == null)
      {
        child = new ChildSequenceLimiter();
      }

      child.readLimit = readLimit;
      child.bytesRead = 0;

      return child;
    }

    public void checkLimit(int readSize)
        throws IOException, BufferUnderflowException
    {
      if(readLimit > 0 && bytesRead + readSize > readLimit)
      {
        throw new BufferUnderflowException();
      }

      parent.checkLimit(readSize);

      bytesRead += readSize;
    }

    public int remaining()
    {
      return readLimit - bytesRead;
    }
  }


  /**
   * Creates a new ASN1 reader whose source is the provided input
   * stream and having a user defined maximum BER element size.
   *
   * @param stream
   *          The stream reader to be read from.
   * @param maxElementSize
   *          The maximum BER element size, or <code>0</code> to
   *          indicate that there is no limit.
   */
  ASN1StreamReader(StreamReader stream, int maxElementSize)
  {
    this.streamReader = stream;
    this.readLimiter = new RootSequenceLimiter();
    this.buffer = new byte[MAX_STRING_BUFFER_SIZE];
    this.maxElementSize = maxElementSize;
  }

  /**
   * Determines if a complete ASN.1 element is ready to be read from the
   * stream reader.
   *
   * @return <code>true</code> if another complete element is available or
   *         <code>false</code> otherwise.
   * @throws ASN1Exception If an error occurs while trying to decode
   *                       an ASN1 element.
   */
  public boolean elementAvailable() throws ASN1Exception
  {
    try
    {
      if(state == ELEMENT_READ_STATE_NEED_TYPE &&
          !needTypeState(true)) {
        return false;
      }
      if(state == ELEMENT_READ_STATE_NEED_FIRST_LENGTH_BYTE &&
          !needFirstLengthByteState(true)) {
        return false;
      }
      if(state == ELEMENT_READ_STATE_NEED_ADDITIONAL_LENGTH_BYTES &&
          !needAdditionalLengthBytesState(true)) {
        return false;
      }

      return peekLength <= readLimiter.remaining();
    }
    catch(Exception ioe)
    {
      Message message =
          ERR_ASN1_READ_ERROR.get(ioe.toString());
      throw new ASN1Exception(message, ioe);
    }
  }

  /**
   * Determines if the input stream contains at least one ASN.1 element to
   * be read.
   *
   * @return <code>true</code> if another element is available or
   *         <code>false</code> otherwise.
   * @throws ASN1Exception If an error occurs while trying to decode
   *                       an ASN1 element.
   */
  public boolean hasNextElement() throws ASN1Exception
  {
    try
    {
      return state != ELEMENT_READ_STATE_NEED_TYPE ||
          needTypeState(true);
    }
    catch(Exception ioe)
    {
      Message message =
          ERR_ASN1_READ_ERROR.get(ioe.toString());
      throw new ASN1Exception(message, ioe);
    }
  }

  /**
   * Internal helper method reading the ASN.1 type byte and transition to
   * the next state if successful.
   *
   * @param ensureRead <code>true</code>  to check for availability first.
   * @return <code>true</code> if the type byte was successfully read
   * @throws IOException If an error occurs while reading from the stream.
   * @throws ASN1Exception If an error occurs while trying to decode
   *                       an ASN1 element.
   */
  private boolean needTypeState(boolean ensureRead)
      throws IOException, ASN1Exception
  {
    // Read just the type.
    if(ensureRead && readLimiter.remaining() <= 0)
    {
      return false;
    }

    readLimiter.checkLimit(1);
    peekType = streamReader.readByte();
    state = ELEMENT_READ_STATE_NEED_FIRST_LENGTH_BYTE;
    return true;
  }

  /**
   * Internal helper method reading the first length bytes and transition to
   * the next state if successful.
   *
   * @param ensureRead <code>true</code> to check for availability first.
   * @return <code>true</code> if the length bytes was successfully read
   * @throws IOException If an error occurs while reading from the stream.
   * @throws ASN1Exception If an error occurs while trying to decode
   *                       an ASN1 element.
   */
  private boolean needFirstLengthByteState(boolean ensureRead)
      throws IOException, ASN1Exception
  {
    if(ensureRead && readLimiter.remaining() <= 0)
    {
      return false;
    }

    readLimiter.checkLimit(1);
    byte readByte = streamReader.readByte();
    peekLength = (readByte & 0x7F);
    if (peekLength != readByte)
    {
      lengthBytesNeeded = peekLength;
      if (lengthBytesNeeded > 4)
      {
        Message message =
            ERR_ASN1_INVALID_NUM_LENGTH_BYTES.get(lengthBytesNeeded);
        throw new ASN1Exception(message);
      }
      peekLength = 0x00;

      if(ensureRead && readLimiter.remaining() < lengthBytesNeeded)
      {
        state = ELEMENT_READ_STATE_NEED_ADDITIONAL_LENGTH_BYTES;
        return false;
      }

      readLimiter.checkLimit(lengthBytesNeeded);
      while(lengthBytesNeeded > 0)
      {
        readByte = streamReader.readByte();
        peekLength = (peekLength << 8) | (readByte & 0xFF);
        lengthBytesNeeded--;
      }
    }

    // Make sure that the element is not larger than the maximum allowed
    // message size.
    if ((maxElementSize > 0) && (peekLength > maxElementSize))
    {
      Message m = ERR_LDAP_CLIENT_DECODE_MAX_REQUEST_SIZE_EXCEEDED.get(
          peekLength, maxElementSize);
      throw new ASN1Exception(m);
    }
    state = ELEMENT_READ_STATE_NEED_VALUE_BYTES;
    return true;
  }

  /**
   * Internal helper method reading the additional ASN.1 length bytes and
   * transition to the next state if successful.
   *
   * @param ensureRead <code>true</code> to check for availability first.
   * @return <code>true</code> if the length bytes was successfully read.
   * @throws IOException If an error occurs while reading from the stream.
   * @throws ASN1Exception If an error occurs while trying to decode
   *                       an ASN1 element.
   */
  private boolean needAdditionalLengthBytesState(boolean ensureRead)
      throws IOException, ASN1Exception
  {
    if(ensureRead && readLimiter.remaining() < lengthBytesNeeded)
    {
      return false;
    }

    byte readByte;
    readLimiter.checkLimit(lengthBytesNeeded);
    while(lengthBytesNeeded > 0)
    {
      readByte = streamReader.readByte();
      peekLength = (peekLength << 8) | (readByte & 0xFF);
      lengthBytesNeeded--;
    }

    // Make sure that the element is not larger than the maximum allowed
    // message size.
    if ((maxElementSize > 0) && (peekLength > maxElementSize))
    {
      Message m = ERR_LDAP_CLIENT_DECODE_MAX_REQUEST_SIZE_EXCEEDED.get(
          peekLength, maxElementSize);
      throw new ASN1Exception(m);
    }
    state = ELEMENT_READ_STATE_NEED_VALUE_BYTES;
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public byte peekType() throws ASN1Exception
  {
    try
    {
      if(state == ELEMENT_READ_STATE_NEED_TYPE)
      {
        needTypeState(false);
      }

      return peekType;
    }
    catch(Exception ioe)
    {
      Message message =
          ERR_ASN1_READ_ERROR.get(ioe.toString());
      throw new ASN1Exception(message, ioe);
    }
  }

  /**
   * {@inheritDoc}
   */
  public int peekLength() throws ASN1Exception
  {
    peekType();

    try
    {
      switch(state)
      {
        case ELEMENT_READ_STATE_NEED_FIRST_LENGTH_BYTE:
          needFirstLengthByteState(false);
          break;

        case ELEMENT_READ_STATE_NEED_ADDITIONAL_LENGTH_BYTES:
          needAdditionalLengthBytesState(false);
      }

      return peekLength;
    }
    catch(Exception ioe)
    {
      Message message =
          ERR_ASN1_READ_ERROR.get(ioe.toString());
      throw new ASN1Exception(message, ioe);
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean readBoolean() throws ASN1Exception
  {
    // Read the header if haven't done so already
    peekLength();

    if (peekLength != 1)
    {
      Message message =
          ERR_ASN1_BOOLEAN_INVALID_LENGTH.get(peekLength);
      throw new ASN1Exception(message);
    }

    try
    {
      readLimiter.checkLimit(peekLength);
      byte readByte = streamReader.readByte();

      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("READ ASN.1 BOOLEAN(type=0x%x, length=%d, value=%s)",
                peekType, peekLength, String.valueOf(readByte != 0x00)));
      }

      state = ELEMENT_READ_STATE_NEED_TYPE;
      return readByte != 0x00;
    }
    catch(Exception ioe)
    {
      Message message =
          ERR_ASN1_READ_ERROR.get(ioe.toString());
      throw new ASN1Exception(message, ioe);
    }
  }

  /**
   * {@inheritDoc}
   */
  public int readEnumerated() throws ASN1Exception
  {
    // Read the header if haven't done so already
    peekLength();

    if ((peekLength < 1) || (peekLength > 4))
    {
      Message message = ERR_ASN1_INTEGER_INVALID_LENGTH.get(peekLength);
      throw new ASN1Exception(message);
    }

    // From an implementation point of view, an enumerated value is
    // equivalent to an integer.
    return (int) readInteger();
  }

  /**
   * {@inheritDoc}
   */
  public long readInteger() throws ASN1Exception
  {
    // Read the header if haven't done so already
    peekLength();

    if ((peekLength < 1) || (peekLength > 8))
    {
      Message message =
          ERR_ASN1_INTEGER_INVALID_LENGTH.get(peekLength);
      throw new ASN1Exception(message);
    }

    try
    {
      readLimiter.checkLimit(peekLength);
      if(peekLength > 4)
      {
        long longValue = 0;
        for (int i=0; i < peekLength; i++)
        {
          int readByte = streamReader.readByte();
          if(readByte == -1)
          {
            Message message =
                ERR_ASN1_INTEGER_TRUNCATED_VALUE.get(peekLength);
            throw new ASN1Exception(message);
          }
          if(i == 0 && ((byte)readByte) < 0)
          {
            longValue = 0xFFFFFFFFFFFFFFFFL;
          }
          longValue = (longValue << 8) | (readByte & 0xFF);
        }

        state = ELEMENT_READ_STATE_NEED_TYPE;
        return longValue;
      }
      else
      {
        int intValue = 0;
        for (int i=0; i < peekLength; i++)
        {
          int readByte = streamReader.readByte();
          if(readByte == -1)
          {
            Message message =
                ERR_ASN1_INTEGER_TRUNCATED_VALUE.get(peekLength);
            throw new ASN1Exception(message);
          }
          if (i == 0 && ((byte)readByte) < 0)
          {
            intValue = 0xFFFFFFFF;
          }
          intValue = (intValue << 8) | (readByte & 0xFF);
        }

        if(debugEnabled())
        {
          TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
              String.format("READ ASN.1 INTEGER(type=0x%x, length=%d, " +
                  "value=%d)", peekType, peekLength, intValue));
        }

        state = ELEMENT_READ_STATE_NEED_TYPE;
        return intValue;
      }
    }
    catch(Exception ioe)
    {
      Message message =
          ERR_ASN1_READ_ERROR.get(ioe.toString());
      throw new ASN1Exception(message, ioe);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void readNull() throws ASN1Exception
  {
    // Read the header if haven't done so already
    peekLength();

    // Make sure that the decoded length is exactly zero byte.
    if (peekLength != 0)
    {
      Message message =
          ERR_ASN1_NULL_INVALID_LENGTH.get(peekLength);
      throw new ASN1Exception(message);
    }

    if(debugEnabled())
    {
      TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
          String.format("READ ASN.1 NULL(type=0x%x, length=%d)",
              peekType, peekLength));
    }

    state = ELEMENT_READ_STATE_NEED_TYPE;
  }

  /**
   * {@inheritDoc}
   */
  public ByteString readOctetString() throws ASN1Exception {
    // Read the header if haven't done so already
    peekLength();

    if(peekLength == 0)
    {
      state = ELEMENT_READ_STATE_NEED_TYPE;
      return ByteString.empty();
    }

    try
    {
      readLimiter.checkLimit(peekLength);
      // Copy the value and construct the element to return.
      byte[] value = new byte[peekLength];
      streamReader.readByteArray(value);

      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("READ ASN.1 OCTETSTRING(type=0x%x, length=%d)",
                peekType, peekLength));
      }

      state = ELEMENT_READ_STATE_NEED_TYPE;
      return ByteString.wrap(value);
    }
    catch(Exception ioe)
    {
      Message message =
          ERR_ASN1_READ_ERROR.get(ioe.toString());
      throw new ASN1Exception(message, ioe);
    }
  }

  /**
   * {@inheritDoc}
   */
  public String readOctetStringAsString() throws ASN1Exception
  {
    // We could cache the UTF-8 CharSet if performance proves to be an
    // issue.
    return readOctetStringAsString("UTF-8");
  }

  /**
   * {@inheritDoc}
   */
  public String readOctetStringAsString(String charSet) throws ASN1Exception
  {
    // Read the header if haven't done so already
    peekLength();

    if(peekLength == 0)
    {
      state = ELEMENT_READ_STATE_NEED_TYPE;
      return "";
    }

    byte[] readBuffer;
    if(peekLength <= buffer.length)
    {
      readBuffer = buffer;
    }
    else
    {
      readBuffer = new byte[peekLength];
    }

    try
    {
      readLimiter.checkLimit(peekLength);
      streamReader.readByteArray(readBuffer, 0, peekLength);

      state = ELEMENT_READ_STATE_NEED_TYPE;
    }
    catch(Exception ioe)
    {
      Message message =
          ERR_ASN1_READ_ERROR.get(ioe.toString());
      throw new ASN1Exception(message, ioe);
    }

    String str;
    try
    {
      str = new String(buffer, 0, peekLength, charSet);
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      str = new String(buffer, 0, peekLength);
    }

    if(debugEnabled())
    {
      TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
          String.format("READ ASN.1 OCTETSTRING(type=0x%x, length=%d, " +
              "value=%s)", peekType, peekLength, str));
    }

    return str;
  }

  /**
   * {@inheritDoc}
   */
  public void readOctetString(ByteStringBuilder buffer) throws ASN1Exception
  {
    // Read the header if haven't done so already
    peekLength();

    if(peekLength == 0)
    {
      state = ELEMENT_READ_STATE_NEED_TYPE;
      return;
    }

    try
    {
      readLimiter.checkLimit(peekLength);
      // Copy the value and construct the element to return.
      buffer.append(streamReader, peekLength);

      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("READ ASN.1 OCTETSTRING(type=0x%x, length=%d)",
                peekType, peekLength));
      }

      state = ELEMENT_READ_STATE_NEED_TYPE;
    }
    catch(Exception ioe)
    {
      Message message =
          ERR_ASN1_READ_ERROR.get(ioe.toString());
      throw new ASN1Exception(message, ioe);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void readStartSequence() throws ASN1Exception
  {
    // Read the header if haven't done so already
    peekLength();

    readLimiter = readLimiter.startSequence(peekLength);

    if(debugEnabled())
    {
      TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
          String.format("READ ASN.1 SEQUENCE(type=0x%x, length=%d)",
              peekType, peekLength));
    }

    // Reset the state
    state = ELEMENT_READ_STATE_NEED_TYPE;
  }

  /**
   * {@inheritDoc}
   */
  public void readStartSet() throws ASN1Exception
  {
    // From an implementation point of view, a set is equivalent to a
    // sequence.
    readStartSequence();
  }

  /**
   * {@inheritDoc}
   */
  public void readEndSequence() throws ASN1Exception
  {
    readLimiter = readLimiter.endSequence();

    // Reset the state
    state = ELEMENT_READ_STATE_NEED_TYPE;
  }

  /**
   * {@inheritDoc}
   */
  public void readEndSet() throws ASN1Exception
  {
    // From an implementation point of view, a set is equivalent to a
    // sequence.
    readEndSequence();
  }

  /**
   * {@inheritDoc}
   */
  public void skipElement() throws ASN1Exception
  {
    // Read the header if haven't done so already
    peekLength();

    try
    {
      readLimiter.checkLimit(peekLength);
      for(int i = 0; i < peekLength; i++)
      {
        streamReader.readByte();
      }
      state = ELEMENT_READ_STATE_NEED_TYPE;
    }
    catch(Exception ioe)
    {
      Message message =
          ERR_ASN1_READ_ERROR.get(ioe.toString());
      throw new ASN1Exception(message, ioe);
    }
  }

  /**
   * Closes this ASN.1 reader and the underlying stream.
   *
   * @throws IOException if an I/O error occurs
   */
  public void close() throws IOException
  {
    // close the stream reader.
    streamReader.close();
  }
}
