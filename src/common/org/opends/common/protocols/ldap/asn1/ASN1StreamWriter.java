package org.opends.common.protocols.ldap.asn1;

import org.opends.server.protocols.asn1.ASN1Writer;
import static org.opends.server.protocols.asn1.ASN1Constants.*;
import org.opends.server.types.ByteStringBuilder;
import org.opends.server.types.DebugLogLevel;
import org.opends.server.types.ByteSequence;
import org.opends.server.loggers.debug.DebugTracer;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import org.opends.server.util.StaticUtils;

import java.io.IOException;

import com.sun.grizzly.streams.StreamWriter;
import com.sun.grizzly.utils.PoolableObject;

public class ASN1StreamWriter implements ASN1Writer, PoolableObject
{
  private static final DebugTracer TRACER = getTracer();
  private static final int SUB_SEQUENCE_BUFFER_INIT_SIZE = 1024;

  private StreamWriter streamWriter;
  private SequenceBuffer sequenceBuffer;
  private final RootSequenceBuffer rootBuffer;

  private interface SequenceBuffer
  {
    public SequenceBuffer endSequence() throws IOException;
    public SequenceBuffer startSequence(byte type) throws IOException;
    public void writeByte(byte b) throws IOException;
    public void writeByteArray(byte[] bs, int offset, int length)
        throws IOException;
    public void writeByteSequence(ByteSequence byteSequence)
        throws IOException;
  }

  private class RootSequenceBuffer implements SequenceBuffer
  {
    private ChildSequenceBuffer child;

    public SequenceBuffer endSequence() throws IOException
    {
      throw new IOException("Sequence not started");
    }

    public SequenceBuffer startSequence(byte type) throws IOException
    {
      if(child == null)
      {
        child = new ChildSequenceBuffer();
        child.parent = this;
      }

      streamWriter.writeByte(type);
      child.buffer.clear();

      return child;
    }

    public void writeByte(byte b)
        throws IOException
    {
      streamWriter.writeByte(b);
    }

    public void writeByteArray(byte[] bs, int offset, int length)
        throws IOException
    {
      streamWriter.writeByteArray(bs, offset, length);
    }

    public void writeByteSequence(ByteSequence byteSequence)
        throws IOException
    {
      byteSequence.copyTo(streamWriter);
    }
  }

  private class ChildSequenceBuffer implements SequenceBuffer
  {
    private SequenceBuffer parent;
    private ChildSequenceBuffer child;
    private ByteStringBuilder buffer =
        new ByteStringBuilder(SUB_SEQUENCE_BUFFER_INIT_SIZE);

    public SequenceBuffer endSequence() throws IOException
    {
      writeLength(parent, buffer.length());
      parent.writeByteArray(buffer.getBackingArray(), 0, buffer.length());

      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("WRITE ASN.1 END SEQUENCE(length=%d)",
                buffer.length()));
      }

