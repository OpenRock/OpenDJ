package org.opends.asn1;



import java.io.Closeable;
import java.io.IOException;

import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;



/**
 * An interface for decoding ASN.1 elements from a data source.
 * <p>
 * Methods for creating {@link ASN1Reader}s are provided in the
 * {@link org.opends.server.protocols.asn1.ASN1} class.
 */
public interface ASN1Reader extends Closeable
{

  /**
   * Determines if a complete ASN.1 element is waiting to be read.
   * 
   * @return <code>true</code> if another complete element is available
   *         or <code>false</code> otherwise.
   * @throws IOException
   *           If an error occurs while trying to decode an ASN1
   *           element.
   */
  public boolean elementAvailable() throws IOException;



  public void readStartSequence(byte expectedTag) throws IOException;



  public void readStartSet(byte expectedTag) throws IOException;



  /**
   * Closes this ASN.1 reader.
   * 
   * @throws IOException
   *           if an I/O error occurs
   */
  void close() throws IOException;



  /**
   * Determines if at least one ASN.1 element is waiting to be read.
   * 
   * @return <code>true</code> if another element is available or
   *         <code>false</code> if the EOF is reached.
   * @throws IOException
   *           If an error occurs while trying to decode an ASN1
   *           element.
   */
  boolean hasNextElement() throws IOException;



  /**
   * Gets the data length of the next element without actually reading
   * the element and advancing the cursor.
   * 
   * @return The data length of the next element or -1 if the EOF is
   *         encountered.
   * @throws IOException
   *           If an error occurs while determining the length.
   */
  int peekLength() throws IOException;



  /**
   * Gets the BER type of the next element without actually reading the
   * element and advancing the cursor.
   * 
   * @return The BER type of the next element or -1 if the EOF is
   *         encountered.
   * @throws IOException
   *           If an error occurs while determining the BER type.
   */
  byte peekType() throws IOException;



  /**
   * Reads the next ASN.1 element as a boolean and advance the cursor.
   * 
   * @return The decoded boolean value.
   * @throws IOException
   *           If the element cannot be decoded as a boolean.
   */
  boolean readBoolean() throws IOException;



  boolean readBoolean(byte expectedTag) throws IOException;



  /**
   * Finishes reading a sequence. Any elements not read in the sequence
   * will be discarded.
   * 
   * @throws IOException
   *           If an error occurs while advancing to the end of the
   *           sequence.
   */
  void readEndSequence() throws IOException;



  /**
   * Finishes reading a set. Any elements not read in the set will be
   * discarded.
   * 
   * @throws IOException
   *           If an error occurs while advancing to the end of the set.
   */
  void readEndSet() throws IOException;



  /**
   * Reads the next ASN.1 element as an enumerated value and advances
   * the cursor.
   * 
   * @return The decoded enumerated value.
   * @throws IOException
   *           If the element cannot be decoded as an enumerated value.
   */
  int readEnumerated() throws IOException;



  int readEnumerated(byte expectedTag) throws IOException;



  /**
   * Reads the next ASN.1 element as an integer and advances the cursor.
   * 
   * @return The decoded integer value.
   * @throws IOException
   *           If the element cannot be decoded as a integer.
   */
  long readInteger() throws IOException;



  long readInteger(byte expectedTag) throws IOException;



  /**
   * Reads the next ASN.1 element as a null element and advances the
   * cursor.
   * 
   * @throws IOException
   *           If the element cannot be decoded as an null element.
   */
  void readNull() throws IOException;



  void readNull(byte expectedTag) throws IOException;



  /**
   * Reads the next ASN.1 element as an octet string and advances the
   * cursor.
   * 
   * @return The decoded octet string value represented using a
   *         {@link ByteString}.
   * @throws IOException
   *           If the element cannot be decoded as an octet string.
   */
  ByteString readOctetString() throws IOException;



  ByteString readOctetString(byte expectedTag) throws IOException;



  void readOctetString(byte expectedTag, ByteStringBuilder buffer)
      throws IOException;



  /**
   * Reads the next ASN.1 element as an octet string and advances the
   * cursor. The data will be appended to the provided
   * {@link ByteStringBuilder}.
   * 
   * @param buffer
   *          The {@link ByteStringBuilder} to append the data to.
   * @throws IOException
   *           If the element cannot be decoded as an octet string.
   */
  void readOctetString(ByteStringBuilder buffer) throws IOException;



  /**
   * Reads the next ASN.1 element as an octet string and advances the
   * cursor. The data will be decoded to a UTF-8 string. This method is
   * equivalent to:
   * 
   * <pre>
   * readOctetStringAsString(&quot;UTF-8&quot;);
   * </pre>
   * 
   * @return The string representation of the octet string data.
   * @throws IOException
   *           If the element cannot be decoded as an octet string.
   */
  String readOctetStringAsString() throws IOException;



  String readOctetStringAsString(byte expectedTag) throws IOException;



  String readOctetStringAsString(byte expectedTag, String charSet)
      throws IOException;



  /**
   * Reads the next ASN.1 element as an octet string and advances the
   * cursor. The data will be decoded to a string using the provided
   * character set.
   * 
   * @param charSet
   *          The character set to use in order to decode the data into
   *          a string.
   * @return The string representation of the octet string data.
   * @throws IOException
   *           If the element cannot be decoded as an octet string.
   */
  String readOctetStringAsString(String charSet) throws IOException;



  /**
   * Reads the next ASN.1 element as a sequence. All further reads will
   * read the elements in the sequence until {@link #readEndSequence()}
   * is called.
   * 
   * @throws IOException
   *           If the next element is not a sequence.
   */
  void readStartSequence() throws IOException;



  /**
   * Reads the next ASN.1 element as a set. All further reads will read
   * the elements in the sequence until {@link #readEndSet()} is called.
   * 
   * @throws IOException
   *           If the next element is not a set.
   */
  void readStartSet() throws IOException;



  /**
   * Advances this ASN.1 reader beyond the next ASN.1 element without
   * decoding it.
   * 
   * @throws IOException
   *           If the next ASN.1 element could not be skipped.
   */
  void skipElement() throws IOException;
}
