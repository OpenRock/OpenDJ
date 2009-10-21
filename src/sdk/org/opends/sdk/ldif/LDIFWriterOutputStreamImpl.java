package org.opends.sdk.ldif;



import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;



/**
 * LDIF output stream writer implementation.
 */
final class LDIFWriterOutputStreamImpl implements LDIFWriterImpl
{

  private final BufferedWriter writer;



  /**
   * Creates a new LDIF output stream writer.
   * 
   * @param out
   *          The output stream.
   */
  LDIFWriterOutputStreamImpl(OutputStream out)
  {
    this.writer = new BufferedWriter(new OutputStreamWriter(out));
  }



  /**
   * {@inheritDoc}
   */
  public void close() throws IOException
  {
    writer.close();
  }



  /**
   * {@inheritDoc}
   */
  public void flush() throws IOException
  {
    writer.flush();
  }



  /**
   * {@inheritDoc}
   */
  public void print(CharSequence s) throws IOException
  {
    writer.append(s);
  }



  /**
   * {@inheritDoc}
   */
  public void println() throws IOException
  {
    writer.newLine();
  }
}
