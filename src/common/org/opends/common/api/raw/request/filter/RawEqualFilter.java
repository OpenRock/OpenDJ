package org.opends.common.api.raw.request.filter;

import org.opends.server.types.ByteString;
import org.opends.server.protocols.asn1.ASN1Writer;
import org.opends.common.protocols.ldap.LDAPEncoder;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 11:58:08
 * AM To change this template use File | Settings | File Templates.
 */
public final class RawEqualFilter extends RawAssertionFilter
{
  public RawEqualFilter(String attributeDescription,
                           ByteString attributeValue)
  {
    super(attributeDescription, attributeValue);
  }

  public void encodeLDAP(ASN1Writer writer) throws IOException
  {
    LDAPEncoder.encodeFilter(writer, this);
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("EqualityFilter(attributeType=");
    buffer.append(attributeType);
    buffer.append(", assertionValue=");
    buffer.append(assertionValue);
    buffer.append(")");
  }
}
