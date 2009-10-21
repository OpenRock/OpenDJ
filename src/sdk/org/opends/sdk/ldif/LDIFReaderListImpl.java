package org.opends.sdk.ldif;



import java.io.IOException;
import java.util.Iterator;
import java.util.List;



/**
 * LDIF output stream writer implementation.
 */
final class LDIFReaderListImpl implements LDIFReaderImpl
{

  private final Iterator<String> iterator;



  /**
   * Creates a new LDIF list reader.
   * 
   * @param ldifLines
   *          The string list.
   */
  LDIFReaderListImpl(List<String> ldifLines)
  {
    this.iterator = ldifLines.iterator();
  }



  /**
   * {@inheritDoc}
   */
  public void close() throws IOException
  {
    // Nothing to do.
  }



  /**
   *{@inheritDoc}
   */
  public String readLine() throws IOException
  {
    if (iterator.hasNext())
    {
      return iterator.next();
    }
    else
    {
      return null;
    }
  }
}
