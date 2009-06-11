package org.opends.common.api.raw.request.filter;

import org.opends.server.util.Validator;
import org.opends.server.protocols.asn1.ASN1Writer;
import org.opends.common.protocols.ldap.LDAPEncoder;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 12:16:56
 * PM To change this template use File | Settings | File Templates.
 */
public final class RawPresenceFilter extends RawFilter
{
  private String attributeType;

  public RawPresenceFilter(String attributeType)
  {
    Validator.ensureNotNull(attributeType);
    this.attributeType = attributeType;
  }

  public String getAttributeType()
  {
    return attributeType;
  }

  public RawPresenceFilter setattributeType(String attributeType)
  {
    Validator.ensureNotNull(attributeType);
    this.attributeType = attributeType;
    return this;
  }

  public void encodeLDAP(ASN1Writer writer) throws IOException
  {
    LDAPEncoder.encodeFilter(writer, this);
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("PresentFilter(attributeType=");
    buffer.append(attributeType);
    buffer.append(")");
  }
}
