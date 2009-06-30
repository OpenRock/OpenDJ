package org.opends.common.api.filter;

import org.opends.server.util.Validator;
import org.opends.common.protocols.ldap.LDAPEncoder;
import org.opends.common.protocols.asn1.ASN1Writer;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 11:51:55
 * AM To change this template use File | Settings | File Templates.
 */
public final class NotFilter extends Filter
{
  private Filter filter;

  public NotFilter(Filter filter)
  {
    Validator.ensureNotNull(filter);
    this.filter = filter;
  }

  public Filter getFilter()
  {
    return filter;
  }

  public NotFilter setFilter(Filter filter)
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
