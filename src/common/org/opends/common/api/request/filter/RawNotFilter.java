package org.opends.common.api.request.filter;

import org.opends.server.util.Validator;
import org.opends.server.protocols.asn1.ASN1Writer;
import org.opends.common.protocols.ldap.LDAPEncoder;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 11:51:55
 * AM To change this template use File | Settings | File Templates.
 */
public final class RawNotFilter extends RawFilter
{
  private RawFilter filter;

  public RawNotFilter(RawFilter filter)
  {
    Validator.ensureNotNull(filter);
    this.filter = filter;
  }

  public RawFilter getFilter()
  {
    return filter;
  }

  public RawNotFilter setFilter(RawFilter filter)
  {
    Validator.ensureNotNull(filter);
    this.filter = filter;
    return this;
  }

  public void encodeLDAP(ASN1Writer writer) throws IOException
  {
    LDAPEncoder.encodeFilter(writer, this);
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("NotFilter(filter=");
    buffer.append(filter);
    buffer.append(")");
  }
}