      return parent;
    }

    public SequenceBuffer startSequence(byte type) throws IOException
    {
      if(child == null)
      {
        child = new ChildSequenceBuffer();
        child.parent = this;
      }

      buffer.append(type);
      child.buffer.clear();

      return child;
    }

    public void writeByte(byte b) throws IOException {
      buffer.append(b);
    }

    public void writeByteArray(byte[] bs, int offset, int length)
        throws IOException
    {
      buffer.append(bs, offset, length);
    }

    public void writeByteSequence(ByteSequence byteSequence)
        throws IOException
    {
      byteSequence.copyTo(buffer);
    }
  }

  /**
   * Creates a new ASN.1 writer that writes to a StreamWriter.
   */
  public ASN1StreamWriter()
  {
    this.sequenceBuffer = this.rootBuffer = new RootSequenceBuffer();
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeInteger(int intValue) throws IOException
  {
    return writeInteger(UNIVERSAL_INTEGER_TYPE, intValue);
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeInteger(byte type, int intValue) throws IOException
  {
    sequenceBuffer.writeByte(type);
    if ((intValue < 0 && ((intValue & 0xFFFFFF80) == 0xFFFFFF80)) ||
        (intValue & 0x0000007F) == intValue)
    {
      writeLength(sequenceBuffer, 1);
      sequenceBuffer.writeByte((byte) (intValue & 0xFF));
      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("WRITE ASN.1 INTEGER(type=0x%x, length=%d, value=%d)",
                type, 1, intValue));
      }
    }
    else if ((intValue < 0 && ((intValue & 0xFFFF8000) == 0xFFFF8000)) ||
        (intValue & 0x00007FFF) == intValue)
    {
      writeLength(sequenceBuffer, 2);
      sequenceBuffer.writeByte((byte) ((intValue >> 8) & 0xFF));
      sequenceBuffer.writeByte((byte) (intValue & 0xFF));
      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("WRITE ASN.1 INTEGER(type=0x%x, length=%d, value=%d)",
                type, 2, intValue));
      }
    }
    else if ((intValue < 0 && ((intValue & 0xFF800000) == 0xFF800000)) ||
        (intValue & 0x007FFFFF) == intValue)
    {
      writeLength(sequenceBuffer, 3);
      sequenceBuffer.writeByte((byte) ((intValue >> 16) & 0xFF));
      sequenceBuffer.writeByte((byte) ((intValue >>  8) & 0xFF));
      sequenceBuffer.writeByte((byte) (intValue & 0xFF));
      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("WRITE ASN.1 INTEGER(type=0x%x, length=%d, value=%d)",
                type, 3, intValue));
      }
    }
    else
    {
      writeLength(sequenceBuffer, 4);
      sequenceBuffer.writeByte((byte) ((intValue >> 24) & 0xFF));
      sequenceBuffer.writeByte((byte) ((intValue >> 16) & 0xFF));
      sequenceBuffer.writeByte((byte) ((intValue >>  8) & 0xFF));
      sequenceBuffer.writeByte((byte) (intValue & 0xFF));
      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("WRITE ASN.1 INTEGER(type=0x%x, length=%d, value=%d)",
                type, 4, intValue));
      }
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeInteger(long longValue) throws IOException
  {
    return writeInteger(UNIVERSAL_INTEGER_TYPE, longValue);
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeInteger(byte type, long longValue) throws IOException
  {
    sequenceBuffer.writeByte(type);
    if ((longValue < 0 &&
         ((longValue & 0xFFFFFFFFFFFFFF80L) == 0xFFFFFFFFFFFFFF80L)) ||
        (longValue & 0x000000000000007FL) == longValue)
    {
      writeLength(sequenceBuffer, 1);
      sequenceBuffer.writeByte((byte) (longValue & 0xFF));
      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("WRITE ASN.1 INTEGER(type=0x%x, length=%d, value=%d)",
                type, 1, longValue));
      }
    }
    else if ((longValue < 0 &&
              ((longValue & 0xFFFFFFFFFFFF8000L) == 0xFFFFFFFFFFFF8000L)) ||
             (longValue & 0x0000000000007FFFL) == longValue)
    {
      writeLength(sequenceBuffer, 2);
      sequenceBuffer.writeByte((byte) ((longValue >> 8) & 0xFF));
      sequenceBuffer.writeByte((byte) (longValue & 0xFF));
      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("WRITE ASN.1 INTEGER(type=0x%x, length=%d, value=%d)",
                type, 2, longValue));
      }
    }
    else if ((longValue < 0 &&
              ((longValue & 0xFFFFFFFFFF800000L) == 0xFFFFFFFFFF800000L)) ||
             (longValue & 0x00000000007FFFFFL) == longValue)
    {
      writeLength(sequenceBuffer, 3);
      sequenceBuffer.writeByte((byte) ((longValue >> 16) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >>  8) & 0xFF));
      sequenceBuffer.writeByte((byte) (longValue & 0xFF));
      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("WRITE ASN.1 INTEGER(type=0x%x, length=%d, value=%d)",
                type, 3, longValue));
      }
    }
    else if ((longValue < 0 &&
              ((longValue & 0xFFFFFFFF80000000L) == 0xFFFFFFFF80000000L)) ||
             (longValue & 0x000000007FFFFFFFL) == longValue)
    {
      writeLength(sequenceBuffer, 4);
      sequenceBuffer.writeByte((byte) ((longValue >> 24) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >> 16) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >>  8) & 0xFF));
      sequenceBuffer.writeByte((byte) (longValue & 0xFF));
      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("WRITE ASN.1 INTEGER(type=0x%x, length=%d, value=%d)",
                type, 4, longValue));
      }
    }
    else if ((longValue < 0 &&
              ((longValue & 0xFFFFFF8000000000L) == 0xFFFFFF8000000000L)) ||
             (longValue & 0x0000007FFFFFFFFFL) == longValue)
    {
      writeLength(sequenceBuffer, 5);
      sequenceBuffer.writeByte((byte) ((longValue >> 32) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >> 24) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >> 16) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >>  8) & 0xFF));
      sequenceBuffer.writeByte((byte) (longValue & 0xFF));
      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("WRITE ASN.1 INTEGER(type=0x%x, length=%d, value=%d)",
                type, 5, longValue));
      }
    }
    else if ((longValue < 0 &&
              ((longValue & 0xFFFF800000000000L) == 0xFFFF800000000000L)) ||
             (longValue & 0x00007FFFFFFFFFFFL) == longValue)
    {
      writeLength(sequenceBuffer, 6);
      sequenceBuffer.writeByte((byte) ((longValue >> 40) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >> 32) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >> 24) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >> 16) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >>  8) & 0xFF));
      sequenceBuffer.writeByte((byte) (longValue & 0xFF));
      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("WRITE ASN.1 INTEGER(type=0x%x, length=%d, value=%d)",
                type, 6, longValue));
      }
    }
    else if ((longValue < 0 &&
              ((longValue & 0xFF80000000000000L) == 0xFF80000000000000L)) ||
             (longValue & 0x007FFFFFFFFFFFFFL) == longValue)
    {
      writeLength(sequenceBuffer, 7);
      sequenceBuffer.writeByte((byte) ((longValue >> 48) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >> 40) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >> 32) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >> 24) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >> 16) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >>  8) & 0xFF));
      sequenceBuffer.writeByte((byte) (longValue & 0xFF));
      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("WRITE ASN.1 INTEGER(type=0x%x, length=%d, value=%d)",
                type, 7, longValue));
      }
    }
    else
    {
      writeLength(sequenceBuffer, 8);
      sequenceBuffer.writeByte((byte) ((longValue >> 56) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >> 48) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >> 40) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >> 32) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >> 24) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >> 16) & 0xFF));
      sequenceBuffer.writeByte((byte) ((longValue >>  8) & 0xFF));
      sequenceBuffer.writeByte((byte) (longValue & 0xFF));
      if(debugEnabled())
      {
        TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
            String.format("WRITE ASN.1 INTEGER(type=0x%x, length=%d, value=%d)",
                type, 8, longValue));
      }
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeEnumerated(int intValue) throws IOException {
    return writeInteger(UNIVERSAL_ENUMERATED_TYPE, intValue);
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeBoolean(boolean booleanValue) throws IOException
  {
    return writeBoolean(UNIVERSAL_BOOLEAN_TYPE, booleanValue);
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeBoolean(byte type, boolean booleanValue)
      throws IOException
  {
    sequenceBuffer.writeByte(type);
    writeLength(sequenceBuffer, 1);
    sequenceBuffer.writeByte(booleanValue ? BOOLEAN_VALUE_TRUE : BOOLEAN_VALUE_FALSE);

    if(debugEnabled())
    {
      TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
          String.format("WRITE ASN.1 BOOLEAN(type=0x%x, length=%d, value=%s)",
              type, 1, String.valueOf(booleanValue)));
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeNull(byte type) throws IOException
  {
    sequenceBuffer.writeByte(type);
    writeLength(sequenceBuffer, 0);

    if(debugEnabled())
    {
      TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
          String.format("WRITE ASN.1 NULL(type=0x%x, length=%d)",
              type, 0));
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeNull() throws IOException
  {
    return writeNull(UNIVERSAL_NULL_TYPE);
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeOctetString(byte type, byte[] value,
                                     int offset, int length)
      throws IOException
  {
    sequenceBuffer.writeByte(type);
    writeLength(sequenceBuffer, length);
    sequenceBuffer.writeByteArray(value, offset, length);

    if(debugEnabled())
    {
      TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
          String.format("WRITE ASN.1 OCTETSTRING(type=0x%x, length=%d)",
              type, length));
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeOctetString(byte[] value, int offset, int length)
      throws IOException
  {
    return writeOctetString(UNIVERSAL_OCTET_STRING_TYPE, value, offset, length);
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeOctetString(byte type, String value) throws IOException
  {
    sequenceBuffer.writeByte(type);

    if(value == null)
    {
      writeLength(sequenceBuffer, 0);
      return this;
    }

    byte[] bytes = StaticUtils.getBytes(value);
    writeLength(sequenceBuffer, bytes.length);
    sequenceBuffer.writeByteArray(bytes, 0, bytes.length);

    if(debugEnabled())
    {
      TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
          String.format("WRITE ASN.1 OCTETSTRING(type=0x%x, length=%d, " +
              "value=%s)", type, bytes.length, value));
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeOctetString(String value) throws IOException
  {
    return writeOctetString(UNIVERSAL_OCTET_STRING_TYPE, value);
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeOctetString(byte type, ByteSequence value)
      throws IOException {
    sequenceBuffer.writeByte(type);
    writeLength(sequenceBuffer, value.length());
    sequenceBuffer.writeByteSequence(value);

    if(debugEnabled())
    {
      TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
          String.format("WRITE ASN.1 OCTETSTRING(type=0x%x, length=%d)",
              type, value.length()));
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeOctetString(ByteSequence value) throws IOException {
    return writeOctetString(UNIVERSAL_OCTET_STRING_TYPE, value);
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeStartSet() throws IOException
  {
    return writeStartSet(UNIVERSAL_SET_TYPE);
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeStartSet(byte type) throws IOException
  {
    // From an implementation point of view, a set is equivalent to a
    // sequence.
    return writeStartSequence(type);
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeStartSequence() throws IOException
  {
    return writeStartSequence(UNIVERSAL_SEQUENCE_TYPE);
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeStartSequence(byte type) throws IOException
  {
    // Get a child sequence buffer
    sequenceBuffer = sequenceBuffer.startSequence(type);

    if(debugEnabled())
    {
      TRACER.debugProtocolElement(DebugLogLevel.VERBOSE,
          String.format("WRITE ASN.1 START SEQUENCE(type=0x%x)",
              type));
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeEndSet() throws IOException
  {
    return writeEndSequence();
  }

  /**
   * {@inheritDoc}
   */
  public ASN1Writer writeEndSequence() throws IOException
  {
    sequenceBuffer = sequenceBuffer.endSequence();

    return this;
  }

  /**
   * Closes this ASN.1 writer and the underlying outputstream. Any unfinished
   * sequences will be ended.
   *
   * @throws IOException if an error occurs while closing the stream.
   */
  public void close() throws IOException {
    streamWriter.close();
  }

  /**
   * Flushes the stream.
   *
   * @throws IOException If an I/O error occurs
   */
  public void flush() throws IOException {
    streamWriter.flush();
  }

  /**
   * Writes the provided value for use as the length of an ASN.1 element.
   *
   * @param buffer The sequence buffer to write to.
   * @param  length  The length to encode for use in an ASN.1 element.
   * @throws IOException if an error occurs while writing.
   */
  private void writeLength(SequenceBuffer buffer, int length) throws IOException
  {
    if (length < 128)
    {
      buffer.writeByte((byte) length);
    }
    else if ((length & 0x000000FF) == length)
    {
      buffer.writeByte((byte) 0x81);
      buffer.writeByte((byte) (length & 0xFF));
    }
    else if ((length & 0x0000FFFF) == length)
    {
      buffer.writeByte((byte) 0x82);
      buffer.writeByte((byte) ((length >> 8) & 0xFF));
      buffer.writeByte((byte) (length & 0xFF));
    }
    else if ((length & 0x00FFFFFF) == length)
    {
      buffer.writeByte((byte) 0x83);
      buffer.writeByte((byte) ((length >> 16) & 0xFF));
      buffer.writeByte((byte) ((length >>  8) & 0xFF));
      buffer.writeByte((byte) (length & 0xFF));
    }
    else
    {
      buffer.writeByte((byte) 0x84);
      buffer.writeByte((byte) ((length >> 24) & 0xFF));
      buffer.writeByte((byte) ((length >> 16) & 0xFF));
      buffer.writeByte((byte) ((length >>  8) & 0xFF));
      buffer.writeByte((byte) (length & 0xFF));
    }
  }

  public void setStreamWriter(StreamWriter streamWriter)
  {
    this.streamWriter = streamWriter;
  }

  public void prepare()
  {
    // nothing to do
  }

  public void release()
  {
    streamWriter = null;
    sequenceBuffer = rootBuffer;
  }
}
