package org.opends.sdk.ldif;



import java.io.IOException;
import java.util.List;



/**
 * LDIF string list writer implementation.
 */
final class LDIFWriterListImpl implements LDIFWriterImpl
{

  private final StringBuilder builder = new StringBuilder();

  private final List<String> ldifLines;



  /**
   * Creates a new LDIF list writer.
   * 
   * @param ldifLines
   *          The string list.
   */
  LDIFWriterListImpl(List<String> ldifLines)
  {
    this.ldifLines = ldifLines;
  }



  /**
   * {@inheritDoc}
   */
  public void close() throws IOException
  {
    // Nothing to do.
  }



  /**
   * {@inheritDoc}
   */
  public void flush() throws IOException
  {
    // Nothing to do.
  }



  /**
   * {@inheritDoc}
   */
  public void print(CharSequence s) throws IOException
  {
    builder.append(s);
  }



  /**
   * {@inheritDoc}
   */
  public void println() throws IOException
  {
    ldifLines.add(builder.toString());
    builder.setLength(0);
  }
}
