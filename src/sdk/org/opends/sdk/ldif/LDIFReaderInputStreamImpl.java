package org.opends.sdk.ldif;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;



/**
 * LDIF output stream writer implementation.
 */
final class LDIFReaderInputStreamImpl implements LDIFReaderImpl
{

  private final BufferedReader reader;



  /**
   * Creates a new LDIF input stream reader implementation.
   * 
   * @param in
   *          The input stream to use.
   */
  LDIFReaderInputStreamImpl(InputStream in)
  {
    this.reader = new BufferedReader(new InputStreamReader(in));
  }



  /**
   * {@inheritDoc}
   */
  public void close() throws IOException
  {
    reader.close();
  }



  /**
   *{@inheritDoc}
   */
  public String readLine() throws IOException
  {
    return reader.readLine();
  }
}
