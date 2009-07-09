package org.opends.asn1;



import static org.opends.messages.ProtocolMessages.*;
import static org.opends.server.protocols.asn1.ASN1Constants.*;

import java.io.IOException;

import org.opends.ldap.ProtocolException;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 26, 2009 Time: 4:18:57
 * PM To change this template use File | Settings | File Templates.
 */
public abstract class AbstractASN1Reader implements ASN1Reader
{
  /**
   * {@inheritDoc}
   */
  public boolean readBoolean(byte expectedTag) throws IOException
  {
    if (expectedTag == 0x00)
    {
      expectedTag = UNIVERSAL_BOOLEAN_TYPE;
    }
    checkTag(expectedTag);
    return readBoolean();
  }



  /**
   * {@inheritDoc}
   */
  public int readEnumerated(byte expectedTag) throws IOException
  {
    if (expectedTag == 0x00)
    {
      expectedTag = UNIVERSAL_ENUMERATED_TYPE;
    }
    checkTag(expectedTag);
    return readEnumerated();
  }



  /**
   * {@inheritDoc}
   */
  public long readInteger(byte expectedTag) throws IOException
  {
    if (expectedTag == 0x00)
    {
      expectedTag = UNIVERSAL_INTEGER_TYPE;
    }
    checkTag(expectedTag);
    return readInteger();
  }



  /**
   * {@inheritDoc}
   */
  public void readNull(byte expectedTag) throws IOException
  {
    if (expectedTag == 0x00)
    {
      expectedTag = UNIVERSAL_NULL_TYPE;
    }
    checkTag(expectedTag);
    readNull();
  }



  /**
   * {@inheritDoc}
   */
  public ByteString readOctetString(byte expectedTag)
      throws IOException
  {
    if (expectedTag == 0x00)
    {
      expectedTag = UNIVERSAL_OCTET_STRING_TYPE;
    }
    checkTag(expectedTag);
    return readOctetString();
  }



  /**
   * {@inheritDoc}
   */
  public void readOctetString(byte expectedTag, ByteStringBuilder buffer)
      throws IOException
  {
    if (expectedTag == 0x00)
    {
      expectedTag = UNIVERSAL_OCTET_STRING_TYPE;
    }
    checkTag(expectedTag);
    readOctetString(buffer);
  }



  /**
   * {@inheritDoc}
   */
  public String readOctetStringAsString() throws IOException
  {
    // We could cache the UTF-8 CharSet if performance proves to be an
    // issue.
    return readOctetStringAsString("UTF-8");
  }



  /**
   * {@inheritDoc}
   */
  public String readOctetStringAsString(byte expectedTag)
      throws IOException
  {
    // We could cache the UTF-8 CharSet if performance proves to be an
    // issue.
    return readOctetStringAsString(expectedTag, "UTF-8");
  }



  /**
   * {@inheritDoc}
   */
  public String readOctetStringAsString(byte expectedTag, String charSet)
      throws IOException
  {
    if (expectedTag == 0x00)
    {
      expectedTag = UNIVERSAL_OCTET_STRING_TYPE;
    }
    checkTag(expectedTag);
    return readOctetStringAsString(charSet);
  }



  /**
   * {@inheritDoc}
   */
  public void readStartSequence(byte expectedTag) throws IOException
  {
    if (expectedTag == 0x00)
    {
      expectedTag = UNIVERSAL_SEQUENCE_TYPE;
    }
    checkTag(expectedTag);
    readStartSequence();
  }



  /**
   * {@inheritDoc}
   */
  public void readStartSet(byte expectedTag) throws IOException
  {
    // From an implementation point of view, a set is equivalent to a
    // sequence.
    if (expectedTag == 0x00)
    {
      expectedTag = UNIVERSAL_SET_TYPE;
    }
    checkTag(expectedTag);
    readStartSet();
  }



  private void checkTag(byte expected) throws IOException
  {
    if (peekType() != expected)
    {
      throw new ProtocolException(ERR_ASN1_UNEXPECTED_TAG.get(expected,
          peekType()));
    }
  }
}
