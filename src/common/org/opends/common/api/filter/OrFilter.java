package org.opends.common.api.filter;

import org.opends.common.protocols.ldap.LDAPEncoder;
import org.opends.common.protocols.asn1.ASN1Writer;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 11:45:11
 * AM To change this template use File | Settings | File Templates.
 */
public final class OrFilter extends CompoundFilter
{
  public OrFilter(Filter component, Filter... components)
  {
    super(component, components);
  }

  public void encodeLDAP(ASN1Writer writer) throws IOException
  {
    LDAPEncoder.encodeFilter(writer, this);
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("OrFilter(components=");
    buffer.append(components);
    buffer.append(")");
  }
}
