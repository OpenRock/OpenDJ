package org.opends.common.api.filter;

import org.opends.server.types.ByteString;
import org.opends.server.util.StaticUtils;
import org.opends.server.util.Validator;
import org.opends.server.protocols.asn1.ASN1Writer;
import org.opends.common.protocols.ldap.LDAPEncoder;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 3:18:32
 * PM To change this template use File | Settings | File Templates.
 */
public class GenericFilter extends Filter
{
  private byte filterTag;
  private ByteString filterBytes;

  public GenericFilter(byte filterTag, ByteString filterBytes)
  {
    Validator.ensureNotNull(filterTag, filterBytes);
    this.filterTag = filterTag;
    this.filterBytes = filterBytes;
  }

  public byte getFilterTag()
  {
    return filterTag;
  }

  public GenericFilter setFilterTag(byte filterTag)
  {
    Validator.ensureNotNull(filterTag);
    this.filterTag = filterTag;
    return this;
  }

  public ByteString getFilterBytes()
  {
    return filterBytes;
  }

  public GenericFilter setFilterBytes(ByteString filterBytes)
  {
    Validator.ensureNotNull(filterBytes);
    this.filterBytes = filterBytes;
    return this;
  }

  public void encodeLDAP(ASN1Writer writer) throws IOException
  {
    LDAPEncoder.encodeFilter(writer, this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("UnknownFilter(filterTag=");
    buffer.append(StaticUtils.byteToHex(filterTag));
    buffer.append(", filterBytes=");
    buffer.append(filterBytes.toHex());
    buffer.append(")");
  }
}
