package org.opends.common.utils;

import com.sun.grizzly.Buffer;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.io.UnsupportedEncodingException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 3, 2009 Time: 11:04:52
 * AM To change this template use File | Settings | File Templates.
 */
public class ByteArrayWrapper implements Buffer<byte[]>
{
  protected ByteBuffer visible;

  private ByteArrayWrapper(ByteBuffer visible)
  {
    this.visible = visible;
  }

  public static ByteArrayWrapper wrap(byte[] buffer)
  {
    return new ByteArrayWrapper(ByteBuffer.wrap(buffer));
  }

  public byte[] prepend(byte[] header)
  {
    checkDispose();
    return visible.array();
  }

  public void trim() {
    checkDispose() ;
    flip();
  }

  public void dispose() {
    checkDispose();
    visible = null;
  }

  public byte[] underlying() {
    checkDispose();
    return visible.array();
  }

  public int capacity() {
    return visible.capacity();
  }

  public int position() {
    return visible.position();
  }

  public ByteArrayWrapper position(int newPosition) {
    visible.position(newPosition);
    return this;
  }

  public int limit() {
    return visible.limit();
  }

  public ByteArrayWrapper limit(int newLimit) {
    visible.limit(newLimit);
    return this;
  }

  public ByteArrayWrapper mark() {
    visible.mark();
    return this;
  }

  public ByteArrayWrapper reset() {
    visible.reset();
    return this;
  }

  public ByteArrayWrapper clear() {
    visible.clear();
    return this;
  }

  public ByteArrayWrapper flip() {
    visible.flip();
    return this;
  }

  public ByteArrayWrapper rewind() {
    visible.rewind();
    return this;
  }

  public int remaining() {
    return visible.remaining();
  }

  public boolean hasRemaining() {
    return visible.hasRemaining();
  }

  public boolean isReadOnly() {
    return visible.isReadOnly();
  }

  public ByteArrayWrapper slice() {
    return new ByteArrayWrapper(visible.slice());
  }

  public ByteArrayWrapper duplicate() {
    return new ByteArrayWrapper(visible.duplicate());
  }

  public ByteArrayWrapper asReadOnlyBuffer() {
    visible.asReadOnlyBuffer();
    return this;
  }

  public byte get() {
    return visible.get();
  }

  public byte get(int index) {
    return visible.get(index);
  }

  public ByteArrayWrapper put(byte b) {
    visible.put(b);
    return this;
  }

  public ByteArrayWrapper put(int index, byte b) {
    visible.put(index, b);
    return this;
  }

  public ByteArrayWrapper get(byte[] dst) {
    visible.get(dst);
    return this;
  }

  public ByteArrayWrapper get(byte[] dst, int offset, int length) {
    visible.get(dst, offset, length);
    return this;
  }

  public ByteArrayWrapper put(Buffer src) {
    visible.put((ByteBuffer) src.underlying());
    return this;
  }

  public ByteArrayWrapper put(byte[] src) {
    visible.put(src);
    return this;
  }

  public ByteArrayWrapper put(byte[] src, int offset, int length) {
    visible.put(src, offset, length);
    return this;
  }

  public ByteArrayWrapper compact() {
    visible.compact();
    return this;
  }

  public ByteOrder order() {
    return visible.order();
  }

  public ByteArrayWrapper order(ByteOrder bo) {
    visible.order(bo);
    return this;
  }

  public char getChar() {
    return visible.getChar();
  }

  public char getChar(int index) {
    return visible.getChar(index);
  }

  public ByteArrayWrapper putChar(char value) {
    visible.putChar(value);
    return this;
  }

  public ByteArrayWrapper putChar(int index, char value) {
    visible.putChar(index, value);
    return this;
  }

  public short getShort() {
    return visible.getShort();
  }

  public short getShort(int index) {
    return visible.getShort(index);
  }

  public ByteArrayWrapper putShort(short value) {
    visible.putShort(value);
    return this;
  }

  public ByteArrayWrapper putShort(int index, short value) {
    visible.putShort(index, value);
    return this;
  }

  public int getInt() {
    return visible.getInt();
  }

  public int getInt(int index) {
    return visible.getInt(index);
  }

  public ByteArrayWrapper putInt(int value) {
    visible.putInt(value);
    return this;
  }

  public ByteArrayWrapper putInt(int index, int value) {
    visible.putInt(index, value);
    return this;
  }

  public long getLong() {
    return visible.getLong();
  }

  public long getLong(int index) {
    return visible.getLong(index);
  }

  public ByteArrayWrapper putLong(long value) {
    visible.putLong(value);
    return this;
  }

  public ByteArrayWrapper putLong(int index, long value) {
    visible.putLong(index, value);
    return this;
  }

  public float getFloat() {
    return visible.getFloat();
  }

  public float getFloat(int index) {
    return visible.getFloat(index);
  }

  public ByteArrayWrapper putFloat(float value) {
    visible.putFloat(value);
    return this;
  }

  public ByteArrayWrapper putFloat(int index, float value) {
    visible.putFloat(index, value);
    return this;
  }

  public double getDouble() {
    return visible.getDouble();
  }

  public double getDouble(int index) {
    return visible.getDouble(index);
  }

  public ByteArrayWrapper putDouble(double value) {
    visible.putDouble(value);
    return this;
  }

  public ByteArrayWrapper putDouble(int index, double value) {
    visible.putDouble(index, value);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ByteArrayWrapper " + super.hashCode() + "[");
    sb.append("visible=[").append(visible).append(']');
    sb.append(']');
    return sb.toString();
  }

  @Override
  public int hashCode() {
    return visible.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ByteArrayWrapper) {
      return visible.equals(((ByteArrayWrapper) obj).visible);
    }

    return false;
  }

  public int compareTo(Buffer<byte[]> buffer)
  {
    if(buffer instanceof ByteArrayWrapper)
    {
      return visible.compareTo(((ByteArrayWrapper)buffer).visible);
    }

    return visible.compareTo(ByteBuffer.wrap(buffer.underlying()));
  }

  private void checkDispose() {
    if (visible == null) {
      throw new IllegalStateException(
          "BufferWrapper has already been disposed") ;
    }
  }

  public String contentAsString(Charset charset) {
    checkDispose();

    // Working with charset name to support JDK1.5
    String charsetName = charset.name();
    try {
      if (visible.hasArray()) {
        return new String(visible.array(),
                          visible.position() + visible.arrayOffset(),
                          visible.remaining(), charsetName);
      } else {
        int oldPosition = visible.position();
        byte[] tmpBuffer = new byte[visible.remaining()];
        visible.get(tmpBuffer);
        visible.position(oldPosition);
        return new String(tmpBuffer, charsetName);
      }
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("Unexpected exception", e);
    }
  }
}
