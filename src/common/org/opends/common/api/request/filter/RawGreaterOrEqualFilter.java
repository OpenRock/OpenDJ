package org.opends.common.api.request.filter;

import org.opends.server.types.ByteString;
import org.opends.server.protocols.asn1.ASN1Writer;
import org.opends.common.protocols.ldap.LDAPEncoder;
import org.opends.common.api.AttributeDescription;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 12:14:53
 * PM To change this template use File | Settings | File Templates.
 */
public final class RawGreaterOrEqualFilter extends RawAssertionFilter
{
  public RawGreaterOrEqualFilter(String attributeDescription, 
                                 ByteString assertionValue)
  {
    super(attributeDescription, assertionValue);
  }

  public RawGreaterOrEqualFilter(AttributeDescription attributeDescription,
                        ByteString assertionValue)
  {
    super(attributeDescription.toString(), assertionValue);
  }

  public void encodeLDAP(ASN1Writer writer) throws IOException
  {
    LDAPEncoder.encodeFilter(writer, this);
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("GreaterOrEqualFilter(attributeDescription=");
    buffer.append(attributeDescription);
    buffer.append(", assertionValue=");
    buffer.append(assertionValue);
    buffer.append(")");
  }
}
